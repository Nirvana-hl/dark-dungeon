<template>
  <div class="shop-container">
    <!-- 商城头部 -->
    <header class="shop-header">
      <div class="shop-title">
        <h1>营地商城</h1>
        <p>购买角色卡、法术、装备和消耗品</p>
      </div>
      
      <div class="user-wallet">
        <div 
          v-for="currency in userCurrencies" 
          :key="currency.currencyType"
          class="currency-item"
        >
          <i :class="getCurrencyIcon(currency.currencyType)"></i>
          <span class="currency-amount">{{ formatAmount(currency.balance) }}</span>
          <span class="currency-type">{{ getCurrencyName(currency.currencyType) }}</span>
        </div>
      </div>
    </header>

    <!-- 商品分类标签 -->
    <div class="shop-tabs">
      <button 
        v-for="tab in shopTabs" 
        :key="tab.key"
        class="tab-button"
        :class="{ active: selectedTab === tab.key }"
        @click="selectTab(tab.key)"
      >
        <i :class="tab.icon"></i>
        {{ tab.label }}
        <span v-if="getTabItemCount(tab.key) > 0" class="tab-count">
          {{ getTabItemCount(tab.key) }}
        </span>
      </button>
    </div>

    <!-- 筛选和搜索 -->
    <div class="shop-controls">
      <div class="search-box">
        <i class="fas fa-search"></i>
        <input 
          v-model="searchQuery"
          type="text" 
          placeholder="搜索商品..."
          @input="filterOffers"
        />
      </div>
      
      <div class="filter-controls">
        <select v-model="selectedRarity" @change="filterOffers">
          <option value="">所有稀有度</option>
          <option v-for="rarity in rarityOptions" :key="rarity.value" :value="rarity.value">
            {{ rarity.label }}
          </option>
        </select>
        
        <select v-model="priceSort" @change="sortOffers">
          <option value="default">默认排序</option>
          <option value="price-asc">价格从低到高</option>
          <option value="price-desc">价格从高到低</option>
          <option value="newest">最新上架</option>
        </select>
      </div>
    </div>

    <!-- 商品展示区 -->
    <main class="shop-content">
      <!-- 精选推荐 -->
      <section v-if="selectedTab === 'featured'" class="featured-section">
        <h2>今日推荐</h2>
        <div class="featured-grid">
          <div 
            v-for="offer in featuredOffers" 
            :key="offer.id"
            class="featured-card"
            @click="showOfferDetails(offer)"
          >
            <div class="featured-badge">
              <i class="fas fa-star"></i>
              精选
            </div>
            <ShopOfferItem 
              :offer="offer"
              :user-currencies="userCurrencies"
              @purchase="handlePurchase"
            />
          </div>
        </div>
      </section>

      <!-- 普通商品列表 -->
      <section class="offers-section">
        <div class="section-header">
          <h2>{{ getTabTitle(selectedTab) }}</h2>
          <span class="offer-count">共 {{ filteredOffers.length }} 件商品</span>
        </div>
        
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <p>加载商品中...</p>
        </div>
        
        <div v-else-if="filteredOffers.length === 0" class="empty-state">
          <i class="fas fa-shopping-cart"></i>
          <p>暂无相关商品</p>
        </div>
        
        <div v-else class="offers-grid">
          <ShopOfferItem 
            v-for="offer in filteredOffers" 
            :key="offer.id"
            :offer="offer"
            :user-currencies="userCurrencies"
            @purchase="handlePurchase"
            @click="showOfferDetails(offer)"
          />
        </div>
      </section>
    </main>

    <!-- 商品详情模态框 -->
    <ShopOfferModal
      v-if="selectedOffer"
      :offer="selectedOffer"
      :user-currencies="userCurrencies"
      @close="selectedOffer = null"
      @purchase="handlePurchase"
    />

    <!-- 购买确认模态框 -->
    <PurchaseConfirmModal
      v-if="purchaseConfirm"
      :offer="purchaseConfirm.offer"
      :quantity="purchaseConfirm.quantity"
      :total-cost="purchaseConfirm.totalCost"
      @close="purchaseConfirm = null"
      @confirm="confirmPurchase"
    />

    <!-- 购买成功提示 -->
    <ToastNotification
      v-if="showToast"
      :message="toastMessage"
      :type="toastType"
      @close="showToast = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { campApi } from '@/lib/api'
import { useWalletStore } from '@/stores/wallet'
import type { ShopOffer, UserWallet, CurrencyType } from '@/types'
import ShopOfferItem from '@/components/ShopOfferItem.vue'
import ShopOfferModal from '@/components/ShopOfferModal.vue'
import PurchaseConfirmModal from '@/components/PurchaseConfirmModal.vue'
import ToastNotification from '@/components/ToastNotification.vue'

const wallet = useWalletStore()

