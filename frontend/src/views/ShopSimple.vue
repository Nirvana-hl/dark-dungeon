<template>
  <div class="shop-simple">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>🛒 商城</h1>
      <p>购买道具、装备和角色卡牌</p>
    </div>

    <!-- 货币显示 -->
    <div class="currency-bar">
      <div class="currencies">
        <div class="currency-item">
          <i class="fas fa-coins gold"></i>
          <span class="currency-amount">{{ currencies.gold }}</span>
        </div>
        <div class="currency-item">
          <i class="fas fa-gem gem"></i>
          <span class="currency-amount">{{ currencies.gems }}</span>
        </div>
        <div class="currency-item">
          <i class="fas fa-crystal crystal"></i>
          <span class="currency-amount">{{ currencies.crystals }}</span>
        </div>
      </div>
      <div class="refresh-shop">
        <button @click="refreshShop" :disabled="isRefreshing" class="refresh-btn">
          <i class="fas fa-sync-alt" :class="{ spinning: isRefreshing }"></i>
          {{ isRefreshing ? '刷新中...' : '刷新商店' }}
        </button>
      </div>
    </div>

    <!-- 商品分类 -->
    <div class="shop-categories">
      <button 
        v-for="category in categories" 
        :key="category.id"
        :class="['category-btn', { active: activeCategory === category.id }]"
        @click="activeCategory = category.id"
      >
        <i :class="category.icon"></i>
        {{ category.name }}
        <span class="item-count">{{ getCategoryCount(category.id) }}</span>
      </button>
    </div>

    <!-- 商品列表 -->
    <div class="shop-content">
      <div class="offers-grid">
        <div 
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
          <div class="offer-header">
            <div class="offer-icon">
              <i :class="offer.icon"></i>
            </div>
            <div class="offer-type">{{ offer.type }}</div>
            <div v-if="offer.discount" class="discount-badge">
              -{{ offer.discount }}%
            </div>
          </div>
          
          <!-- 商品信息 -->
          <div class="offer-info">
            <h3 class="offer-name">{{ offer.name }}</h3>
            <p class="offer-description">{{ offer.description }}</p>
            
            <!-- 商品属性 -->
            <div class="offer-stats" v-if="offer.stats">
              <div v-for="stat in offer.stats" :key="stat.name" class="stat-item">
                <i :class="stat.icon"></i>
                <span>{{ stat.value }}</span>
              </div>
            </div>
            
            <!-- 稀有度指示器 -->
            <div class="rarity-indicator" :class="offer.rarity">
              <div v-for="i in getRarityStars(offer.rarity)" :key="i" class="star">
                <i class="fas fa-star"></i>
              </div>
            </div>
          </div>
          
          <!-- 价格区域 -->
          <div class="offer-pricing">
            <div v-if="offer.discount" class="original-price">
              <i class="fas fa-coins gold"></i>
              {{ offer.originalPrice }}
            </div>
            <div class="current-price">
              <i class="fas fa-coins gold"></i>
              {{ offer.currentPrice }}
            </div>
          </div>
          
          <!-- 购买按钮 -->
          <div class="offer-actions">
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
          </div>
          
          <!-- 库存显示 -->
          <div class="stock-info">
            <span v-if="offer.stock !== undefined" class="stock-count">
              库存: {{ offer.stock }}
            </span>
            <span v-if="offer.limitPerPlayer" class="limit-info">
              限购: {{ offer.limitPerPlayer - (offer.purchasedCount || 0) }}/{{ offer.limitPerPlayer }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="filteredOffers.length === 0" class="empty-shop">
        <i class="fas fa-store-slash"></i>
        <h3>暂无商品</h3>
        <p>该分类下暂时没有可购买的商品</p>
      </div>
    </div>

    <!-- 商品详情模态框 -->
    <div v-if="selectedOffer" class="modal-overlay" @click="closeOfferDetails">
      <div class="offer-modal" @click.stop>
        <div class="modal-header">
          <h2>{{ selectedOffer.name }}</h2>
          <button @click="closeOfferDetails" class="close-btn">
            <i class="fas fa-times"></i>
          </button>
        </div>
        
        <div class="modal-content">
          <div class="offer-preview">
            <div class="preview-icon">
              <i :class="selectedOffer.icon"></i>
            </div>
            <div class="preview-info">
              <div class="rarity-badge" :class="selectedOffer.rarity">
                {{ getRarityName(selectedOffer.rarity) }}
              </div>
              <div class="offer-type">{{ selectedOffer.type }}</div>
            </div>
          </div>
          
          <div class="offer-description">
            <p>{{ selectedOffer.description }}</p>
            <p v-if="selectedOffer.longDescription">{{ selectedOffer.longDescription }}</p>
          </div>
          
          <div class="offer-effects" v-if="selectedOffer.effects">
            <h4>效果说明:</h4>
            <ul>
              <li v-for="effect in selectedOffer.effects" :key="effect">
                {{ effect }}
              </li>
            </ul>
          </div>
          
          <div class="purchase-details">
            <div class="price-breakdown">
              <div class="price-row">
                <span>商品价格:</span>
                <span>
                  <i class="fas fa-coins gold"></i>
                  {{ selectedOffer.currentPrice }}
                </span>
              </div>
              <div v-if="selectedOffer.discount" class="price-row discount">
                <span>优惠折扣:</span>
                <span>-{{ selectedOffer.discount }}%</span>
              </div>
              <div class="price-row total">
                <span>总计:</span>
                <span>
                  <i class="fas fa-coins gold"></i>
                  {{ selectedOffer.currentPrice }}
                </span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="modal-actions">
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
        </div>
      </div>
    </div>

    <!-- 通知提示 -->
    <div v-if="notification" class="notification" :class="notification.type">
      <i :class="notification.icon"></i>
      <span>{{ notification.message }}</span>
      <button @click="notification = null" class="close-btn">
        <i class="fas fa-times"></i>
      </button>
    </div>
  </div>
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
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  min-height: calc(100vh - 80px);
}

