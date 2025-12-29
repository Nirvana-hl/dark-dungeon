<template>
  <view class="shop-container">
    <!-- 顶部头部 -->
    <view class="shop-header">
      <view class="header-left">
        <i class="fas fa-store shop-icon"></i>
        <text class="shop-title">神秘商店</text>
    </view>
      <view class="header-right">
        <i class="fas fa-coins gold-icon"></i>
        <text class="gold-amount">{{ formatGold(goldBalance) }}</text>
        </view>
    </view>

    <!-- 导航标签页 -->
    <view class="shop-tabs">
      <view 
        v-for="tab in tabs" 
        :key="tab.type"
        :class="['tab-item', { active: activeTab === tab.type }]"
        @click="switchTab(tab.type)"
      >
        <i :class="tab.icon"></i>
        <text>{{ tab.name }}</text>
        </view>
        </view>

    <!-- 特色区域（NPC介绍） -->
    <view class="featured-section">
      <view class="npc-avatar">
        <image 
          v-if="currentTabInfo.avatar && !avatarError"
          :src="currentTabInfo.avatar" 
          mode="aspectFill"
          class="avatar-img"
          @error="handleAvatarError"
        />
        <view v-else class="avatar-placeholder">
          <i :class="currentTabInfo.icon"></i>
        </view>
      </view>
      <view class="welcome-message">
        <text class="welcome-text">{{ currentTabInfo.welcome }}</text>
      </view>
    </view>

    <!-- 商品展示区域 -->
    <view class="products-section">
      <view class="section-header">
        <view class="header-left">
          <i class="fas fa-gift section-icon"></i>
          <text class="section-title">{{ currentTabInfo.productTitle }}</text>
        </view>
        <view class="header-right">
          <button 
            class="refresh-btn-small"
            :class="{ disabled: isRefreshing || !canAffordRefresh || activeTab === 'spell' || activeTab === 'equipment' }"
            :disabled="isRefreshing || !canAffordRefresh || activeTab === 'spell' || activeTab === 'equipment'"
            @click="handleRefresh"
          >
            <i class="fas fa-sync-alt" :class="{ spinning: isRefreshing }"></i>
            <text>{{ (activeTab === 'spell' || activeTab === 'equipment') ? '暂不支持' : '刷新' }}</text>
            <view v-if="activeTab !== 'spell' && activeTab !== 'equipment'" class="refresh-cost">
              <i class="fas fa-coins"></i>
              <text>50</text>
            </view>
          </button>
        </view>
    </view>

      <!-- 商品网格 -->
      <view class="products-grid">
        <view 
          v-for="(offer, index) in limitedOffers" 
          :key="offer.id || index"
          class="product-card"
          :class="getRarityClass(offer)"
          @click="showProductDetail(offer)"
        >
          <!-- 商品图片 -->
          <view class="product-image-wrapper">
            <image 
              :src="getProductImage(offer)" 
              mode="aspectFit"
              class="product-image"
              @error="handleImageError"
            />
            <!-- 角色属性覆盖在图片左下 / 右下角 -->
            <view 
              v-if="offer.cardCharacter && (offer.cardCharacter.baseAttack || offer.cardCharacter.baseHp)" 
              class="card-stat-overlay"
            >
              <view v-if="offer.cardCharacter.baseAttack" class="stat-badge stat-badge-attack">
                <i class="fas fa-sword"></i>
                <text>{{ offer.cardCharacter.baseAttack }}</text>
              </view>
              <view v-if="offer.cardCharacter.baseHp" class="stat-badge stat-badge-health">
                <i class="fas fa-heart"></i>
                <text>{{ offer.cardCharacter.baseHp }}</text>
              </view>
            </view>
            <!-- 价格标签（右上角，仅在未拥有时显示） -->
            <view v-if="!isOwned(offer)" class="price-badge">
              <i class="fas fa-coins"></i>
              <text>{{ formatPrice(offer.price) }}</text>
            </view>
            <!-- 已拥有标记 -->
            <view v-else class="owned-badge">
              <i class="fas fa-check-circle"></i>
            </view>
          </view>
          
          <!-- 商品信息 -->
          <view class="product-info">
            <!-- 名称和稀有度 -->
            <view class="product-header">
              <text class="product-name">{{ getProductName(offer) }}</text>
              <view class="rarity-badge" :class="getRarityBadgeClass(offer)">
                <text>{{ getRarityText(offer) }}</text>
              </view>
            </view>
          
            <!-- 描述 -->
            <view class="product-description">
              <i class="fas fa-star star-icon"></i>
              <text class="desc-text">{{ getProductDescription(offer) }}</text>
            </view>
          </view>
          
          <!-- 购买按钮或已拥有标记 -->
          <view class="product-actions">
            <button 
              v-if="!isOwned(offer)"
              class="buy-btn"
              :class="{ disabled: !canAfford(offer) }"
              :disabled="!canAfford(offer)"
              @click.stop="handlePurchase(offer)"
            >
              <i class="fas fa-shopping-cart"></i>
              <text>{{ canAfford(offer) ? '购买' : '余额不足' }}</text>
            </button>
            <view v-else class="owned-indicator">
              <i class="fas fa-check-circle"></i>
              <text>已拥有</text>
            </view>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="limitedOffers.length === 0 && !loading" class="empty-state">
        <i class="fas fa-box-open"></i>
        <text>暂无商品</text>
        <text class="empty-hint">点击上方"刷新商品"按钮获取更多商品</text>
        <text class="empty-tip">提示：刷新需要消耗50金币</text>
    </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="loading-state">
        <i class="fas fa-spinner fa-spin"></i>
        <text>加载中...</text>
        </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { shopApi } from '@/api/request'
