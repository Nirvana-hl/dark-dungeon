<template>
  <view class="shop-simple">
    <!-- 页面标题 -->
    <view class="page-header">
      <h1>🛒 商城</h1>
      <p>购买道具、装备和角色卡牌</p>
    </view>

    <!-- 货币显示 -->
    <view class="currency-bar">
      <view class="currencies">
        <view class="currency-item">
          <i class="fas fa-coins gold"></i>
          <text class="currency-amount">{{ currencies.gold }}</text>
        </view>
        <view class="currency-item">
          <i class="fas fa-gem gem"></i>
          <text class="currency-amount">{{ currencies.gems }}</text>
        </view>
        <view class="currency-item">
          <i class="fas fa-crystal crystal"></i>
          <text class="currency-amount">{{ currencies.crystals }}</text>
        </view>
      </view>
      <view class="refresh-shop">
        <button @click="refreshShop" :disabled="isRefreshing" class="refresh-btn">
          <i class="fas fa-sync-alt" :class="{ spinning: isRefreshing }"></i>
          {{ isRefreshing ? '刷新中...' : '刷新商店' }}
        </button>
      </view>
    </view>

    <!-- 商品分类 -->
    <view class="shop-categories">
      <button 
        v-for="category in categories" 
        :key="category.id"
        :class="['category-btn', { active: activeCategory === category.id }]"
        @click="activeCategory = category.id"
      >
        <i :class="category.icon"></i>
        {{ category.name }}
        <text class="item-count">{{ getCategoryCount(category.id) }}</text>
      </button>
    </view>

    <!-- 商品列表 -->
    <view class="shop-content">
      <view class="offers-grid">
        <view 
          v-for="offer in filteredOffers" 
          :key="offer.id"
          class="offer-card"
          :class="{ 
            'discount': offer.discount,
            'purchased': offer.purchased,
            'locked': !canAfford(offer)
          }"
          @click="showOfferDetails(offer)"
        >
          <!-- 商品头部 -->
          <view class="offer-header">
            <view class="offer-icon">
              <i :class="offer.icon"></i>
            </view>
            <view class="offer-type">{{ offer.type }}</view>
            <view v-if="offer.discount" class="discount-badge">
              -{{ offer.discount }}%
            </view>
          </view>
          
          <!-- 商品信息 -->
          <view class="offer-info">
            <h3 class="offer-name">{{ offer.name }}</h3>
            <p class="offer-description">{{ offer.description }}</p>
            
            <!-- 商品属性 -->
            <view class="offer-stats" v-if="offer.stats">
              <view v-for="stat in offer.stats" :key="stat.name" class="stat-item">
                <i :class="stat.icon"></i>
                <text>{{ stat.value }}</text>
              </view>
            </view>
            
            <!-- 稀有度指示器 -->
            <view class="rarity-indicator" :class="offer.rarity">
              <view v-for="i in getRarityStars(offer.rarity)" :key="i" class="star">
                <i class="fas fa-star"></i>
              </view>
            </view>
          </view>
          
          <!-- 价格区域 -->
          <view class="offer-pricing">
            <view v-if="offer.discount" class="original-price">
              <i class="fas fa-coins gold"></i>
              {{ offer.originalPrice }}
            </view>
            <view class="current-price">
              <i class="fas fa-coins gold"></i>
              {{ offer.currentPrice }}
            </view>
          </view>
          
          <!-- 购买按钮 -->
          <view class="offer-actions">
            <button 
              v-if="!offer.purchased"
              class="purchase-btn"
              :disabled="!canAfford(offer)"
              @click.stop="purchaseOffer(offer)"
            >
              <i class="fas fa-shopping-cart"></i>
              {{ canAfford(offer) ? '购买' : '金币不足' }}
            </button>
            <button v-else class="purchased-btn" disabled>
              <i class="fas fa-check"></i>
              已购买
            </button>
          </view>
          
          <!-- 库存显示 -->
          <view class="stock-info">
            <text v-if="offer.stock !== undefined" class="stock-count">
              库存: {{ offer.stock }}
            </text>
            <text v-if="offer.limitPerPlayer" class="limit-info">
              限购: {{ offer.limitPerPlayer - (offer.purchasedCount || 0) }}/{{ offer.limitPerPlayer }}
            </text>
          </view>
        </view>
      </view>
      
      <!-- 空状态 -->
      <view v-if="filteredOffers.length === 0" class="empty-shop">
        <i class="fas fa-store-slash"></i>
        <h3>暂无商品</h3>
        <p>该分类下暂时没有可购买的商品</p>
      </view>
    </view>

    <!-- 商品详情模态框 -->
    <view v-if="selectedOffer" class="modal-overlay" @click="closeOfferDetails">
      <view class="offer-modal" @click.stop>
        <view class="modal-header">
          <h2>{{ selectedOffer.name }}</h2>
          <button @click="closeOfferDetails" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </view>
        
        <view class="modal-content">
          <view class="offer-preview">
            <view class="preview-icon">
              <i :class="selectedOffer.icon"></i>
            </view>
            <view class="preview-info">
              <view class="rarity-badge" :class="selectedOffer.rarity">
                {{ getRarityName(selectedOffer.rarity) }}
              </view>
              <view class="offer-type">{{ selectedOffer.type }}</view>
            </view>
          </view>
          
          <view class="offer-description">
            <p>{{ selectedOffer.description }}</p>
            <p v-if="selectedOffer.longDescription">{{ selectedOffer.longDescription }}</p>
          </view>
          
          <view class="offer-effects" v-if="selectedOffer.effects">
            <h4>效果说明:</h4>
            <ul>
              <li v-for="effect in selectedOffer.effects" :key="effect">
                {{ effect }}
              </li>
            </ul>
          </view>
          
          <view class="purchase-details">
            <view class="price-breakdown">
              <view class="price-row">
                <text>商品价格:</text>
                <text>
                  <i class="fas fa-coins gold"></i>
                  {{ selectedOffer.currentPrice }}
                </text>
              </view>
              <view v-if="selectedOffer.discount" class="price-row discount">
                <text>优惠折扣:</text>
                <text>-{{ selectedOffer.discount }}%</text>
              </view>
              <view class="price-row total">
                <text>总计:</text>
                <text>
                  <i class="fas fa-coins gold"></i>
                  {{ selectedOffer.currentPrice }}
                </text>
              </view>
            </view>
          </view>
        </view>
        
        <view class="modal-actions">
          <button @click="closeOfferDetails" class="cancel-btn">
            取消
          </button>
          <button 
            @click="purchaseOffer(selectedOffer)"
            class="confirm-purchase-btn"
            :disabled="!canAfford(selectedOffer)"
          >
            <i class="fas fa-shopping-cart"></i>
            确认购买
          </button>
        </view>
      </view>
    </view>

    <!-- 通知提示 -->
    <view v-if="notification" class="notification" :class="notification.type">
      <i :class="notification.icon"></i>
      <text>{{ notification.message }}</text>
      <button @click="notification = null" class="close-btn">
        <i class="fas fa-times"></i>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

