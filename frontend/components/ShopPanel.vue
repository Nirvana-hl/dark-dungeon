<template>
  <view class="shop-panel">
    <!-- 商店类型标签页 -->
    <view class="shop-tabs">
      <button 
        v-for="tab in shopTabs" 
        :key="tab.type"
        :class="['shop-tab', { active: selectedShopType === tab.type }]"
        @click="selectShopType(tab.type as 'item' | 'card_character')"
      >
        <i :class="tab.icon"></i>
        <text>{{ tab.name }}</text>
      </button>
    </view>

    <!-- 商店头部 -->
    <view class="shop-header">
      <view class="header-left">
        <h3>
          <i :class="currentShopTab.icon"></i>
          {{ currentShopTab.name }}
        </h3>
        <p class="shop-description">{{ currentShopTab.description }}</p>
      </view>
      <view class="header-actions">
        <button 
          class="refresh-btn"
          :disabled="refreshing"
          @click="() => handleRefreshShop(true)"
        >
          <i class="fas fa-sync-alt" :class="{ spinning: refreshing }"></i>
          <text>{{ refreshing ? '刷新中...' : '刷新商店' }}</text>
        </button>
      </view>
    </view>
    
    <!-- 商品网格（固定8个位置） -->
    <view class="products-grid">
      <view 
        v-for="(offer, index) in shopSlots" 
        :key="index"
        class="product-slot"
        :class="{ empty: !offer }"
        :style="{ '--delay': index * 0.05 + 's' }"
      >
        <!-- 有商品时显示商品卡片 -->
        <view 
          v-if="offer" 
          class="product-card"
          :class="getCardRarityClasses(offer)"
        >
          <view class="product-visual">
            <view class="product-icon-wrapper">
              <i :class="getItemIcon(offer)"></i>
            </view>
          </view>
          <view class="product-info">
            <h4 class="product-name">{{ offer.name }}</h4>
            <p class="product-desc">{{ offer.description || '暂无描述' }}</p>
            <view class="product-tags" v-if="offer.tags && offer.tags.length > 0">
              <text v-for="tag in offer.tags" :key="tag" class="product-tag">{{ tag }}</text>
            </view>
          </view>
          <view class="product-purchase">
            <view class="price-section">
              <view class="current-price">
                <i class="fas fa-coins gold"></i>
                <text class="price-value">{{ formatNumber(offer.price || 0) }}</text>
              </view>
            </view>
            <button 
              class="buy-btn"
              :class="{ disabled: !canAfford(offer) }"
              @click="purchaseItem(offer)"
            >
              <i class="fas fa-shopping-cart"></i>
              {{ canAfford(offer) ? '购买' : '余额不足' }}
            </button>
          </view>
        </view>
        
        <!-- 空位置 -->
        <view v-else class="empty-slot">
          <i class="fas fa-box-open"></i>
          <text>空位</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { campApi } from '@/api/request'

interface Props {
  userBalance: Array<{ 
    currencyType: string
    balance: bigint
  }>
}

const props = defineProps<Props>()

const emit = defineEmits<{
  purchaseItem: [offer: any]
}>()

// 商店类型
const shopTabs = [
  { 
    type: 'item', 
    name: '道具商店', 
    icon: 'fas fa-flask',
    description: '购买各种道具和消耗品'
  },
  { 
    type: 'card', 
    name: '法术装备商店', 
    icon: 'fas fa-hat-wizard',
    description: '购买法术与装备卡牌，强化你的战斗策略'
  },
  { 
    type: 'card_character', 
    name: '角色卡商店', 
    icon: 'fas fa-users',
    description: '购买英雄角色卡牌'
  }
]

// 状态
const selectedShopType = ref<'item' | 'card' | 'card_character'>('item')
const shopOffers = ref<any[]>([])
const refreshing = ref(false)

// 计算属性
const currentShopTab = computed(() => {
  return shopTabs.find(tab => tab.type === selectedShopType.value) || shopTabs[0]
})

// 确保始终有且只有8个位置
const shopSlots = computed(() => {
  const slots: (any | null)[] = []
  
  // 先添加所有有效商品（最多8个）
  for (let i = 0; i < Math.min(shopOffers.value.length, 8); i++) {
    slots.push(shopOffers.value[i])
  }
  
  // 如果不足8个，用null填充空位
  while (slots.length < 8) {
    slots.push(null)
  }
  
  // 确保只有8个位置
  return slots.slice(0, 8)
})

// 方法
const selectShopType = async (type: 'item' | 'card' | 'card_character') => {
  selectedShopType.value = type
  await loadShopOffers()
}