import { useWalletStore } from '@/stores/wallet'
import { useShopStore } from '@/stores/shop'
import { CurrencyType } from '@/types'

// 标签页配置
type TabType = 'card_character' | 'spell' | 'equipment' | 'item'

const tabs: Array<{
  type: TabType
  name: string
  icon: string
  productTitle: string
  avatar: string
  welcome: string
}> = [
  {
    type: 'card_character',
    name: '角色',
    icon: 'fas fa-user',
    productTitle: '角色',
    avatar: '/static/tabbar/touxiang.jpg',
    welcome: '欢迎来到战士公会! 这里有最强大的战士和勇者, 选择你的战友吧!'
  },
  {
    type: 'spell',
    name: '法术',
    icon: 'fas fa-magic',
    productTitle: '法术',
    avatar: '/static/tabbar/touxiang.jpg',
    welcome: '欢迎来到法师塔! 这里有最强大的法术, 选择你的魔法吧!'
  },
  {
    type: 'equipment',
    name: '装备',
    icon: 'fas fa-shield-alt',
    productTitle: '装备',
    avatar: '/static/tabbar/touxiang.jpg',
    welcome: '欢迎来到铁匠铺! 这里有最精良的装备, 选择你的武器吧!'
  },
  {
    type: 'item',
    name: '道具',
    icon: 'fas fa-flask',
    productTitle: '道具',
    avatar: '/static/tabbar/touxiang.jpg',
    welcome: '欢迎来到杂货铺! 这里有各种实用的道具, 选择你需要的物品吧!'
  }
]

// 状态
const activeTab = ref<TabType>('card_character')
const offers = ref<any[]>([])
const loading = ref(false)
const isRefreshing = ref(false)
const avatarError = ref(false)
const walletStore = useWalletStore()
const shopStore = useShopStore()

// 计算属性
const currentTabInfo = computed(() => {
  return tabs.find(tab => tab.type === activeTab.value) || tabs[0]
})

const goldBalance = computed(() => {
  return walletStore.getBalance(CurrencyType.GOLD)
})

const canAffordRefresh = computed(() => {
  return goldBalance.value >= 50n
})

const filteredOffers = computed(() => {
  // 先过滤掉 null/undefined
  const validOffers = offers.value.filter(offer => offer !== null && offer !== undefined)
  
  // 根据当前标签页过滤商品
  return validOffers.filter(offer => {
    // 确保有基本数据
    if (!offer || !offer.offerType) return false
    
    // 根据当前标签页匹配商品类型
    if (activeTab.value === 'card_character') {
      // 角色商店：只显示角色商品
      return offer.offerType === 'card_character' && !!offer.cardCharacter
    } else if (activeTab.value === 'spell') {
      // 法术商店：只显示法术卡牌（cardType === 'spell'）
      return offer.offerType === 'card' && !!offer.card && offer.card.cardType === 'spell'
    } else if (activeTab.value === 'equipment') {
      // 装备商店：只显示装备卡牌（cardType === 'equipment'）
      return offer.offerType === 'card' && !!offer.card && offer.card.cardType === 'equipment'
    } else if (activeTab.value === 'item') {
      // 道具商店：只显示道具商品
      return offer.offerType === 'item' && !!offer.item
    }
    return false
  })
})

