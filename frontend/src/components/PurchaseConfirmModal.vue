<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-container" @click.stop>
      <!-- 模态框头部 -->
      <header class="modal-header">
        <div class="modal-icon">
          <i class="fas fa-shopping-cart"></i>
        </div>
        <h2>确认购买</h2>
        <button class="close-btn" @click="$emit('close')">
          <i class="fas fa-times"></i>
        </button>
      </header>

      <!-- 模态框内容 -->
      <main class="modal-content">
        <!-- 商品信息 -->
        <div class="purchase-item">
          <div class="item-image">
            <img 
              :src="getOfferImage()" 
              :alt="offer.name"
              @error="handleImageError"
            />
          </div>
          <div class="item-details">
            <h3>{{ offer.name }}</h3>
            <p class="item-description">{{ offer.description }}</p>
            <div class="item-meta">
              <span class="quantity">数量: {{ quantity }}</span>
              <span class="rarity" :class="offer.rarity">
                {{ getRarityText(offer.rarity) }}
              </span>
            </div>
          </div>
        </div>

        <!-- 价格明细 -->
        <div class="price-breakdown">
          <h3>价格明细</h3>
          <div class="price-row">
            <span class="price-label">单价:</span>
            <span class="price-value">
              <i :class="getCurrencyIcon(offer.currencyType)"></i>
              {{ formatAmount(offer.price) }}
            </span>
          </div>
          <div class="price-row">
            <span class="price-label">数量:</span>
            <span class="price-value">{{ quantity }}</span>
          </div>
          <div class="price-row total">
            <span class="price-label">总计:</span>
            <span class="price-value total-amount">
              <i :class="getCurrencyIcon(offer.currencyType)"></i>
              {{ formatAmount(totalCost) }}
              <span class="currency-name">{{ getCurrencyName(offer.currencyType) }}</span>
            </span>
          </div>
        </div>

        <!-- 余额信息 -->
        <div class="balance-section" :class="{ 'insufficient': !hasEnoughBalance }">
          <h3>账户余额</h3>
          <div class="balance-row">
            <span class="balance-label">当前余额:</span>
            <span class="balance-amount">
              <i :class="getCurrencyIcon(offer.currencyType)"></i>
              {{ formatAmount(currentBalance) }}
              <span class="currency-name">{{ getCurrencyName(offer.currencyType) }}</span>
            </span>
          </div>
          <div class="balance-row remaining">
            <span class="balance-label">购买后余额:</span>
            <span class="balance-amount" :class="{ 'negative': remainingBalance < 0 }">
              <i :class="getCurrencyIcon(offer.currencyType)"></i>
              {{ formatAmount(remainingBalance > 0n ? remainingBalance : 0n) }}
              <span class="currency-name">{{ getCurrencyName(offer.currencyType) }}</span>
            </span>
          </div>
        </div>

        <!-- 警告信息 -->
        <div v-if="!hasEnoughBalance" class="warning-section">
          <div class="warning-icon">
            <i class="fas fa-exclamation-triangle"></i>
          </div>
          <div class="warning-message">
            <strong>余额不足</strong>
            <p>您的账户余额不足以完成此购买。请充值后再试。</p>
          </div>
        </div>

        <!-- 购买提醒 -->
        <div class="notice-section">
          <div class="notice-icon">
            <i class="fas fa-info-circle"></i>
          </div>
          <div class="notice-message">
            <ul>
              <li>购买的商品将直接添加到您的背包或卡组中</li>
              <li>虚拟商品一经购买，不支持退换</li>
              <li>如有疑问，请联系客服</li>
            </ul>
          </div>
        </div>
      </main>

      <!-- 模态框底部 -->
      <footer class="modal-footer">
        <button class="cancel-btn" @click="$emit('close')">
          <i class="fas fa-times"></i>
          取消
        </button>
        <button 
          class="confirm-btn"
          :class="{ 'disabled': !hasEnoughBalance }"
          @click="handleConfirm"
          :disabled="!hasEnoughBalance"
        >
          <i class="fas fa-check"></i>
          {{ hasEnoughBalance ? '确认购买' : '余额不足' }}
        </button>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { ShopOffer, CurrencyType } from '@/types'

interface Props {
  offer: ShopOffer
  quantity: number
  totalCost: bigint
}

