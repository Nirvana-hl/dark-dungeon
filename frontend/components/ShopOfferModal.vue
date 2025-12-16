<template>
  <view class="modal-overlay" @click="$emit('close')">
    <view class="modal-container" @click.stop>
      <!-- 模态框头部 -->
      <header class="modal-header">
        <h2>{{ offer.name }}</h2>
        <button class="close-btn" @click="$emit('close')">
          <i class="fas fa-times"></i>
        </button>
      </header>

      <!-- 模态框内容 -->
      <main class="modal-content">
        <view class="offer-showcase">
          <!-- 商品大图 -->
          <view class="offer-image-large">
            <image 
              :src="getOfferImage()" 
              :alt="offer.name"
              @error="handleImageError"
            />
            <view v-if="hasDiscount" class="discount-badge-large">
              限时优惠 -{{ discountPercentage }}%
            </view>
          </view>

          <!-- 商品详细信息 -->
          <view class="offer-details">
            <view class="offer-meta">
              <view class="rarity-badge" :class="offer.rarity">
                {{ getRarityText(offer.rarity) }}
              </view>
              <view class="offer-type">
                {{ getOfferTypeText(offer.offerType) }}
              </view>
            </view>

            <view class="offer-description-full">
              <h3>商品描述</h3>
              <p>{{ offer.description }}</p>
            </view>

            <!-- 卡牌属性（如果是卡牌） -->
            <view v-if="isCard" class="card-attributes">
              <h3>属性详情</h3>
              <view class="attributes-grid">
                <view v-if="cardStats.attack" class="attribute-item">
                  <i class="fas fa-sword attribute-icon attack"></i>
                  <view class="attribute-info">
                    <text class="attribute-name">攻击力</text>
                    <text class="attribute-value">{{ cardStats.attack }}</text>
                  </view>
                </view>
                <view v-if="cardStats.health" class="attribute-item">
                  <i class="fas fa-heart attribute-icon health"></i>
                  <view class="attribute-info">
                    <text class="attribute-name">生命值</text>
                    <text class="attribute-value">{{ cardStats.health }}</text>
                  </view>
                </view>
                <view v-if="cardStats.actionPointCost" class="attribute-item">
                  <i class="fas fa-bolt attribute-icon ap"></i>
                  <view class="attribute-info">
                    <text class="attribute-name">行动点消耗</text>
                    <text class="attribute-value">{{ cardStats.actionPointCost }}</text>
                  </view>
                </view>
                <view v-if="cardTraits.length > 0" class="attribute-item">
                  <i class="fas fa-sparkles attribute-icon trait"></i>
                  <view class="attribute-info">
                    <text class="attribute-name">特性</text>
                    <view class="traits-list">
                      <text 
                        v-for="trait in cardTraits" 
                        :key="trait.name"
                        class="trait-tag"
                        :title="trait.description"
                      >
                        {{ trait.name }}
                      </text>
                    </view>
                  </view>
                </view>
              </view>
            </view>

            <!-- 使用效果（如果是道具） -->
            <view v-if="isItem" class="item-effects">
              <h3>使用效果</h3>
              <view class="effects-list">
                <view class="effect-item">
                  <i class="fas fa-plus effect-icon heal"></i>
                  <text>恢复 50 点生命值</text>
                </view>
              </view>
            </view>

            <!-- 礼包内容（如果是礼包） -->
            <view v-if="isBundle" class="bundle-contents">
              <h3>礼包内容</h3>
              <view class="contents-list">
                <view class="content-item">
                  <i class="fas fa-users"></i>
                  <text>随机角色卡 x3</text>
                </view>
                <view class="content-item">
                  <i class="fas fa-coins"></i>
                  <text>金币 x500</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 购买信息区 -->
        <view class="purchase-section">
          <view class="price-info">
            <view v-if="hasDiscount" class="discount-info">
              <text class="original-price">
                原价: {{ formatAmount(originalPrice) }} {{ getCurrencyName(offer.currencyType) }}
              </text>
              <text class="discount-savings">
                节省: {{ formatAmount(originalPrice - offer.price) }} {{ getCurrencyName(offer.currencyType) }}
              </text>
            </view>
            <view class="current-price">
              <text class="price-label">现价</text>
              <view class="price-amount">
                <i :class="getCurrencyIcon(offer.currencyType)"></i>
                <text class="amount">{{ formatAmount(offer.price) }}</text>
              </view>
              <text class="currency-name">{{ getCurrencyName(offer.currencyType) }}</text>
            </view>
          </view>

          <!-- 购买数量选择 -->
          <view v-if="allowQuantity" class="quantity-selector">
            <label>购买数量:</label>
            <view class="quantity-controls">
              <button 
                class="quantity-btn"
                @click="decreaseQuantity"
                :disabled="quantity <= 1"
              >
                <i class="fas fa-minus"></i>
              </button>
              <input 
                v-model.number="quantity"
                type="number"
                min="1"
                :max="maxQuantity"
                class="quantity-input"
              />
              <button 
                class="quantity-btn"
                @click="increaseQuantity"
                :disabled="quantity >= maxQuantity"
              >
                <i class="fas fa-plus"></i>
              </button>
            </view>
            <text class="max-quantity">最大: {{ maxQuantity }}</text>
          </view>

          <!-- 总价显示 -->
          <view class="total-price">
            <text class="total-label">总价:</text>
            <view class="total-amount">
              <i :class="getCurrencyIcon(offer.currencyType)"></i>
              <text>{{ formatAmount(totalPrice) }}</text>
            </view>
            <text class="currency-name">{{ getCurrencyName(offer.currencyType) }}</text>
          </view>

          <!-- 库存信息 -->
          <view v-if="offer.stock !== undefined" class="stock-status">
            <text class="stock-label">剩余库存:</text>
            <text class="stock-count" :class="{ 'low-stock': offer.stock <= 10 }">
              {{ offer.stock }}
            </text>
          </view>
        </view>
      </main>

      <!-- 模态框底部 -->
      <footer class="modal-footer">
        <view class="user-balance">
          <text class="balance-label">当前余额:</text>
          <view class="balance-amount">
            <i :class="getCurrencyIcon(offer.currencyType)"></i>
            <text>{{ formatAmount(userBalance) }}</text>
            <text class="currency-name">{{ getCurrencyName(offer.currencyType) }}</text>
          </view>
        </view>

        <view class="modal-actions">
          <button class="cancel-btn" @click="$emit('close')">
            取消
          </button>
          <button 
            class="purchase-btn"
            :class="{ 'insufficient-funds': !canAfford, 'out-of-stock': !inStock }"
            @click="handlePurchase"
            :disabled="!canAfford || !inStock"
          >
            <i class="fas fa-shopping-cart"></i>
            {{ getPurchaseButtonText() }}
          </button>
        </view>
      </footer>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { ShopOffer, CurrencyType } from '@/types'