// 限制最多显示6个商品
const limitedOffers = computed(() => {
  return filteredOffers.value.slice(0, 6)
})

// 方法
async function switchTab(tabType: TabType) {
  activeTab.value = tabType
  avatarError.value = false // 切换标签时重置头像错误状态
  await loadOffers()
}

async function loadOffers() {
  try {
    loading.value = true
    console.log('[Shop] 加载商品:', activeTab.value)
    
    // 先尝试从 shopStore 获取（如果已加载）
    let rawOffers: any[] = []
    
    // 方法1：尝试按类型获取（推荐）
    // 设置超时，避免长时间等待
    try {
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('请求超时')), 5000) // 5秒超时
      })
      
      // 直接按当前标签类型从后端获取对应商店商品
      const response = await Promise.race([
        shopApi.getOffersByType(activeTab.value),
        timeoutPromise
      ]) as any
      
      if (response && response.data && response.data.code === 200) {
        rawOffers = response.data.data || []
        console.log('[Shop] 按类型获取成功:', {
          shopType: activeTab.value,
          count: rawOffers.length
        })
      }
    } catch (error: any) {
      console.warn('[Shop] 按类型获取失败，尝试从store获取:', error.message || '请求超时或网络错误')
      // 不显示错误提示，静默使用备用方案
    }
    
    // 方法2：如果按类型获取为空，尝试从所有商品中过滤
    if (rawOffers.length === 0 || rawOffers.every((o: any) => o === null)) {
      console.log('[Shop] 按类型获取为空，尝试从所有商品中过滤（使用 shopStore 作为备用数据源）...')
      
      // 确保 shopStore 有数据
      if (shopStore.offers.length === 0) {
        console.log('[Shop] shopStore 无数据，先加载所有商品...')
        await shopStore.fetchShopOffers()
      }
      
      // 从所有商品中过滤出当前类型的商品
      // shopStore 的数据已经保留了原始数据（cardCharacter, card, item）
      const allOffers = shopStore.offers as any[]
      console.log('[Shop] shopStore 所有商品示例:', allOffers.slice(0, 3).map((o: any) => ({
        id: o.id,
        offerType: o.offerType,
        name: o.name,
        hasItem: !!o.item,
        hasCard: !!o.card,
        hasCardCharacter: !!o.cardCharacter
      })))
      
      rawOffers = allOffers.filter((offer: any) => {
        if (!offer) return false
        
        // shopStore 的数据已经保留了原始数据，直接检查
        if (activeTab.value === 'card_character') {
          return offer.offerType === 'card_character' && !!offer.cardCharacter
        } else if (activeTab.value === 'spell') {
          // 法术：只显示 cardType === 'spell' 的卡牌
          return offer.offerType === 'card' && !!offer.card && offer.card.cardType === 'spell'
        } else if (activeTab.value === 'equipment') {
          // 装备：只显示 cardType === 'equipment' 的卡牌
          return offer.offerType === 'card' && !!offer.card && offer.card.cardType === 'equipment'
        } else if (activeTab.value === 'item') {
          return offer.offerType === 'item' && !!offer.item
        }
        return false
      })
      
      console.log('[Shop] 过滤后的商品:', rawOffers.length, '个', activeTab.value, '类型商品')
      
      console.log('[Shop] 从所有商品中过滤结果:', {
        shopType: activeTab.value,
        totalOffers: allOffers.length,
        filteredCount: rawOffers.length
      })
    }
    
    // 过滤掉 null 值（后端可能返回 null 填充空位）
    offers.value = rawOffers.filter((offer: any) => offer !== null && offer !== undefined)
    
    console.log('[Shop] 商品加载完成:', {
      shopType: activeTab.value,
      total: rawOffers.length,
      valid: offers.value.length,
      offers: offers.value.map((o: any) => ({
        id: o.id,
        offerType: o.offerType,
        hasItem: !!o.item,
        hasCard: !!o.card,
        hasCardCharacter: !!o.cardCharacter,
        price: o.price,
        name: getProductName(o)
      }))
    })
    
    // 如果没有商品，提示用户刷新
    if (offers.value.length === 0) {
      console.warn('[Shop] 当前没有商品，建议刷新商店')
    }
  } catch (error: any) {
    console.error('[Shop] 加载商品异常:', error)
    console.error('[Shop] 错误详情:', {
      message: error.message,
      userMessage: error.userMessage,
      response: error.response?.data
    })
    offers.value = []
  } finally {
    loading.value = false
  }
}