interface Emits {
  (e: 'close'): void
  (e: 'confirm'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 模拟用户余额，实际应从props传入或从store获取
const currentBalance = computed(() => {
  // 这里应该是真实的用户余额
  switch (props.offer.currencyType) {
    case 'gold':
      return BigInt(1500)
    case 'soulstone':
      return BigInt(100)
    default:
      return BigInt(0)
  }
})

const remainingBalance = computed(() => {
  return currentBalance.value - props.totalCost
})

const hasEnoughBalance = computed(() => {
  return currentBalance.value >= props.totalCost
})

// 方法
function getOfferImage(): string {
  switch (props.offer.offerType) {
    case 'card':
      return `/api/character/image/${props.offer.targetId}`
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

function handleConfirm() {
  if (hasEnoughBalance.value) {
    emit('confirm')
  }
}

function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-item.png'
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
  backdrop-filter: blur(4px);
}

.modal-container {
  background: var(--secondary-bg);
  border-radius: 16px;
  max-width: 500px;
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
  align-items: center;
  gap: 12px;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-icon {
  width: 40px;
  height: 40px;
  background: var(--text-accent);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
  flex: 1;
}

.close-btn {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  font-size: 16px;
  cursor: pointer;
  width: 28px;
  height: 28px;
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

.purchase-item {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: var(--tertiary-bg);
  border-radius: 12px;
}

.item-image {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: var(--secondary-bg);
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-details {
  flex: 1;
  min-width: 0;
}

.item-details h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
}

.item-description {
  margin: 0 0 8px 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.4;
  display: -webkit-box;
  display: -moz-box;
  display: box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  -moz-box-orient: vertical;
  box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.quantity {
  color: var(--text-secondary);
  font-size: 14px;
}

.rarity {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.rarity.common { background: var(--common); color: white; }
.rarity.rare { background: var(--rare); color: white; }
.rarity.epic { background: var(--epic); color: white; }
.rarity.legendary { 
  background: linear-gradient(135deg, var(--legendary), #ff5722); 
  color: white; 
}

.price-breakdown,
.balance-section {
  margin-bottom: 20px;
}

.price-breakdown h3,
.balance-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
}

.price-row,
.balance-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.price-row:last-child,
.balance-row:last-child {
  border-bottom: none;
}

.price-row.total {
  border-top: 2px solid var(--border-color);
  margin-top: 8px;
  padding-top: 12px;
}

.balance-row.remaining {
  margin-top: 8px;
  padding-top: 12px;
}

.price-label,
.balance-label {
  color: var(--text-secondary);
  font-size: 14px;
}

.price-value,
.balance-amount {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-primary);
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: var(--text-accent);
}

.balance-amount.negative {
  color: var(--error);
}

.currency-name {
  color: var(--text-secondary);
  font-size: 12px;
}

.balance-section.insufficient {
  background: rgba(244, 67, 54, 0.1);
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}

.balance-section.insufficient h3 {
  color: var(--error);
}

.warning-section {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(255, 152, 0, 0.1);
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid var(--warning);
}

.warning-icon {
  color: var(--warning);
  font-size: 20px;
  flex-shrink: 0;
}

.warning-message strong {
  display: block;
  color: var(--warning);
  font-size: 16px;
  margin-bottom: 4px;
}

.warning-message p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.4;
}

.notice-section {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 8px;
  margin-bottom: 20px;
}

.notice-icon {
  color: var(--info);
  font-size: 16px;
  flex-shrink: 0;
}

.notice-message ul {
  margin: 0;
  padding-left: 20px;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.5;
}

.notice-message li {
  margin-bottom: 4px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid var(--border-color);
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 16px;
}

.cancel-btn {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
}

.cancel-btn:hover {
  background: var(--secondary-bg);
  transform: translateY(-1px);
}

.confirm-btn {
  background: var(--success);
  border: none;
  color: white;
}

.confirm-btn:hover:not(.disabled) {
  background: #45a049;
  transform: translateY(-1px);
}

.confirm-btn.disabled {
  background: var(--text-muted);
  cursor: not-allowed;
  opacity: 0.6;
}

@media (max-width: 768px) {
  .modal-container {
    max-height: 95vh;
    margin: 10px;
  }
  
  .modal-content {
    padding: 16px;
  }
  
  .purchase-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }
  
  .item-image {
    margin: 0 auto;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .cancel-btn,
  .confirm-btn {
    width: 100%;
  }
}
</style>