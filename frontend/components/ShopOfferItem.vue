<template>
  <view 
    class="shop-offer-item"
    :class="{ 
      'featured': featured,
      'discounted': hasDiscount,
      'out-of-stock': !inStock
    }"
    @click="$emit('click')"
  >
    <!-- 稀有度边框 -->
    <view class="rarity-border" :class="`rarity-${offer.rarity}`">
      <!-- 商品图片 -->
      <view class="offer-image">
        <image 
          :src="getOfferImage()" 
          :alt="offer.name"
          @error="handleImageError"
        />
        <view v-if="hasDiscount" class="discount-badge">
          -{{ discountPercentage }}%
        </view>
        <view v-if="!inStock" class="out-of-stock-overlay">
          <text>缺货</text>
        </view>
      </view>
      
      <!-- 商品信息 -->
      <view class="offer-info">
        <view class="offer-header">
          <h3 class="offer-name">{{ offer.name }}</h3>
          <view class="rarity-tag" :class="offer.rarity">
            {{ getRarityText(offer.rarity) }}
          </view>
        </view>
        
        <p class="offer-description">{{ offer.description }}</p>
        
        <!-- 属性展示（如果是卡牌） -->
        <view v-if="isCard" class="offer-stats">
          <view v-if="cardStats.attack" class="stat">
            <i class="fas fa-sword"></i>
            {{ cardStats.attack }}
          </view>
          <view v-if="cardStats.health" class="stat">
            <i class="fas fa-heart"></i>
            {{ cardStats.health }}
          </view>
          <view v-if="cardStats.actionPointCost" class="stat">
            <i class="fas fa-bolt"></i>
            {{ cardStats.actionPointCost }}
          </view>
        </view>
      </view>
      
      <!-- 价格和操作区 -->
      <view class="offer-footer">
        <view class="price-section">
          <view v-if="hasDiscount" class="original-price">
            <i :class="getCurrencyIcon(offer.currencyType)"></i>
            {{ formatAmount(originalPrice) }}
          </view>
          <view class="current-price">
            <i :class="getCurrencyIcon(offer.currencyType)"></i>
            <text class="price-amount">{{ formatAmount(offer.price) }}</text>
            <text class="currency-name">{{ getCurrencyName(offer.currencyType) }}</text>
          </view>
        </view>
        
        <button 
          class="purchase-btn"
          :class="{ 
            'disabled': !inStock || !canAfford,
            'featured': featured
          }"
          @click.stop="$emit('purchase', offer)"
          :disabled="!inStock || !canAfford"
        >
          <i class="fas fa-shopping-cart"></i>
          {{ getPurchaseButtonText() }}
        </button>
      </view>
      
      <!-- 库存信息 -->
      <view v-if="offer.stock !== undefined" class="stock-info">
        <text class="stock-label">库存:</text>
        <text class="stock-count" :class="{ 'low': offer.stock <= 10 }">
          {{ offer.stock }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ShopOffer, CurrencyType } from '@/types'

interface Props {
  offer: ShopOffer
  featured?: boolean
  userCurrencies: Array<{
    currencyType: CurrencyType
    balance: bigint
  }>
}

interface Emits {
  (e: 'purchase', offer: ShopOffer): void
  (e: 'click'): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false
})

const emit = defineEmits<Emits>()

// 计算属性
const isCard = computed(() => props.offer.offerType === 'card')

const hasDiscount = computed(() => {
  // 简化处理，实际应有折扣逻辑
  return false // 这里可以根据业务逻辑判断是否有折扣
})

const discountPercentage = computed(() => {
  // 简化处理，实际应从offer数据获取
  return 20
})

const originalPrice = computed(() => {
  if (!hasDiscount.value) return props.offer.price
  return props.offer.price * BigInt(100) / BigInt(100 - discountPercentage.value)
})

const inStock = computed(() => {
  return props.offer.stock === undefined || props.offer.stock > 0
})

const canAfford = computed(() => {
  const userCurrency = props.userCurrencies.find(
    c => c.currencyType === props.offer.currencyType
  )
  return userCurrency ? userCurrency.balance >= props.offer.price : false
})

const cardStats = computed(() => {
  // 模拟卡牌属性，实际应从API获取
  return {
    attack: isCard.value ? Math.floor(Math.random() * 10) + 1 : 0,
    health: isCard.value ? Math.floor(Math.random() * 10) + 5 : 0,
    actionPointCost: isCard.value ? Math.floor(Math.random() * 3) + 1 : 0
  }
})

// 方法
function getOfferImage(): string {
  switch (props.offer.offerType) {
    case 'card':
      if (props.offer.targetId.includes('char')) {
        return `/api/character/image/${props.offer.targetId}`
      } else {
        return `/api/card/image/${props.offer.targetId}`
      }
    case 'item':
      return `/api/item/image/${props.offer.targetId}`
    case 'bundle':
      return '/images/bundle.png'
    default:
      return '/images/default-item.png'
  }
}

function getRarityText(rarity: string): string {
  switch (rarity) {
    case 'common': return '普通'
    case 'rare': return '稀有'
    case 'epic': return '史诗'
    case 'legendary': return '传说'
    default: return '未知'
  }
}