async function handleRefresh() {
  if (isRefreshing.value || !canAffordRefresh.value) return
  
  // 检查是否支持刷新（目前后端只支持 item 和 card_character）
  if (activeTab.value === 'spell' || activeTab.value === 'equipment') {
    uni.showToast({
      title: '该商店暂不支持刷新，请稍后再试',
      icon: 'none',
      duration: 2000
    })
    return
  }
  
  try {
    isRefreshing.value = true
    console.log('[Shop] 刷新商品:', activeTab.value)
    
    const response = await shopApi.refreshShop(activeTab.value)
    
    if (response.data.code === 200) {
      // 刷新成功后重新加载商品
      await loadOffers()
      // 刷新钱包余额
      await walletStore.loadWallets()
      console.log('[Shop] 刷新成功')
      uni.showToast({
        title: '刷新成功',
        icon: 'success'
      })
    } else {
      console.error('[Shop] 刷新失败:', response.data.message)
      uni.showToast({
        title: response.data.message || '刷新失败',
        icon: 'none',
        duration: 2000
      })
    }
  } catch (error: any) {
    console.error('[Shop] 刷新异常:', error)
    const errorMsg = error.userMessage || error.response?.data?.message || '刷新失败，请稍后重试'
    uni.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 2000
    })
  } finally {
    isRefreshing.value = false
  }
}

async function handlePurchase(offer: any) {
  if (!canAfford(offer)) {
    uni.showToast({
      title: '余额不足',
      icon: 'none'
    })
    return
  }
  
  try {
    console.log('[Shop] 购买商品:', offer.id)
    
    const response = await shopApi.purchaseItem({
      offerType: offer.offerType,
      targetId: Number(offer.targetId || offer.id),
      quantity: 1,
    })
    
    if (response.data.code === 200) {
      uni.showToast({
        title: '购买成功',
        icon: 'success'
      })
      // 刷新商品列表和钱包余额
      await loadOffers()
      await walletStore.loadWallets()
    } else {
      uni.showToast({
        title: response.data.message || '购买失败',
        icon: 'none'
      })
    }
  } catch (error: any) {
    console.error('[Shop] 购买异常:', error)
    uni.showToast({
      title: error.userMessage || '购买失败',
      icon: 'none'
    })
  }
}

function canAfford(offer: any): boolean {
  if (!offer) return false
  
  // 处理价格可能是 number 或 bigint
  const price = typeof offer.price === 'bigint' ? offer.price : BigInt(offer.price || 0)
  if (price <= 0n) return false
  
  return goldBalance.value >= price
}

function getProductName(offer: any): string {
  if (!offer) return '未知商品'
  
  // 优先使用关联对象的名称
  if (offer.cardCharacter && offer.cardCharacter.name) {
    return offer.cardCharacter.name
  }
  if (offer.card && offer.card.name) {
    return offer.card.name
  }
  if (offer.item && offer.item.name) {
    return offer.item.name
  }
  
  // 如果没有关联对象，尝试使用 offerType 判断
  if (offer.offerType === 'card_character') return '未知角色'
  if (offer.offerType === 'card') return '未知卡牌'
  if (offer.offerType === 'item') return '未知道具'
  
  return '未知商品'
}

function getProductDescription(offer: any): string {
  if (!offer) return '暂无描述'
  
  // 角色：使用 lore 字段（背景故事）
  if (offer.cardCharacter) {
    // 优先使用 lore 字段
    const lore = offer.cardCharacter.lore || offer.cardCharacter.description || ''
    if (lore) {
      // 如果 lore 太长，截取前18个字符
      return lore.length > 18 ? lore.substring(0, 18) + '...' : lore
    }
    // 如果没有 lore，尝试使用特性描述
    if (offer.cardCharacter.traits && offer.cardCharacter.traits.length > 0) {
      const trait = offer.cardCharacter.traits[0]
      const traitName = trait.name || ''
      return traitName || '暂无描述'
    }
    return '暂无描述'
  }
  
  // 卡牌：显示描述
  if (offer.card) {
    const desc = offer.card.description || ''
    // 如果描述太长，截取
    return desc.length > 18 ? desc.substring(0, 18) + '...' : desc || '暂无描述'
  }
  
  // 道具：显示描述
  if (offer.item) {
    const desc = offer.item.description || ''
    return desc.length > 18 ? desc.substring(0, 18) + '...' : desc || '暂无描述'
  }
  
  return '暂无描述'
}