// 货币数据
const currencies = ref({
  gold: 1000,
  gems: 50,
  crystals: 10
})

// 商城分类
const categories = ref([
  { id: 'characters', name: '角色', icon: 'fas fa-users' },
  { id: 'equipment', name: '装备', icon: 'fas fa-shield-alt' },
  { id: 'spells', name: '法术', icon: 'fas fa-magic' },
  { id: 'consumables', name: '消耗品', icon: 'fas fa-flask' },
  { id: 'bundles', name: '礼包', icon: 'fas fa-gift' }
])

const activeCategory = ref('characters')
const isRefreshing = ref(false)
const selectedOffer = ref<any>(null)
const notification = ref<any>(null)

// 商品数据
const offers = ref([
  // 角色
  {
    id: '1',
    name: '狂战士',
    type: '角色卡',
    category: 'characters',
    description: '攻击力强大的近战角色',
    longDescription: '狂战士拥有极高的攻击力和生命值，但防御较弱。适合担任前排输出角色。',
    icon: 'fas fa-sword',
    rarity: 'rare',
    currentPrice: 200,
    originalPrice: 250,
    discount: 20,
    stock: 5,
    limitPerPlayer: 1,
    purchased: false,
    purchasedCount: 0,
    stats: [
      { name: '攻击', value: '+15', icon: 'fas fa-sword' },
      { name: '生命', value: '+20', icon: 'fas fa-heart' }
    ],
    effects: ['攻击力 +15%', '生命值 +20', '特性: 狂暴']
  },
  {
    id: '2',
    name: '圣骑士',
    type: '角色卡',
    category: 'characters',
    description: '防御力强悍的坦克角色',
    icon: 'fas fa-shield-alt',
    rarity: 'epic',
    currentPrice: 300,
    stock: 3,
    limitPerPlayer: 1,
    purchased: false,
    stats: [
      { name: '防御', value: '+25', icon: 'fas fa-shield' },
      { name: '生命', value: '+30', icon: 'fas fa-heart' }
    ],
    effects: ['防御力 +25%', '生命值 +30', '特性: 守护']
  },
  
  // 装备
  {
    id: '3',
    name: '炎龙之剑',
    type: '武器',
    category: 'equipment',
    description: '带有火焰伤害的传说武器',
    icon: 'fas fa-fire',
    rarity: 'legendary',
    currentPrice: 500,
    stock: 1,
    limitPerPlayer: 1,
    purchased: false,
    stats: [
      { name: '攻击', value: '+35', icon: 'fas fa-sword' },
      { name: '火焰', value: '+15', icon: 'fas fa-fire' }
    ],
    effects: ['攻击力 +35', '附加火焰伤害 +15', '特效: 燃烧']
  },
  
  // 消耗品
  {
    id: '4',
    name: '生命药水',
    type: '消耗品',
    category: 'consumables',
    description: '恢复50点生命值',
    icon: 'fas fa-flask',
    rarity: 'common',
    currentPrice: 20,
    stock: 20,
    limitPerPlayer: 10,
    purchased: false,
    effects: ['立即恢复 50 生命值']
  },
  
  // 礼包
  {
    id: '5',
    name: '新手礼包',
    type: '礼包',
    category: 'bundles',
    description: '包含多种道具的新手福利',
    longDescription: '适合刚开始冒险的新手玩家，包含基础装备和消耗品。',
    icon: 'fas fa-gift',
    rarity: 'rare',
    currentPrice: 100,
    originalPrice: 150,
    discount: 33,
    stock: 10,
    limitPerPlayer: 1,
    purchased: false,
    effects: ['随机角色卡 x1', '生命药水 x5', '金币 x100']
  }
])