// 响应式数据
const shopOffers = ref<ShopOffer[]>([])
const selectedTab = ref('featured')
const searchQuery = ref('')
const selectedRarity = ref('')
const priceSort = ref('default')
const selectedOffer = ref<ShopOffer | null>(null)
const purchaseConfirm = ref<{
  offer: ShopOffer
  quantity: number
  totalCost: bigint
} | null>(null)
const loading = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref<'success' | 'error' | 'info'>('success')

// 商城标签页
const shopTabs = [
  { key: 'featured', label: '精选推荐', icon: 'fas fa-star' },
  { key: 'character', label: '角色卡', icon: 'fas fa-users' },
  { key: 'spell', label: '法术卡', icon: 'fas fa-magic' },
  { key: 'equipment', label: '装备', icon: 'fas fa-shield-alt' },
  { key: 'item', label: '道具', icon: 'fas fa-flask' },
  { key: 'bundle', label: '礼包', icon: 'fas fa-gift' }
]

// 稀有度选项
const rarityOptions = [
  { value: 'common', label: '普通' },
  { value: 'rare', label: '稀有' },
  { value: 'epic', label: '史诗' },
  { value: 'legendary', label: '传说' }
]

// 计算属性
const userCurrencies = computed(() => {
  return wallet.wallets as UserWallet[]
})

const featuredOffers = computed(() => {
  return shopOffers.value
    .filter(offer => offer.displayOrder <= 3)
    .slice(0, 6)
})

const filteredOffers = computed(() => {
  let offers = shopOffers.value

  // 按标签页筛选
  if (selectedTab.value !== 'featured') {
    offers = offers.filter(offer => {
      switch (selectedTab.value) {
        case 'character':
          return offer.offerType === 'card' && isCharacterCard(offer.targetId)
        case 'spell':
          return offer.offerType === 'card' && isSpellCard(offer.targetId)
        case 'equipment':
          return offer.offerType === 'card' && isEquipmentCard(offer.targetId)
        case 'item':
          return offer.offerType === 'item'
        case 'bundle':
          return offer.offerType === 'bundle'
        default:
          return true
      }
    })
  }

  // 按搜索关键词筛选
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    offers = offers.filter(offer => 
      offer.name.toLowerCase().includes(query) ||
      offer.description.toLowerCase().includes(query)
    )
  }

  // 按稀有度筛选
  if (selectedRarity.value) {
    offers = offers.filter(offer => offer.rarity === selectedRarity.value)
  }

  // 排序
  switch (priceSort.value) {
    case 'price-asc':
      offers.sort((a, b) => Number(a.price - b.price))
      break
    case 'price-desc':
      offers.sort((a, b) => Number(b.price - a.price))
      break
    case 'newest':
      offers.sort((a, b) => b.displayOrder - a.displayOrder)
      break
    default:
      offers.sort((a, b) => a.displayOrder - b.displayOrder)
  }

  return offers
})

// 方法
async function loadShopOffers() {
  loading.value = true
  try {
    const response = await campApi.getShopOffers()
    if (response.data.code === 200) {
      shopOffers.value = response.data.data || []
    } else {
      console.error('获取商城数据失败:', response.data.message)
      shopOffers.value = [] // 失败时显示空列表，而不是使用 mock 数据
    }
  } catch (error) {
    console.error('Failed to load shop offers:', error)
    shopOffers.value = [] // 失败时显示空列表，而不是使用 mock 数据
    // 可以显示错误提示，但不使用 mock 数据
  } finally {
    loading.value = false
  }
}

function isCharacterCard(cardId: string): boolean {
  // 简化判断，实际应从API获取卡牌类型
  return cardId.startsWith('char-')
}

function isSpellCard(cardId: string): boolean {
  return cardId.startsWith('spell-')
}

function isEquipmentCard(cardId: string): boolean {
  return cardId.startsWith('equip-')
}

function selectTab(tab: string) {
  selectedTab.value = tab
  searchQuery.value = ''
  selectedRarity.value = ''
  priceSort.value = 'default'
}

function filterOffers() {
  // 触发计算属性重新计算
}

function sortOffers() {
  // 触发计算属性重新计算
}

function getTabItemCount(tabKey: string): number {
  switch (tabKey) {
    case 'character':
      return shopOffers.value.filter(offer => 
        offer.offerType === 'card' && isCharacterCard(offer.targetId)
      ).length
    case 'spell':
      return shopOffers.value.filter(offer => 
        offer.offerType === 'card' && isSpellCard(offer.targetId)
      ).length
    case 'equipment':
      return shopOffers.value.filter(offer => 
        offer.offerType === 'card' && isEquipmentCard(offer.targetId)
      ).length
    case 'item':
      return shopOffers.value.filter(offer => offer.offerType === 'item').length
    case 'bundle':
      return shopOffers.value.filter(offer => offer.offerType === 'bundle').length
    default:
      return 0
  }
}

function getTabTitle(tabKey: string): string {
  const tab = shopTabs.find(t => t.key === tabKey)
  return tab ? tab.label : '商品'
}

function getCurrencyIcon(type: CurrencyType): string {
  switch (type) {
    case 'gold':
      return 'fas fa-coins'
    case 'soulstone':
      return 'fas fa-gem'
    default:
      return 'fas fa-question-circle'
  }
}