function getProductImage(offer: any): string {
  // 根据商品类型返回图片路径
  if (offer.cardCharacter) {
    return `/static/images/shop/characters/${offer.cardCharacter.id || 'default'}.png`
  }
  if (offer.card) {
    return `/static/images/shop/cards/${offer.card.id || 'default'}.png`
  }
  if (offer.item) {
    return `/static/images/shop/items/${offer.item.id || 'default'}.png`
  }
  return '/static/images/shop/default.png'
}

function getRarityClass(offer: any): string {
  const rarity = getRarity(offer)
  return `rarity-${rarity}`
}

function getRarity(offer: any): string {
  if (!offer) return 'common'
  
  // 优先从关联对象获取稀有度
  if (offer.cardCharacter && offer.cardCharacter.rarity) {
    return offer.cardCharacter.rarity.toLowerCase()
  }
  if (offer.card && offer.card.rarity) {
    return offer.card.rarity.toLowerCase()
  }
  if (offer.item && offer.item.rarity) {
    return offer.item.rarity.toLowerCase()
  }
  
  return 'common'
}

function getRarityText(offer: any): string {
  const rarity = getRarity(offer)
  const rarityMap: { [key: string]: string } = {
    common: '普通',
    rare: '稀有',
    epic: '史诗',
    legendary: '传说'
  }
  return rarityMap[rarity] || '普通'
}

function getRarityBadgeClass(offer: any): string {
  const rarity = getRarity(offer)
  return `badge-${rarity}`
}

function formatPrice(price: number | bigint): string {
  const num = typeof price === 'bigint' ? Number(price) : price
  if (num >= 1000000) return (num / 1000000).toFixed(1) + 'M'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'K'
  return num.toString()
}

function formatGold(balance: bigint): string {
  if (balance >= 1000000n) {
    const millions = Number(balance) / 1000000
    return millions.toFixed(1) + 'M'
  }
  if (balance >= 1000n) {
    const thousands = Number(balance) / 1000
    return thousands.toFixed(1) + 'K'
  }
  return balance.toString()
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  // 尝试使用默认图片，如果还是失败就隐藏图片
  if (img.src && !img.src.includes('default')) {
    img.src = '/static/tabbar/touxiang.jpg'
  } else {
    img.style.display = 'none'
  }
}

function handleAvatarError(event: Event) {
  console.log('[Shop] NPC头像加载失败，使用占位符')
  avatarError.value = true
}

function isOwned(offer: any): boolean {
  // TODO: 实现检查用户是否已拥有该商品的逻辑
  // 目前暂时返回 false，后续可以从 characters store 或 cards store 检查
  // 对于角色：检查 user_card_characters 表
  // 对于卡牌：检查 user_cards 表
  // 对于道具：检查 user_items 表
  return false
}

function showProductDetail(offer: any) {
  // 可以在这里实现商品详情弹窗
  console.log('[Shop] 查看商品详情:', offer)
}

// 生命周期
onMounted(async () => {
  console.log('[Shop] 组件已挂载，开始初始化...')
  
  // 加载钱包余额
  try {
    await walletStore.loadWallets()
    console.log('[Shop] 钱包余额加载完成:', {
      gold: walletStore.goldBalance.toString(),
      wallets: walletStore.wallets
    })
  } catch (error) {
    console.error('[Shop] 钱包余额加载失败:', error)
  }
  
  // 预加载所有商品到 shopStore（如果还没有加载）
  try {
    if (shopStore.offers.length === 0) {
      console.log('[Shop] 预加载所有商品到 shopStore...')
      await shopStore.fetchShopOffers()
      console.log('[Shop] shopStore 商品数量:', shopStore.offers.length)
    } else {
      console.log('[Shop] shopStore 已有商品:', shopStore.offers.length)
    }
  } catch (error) {
    console.warn('[Shop] 预加载商品失败，将使用按类型获取:', error)
  }
  
  // 加载商品列表
  await loadOffers()
  
  console.log('[Shop] 初始化完成:', {
    activeTab: activeTab.value,
    offersCount: offers.value.length,
    filteredCount: filteredOffers.value.length,
    shopStoreCount: shopStore.offers.length
  })
})
</script>

<style scoped>
.shop-container {
  height: 100vh;
  background: linear-gradient(180deg, #2c1810 0%, #1a0f08 100%);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: relative;
}

/* 木质纹理背景 */
.shop-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 2px,
      rgba(139, 90, 43, 0.1) 2px,
      rgba(139, 90, 43, 0.1) 4px
    ),
    repeating-linear-gradient(
      90deg,
      transparent,
      transparent 2px,
      rgba(101, 67, 33, 0.1) 2px,
      rgba(101, 67, 33, 0.1) 4px
    );
  pointer-events: none;
  z-index: 0;
}