const loadShopOffers = async (autoRefresh: boolean = true) => {
  try {
    console.log(`[ShopPanel] 加载${currentShopTab.value.name}商品...`)
    const response = await campApi.getShopOffersByType(selectedShopType.value)
    
    if (response.data.code === 200) {
      // 转换后端数据格式
      const backendOffers = response.data.data || []
      const validOffers = backendOffers.filter((offer: any) => offer !== null)
      
      // 如果没有商品且允许自动刷新，则自动刷新
      if (validOffers.length === 0 && autoRefresh) {
        console.log(`[ShopPanel] ${currentShopTab.value.name}没有商品，自动刷新...`)
        await handleRefreshShop(false) // 不自动刷新，避免无限循环
        return
      }
      
      // 确保只处理前8个商品（后端应该已经限制，但前端也做一次限制确保安全）
      const limitedOffers = backendOffers.slice(0, 8)
      
      shopOffers.value = limitedOffers.map((backendOffer: any) => {
        // 如果是null，直接返回null（用于显示空位）
        if (backendOffer === null) {
          return null
        }
        
        // 不同商店只显示各自类型的商品，过滤掉其他类型
        if (selectedShopType.value === 'card_character') {
          // 只保留 card_character 类型的商品
          if (backendOffer.offerType !== 'card_character' && !backendOffer.cardCharacter) {
            return null // 过滤掉非角色商品
          }
        } else if (selectedShopType.value === 'card') {
          // 只保留法术/装备卡牌商品
          if (backendOffer.offerType !== 'card' && !backendOffer.card) {
            return null
          }
        }
        
        // 转换有效商品
        let name = '未知商品'
        let description = ''
        const tags: string[] = []
        
        if (backendOffer.item) {
          name = backendOffer.item.name || name
          description = backendOffer.item.description || description
          if (backendOffer.item.rarity) {
            tags.push(backendOffer.item.rarity)
          }
          if (backendOffer.item.itemType) {
            tags.push(backendOffer.item.itemType)
          }
        } else if (backendOffer.cardCharacter) {
          name = backendOffer.cardCharacter.name || name
          description = backendOffer.cardCharacter.description || description
          if (backendOffer.cardCharacter.rarity) {
            tags.push(backendOffer.cardCharacter.rarity)
          }
          if (backendOffer.cardCharacter.classType) {
            tags.push(backendOffer.cardCharacter.classType)
          }
          // 添加"角色"标签
          if (!tags.includes('角色')) {
            tags.push('角色')
          }
        } else if (backendOffer.card) {
          name = backendOffer.card.name || name
          description = backendOffer.card.description || description
          if (backendOffer.card.rarity) {
            tags.push(backendOffer.card.rarity)
          }
          if (backendOffer.card.cardType) {
            tags.push(backendOffer.card.cardType)
          }
        }
        
        return {
          id: String(backendOffer.id),
          name: name,
          description: description,
          price: Number(backendOffer.price || 0),
          currencyType: 'gold' as const,
          tags: tags,
          offerType: backendOffer.offerType,
          item: backendOffer.item,
          cardCharacter: backendOffer.cardCharacter,
          card: backendOffer.card,
          rarity: backendOffer.cardCharacter?.rarity || backendOffer.card?.rarity
        }
      }).filter((offer: any) => offer !== null) // 过滤掉被标记为null的商品
      
      // 确保 shopOffers.value 只有8个元素（不足的用null填充）
      while (shopOffers.value.length < 8) {
        shopOffers.value.push(null)
      }
      // 确保不超过8个
      shopOffers.value = shopOffers.value.slice(0, 8)
      
      // 卡牌商店只显示对应类型，再次过滤确保没有其他类型
      if (selectedShopType.value === 'card_character') {
        shopOffers.value = shopOffers.value.map((offer: any) => {
          if (offer && offer.offerType !== 'card_character' && !offer.cardCharacter) {
            return null // 过滤掉非角色商品
          }
          return offer
        })
        
        // 过滤后重新填充到8个位置
        const validOffers = shopOffers.value.filter((o: any) => o !== null)
        shopOffers.value = []
        // 先添加有效商品
        for (let i = 0; i < Math.min(validOffers.length, 8); i++) {
          shopOffers.value.push(validOffers[i])
        }
        // 不足8个用null填充
        while (shopOffers.value.length < 8) {
          shopOffers.value.push(null)
        }
        // 确保只有8个
        shopOffers.value = shopOffers.value.slice(0, 8)
      } else if (selectedShopType.value === 'card') {
        shopOffers.value = shopOffers.value.map((offer: any) => {
          if (offer && offer.offerType !== 'card' && !offer.card) {
            return null
          }
          return offer
        })

        const validOffers = shopOffers.value.filter((o: any) => o !== null)
        shopOffers.value = []
        for (let i = 0; i < Math.min(validOffers.length, 8); i++) {
          shopOffers.value.push(validOffers[i])
        }
        while (shopOffers.value.length < 8) {
          shopOffers.value.push(null)
        }
        shopOffers.value = shopOffers.value.slice(0, 8)
      }
      
      console.log(`[ShopPanel] ${currentShopTab.value.name}商品加载完成:`, {
        total: shopOffers.value.length,
        valid: shopOffers.value.filter((o: any) => o !== null).length,
        empty: shopOffers.value.filter((o: any) => o === null).length,
        shopSlotsCount: shopSlots.value.length
      })
      
      // 验证确保只有8个位置
      if (shopSlots.value.length !== 8) {
        console.warn(`[ShopPanel] 警告: shopSlots 数量不正确，期望8个，实际${shopSlots.value.length}个`)
      }
    } else {
      console.error('[ShopPanel] 加载商品失败:', response.data.message)
      shopOffers.value = []
    }
  } catch (error) {
    console.error('[ShopPanel] 加载商品异常:', error)
    shopOffers.value = []
  }
}