// 计算属性
const filteredOffers = computed(() => {
  return offers.value.filter(offer => offer.category === activeCategory.value)
})

// 方法
function getCategoryCount(categoryId: string) {
  return offers.value.filter(offer => offer.category === categoryId).length
}

function canAfford(offer: any) {
  return currencies.value.gold >= offer.currentPrice
}

function getRarityStars(rarity: string) {
  const rarityMap: { [key: string]: number } = {
    common: 1,
    rare: 2,
    epic: 3,
    legendary: 4
  }
  return rarityMap[rarity] || 1
}

function getRarityName(rarity: string) {
  const rarityMap: { [key: string]: string } = {
    common: '普通',
    rare: '稀有',
    epic: '史诗',
    legendary: '传说'
  }
  return rarityMap[rarity] || '普通'
}

function showOfferDetails(offer: any) {
  selectedOffer.value = offer
}

function closeOfferDetails() {
  selectedOffer.value = null
}

function purchaseOffer(offer: any) {
  if (!canAfford(offer)) {
    showNotification('error', '金币不足', 'fas fa-exclamation-circle')
    return
  }
  
  if (offer.stock !== undefined && offer.stock <= 0) {
    showNotification('error', '商品已售罄', 'fas fa-times-circle')
    return
  }
  
  if (offer.limitPerPlayer && (offer.purchasedCount || 0) >= offer.limitPerPlayer) {
    showNotification('error', '已达到购买上限', 'fas fa-exclamation-circle')
    return
  }
  
  // 执行购买
  currencies.value.gold -= offer.currentPrice
  offer.purchased = true
  offer.purchasedCount = (offer.purchasedCount || 0) + 1
  if (offer.stock !== undefined) {
    offer.stock -= 1
  }
  
  showNotification('success', `成功购买 ${offer.name}!`, 'fas fa-check-circle')
  closeOfferDetails()
}

function refreshShop() {
  isRefreshing.value = true
  
  setTimeout(() => {
    // 模拟刷新商店
    offers.value.forEach(offer => {
      if (!offer.purchased) {
        offer.discount = Math.random() > 0.7 ? Math.floor(Math.random() * 30) + 10 : undefined
        if (offer.discount) {
          offer.originalPrice = offer.currentPrice
          offer.currentPrice = Math.floor(offer.currentPrice * (1 - offer.discount / 100))
        }
      }
    })
    
    isRefreshing.value = false
    showNotification('info', '商店已刷新', 'fas fa-sync-alt')
  }, 1500)
}