/* 修复WXSS编译错误：微信小程序不支持通配符选择器，改为具体选择器 */
.shop-container .shop-header,
.shop-container .shop-tabs,
.shop-container .featured-section,
.shop-container .products-section {
  position: relative;
  z-index: 1;
}

/* 顶部头部 */
.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 40rpx 32rpx;
  background: linear-gradient(180deg, rgba(139, 90, 43, 0.8) 0%, rgba(101, 67, 33, 0.9) 100%);
  border-bottom: 3px solid rgba(101, 67, 33, 1);
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.5);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.shop-icon {
  font-size: 48rpx;
  color: #ffd700;
}

.shop-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #ffffff;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.gold-icon {
  font-size: 40rpx;
  color: #ffd700;
}

.gold-amount {
  font-size: 32rpx;
  font-weight: bold;
  color: #ffffff;
}

/* 导航标签页 */
.shop-tabs {
  display: flex;
  padding: 24rpx 32rpx;
  gap: 16rpx;
  background: linear-gradient(180deg, rgba(101, 67, 33, 0.6) 0%, rgba(80, 53, 26, 0.7) 100%);
  border-bottom: 2px solid rgba(101, 67, 33, 1);
  flex-shrink: 0;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx;
  background: linear-gradient(180deg, rgba(139, 90, 43, 0.4) 0%, rgba(101, 67, 33, 0.5) 100%);
  border: 2px solid rgba(101, 67, 33, 0.8);
  border-radius: 12rpx;
  color: #d4a574;
  transition: all 0.3s;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
}

.tab-item.active {
  background: linear-gradient(180deg, rgba(205, 133, 63, 0.8) 0%, rgba(160, 82, 45, 0.9) 100%);
  border: 2px solid #d4a574;
  color: #ffffff;
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.4), inset 0 2rpx 4rpx rgba(255, 255, 255, 0.2);
}

.tab-item i {
  font-size: 40rpx;
}

.tab-item text {
  font-size: 24rpx;
}

/* 特色区域 */
.featured-section {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 32rpx;
  margin: 24rpx 32rpx;
  background: linear-gradient(180deg, rgba(139, 90, 43, 0.5) 0%, rgba(101, 67, 33, 0.6) 100%);
  border-radius: 16rpx;
  border: 3px solid rgba(101, 67, 33, 0.9);
  box-shadow: inset 0 2rpx 8rpx rgba(0, 0, 0, 0.4), 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
  flex-shrink: 0;
}

.npc-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #ffd700;
  flex-shrink: 0;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 215, 0, 0.2);
  color: #ffd700;
  font-size: 48rpx;
}

.welcome-message {
  flex: 1;
}

.welcome-text {
  font-size: 28rpx;
  color: #ffffff;
  line-height: 1.6;
}

/* 刷新按钮 */
.refresh-btn-small {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 6rpx 12rpx;
  background: linear-gradient(135deg, #4caf50, #45a049);
  border: none;
  border-radius: 8rpx;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
  transition: all 0.3s;
  white-space: nowrap;
}

.refresh-btn-small.disabled,
.refresh-btn-small:disabled {
  opacity: 0.5;
  background: #666;
  cursor: not-allowed;
}

.refresh-btn-small:not(:disabled):active {
  transform: scale(0.98);
}

.refresh-btn-small .refresh-cost {
  display: flex;
  align-items: center;
  gap: 3rpx;
  margin-left: 6rpx;
}

.refresh-btn-small .refresh-cost i {
  color: #ffd700;
  font-size: 16rpx;
}

.refresh-btn-small .refresh-cost text {
  font-size: 18rpx;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 商品展示区域 */
.products-section {
  padding: 0 32rpx 24rpx;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden; /* 禁止滚动，保持一屏展示 */
  min-height: 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.section-icon {
  font-size: 32rpx;
  color: #ffd700;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #ffffff;
}

.header-right {
  display: flex;
  align-items: center;
}

/* 商品网格 */
.products-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12rpx;
  margin-bottom: 0;
  flex: 1;
  align-content: flex-start; /* 从顶部开始排列 */
  overflow-y: auto; /* 允许滚动，防止超出底部 */
  max-height: 100%;
}

.product-card {
  background: linear-gradient(180deg, rgba(139, 90, 43, 0.6) 0%, rgba(101, 67, 33, 0.7) 100%);
  border-radius: 12rpx;
  overflow: hidden;
  border: 3px solid rgba(101, 67, 33, 0.9);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  min-width: 0;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.3), 0 2rpx 8rpx rgba(0, 0, 0, 0.4);
  position: relative;
  height: 100%;
  max-height: 100%;
}

.product-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    repeating-linear-gradient(
      0deg,
      transparent,
      transparent 1px,
      rgba(101, 67, 33, 0.2) 1px,
      rgba(101, 67, 33, 0.2) 2px
    );
  pointer-events: none;
  z-index: 0;
}