function getCurrencyName(type: CurrencyType): string {
  switch (type) {
    case 'gold':
      return '金币'
    case 'soulstone':
      return '魂晶'
    default:
      return '未知'
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

function showOfferDetails(offer: ShopOffer) {
  selectedOffer.value = offer
}

async function handlePurchase(offer: ShopOffer, quantity: number = 1) {
  const totalCost = offer.price * BigInt(quantity)
  
  // 检查货币余额
  const currency = userCurrencies.value.find((c: UserWallet) => c.currencyType === offer.currencyType)
  if (!currency || currency.balance < totalCost) {
    showNotification('货币余额不足！', 'error')
    return
  }

  // 检查库存
  if (offer.stock && offer.stock < quantity) {
    showNotification('库存不足！', 'error')
    return
  }

  // 显示确认对话框
  purchaseConfirm.value = {
    offer,
    quantity,
    totalCost
  }
}

async function confirmPurchase() {
  if (!purchaseConfirm.value) return

  try {
    const response = await campApi.purchaseItem(
      purchaseConfirm.value.offer.id,
      purchaseConfirm.value.quantity
    )

    if (response.data.code === 200) {
      showNotification('购买成功！', 'success')
      
      // 更新钱包余额
      await wallet.loadWallets()
      
      // 更新库存
      if (purchaseConfirm.value.offer.stock) {
        purchaseConfirm.value.offer.stock -= purchaseConfirm.value.quantity
      }
      
      purchaseConfirm.value = null
      selectedOffer.value = null
    } else {
      showNotification(response.data.message || '购买失败', 'error')
    }
  } catch (error) {
    console.error('Purchase failed:', error)
    showNotification('购买失败，请稍后重试', 'error')
  }
}

function showNotification(message: string, type: 'success' | 'error' | 'info') {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

// 生命周期
onMounted(() => {
  loadShopOffers()
  wallet.loadWallets()
})
</script>

<style scoped>
.shop-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #ffffff;
  padding: 20px;
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: var(--secondary-bg);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.shop-title h1 {
  margin: 0 0 5px 0;
  font-size: 28px;
  font-weight: bold;
  color: var(--text-primary);
}

.shop-title p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.user-wallet {
  display: flex;
  gap: 20px;
}

.currency-item {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--tertiary-bg);
  padding: 8px 16px;
  border-radius: 20px;
  border: 1px solid var(--border-color);
}

.currency-item i {
  color: var(--warning);
}

.currency-amount {
  font-weight: bold;
  color: var(--text-primary);
}

.currency-type {
  color: var(--text-secondary);
  font-size: 14px;
}

.shop-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  overflow-x: auto;
  padding-bottom: 5px;
}

.tab-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  border-radius: 25px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.3s;
  white-space: nowrap;
  font-weight: 500;
}

.tab-button:hover {
  background: var(--secondary-bg);
  border-color: var(--text-accent);
}

.tab-button.active {
  background: var(--text-accent);
  border-color: var(--text-accent);
  color: white;
}

.tab-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: bold;
}

.shop-controls {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  border-radius: 25px;
  padding: 8px 16px;
  flex: 1;
  min-width: 200px;
  max-width: 400px;
}

.search-box i {
  color: var(--text-muted);
}

.search-box input {
  flex: 1;
  background: transparent;
  border: none;
  color: var(--text-primary);
  outline: none;
  font-size: 14px;
}

.search-box input::placeholder {
  color: var(--text-muted);
}

.filter-controls {
  display: flex;
  gap: 10px;
}

.filter-controls select {
  background: var(--tertiary-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 8px 12px;
  border-radius: 6px;
  min-width: 120px;
}

.shop-content {
  margin-bottom: 30px;
}

.featured-section {
  margin-bottom: 40px;
}

.featured-section h2 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.featured-card {
  position: relative;
  background: var(--secondary-bg);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s;
}

.featured-card:hover {
  transform: translateY(-5px);
}

.featured-badge {
  position: absolute;
  top: 10px;
  right: 10px;
  background: var(--text-accent);
  color: white;
  padding: 4px 8px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 10;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
}

.offer-count {
  color: var(--text-secondary);
  font-size: 14px;
}

.loading-state,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  background: var(--secondary-bg);
  border-radius: 12px;
  border: 1px solid var(--border-color);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--border-color);
  border-top: 4px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.loading-state p,
.empty-state p {
  color: var(--text-secondary);
  font-size: 16px;
  margin: 0;
}

.empty-state i {
  font-size: 48px;
  color: var(--text-muted);
  margin-bottom: 16px;
}

.offers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .shop-container {
    padding: 12px;
  }
  
  .shop-header {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  
  .user-wallet {
    width: 100%;
    justify-content: center;
    gap: 15px;
  }
  
  .shop-controls {
    flex-direction: column;
    gap: 15px;
  }
  
  .search-box {
    max-width: 100%;
  }
  
  .featured-grid,
  .offers-grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 12px;
  }
}
</style>