const handleRefreshShop = async (autoRefresh: boolean = true) => {
  if (refreshing.value) return
  
  try {
    refreshing.value = true
    console.log(`[ShopPanel] 刷新${currentShopTab.value.name}...`)
    
    const response = await campApi.refreshShop(selectedShopType.value)
    
    if (response.data.code === 200) {
      // 重新加载商品列表（不自动刷新，避免循环）
      await loadShopOffers(false)
      console.log(`[ShopPanel] ${currentShopTab.value.name}刷新成功`)
    } else {
      console.error('[ShopPanel] 刷新失败:', response.data.message)
      // 即使刷新失败，也重新加载一次（可能数据库中有旧数据）
      await loadShopOffers(false)
    }
  } catch (error) {
    console.error('[ShopPanel] 刷新异常:', error)
    // 刷新失败时，尝试重新加载现有数据
    await loadShopOffers(false)
  } finally {
    refreshing.value = false
  }
}

const canAfford = (offer: any) => {
  if (!offer) return false
  const wallet = props.userBalance.find(w => w.currencyType === offer.currencyType || w.currencyType === 'gold')
  if (!wallet) return false
  return Number(wallet.balance) >= offer.price
}

const purchaseItem = (offer: any) => {
  if (canAfford(offer)) {
    emit('purchaseItem', offer)
  }
}

const isCardCharacterOffer = (offer: any) => {
  if (!offer) return false
  return offer.offerType === 'card_character' || !!offer.cardCharacter
}

const getCardRarity = (offer: any) => {
  if (!offer) return ''
  const rarity = offer.cardCharacter?.rarity || offer.card?.rarity || offer.rarity
  return typeof rarity === 'string' ? rarity.toLowerCase() : ''
}

const getCardRarityClasses = (offer: any) => {
  const rarity = getCardRarity(offer)
  return rarity ? ['card-character-offer', `card-rarity-${rarity}`] : []
}

const getItemIcon = (offer: any) => {
  if (offer.item) {
    const itemType = offer.item.itemType
    const icons: { [key: string]: string } = {
      'consumable': 'fas fa-flask',
      'material': 'fas fa-gem',
      'blueprint': 'fas fa-scroll',
      'currency_bundle': 'fas fa-coins',
      'cosmetic': 'fas fa-palette'
    }
    return icons[itemType] || 'fas fa-box'
  } else if (offer.cardCharacter) {
    return 'fas fa-user-shield'
  } else if (offer.card) {
    const type = offer.card.cardType
    if (type === 'spell') return 'fas fa-magic'
    if (type === 'equipment') return 'fas fa-shield-alt'
    return 'fas fa-scroll'
  }
  return 'fas fa-box'
}