/* 修复WXSS编译错误：微信小程序不支持通配符选择器 */
.product-card .product-image-wrapper,
.product-card .product-info,
.product-card .product-actions {
  position: relative;
  z-index: 1;
}

.product-card:active {
  transform: scale(0.98);
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.4), 0 1rpx 4rpx rgba(0, 0, 0, 0.3);
}

.product-card:active {
  transform: scale(0.98);
}

/* 稀有度边框颜色 */
.product-card.rarity-common {
  border-color: rgba(128, 128, 128, 0.5);
}

.product-card.rarity-rare {
  border-color: rgba(33, 150, 243, 0.6);
  box-shadow: 0 0 20rpx rgba(33, 150, 243, 0.3);
}

.product-card.rarity-epic {
  border-color: rgba(156, 39, 176, 0.6);
  box-shadow: 0 0 20rpx rgba(156, 39, 176, 0.3);
}

.product-card.rarity-legendary {
  border-color: rgba(255, 152, 0, 0.8);
  box-shadow: 0 0 30rpx rgba(255, 152, 0, 0.4);
}

.product-image-wrapper {
  position: relative;
  width: 100%;
  flex: 1;
  min-height: 360rpx;
  max-height: 480rpx;
  overflow: hidden;
  background: linear-gradient(180deg, rgba(80, 53, 26, 0.8) 0%, rgba(60, 40, 20, 0.9) 100%);
  flex-shrink: 0;
  border-bottom: 2px solid rgba(101, 67, 33, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 8rpx;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  object-position: center;
  max-width: 100%;
  max-height: 100%;
}

/* 角色攻击 / 生命覆盖在图片底部左右两侧 */
.card-stat-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 8rpx;
  display: flex;
  justify-content: space-between;
  padding: 0 8rpx;
  pointer-events: none;
  z-index: 2;
}

.stat-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.75);
  border: 2px solid rgba(255, 255, 255, 0.3);
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
  position: relative;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
}

.stat-badge i {
  position: absolute;
  font-size: 20rpx;
  opacity: 0.7;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.stat-badge text {
  position: relative;
  z-index: 2;
  font-size: 20rpx;
  font-weight: bold;
  text-shadow: 0 1rpx 3rpx rgba(0, 0, 0, 0.9);
  line-height: 1;
}

.stat-badge-attack {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.9) 0%, rgba(220, 38, 38, 0.9) 100%);
  border-color: rgba(255, 200, 150, 0.5);
}

.stat-badge-attack i {
  color: #ffffff;
  font-size: 20rpx;
}

.stat-badge-attack text {
  color: #ffffff;
}

.stat-badge-health {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.9) 0%, rgba(185, 28, 28, 0.9) 100%);
  border-color: rgba(255, 200, 200, 0.5);
}

.stat-badge-health i {
  color: #ffffff;
  font-size: 20rpx;
}

.stat-badge-health text {
  color: #ffffff;
}

.price-badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  display: flex;
  align-items: center;
  gap: 2rpx;
  padding: 4rpx 8rpx;
  background: linear-gradient(135deg, rgba(139, 90, 43, 0.9) 0%, rgba(101, 67, 33, 0.95) 100%);
  border: 2px solid rgba(212, 165, 116, 0.8);
  border-radius: 12rpx;
  color: #ffd700;
  font-size: 16rpx;
  font-weight: bold;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.4);
}

.price-badge i {
  font-size: 16rpx;
}

.owned-badge {
  position: absolute;
  top: 4rpx;
  right: 4rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48rpx;
  height: 48rpx;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.9) 0%, rgba(56, 142, 60, 0.95) 100%);
  border: 2px solid rgba(129, 199, 132, 0.8);
  border-radius: 50%;
  color: #ffffff;
  font-size: 24rpx;
  box-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.4);
}