function getCurrencyIcon(type: CurrencyType): string {
  switch (type) {
    case 'gold': return 'fas fa-coins'
    case 'soulstone': return 'fas fa-gem'
    default: return 'fas fa-question-circle'
  }
}

function getCurrencyName(type: CurrencyType): string {
  switch (type) {
    case 'gold': return '金币'
    case 'soulstone': return '魂晶'
    default: return '未知'
  }
}

function formatAmount(amount: bigint): string {
  if (amount >= BigInt(1000000)) {
    return `${(Number(amount) / 1000000).toFixed(1)}M`
  } else if (amount >= BigInt(1000)) {
    return `${(Number(amount) / 1000).toFixed(1)}K`
  }
  return amount.toString()
}

function getPurchaseButtonText(): string {
  if (!inStock.value) return '缺货'
  if (!canAfford.value) return '余额不足'
  return '购买'
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  
  switch (props.offer.offerType) {
    case 'card':
      img.src = '/images/default-card.png'
      break
    case 'item':
      img.src = '/images/default-item.png'
      break
    case 'bundle':
      img.src = '/images/bundle.png'
      break
    default:
      img.src = '/images/default-item.png'
  }
}
</script>

<style scoped>
.shop-offer-item {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.shop-offer-item:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-medium);
}

.shop-offer-item.featured {
  border-color: var(--text-accent);
  box-shadow: 0 0 20px rgba(229, 62, 62, 0.3);
}

.shop-offer-item.discounted {
  border-color: var(--success);
}

.shop-offer-item.out-of-stock {
  opacity: 0.7;
  cursor: not-allowed;
}

.rarity-border {
  position: relative;
  height: 100%;
}

.rarity-border.rarity-common {
  border-color: var(--common);
}

.rarity-border.rarity-rare {
  border-color: var(--rare);
  box-shadow: 0 0 10px rgba(33, 150, 243, 0.3);
}

.rarity-border.rarity-epic {
  border-color: var(--epic);
  box-shadow: 0 0 15px rgba(156, 39, 176, 0.4);
}

.rarity-border.rarity-legendary {
  border-color: var(--legendary);
  box-shadow: 0 0 20px rgba(255, 152, 0, 0.5);
  animation: legendary-glow 2s ease-in-out infinite alternate;
}

.offer-image {
  position: relative;
  width: 100%;
  height: 150px;
  overflow: hidden;
  background: var(--tertiary-bg);
}

.offer-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.shop-offer-item:hover .offer-image img {
  transform: scale(1.05);
}

.discount-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: var(--success);
  color: white;
  padding: 4px 8px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  z-index: 10;
}

.out-of-stock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.out-of-stock-overlay span {
  color: white;
  font-weight: bold;
  font-size: 18px;
}

.offer-info {
  padding: 16px;
}

.offer-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.offer-name {
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
  margin: 0;
  flex: 1;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rarity-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
  margin-left: 8px;
}

.rarity-tag.common {
  background: var(--common);
  color: white;
}

.rarity-tag.rare {
  background: var(--rare);
  color: white;
}

.rarity-tag.epic {
  background: var(--epic);
  color: white;
}

.rarity-tag.legendary {
  background: linear-gradient(135deg, var(--legendary), #ff5722);
  color: white;
}

.offer-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.4;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.offer-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: bold;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 6px;
  border-radius: 10px;
}

.offer-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.price-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.original-price {
  font-size: 12px;
  color: var(--text-muted);
  text-decoration: line-through;
  display: flex;
  align-items: center;
  gap: 4px;
}

.current-price {
  display: flex;
  align-items: center;
  gap: 6px;
}

.price-amount {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-primary);
}

.currency-name {
  font-size: 12px;
  color: var(--text-secondary);
}

.purchase-btn {
  background: var(--success);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.purchase-btn:hover:not(.disabled) {
  background: #45a049;
  transform: translateY(-1px);
}

.purchase-btn.featured {
  background: var(--text-accent);
}

.purchase-btn.featured:hover:not(.disabled) {
  background: #d84315;
}

.purchase-btn.disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.6;
}

.stock-info {
  position: absolute;
  bottom: 8px;
  left: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.stock-label {
  color: var(--text-secondary);
}

.stock-count {
  color: var(--success);
  font-weight: bold;
}

.stock-count.low {
  color: var(--warning);
}

@keyframes legendary-glow {
  0% { box-shadow: 0 0 15px rgba(255, 152, 0, 0.5); }
  100% { box-shadow: 0 0 25px rgba(255, 152, 0, 0.8); }
}

@media (max-width: 768px) {
  .offer-image {
    height: 120px;
  }
  
  .offer-info {
    padding: 12px;
  }
  
  .offer-name {
    font-size: 14px;
  }
  
  .offer-description {
    font-size: 13px;
  }
  
  .offer-stats {
    gap: 8px;
  }
  
  .stat {
    font-size: 11px;
    padding: 2px 4px;
  }
  
  .price-amount {
    font-size: 16px;
  }
  
  .purchase-btn {
    padding: 6px 12px;
    font-size: 13px;
  }
}
</style>