const formatNumber = (num: number): string => {
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

// 生命周期
onMounted(() => {
  loadShopOffers()
})

// 监听商店类型变化
watch(selectedShopType, () => {
  loadShopOffers()
})
</script>

<style scoped>
.shop-panel {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.shop-tabs {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.shop-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.95rem;
  color: #9ca3af;
  font-weight: 500;
}

.shop-tab:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.shop-tab.active {
  background: rgba(255, 152, 0, 0.2);
  border-color: rgba(255, 152, 0, 0.4);
  color: #fb923c;
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left h3 {
  font-size: 1.3rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.shop-description {
  color: #9ca3af;
  font-size: 0.9rem;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.refresh-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.refresh-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-btn .spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
  /* 确保始终显示8个位置（2行 x 4列） */
  max-width: 100%;
  /* 固定网格，确保只显示8个位置 */
  grid-auto-rows: auto;
  margin-bottom: 3rem; /* 在商店下方添加空白间距 */
}

.product-slot {
  min-height: 280px;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
  animation-fill-mode: both;
}

.product-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.25rem;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.product-card:not(.card-character-offer):hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.3);
}

.product-card.card-character-offer:hover {
  transform: translateY(-4px);
}

.product-card.card-character-offer {
  border-width: 2px;
  background: rgba(12, 12, 12, 0.75);
  box-shadow: 0 20px 45px rgba(0, 0, 0, 0.55);
}

.product-card.card-character-offer .product-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 18px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.product-card.card-character-offer:hover .product-icon-wrapper {
  transform: scale(1.05);
}

.product-card.card-rarity-common {
  border-color: rgba(128, 128, 128, 0.6);
  background: linear-gradient(135deg, rgba(128, 128, 128, 0.3), rgba(10, 10, 10, 0.8));
  box-shadow: 0 15px 35px rgba(128, 128, 128, 0.25);
}

.product-card.card-rarity-common .product-icon-wrapper {
  background: rgba(128, 128, 128, 0.3);
  color: #ffffff;
  box-shadow: 0 0 18px rgba(128, 128, 128, 0.35);
}

.product-card.card-rarity-rare {
  border-color: rgba(33, 150, 243, 0.7);
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.3), rgba(9, 16, 33, 0.9));
  box-shadow: 0 18px 40px rgba(33, 150, 243, 0.35);
}

.product-card.card-rarity-rare .product-icon-wrapper {
  background: rgba(33, 150, 243, 0.35);
  color: #e3f2fd;
  box-shadow: 0 0 20px rgba(33, 150, 243, 0.5);
}

.product-card.card-rarity-epic {
  border-color: rgba(156, 39, 176, 0.7);
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.35), rgba(32, 6, 42, 0.9));
  box-shadow: 0 20px 45px rgba(156, 39, 176, 0.4);
}

.product-card.card-rarity-epic .product-icon-wrapper {
  background: rgba(156, 39, 176, 0.4);
  color: #f3e5f5;
  box-shadow: 0 0 22px rgba(156, 39, 176, 0.6);
}

.product-card.card-rarity-legendary {
  border-color: rgba(255, 152, 0, 0.9);
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.4), rgba(71, 23, 5, 0.95));
  box-shadow: 0 24px 55px rgba(255, 152, 0, 0.45);
}

.product-card.card-rarity-legendary .product-icon-wrapper {
  background: rgba(255, 152, 0, 0.45);
  color: #fff3e0;
  box-shadow: 0 0 24px rgba(255, 152, 0, 0.65);
}

.empty-slot {
  background: rgba(255, 255, 255, 0.02);
  border: 2px dashed rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 280px;
  color: #6b7280;
  font-size: 0.9rem;
  gap: 0.5rem;
}

.empty-slot i {
  font-size: 2rem;
  opacity: 0.5;
}

.product-visual {
  position: relative;
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.product-icon-wrapper {
  width: 60px;
  height: 60px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: rgba(255, 255, 255, 0.8);
}

.product-info {
  margin-bottom: 1rem;
  flex: 1;
}

.product-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
}

.product-desc {
  font-size: 0.8rem;
  color: #9ca3af;
  margin: 0 0 0.75rem 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-tags {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
}

.product-tag {
  background: rgba(33, 150, 243, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(33, 150, 243, 0.4);
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 0.65rem;
  font-weight: 500;
}

.product-purchase {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.current-price {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
  color: #ffd700;
  font-size: 1.2rem;
}

.price-value {
  color: #ffd700;
}

.buy-btn {
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.buy-btn:hover:not(.disabled) {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(76, 175, 80, 0.3);
}

.buy-btn.disabled {
  background: #4b5563;
  color: #9ca3af;
  cursor: not-allowed;
  opacity: 0.6;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 确保始终显示8个位置，使用4列布局（2行 x 4列） */
@media (max-width: 1200px) {
  .products-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

@media (max-width: 768px) {
  .shop-header {
    flex-direction: column;
    gap: 1rem;
  }
  
  /* 小屏幕使用2列布局（4行 x 2列），仍然保持8个位置 */
  .products-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
  
  .product-card {
    padding: 1rem;
  }
}
</style>