function showNotification(type: string, message: string, icon: string) {
  notification.value = { type, message, icon }
  setTimeout(() => {
    notification.value = null
  }, 3000)
}
</script>

<style scoped>
.shop-simple {
  padding: 64rpx;
  max-width: 2400rpx;
  margin: 0 auto;
  min-height: calc(100vh - 160rpx);
}

.page-header {
  text-align: center;
  margin-bottom: 64rpx;
  color: var(--text-primary);
}

.page-header h1 {
  font-size: 80rpx;
  margin-bottom: 16rpx;
  background: linear-gradient(135deg, #ffd700, #ff9800);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  font-size: 35rpx;
  color: var(--text-secondary);
}

.currency-bar {
  background: linear-gradient(135deg, var(--secondary-bg), rgba(255, 215, 0, 0.1));
  border: 1rpx solid #ffd700;
  border-radius: 24rpx;
  padding: 48rpx;
  margin-bottom: 64rpx;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 32rpx;
}

.currencies {
  display: flex;
  gap: 64rpx;
}

.currency-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-weight: bold;
}

.currency-amount {
  font-size: 38rpx;
  color: var(--text-primary);
}

.gold { color: #ffd700; }
.gem { color: #e91e63; }
.crystal { color: #00bcd4; }

.refresh-btn {
  background: #ffd700;
  color: #000;
  border: none;
  padding: 24rpx 48rpx;
  border-radius: 12rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.refresh-btn:disabled {
  opacity: 0.6;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.shop-categories {
  display: flex;
  gap: 16rpx;
  margin-bottom: 64rpx;
  background: var(--secondary-bg);
  padding: 16rpx;
  border-radius: 16rpx;
  border: 1rpx solid var(--border-color);
  flex-wrap: wrap;
}

.category-btn {
  padding: 24rpx 32rpx;
  background: transparent;
  border: none;
  border-radius: 12rpx;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-weight: bold;
  position: relative;
}

.category-btn.active {
  background: #ffd700;
  color: #000;
}

.item-count {
  background: rgba(0, 0, 0, 0.3);
  color: white;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  margin-left: 8rpx;
}

.offers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(560rpx, 1fr));
  gap: 48rpx;
}

.offer-card {
  background: var(--secondary-bg);
  border: 2rpx solid var(--border-color);
  border-radius: 24rpx;
  overflow: hidden;
  position: relative;
}

.offer-card.discount {
  border-color: #4caf50;
}

.offer-card.purchased {
  opacity: 0.6;
  border-color: var(--text-muted);
}

.offer-card.locked {
  opacity: 0.4;
}

.offer-header {
  position: relative;
  padding: 32rpx;
  background: var(--tertiary-bg);
  border-bottom: 1rpx solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.offer-icon {
  font-size: 64rpx;
  color: var(--text-primary);
}

.offer-type {
  font-size: 26rpx;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.3);
  padding: 8rpx 16rpx;
  border-radius: 8rpx;
}

.discount-badge {
  position: absolute;
  top: -20rpx;
  right: -20rpx;
  background: #4caf50;
  color: white;
  padding: 16rpx;
  border-radius: 50%;
  font-size: 26rpx;
  font-weight: bold;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
}

.offer-info {
  padding: 32rpx;
}

.offer-name {
  font-size: 35rpx;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 16rpx;
}

.offer-description {
  color: var(--text-secondary);
  font-size: 29rpx;
  margin-bottom: 32rpx;
}

.offer-stats {
  display: flex;
  gap: 24rpx;
  margin-bottom: 32rpx;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
  font-size: 26rpx;
  color: var(--text-secondary);
}

.rarity-indicator {
  display: flex;
  gap: 8rpx;
  margin-bottom: 32rpx;
}

.rarity-indicator.common .star { color: #9e9e9e; }
.rarity-indicator.rare .star { color: #2196f3; }
.rarity-indicator.epic .star { color: #9c27b0; }
.rarity-indicator.legendary .star { color: #ff9800; }

.offer-pricing {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 0 32rpx;
  margin-bottom: 32rpx;
}

.original-price {
  text-decoration: line-through;
  color: var(--text-muted);
  font-size: 29rpx;
}

.current-price {
  font-weight: bold;
  font-size: 38rpx;
  color: #ffd700;
}

.offer-actions {
  padding: 0 32rpx 32rpx;
}

.purchase-btn {
  width: 100%;
  background: #ffd700;
  color: #000;
  border: none;
  padding: 24rpx;
  border-radius: 12rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.purchase-btn:disabled {
  background: var(--text-muted);
  color: var(--text-secondary);
}

.purchased-btn {
  width: 100%;
  background: #4caf50;
  color: white;
  border: none;
  padding: 24rpx;
  border-radius: 12rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.stock-info {
  padding: 16rpx 32rpx;
  border-top: 1rpx solid var(--border-color);
  display: flex;
  justify-content: space-between;
  font-size: 26rpx;
  color: var(--text-secondary);
}

.empty-shop {
  text-align: center;
  padding: 96rpx;
  color: var(--text-muted);
}

.empty-shop i {
  font-size: 128rpx;
  margin-bottom: 32rpx;
  opacity: 0.5;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 32rpx;
}

.offer-modal {
  background: var(--secondary-bg);
  border: 1rpx solid var(--border-color);
  border-radius: 24rpx;
  max-width: 1000rpx;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 48rpx;
  border-bottom: 1rpx solid var(--border-color);
}

.modal-header h2 {
  font-size: 48rpx;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  font-size: 48rpx;
  padding: 16rpx;
  border-radius: 8rpx;
}

.modal-content {
  padding: 48rpx;
}

.offer-preview {
  display: flex;
  align-items: center;
  gap: 32rpx;
  margin-bottom: 48rpx;
}

.preview-icon {
  font-size: 96rpx;
  color: var(--text-primary);
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.rarity-badge {
  padding: 8rpx 24rpx;
  border-radius: 30rpx;
  font-size: 26rpx;
  font-weight: bold;
  text-align: center;
}

.rarity-badge.common { background: #9e9e9e; color: white; }
.rarity-badge.rare { background: #2196f3; color: white; }
.rarity-badge.epic { background: #9c27b0; color: white; }
.rarity-badge.legendary { background: #ff9800; color: white; }

.offer-description {
  margin-bottom: 48rpx;
  color: var(--text-secondary);
  line-height: 1.6;
}

.offer-effects {
  margin-bottom: 48rpx;
}

.offer-effects h4 {
  color: var(--text-primary);
  margin-bottom: 16rpx;
}

.offer-effects ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.offer-effects li {
  padding: 8rpx 0;
  color: var(--text-secondary);
}

.offer-effects li::before {
  content: "✓ ";
  color: #4caf50;
  font-weight: bold;
}

.price-breakdown {
  background: var(--tertiary-bg);
  padding: 32rpx;
  border-radius: 16rpx;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
}

.price-row.discount {
  color: #4caf50;
}

.price-row.total {
  border-top: 1rpx solid var(--border-color);
  padding-top: 16rpx;
  font-weight: bold;
  font-size: 35rpx;
  color: var(--text-primary);
}

.modal-actions {
  display: flex;
  gap: 32rpx;
  padding: 48rpx;
  border-top: 1rpx solid var(--border-color);
}

.cancel-btn {
  flex: 1;
  background: var(--tertiary-bg);
  color: var(--text-primary);
  border: 1rpx solid var(--border-color);
  padding: 24rpx;
  border-radius: 12rpx;
  font-weight: bold;
}

.confirm-purchase-btn {
  flex: 2;
  background: #ffd700;
  color: #000;
  border: none;
  padding: 24rpx;
  border-radius: 12rpx;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
}

.confirm-purchase-btn:disabled {
  background: var(--text-muted);
  color: var(--text-secondary);
}

.notification {
  position: fixed;
  top: 40rpx;
  right: 40rpx;
  padding: 32rpx 48rpx;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  z-index: 1001;
  max-width: 600rpx;
  box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.3);
}

.notification.success {
  background: #4caf50;
  color: white;
}

.notification.error {
  background: #f44336;
  color: white;
}

.notification.info {
  background: #2196f3;
  color: white;
}

@media (max-width: 768rpx) {
  .shop-simple {
    padding: 32rpx;
  }

  .currency-bar {
    flex-direction: column;
    text-align: center;
  }

  .offers-grid {
    grid-template-columns: 1fr;
  }

  .shop-categories {
    justify-content: center;
  }

  .modal-actions {
    flex-direction: column;
  }
}
</style>