.product-info {
  padding: 6rpx 8rpx;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.product-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 6rpx;
  gap: 4rpx;
}

.product-name {
  flex: 1;
  font-size: 20rpx;
  font-weight: bold;
  color: #ffffff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.2;
  min-width: 0;
}

.rarity-badge {
  padding: 2rpx 8rpx;
  border-radius: 12rpx;
  font-size: 16rpx;
  font-weight: bold;
  white-space: nowrap;
  flex-shrink: 0;
}

.rarity-badge.badge-common {
  background: rgba(128, 128, 128, 0.8);
  color: #ffffff;
}

.rarity-badge.badge-rare {
  background: rgba(33, 150, 243, 0.8);
  color: #ffffff;
}

.rarity-badge.badge-epic {
  background: rgba(156, 39, 176, 0.8);
  color: #ffffff;
}

.rarity-badge.badge-legendary {
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.9), rgba(255, 87, 34, 0.9));
  color: #ffffff;
}

/* 角色属性显示 */
.product-stats {
  display: flex;
  gap: 12rpx;
  margin-bottom: 6rpx;
  align-items: center;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 8rpx;
  border-radius: 8rpx;
  font-size: 18rpx;
  font-weight: bold;
}

.stat-item.attack {
  background: rgba(244, 67, 54, 0.2);
  color: #f44336;
}

.stat-item.attack i {
  color: #f44336;
  font-size: 16rpx;
}

.stat-item.health {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.stat-item.health i {
  color: #4caf50;
  font-size: 16rpx;
}

.product-description {
  display: flex;
  align-items: flex-start;
  gap: 4rpx;
  margin-bottom: 4rpx;
  flex-shrink: 0;
  min-height: 0;
}

.star-icon {
  font-size: 16rpx;
  color: #ffd700;
  margin-top: 2rpx;
  flex-shrink: 0;
}

.desc-text {
  flex: 1;
  font-size: 18rpx;
  color: #9ca3af;
  line-height: 1.3;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  word-break: break-word;
  white-space: nowrap;
}

.product-actions {
  padding: 0 10rpx 10rpx;
  margin-top: auto;
}

.buy-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  padding: 10rpx;
  background: linear-gradient(135deg, rgba(205, 133, 63, 0.9) 0%, rgba(160, 82, 45, 0.95) 100%);
  border: 2px solid rgba(212, 165, 116, 0.8);
  border-radius: 8rpx;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
  transition: all 0.3s;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.3), 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

.buy-btn:not(.disabled):active {
  transform: scale(0.98);
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.4);
}

.buy-btn.disabled {
  background: linear-gradient(135deg, rgba(80, 53, 26, 0.6) 0%, rgba(60, 40, 20, 0.7) 100%);
  border-color: rgba(101, 67, 33, 0.5);
  opacity: 0.6;
}

.owned-indicator {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding: 10rpx;
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.6) 0%, rgba(56, 142, 60, 0.7) 100%);
  border: 2px solid rgba(129, 199, 132, 0.8);
  border-radius: 8rpx;
  color: #ffffff;
  font-size: 20rpx;
  font-weight: bold;
  box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.2);
}

.owned-indicator i {
  font-size: 24rpx;
  color: #4caf50;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  color: #9ca3af;
  gap: 16rpx;
}

.empty-state i {
  font-size: 80rpx;
  opacity: 0.5;
  margin-bottom: 16rpx;
}

.empty-state text {
  font-size: 28rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #6b7280;
  margin-top: 8rpx;
}

.empty-tip {
  font-size: 22rpx;
  color: #4b5563;
  margin-top: 4rpx;
}

/* 加载状态 */
.loading-state {
  display: flex;
    flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80rpx 32rpx;
  color: #9ca3af;
  gap: 24rpx;
}

.loading-state i {
  font-size: 60rpx;
}

.loading-state text {
  font-size: 28rpx;
}

/* 响应式适配 - 确保始终显示3列 */
@media (max-width: 768px) {
  .products-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 10rpx;
  }
  
  /* 在小屏幕上进一步缩小 */
  .product-image-wrapper {
    min-height: 320rpx;
    max-height: 420rpx;
  }
  
  .product-info {
    padding: 8rpx;
  }
  
  .product-name {
    font-size: 18rpx;
  }
  
  .desc-text {
    font-size: 16rpx;
  }
  
  .buy-btn {
    padding: 8rpx;
    font-size: 18rpx;
  }
}
</style>