.page-header {
  text-align: center;
  margin-bottom: 2rem;
  color: var(--text-primary);
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #ffd700, #ff9800);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  font-size: 1.1rem;
  color: var(--text-secondary);
}

.currency-bar {
  background: linear-gradient(135deg, var(--secondary-bg), rgba(255, 215, 0, 0.1));
  border: 1px solid #ffd700;
  border-radius: 12px;
  padding: 1.5rem;
  margin-bottom: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.currencies {
  display: flex;
  gap: 2rem;
}

.currency-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
}

.currency-amount {
  font-size: 1.2rem;
  color: var(--text-primary);
}

.gold { color: #ffd700; }
.gem { color: #e91e63; }
.crystal { color: #00bcd4; }

.refresh-btn {
  background: #ffd700;
  color: #000;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.refresh-btn:hover:not(:disabled) {
  background: #ffed4e;
  transform: translateY(-1px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  gap: 0.5rem;
  margin-bottom: 2rem;
  background: var(--secondary-bg);
  padding: 0.5rem;
  border-radius: 8px;
  border: 1px solid var(--border-color);
  flex-wrap: wrap;
}

.category-btn {
  padding: 0.75rem 1rem;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: bold;
  position: relative;
}

.category-btn:hover {
  background: var(--tertiary-bg);
  color: var(--text-primary);
}

.category-btn.active {
  background: #ffd700;
  color: #000;
}

.item-count {
  background: rgba(0, 0, 0, 0.3);
  color: white;
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  margin-left: 0.25rem;
}

.offers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
}

.offer-card {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.offer-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3);
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
  cursor: not-allowed;
}

.offer-header {
  position: relative;
  padding: 1rem;
  background: var(--tertiary-bg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.offer-icon {
  font-size: 2rem;
  color: var(--text-primary);
}

.offer-type {
  font-size: 0.8rem;
  color: var(--text-secondary);
  background: rgba(0, 0, 0, 0.3);
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
}

.discount-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #4caf50;
  color: white;
  padding: 0.5rem;
  border-radius: 50%;
  font-size: 0.8rem;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.offer-info {
  padding: 1rem;
}

.offer-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.offer-description {
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.offer-stats {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.rarity-indicator {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1rem;
}

.rarity-indicator.common .star { color: #9e9e9e; }
.rarity-indicator.rare .star { color: #2196f3; }
.rarity-indicator.epic .star { color: #9c27b0; }
.rarity-indicator.legendary .star { color: #ff9800; }

.offer-pricing {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0 1rem;
  margin-bottom: 1rem;
}

.original-price {
  text-decoration: line-through;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.current-price {
  font-weight: bold;
  font-size: 1.2rem;
  color: #ffd700;
}

.offer-actions {
  padding: 0 1rem 1rem;
}

.purchase-btn {
  width: 100%;
  background: #ffd700;
  color: #000;
  border: none;
  padding: 0.75rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.purchase-btn:hover:not(:disabled) {
  background: #ffed4e;
}

.purchase-btn:disabled {
  background: var(--text-muted);
  color: var(--text-secondary);
  cursor: not-allowed;
}

.purchased-btn {
  width: 100%;
  background: #4caf50;
  color: white;
  border: none;
  padding: 0.75rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.stock-info {
  padding: 0.5rem 1rem;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  font-size: 0.8rem;
  color: var(--text-secondary);
}

.empty-shop {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted);
}

.empty-shop i {
  font-size: 4rem;
  margin-bottom: 1rem;
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
  padding: 1rem;
}

.offer-modal {
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  font-size: 1.5rem;
  color: var(--text-primary);
}

.close-btn {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 1.5rem;
  padding: 0.5rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: var(--tertiary-bg);
  color: var(--text-primary);
}

.modal-content {
  padding: 1.5rem;
}

.offer-preview {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.preview-icon {
  font-size: 3rem;
  color: var(--text-primary);
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.rarity-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: bold;
  text-align: center;
}

.rarity-badge.common { background: #9e9e9e; color: white; }
.rarity-badge.rare { background: #2196f3; color: white; }
.rarity-badge.epic { background: #9c27b0; color: white; }
.rarity-badge.legendary { background: #ff9800; color: white; }

.offer-description {
  margin-bottom: 1.5rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.offer-effects {
  margin-bottom: 1.5rem;
}

.offer-effects h4 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.offer-effects ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.offer-effects li {
  padding: 0.25rem 0;
  color: var(--text-secondary);
}

.offer-effects li::before {
  content: "✓ ";
  color: #4caf50;
  font-weight: bold;
}

.price-breakdown {
  background: var(--tertiary-bg);
  padding: 1rem;
  border-radius: 8px;
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.price-row.discount {
  color: #4caf50;
}

.price-row.total {
  border-top: 1px solid var(--border-color);
  padding-top: 0.5rem;
  font-weight: bold;
  font-size: 1.1rem;
  color: var(--text-primary);
}

.modal-actions {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid var(--border-color);
}

.cancel-btn {
  flex: 1;
  background: var(--tertiary-bg);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  padding: 0.75rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn:hover {
  background: var(--border-color);
}

.confirm-purchase-btn {
  flex: 2;
  background: #ffd700;
  color: #000;
  border: none;
  padding: 0.75rem;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.confirm-purchase-btn:hover:not(:disabled) {
  background: #ffed4e;
}

.confirm-purchase-btn:disabled {
  background: var(--text-muted);
  color: var(--text-secondary);
  cursor: not-allowed;
}

.notification {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  z-index: 1001;
  max-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
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

@media (max-width: 768px) {
  .shop-simple {
    padding: 1rem;
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