interface Props {
  offer: ShopOffer
  userCurrencies: Array<{
    currencyType: CurrencyType
    balance: bigint
  }>
}

interface Emits {
  (e: 'close'): void
  (e: 'purchase', offer: ShopOffer, quantity: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 响应式数据
const quantity = ref(1)

// 计算属性
const isCard = computed(() => props.offer.offerType === 'card')
const isItem = computed(() => props.offer.offerType === 'item')
const isBundle = computed(() => props.offer.offerType === 'bundle')
const allowQuantity = computed(() => isItem.value || isBundle.value)

const hasDiscount = computed(() => {
  // 简化处理，实际应有折扣逻辑
  return false
})

const discountPercentage = computed(() => {
  return 20 // 模拟折扣
})

const originalPrice = computed(() => {
  if (!hasDiscount.value) return props.offer.price
  return props.offer.price * BigInt(100) / BigInt(100 - discountPercentage.value)
})

const maxQuantity = computed(() => {
  if (props.offer.stock !== undefined) {
    return Math.min(props.offer.stock, 99)
  }
  return 99
})

const userBalance = computed(() => {
  const currency = props.userCurrencies.find(c => c.currencyType === props.offer.currencyType)
  return currency ? currency.balance : BigInt(0)
})

const canAfford = computed(() => {
  return userBalance.value >= totalPrice.value
})

const inStock = computed(() => {
  return props.offer.stock === undefined || props.offer.stock > 0
})

const totalPrice = computed(() => {
  return props.offer.price * BigInt(quantity.value)
})

const cardStats = computed(() => {
  // 模拟卡牌属性，实际应从API获取
  return {
    attack: isCard.value ? Math.floor(Math.random() * 10) + 5 : 0,
    health: isCard.value ? Math.floor(Math.random() * 10) + 10 : 0,
    actionPointCost: isCard.value ? Math.floor(Math.random() * 3) + 1 : 0
  }
})

const cardTraits = computed(() => {
  // 模拟特性数据
  return [
    { name: '勇敢', description: '攻击力+1' },
    { name: '坚韧', description: '生命值+2' }
  ]
})

// 方法
function getOfferImage(): string {
  switch (props.offer.offerType) {
    case 'card':
      return `/api/character/image/${props.offer.targetId}`
    case 'item':
      return `/api/item/image/${props.offer.targetId}`
    case 'bundle':
      return '/images/bundle-large.png'
    default:
      return '/images/default-item-large.png'
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

function getOfferTypeText(offerType: string): string {
  switch (offerType) {
    case 'card': return '卡牌'
    case 'item': return '道具'
    case 'bundle': return '礼包'
    default: return '商品'
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

function increaseQuantity() {
  if (quantity.value < maxQuantity.value) {
    quantity.value++
  }
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--
  }
}

function getPurchaseButtonText(): string {
  if (!inStock.value) return '缺货'
  if (!canAfford.value) return '余额不足'
  return '立即购买'
}

function handlePurchase() {
  emit('purchase', props.offer, quantity.value)
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-item-large.png'
}
</script>

<style scoped>
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
  padding: 20px;
}

.modal-container {
  background: var(--secondary-bg);
  border-radius: 16px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: var(--shadow-heavy);
  border: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: var(--text-primary);
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 20px;
  cursor: pointer;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: var(--tertiary-bg);
  color: var(--text-primary);
}

.modal-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.offer-showcase {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.offer-image-large {
  position: relative;
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  border-radius: 12px;
  overflow: hidden;
  background: var(--tertiary-bg);
}

.offer-image-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.discount-badge-large {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--success);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
}

.offer-details {
  flex: 1;
  min-width: 0;
}

.offer-meta {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.rarity-badge {
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.rarity-badge.common { background: var(--common); color: white; }
.rarity-badge.rare { background: var(--rare); color: white; }
.rarity-badge.epic { background: var(--epic); color: white; }
.rarity-badge.legendary { 
  background: linear-gradient(135deg, var(--legendary), #ff5722); 
  color: white; 
}

.offer-type {
  background: var(--tertiary-bg);
  color: var(--text-secondary);
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
}

.offer-description-full h3,
.card-attributes h3,
.item-effects h3,
.bundle-contents h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
}

.offer-description-full p {
  margin: 0 0 20px 0;
  color: var(--text-secondary);
  line-height: 1.5;
}

.attributes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.attribute-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--tertiary-bg);
  padding: 12px;
  border-radius: 8px;
}

.attribute-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.attribute-icon.attack { color: var(--error); }
.attribute-icon.health { color: var(--success); }
.attribute-icon.ap { color: var(--text-accent); }
.attribute-icon.trait { color: var(--epic); }

.attribute-info {
  flex: 1;
}

.attribute-name {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 2px;
}

.attribute-value {
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
}

.traits-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.trait-tag {
  background: rgba(156, 39, 176, 0.2);
  color: var(--epic);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 10px;
  font-weight: bold;
}

.effects-list,
.contents-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.effect-item,
.content-item {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--tertiary-bg);
  padding: 8px 12px;
  border-radius: 6px;
}

.effect-icon.heal {
  color: var(--success);
}

.purchase-section {
  background: var(--tertiary-bg);
  border-radius: 12px;
  padding: 20px;
  margin-top: 20px;
}

.price-info {
  margin-bottom: 16px;
}

.discount-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
}

.original-price {
  text-decoration: line-through;
  color: var(--text-muted);
}

.discount-savings {
  color: var(--success);
  font-weight: bold;
}

.current-price {
  display: flex;
  align-items: center;
  gap: 12px;
}

.price-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.price-amount {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 24px;
  font-weight: bold;
  color: var(--text-primary);
}

.currency-name {
  color: var(--text-secondary);
  font-size: 14px;
}

.quantity-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.quantity-selector label {
  color: var(--text-secondary);
  font-size: 14px;
  min-width: 80px;
}

.quantity-controls {
  display: flex;
  align-items: center;
  gap: 0;
}

.quantity-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  background: var(--secondary-bg);
  color: var(--text-primary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.quantity-btn:first-child {
  border-radius: 4px 0 0 4px;
}

.quantity-btn:last-child {
  border-radius: 0 4px 4px 0;
}

.quantity-btn:hover:not(:disabled) {
  background: var(--text-accent);
  border-color: var(--text-accent);
  color: white;
}

.quantity-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  height: 32px;
  text-align: center;
  border: 1px solid var(--border-color);
  background: var(--secondary-bg);
  color: var(--text-primary);
  border-left: none;
  border-right: none;
  font-size: 14px;
  font-weight: bold;
}

.max-quantity {
  color: var(--text-muted);
  font-size: 12px;
}

.total-price {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.total-label {
  color: var(--text-secondary);
  font-size: 16px;
}

.total-amount {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
}

.stock-status {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;
}

.stock-count.low-stock {
  color: var(--warning);
  font-weight: bold;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid var(--border-color);
}

.user-balance {
  display: flex;
  align-items: center;
  gap: 8px;
}

.balance-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.balance-amount {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  color: var(--text-primary);
}

.modal-actions {
  display: flex;
  gap: 12px;
}

.cancel-btn {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: var(--secondary-bg);
}

.purchase-btn {
  background: var(--success);
  border: none;
  color: white;
  padding: 10px 20px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
  justify-content: center;
}

.purchase-btn:hover:not(:disabled) {
  background: #45a049;
  transform: translateY(-1px);
}

.purchase-btn.insufficient-funds {
  background: var(--warning);
}

.purchase-btn.out-of-stock {
  background: var(--text-muted);
  cursor: not-allowed;
}

.purchase-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .modal-container {
    max-height: 95vh;
    margin: 10px;
  }
  
  .modal-content {
    padding: 16px;
  }
  
  .offer-showcase {
    flex-direction: column;
    gap: 16px;
  }
  
  .offer-image-large {
    width: 100%;
    max-width: 200px;
    margin: 0 auto;
  }
  
  .attributes-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-footer {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .modal-actions {
    justify-content: stretch;
  }
  
  .cancel-btn,
  .purchase-btn {
    flex: 1;
  }
}
</style>