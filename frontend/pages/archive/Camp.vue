<template>
  <div class="camp-container">
    <!-- 顶部标题栏 -->
    <header class="camp-header">
      <div class="header-content">
        <div class="header-title">
          <h1 class="title-text">🏕️ 冒险者营地</h1>
          <p class="title-subtitle">管理你的装备、卡牌和资源</p>
        </div>
      </div>
    </header>

    <!-- 玩家职业信息卡片 -->
    <div v-if="playerClassInfo" class="class-info-banner">
      <div class="banner-content">
        <div class="class-icon-large">{{ getClassIcon(playerClassInfo.code) }}</div>
        <div class="class-details">
          <h2 class="class-name">{{ playerClassInfo.name }}</h2>
          <p class="class-description">{{ getClassDescription(playerClassInfo.code) }}</p>
          <div class="class-stats-row">
            <div class="stat-badge">
              <span class="stat-label">基础HP</span>
              <span class="stat-value">{{ playerClassInfo.baseHp }}</span>
            </div>
            <div class="stat-badge">
              <span class="stat-label">每级HP</span>
              <span class="stat-value">+{{ playerClassInfo.hpPerLevel }}</span>
            </div>
            <div class="stat-badge">
              <span class="stat-label">当前等级</span>
              <span class="stat-value">{{ playerLevel }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="camp-content">
      <!-- 左侧主区域 -->
      <main class="main-section">
        <!-- 手牌管理 -->
        <div class="section-card">
          <div class="card-header">
            <h3 class="card-title">📚 我的卡牌</h3>
            <button class="btn-sync" @click="syncToBattle">
              <span class="btn-icon">🔄</span>
              <span>同步到战斗</span>
            </button>
          </div>

          <div v-if="userCards.length === 0" class="empty-state">
            <div class="empty-icon">📭</div>
            <p class="empty-text">暂无卡牌，前往商店购买</p>
          </div>

          <div v-else class="cards-content">
            <!-- 角色卡牌 -->
            <div v-if="cardsCharacters.length" class="card-category">
              <div class="category-header">
                <span class="category-icon">⚔️</span>
                <span class="category-title">角色卡牌</span>
                <span class="category-count">({{ cardsCharacters.length }})</span>
              </div>
              <div class="cards-grid">
                <div
                  v-for="uc in cardsCharacters"
                  :key="'char-' + uc.name"
                  class="card-item character-card"
                >
                  <div class="card-item-header">
                    <div class="card-name">{{ uc.name }}</div>
                    <div class="card-badge character-badge">角色</div>
                  </div>
                  <div class="card-item-body">
                    <div class="card-stats">
                      <div class="stat">
                        <span class="stat-icon">⚔️</span>
                        <span class="stat-text">攻击 {{ uc.attack ?? 0 }}</span>
                      </div>
                      <div class="stat">
                        <span class="stat-icon">❤️</span>
                        <span class="stat-text">生命 {{ uc.health ?? 1 }}</span>
                      </div>
                    </div>
                    <div class="card-trait">{{ charTrait(uc.name, (uc.stars ?? 1)) }}</div>
                  </div>
                  <div class="card-item-footer">
                    <div class="card-quantity">数量: ×{{ uc.quantity ?? 0 }}</div>
                    <button
                      v-if="(uc.quantity ?? 0) >= 3"
                      class="btn-combine"
                      @click="combineItem(uc)"
                    >
                      合成
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 法术卡牌 -->
            <div v-if="cardsSpells.length" class="card-category">
              <div class="category-header">
                <span class="category-icon">✨</span>
                <span class="category-title">法术卡牌</span>
                <span class="category-count">({{ cardsSpells.length }})</span>
              </div>
              <div class="cards-grid">
                <div
                  v-for="uc in cardsSpells"
                  :key="'spell-' + uc.name"
                  class="card-item spell-card"
                >
                  <div class="card-item-header">
                    <div class="card-name">{{ uc.name }}</div>
                    <div class="card-badge spell-badge">法术</div>
                  </div>
                  <div class="card-item-body">
                    <div class="card-effect">效果: {{ uc.effect || '未知' }}</div>
                  </div>
                  <div class="card-item-footer">
                    <div class="card-quantity">数量: ×{{ uc.quantity ?? 0 }}</div>
                    <button
                      v-if="(uc.quantity ?? 0) >= 3"
                      class="btn-combine"
                      @click="combineItem(uc)"
                    >
                      合成
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- 装备卡牌 -->
            <div v-if="cardsEquipment.length" class="card-category">
              <div class="category-header">
                <span class="category-icon">🛡️</span>
                <span class="category-title">装备卡牌</span>
                <span class="category-count">({{ cardsEquipment.length }})</span>
              </div>
              <div class="cards-grid">
                <div
                  v-for="uc in cardsEquipment"
                  :key="'equip-' + uc.name"
                  class="card-item equipment-card"
                >
                  <div class="card-item-header">
                    <div class="card-name">{{ uc.name }}</div>
                    <div class="card-badge equipment-badge">装备</div>
                  </div>
                  <div class="card-item-body">
                    <div class="card-effect">效果: {{ uc.effect || '未知' }}</div>
                  </div>
                  <div class="card-item-footer">
                    <div class="card-quantity">数量: ×{{ uc.quantity ?? 0 }}</div>
                    <button
                      v-if="(uc.quantity ?? 0) >= 3"
                      class="btn-combine"
                      @click="combineItem(uc)"
                    >
                      合成
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 右侧功能区 -->
      <aside class="sidebar">
        <!-- 营地商店 -->
        <div class="section-card">
          <div class="card-header">
            <h3 class="card-title">🛒 营地商店</h3>
            <button class="btn-refresh" @click="refreshShop" :disabled="isRefreshing">
              <span class="btn-icon" :class="{ spinning: isRefreshing }">🔄</span>
              <span>{{ isRefreshing ? '刷新中...' : '刷新商店' }}</span>
            </button>
          </div>

          <!-- 商品分类 -->
          <div class="shop-categories">
            <button 
              v-for="category in shopCategories" 
              :key="category.id"
              :class="['category-btn', { active: activeShopCategory === category.id }]"
              @click="activeShopCategory = category.id"
            >
              <i :class="category.icon"></i>
              {{ category.name }}
              <span class="item-count">{{ getCategoryCount(category.id) }}</span>
            </button>
          </div>

          <div v-if="loadingShop" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>

          <div v-else-if="filteredShopOffers.length === 0" class="empty-state">
            <div class="empty-icon">🏪</div>
            <p class="empty-text">该分类下暂无商品</p>
          </div>

          <div v-else class="shop-grid">
            <div
              v-for="offer in filteredShopOffers"
              :key="offer.id"
              class="shop-item"
              :class="{ 
                'discount': offer.discount,
                'purchased': offer.purchased,
                'locked': !canAffordOffer(offer)
              }"
              @click="showOfferDetails(offer)"
            >
              <div class="shop-item-header">
                <div class="offer-icon">
                  <i :class="offer.icon || 'fas fa-box'"></i>
                </div>
                <div class="shop-item-name">{{ offer.name }}</div>
                <div class="shop-item-type" :class="badgeClass(offer.type || offer.category || '')">
                  {{ offer.type || offer.category }}
                </div>
                <div v-if="offer.discount" class="discount-badge">
                  -{{ offer.discount }}%
                </div>
              </div>
              
              <div class="shop-item-body">
                <p class="shop-item-description">{{ offer.description || '暂无描述' }}</p>
                
                <!-- 商品属性 -->
                <div class="offer-stats" v-if="offer.stats">
                  <div v-for="stat in offer.stats" :key="stat.name" class="stat-item">
                    <i :class="stat.icon"></i>
                    <span>{{ stat.value }}</span>
                  </div>
                </div>
                
                <!-- 稀有度指示器 -->
                <div class="rarity-indicator" :class="offer.rarity" v-if="offer.rarity">
                  <div v-for="i in getRarityStars(offer.rarity)" :key="i" class="star">
                    <i class="fas fa-star"></i>
                  </div>
                </div>
              </div>
              
              <div class="shop-item-footer">
                <div class="shop-item-price">
                  <span v-if="offer.discount" class="original-price">
                    <span class="price-icon">🪙</span>
                    {{ offer.originalPrice }}
                  </span>
                  <span class="current-price">
                    <span class="price-icon">🪙</span>
                    {{ offer.currentPrice || offer.price }}
                  </span>
                </div>
                <button 
                  class="btn-buy"
                  :disabled="!canAffordOffer(offer) || offer.purchased"
                  @click.stop="purchaseOffer(offer)"
                >
                  {{ offer.purchased ? '已购买' : (canAffordOffer(offer) ? '购买' : '金币不足') }}
                </button>
              </div>
              
              <!-- 库存显示 -->
              <div class="stock-info" v-if="offer.stock !== undefined || offer.limitPerPlayer">
                <span v-if="offer.stock !== undefined" class="stock-count">
                  库存: {{ offer.stock }}
                </span>
                <span v-if="offer.limitPerPlayer" class="limit-info">
                  限购: {{ offer.limitPerPlayer - (offer.purchasedCount || 0) }}/{{ offer.limitPerPlayer }}
                </span>
              </div>
            </div>
          </div>

          <div v-if="buyMsg" class="message-box" :class="buyMsg.includes('失败') ? 'message-error' : 'message-success'">
            {{ buyMsg }}
          </div>
        </div>

        <!-- 背包道具 -->
        <div class="section-card">
          <div class="card-header">
            <h3 class="card-title">🎒 背包道具</h3>
            <div v-if="selectedChar" class="selected-char-info">
              {{ (selectedChar as any)?.name || '已选择角色' }}
            </div>
          </div>

          <div v-if="!selectedChar" class="empty-state">
            <div class="empty-icon">👤</div>
            <p class="empty-text">请先选择角色</p>
          </div>

          <div v-else-if="loadingBag" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载中...</p>
          </div>

          <div v-else-if="bag.length === 0" class="empty-state">
            <div class="empty-icon">📦</div>
            <p class="empty-text">背包为空</p>
          </div>

          <div v-else class="bag-grid">
            <div
              v-for="b in bag"
              :key="b.invId"
              class="bag-item"
            >
              <div class="bag-item-header">
                <div class="bag-item-name">{{ b.name }}</div>
                <div class="bag-item-type" :class="badgeClass(b.type)">
                  {{ b.type }}
                </div>
              </div>
              <div class="bag-item-description">{{ b.description || '无描述' }}</div>
              <div class="bag-item-effects">
                <span v-if="b.attrs?.heal">回血 {{ b.attrs.heal }}</span>
                <span v-if="b.attrs?.mpRestore">回蓝 {{ b.attrs.mpRestore }}</span>
                <span v-if="b.attrs?.starShard">升星碎片</span>
              </div>
              <div class="bag-item-footer">
                <div class="bag-item-quantity">×{{ b.quantity }}</div>
                <button
                  class="btn-use"
                  @click="useItem(b)"
                  :disabled="b.quantity <= 0"
                >
                  使用
                </button>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- 商品详情模态框 -->
    <Transition name="modal">
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
                <i :class="selectedOffer.icon || 'fas fa-box'"></i>
              </div>
              <div class="preview-info">
                <div class="rarity-badge" :class="selectedOffer.rarity || 'common'" v-if="selectedOffer.rarity">
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
                    <span class="price-icon">🪙</span>
                    {{ selectedOffer.currentPrice || selectedOffer.price }}
                  </span>
                </div>
                <div v-if="selectedOffer.discount" class="price-row discount">
                  <span>优惠折扣:</span>
                  <span>-{{ selectedOffer.discount }}%</span>
                </div>
                <div class="price-row total">
                  <span>总计:</span>
                  <span>
                    <span class="price-icon">🪙</span>
                    {{ selectedOffer.currentPrice || selectedOffer.price }}
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
              :disabled="!canAffordOffer(selectedOffer) || selectedOffer.purchased"
            >
              <i class="fas fa-shopping-cart"></i>
              {{ selectedOffer.purchased ? '已购买' : '确认购买' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useCharactersStore } from '@/stores/characters'
import { useWalletStore } from '@/stores/wallet'
import { useGameStore } from '@/stores/game'
import { gameApi } from '@/lib/api'
import AIChat from '@/components/AIChat.vue'

const chars = useCharactersStore()
const wallet = useWalletStore()
const game = useGameStore()

// 玩家职业信息
const playerClassInfo = ref<any>(null)
const playerLevel = ref<number>(1)

// 职业图标映射
function getClassIcon(code: string): string {
  const iconMap: Record<string, string> = {
    'warrior': '⚔️',
    'occultist': '🔮',
    'ranger': '🏹',
    'priest': '✨',
    'mage': '🔥',
    'rogue': '🗡️'
  }
  return iconMap[code] || '⚔️'
}

// 职业描述映射
function getClassDescription(code: string): string {
  const descMap: Record<string, string> = {
    'warrior': '近战战士，拥有强大的防御力和生命值，擅长在前线承受伤害',
    'occultist': '神秘学者，掌握黑暗魔法，能够召唤亡灵和施放诅咒',
    'ranger': '远程射手，精准的箭术和陷阱技能，适合远程输出',
    'priest': '神圣牧师，治疗和支援专家，能够恢复生命和驱散负面效果',
    'mage': '元素法师，掌控火焰、冰霜和雷电，强大的范围伤害',
    'rogue': '敏捷盗贼，高爆发伤害和闪避能力，擅长暗杀和偷袭'
  }
  return descMap[code] || '未知职业'
}

// 加载玩家职业信息
function loadPlayerClassInfo() {
  const mockData = localStorage.getItem('mockPlayerCharacter')
  if (mockData) {
    try {
      const data = JSON.parse(mockData)
      const mockClasses: Record<string, any> = {
        '1': { id: '1', code: 'warrior', name: '战士', baseHp: 120, hpPerLevel: 15 },
        '2': { id: '2', code: 'occultist', name: '神秘学者', baseHp: 80, hpPerLevel: 10 },
        '3': { id: '3', code: 'ranger', name: '游侠', baseHp: 90, hpPerLevel: 12 },
        '4': { id: '4', code: 'priest', name: '牧师', baseHp: 85, hpPerLevel: 11 },
        '5': { id: '5', code: 'mage', name: '法师', baseHp: 75, hpPerLevel: 9 },
        '6': { id: '6', code: 'rogue', name: '盗贼', baseHp: 95, hpPerLevel: 13 }
      }
      
      const classId = data.playerCharacterId
      playerClassInfo.value = mockClasses[classId] || mockClasses['1']
      playerLevel.value = data.level || 1
    } catch (e) {
      console.error('Failed to parse player class info:', e)
    }
  }
}

// 卡牌数据
const userCards = ref<Array<{ name: string; type: 'character' | 'spell' | 'equipment'; attack?: number; health?: number; effect?: string; stars?: number; quantity?: number }>>([])

const cardsCharacters = computed(() => userCards.value.filter(c => c.type === 'character'))
const cardsSpells = computed(() => userCards.value.filter(c => c.type === 'spell'))
const cardsEquipment = computed(() => userCards.value.filter(c => c.type === 'equipment'))

async function loadUserCards() {
  // 使用模拟数据
  userCards.value = [
    { name: '新兵', type: 'character' as const, attack: 1, health: 1, stars: 1, quantity: 2 },
    { name: '盾卫', type: 'character' as const, attack: 2, health: 3, stars: 1, quantity: 1 },
    { name: '祭司', type: 'character' as const, attack: 2, health: 4, stars: 1, quantity: 1 },
    { name: '火球术', type: 'spell' as const, effect: 'fireball3', quantity: 2 },
    { name: '战旗', type: 'equipment' as const, effect: 'teamBuffAtk1', quantity: 1 }
  ]
}

function syncToBattle() {
  const charsForBattle = userCards.value
    .filter(c => c.type === 'character')
    .map(c => ({ name: c.name, attack: Number(c.attack ?? 1), health: Number(c.health ?? 1) }))
  const itemsForBattle = userCards.value
    .filter(c => c.type !== 'character')
    .map(c => ({
      name: c.name,
      effect: (c.effect as any) ?? (c.type === 'spell' ? 'fireball3' : 'teamBuffAtk1'),
      cost: c.type === 'spell' ? 2 : 3
    }))
  game.setDeckFromCamp(charsForBattle, itemsForBattle)
}

function calcCharStats(name: string, stars: number): { attack: number; health: number; trait: string } {
  const base: Record<string, { attack: number; health: number; trait: string }> = {
    '游侠·莉雅': { attack: 3, health: 4, trait: '敏捷射术' },
    '法师·应龙': { attack: 4, health: 3, trait: '元素亲和' },
    '战士·江陵': { attack: 3, health: 6, trait: '钢铁意志' },
    '新兵': { attack: 1, health: 1, trait: '基础训练' },
    '盾卫': { attack: 2, health: 3, trait: '重盾防御' },
    '祭司': { attack: 2, health: 4, trait: '祈福治疗' }
  }
  const b = base[name] ?? { attack: 2, health: 3, trait: '通用适应' }
  const atk = Math.round(b.attack * (1 + (stars - 1) * 0.25))
  const hp = Math.round(b.health * (1 + (stars - 1) * 0.3))
  const traitTier = stars >= 5 ? 'MAX' : stars >= 4 ? 'IV' : stars >= 3 ? 'III' : stars >= 2 ? 'II' : 'I'
  const trait = `${b.trait}·${traitTier}`
  return { attack: atk, health: hp, trait }
}

const traitsMap = ref<Record<string, { trait_key: string; base_power: number; power_per_star: number; description: string }>>({})

async function loadTraits() {
  try {
    const response = await gameApi.getCharacterTraits()
    if (response.data.code === 200 && response.data.data) {
      traitsMap.value = response.data.data
    }
  } catch (error) {
    console.error('加载角色特性失败:', error)
  }
}

function charTrait(name: string, stars: number): string {
  const t = traitsMap.value[name]
  if (!t) {
    return calcCharStats(name, stars).trait
  }
  const tier = stars >= 5 ? 'MAX' : stars >= 4 ? 'IV' : stars >= 3 ? 'III' : stars >= 2 ? 'II' : 'I'
  return `${t.description}·${tier}`
}

async function combineItem(uc: { id?: string; name: string; type: 'character' | 'spell' | 'equipment'; quantity?: number; stars?: number; effect?: string }) {
  // 模拟合成逻辑
  console.log('Combining item:', uc.name)
  await loadUserCards()
}

// 商店数据
type ShopItem = { 
  id?: string
  offerId?: string
  itemId?: string
  name: string
  type: string
  category?: string
  price: number
  currentPrice?: number
  originalPrice?: number
  description?: string
  longDescription?: string
  icon?: string
  rarity?: string
  discount?: number
  stock?: number
  limitPerPlayer?: number
  purchased?: boolean
  purchasedCount?: number
  stats?: Array<{ name: string; value: string; icon: string }>
  effects?: string[]
}

const shop = ref<ShopItem[]>([])
const loadingShop = ref(false)
const buyMsg = ref<string | null>(null)
const isRefreshing = ref(false)
const selectedOffer = ref<ShopItem | null>(null)
const activeShopCategory = ref('characters')

// 商城分类
const shopCategories = ref([
  { id: 'characters', name: '角色', icon: 'fas fa-users' },
  { id: 'equipment', name: '装备', icon: 'fas fa-shield-alt' },
  { id: 'spells', name: '法术', icon: 'fas fa-magic' },
  { id: 'consumables', name: '消耗品', icon: 'fas fa-flask' },
  { id: 'bundles', name: '礼包', icon: 'fas fa-gift' }
])

// 过滤后的商品
const filteredShopOffers = computed(() => {
  return shop.value.filter(offer => {
    const category = offer.category || offer.type?.toLowerCase() || ''
    if (activeShopCategory.value === 'characters') {
      return category.includes('character') || offer.type === 'character'
    } else if (activeShopCategory.value === 'equipment') {
      return category.includes('equipment') || offer.type === 'weapon' || offer.type === 'armor'
    } else if (activeShopCategory.value === 'spells') {
      return category.includes('spell') || offer.type === 'spell'
    } else if (activeShopCategory.value === 'consumables') {
      return category.includes('consumable') || offer.type === 'consumable'
    } else if (activeShopCategory.value === 'bundles') {
      return category.includes('bundle') || offer.type === '礼包'
    }
    return true
  })
})

function getCategoryCount(categoryId: string) {
  return filteredShopOffers.value.length
}

function canAffordOffer(offer: ShopItem) {
  const price = offer.currentPrice || offer.price
  // 这里应该从钱包获取金币，暂时使用模拟值
  return true // 简化处理，实际应从钱包获取
}

function getRarityStars(rarity?: string) {
  if (!rarity) return 1
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

function showOfferDetails(offer: ShopItem) {
  selectedOffer.value = offer
}

function closeOfferDetails() {
  selectedOffer.value = null
}

function badgeClass(t: string) {
  switch ((t || '').toLowerCase()) {
    case 'weapon':
      return 'badge-red'
    case 'armor':
      return 'badge-blue'
    case 'consumable':
      return 'badge-green'
    case 'spell':
      return 'badge-purple'
    case 'character':
      return 'badge-amber'
    default:
      return 'badge-gray'
  }
}

// 背包数据
type BagItem = { invId: string; name: string; type: string; description?: string; quantity: number; attrs: any }
const bag = ref<BagItem[]>([])
const loadingBag = ref(false)

async function loadBag() {
  loadingBag.value = true
  // 模拟数据
  bag.value = []
    loadingBag.value = false
}

async function useItem(b: BagItem) {
  // 模拟使用道具
  console.log('Using item:', b.name)
  await loadBag()
}

const selectedChar = computed(() => chars.selected)

watch(selectedChar, async () => { await loadBag() })

async function loadShop() {
  loadingShop.value = true
  buyMsg.value = null
  // 模拟商店数据（包含更多商品和详细信息）
  shop.value = [
    // 角色
    {
      id: '1',
      offerId: '1',
      itemId: '1',
      name: '狂战士',
      type: 'character',
      category: 'characters',
      description: '攻击力强大的近战角色',
      longDescription: '狂战士拥有极高的攻击力和生命值，但防御较弱。适合担任前排输出角色。',
      icon: 'fas fa-sword',
      rarity: 'rare',
      price: 200,
      currentPrice: 200,
      originalPrice: 250,
      discount: 20,
      stock: 5,
      limitPerPlayer: 1,
      purchased: false,
      stats: [
        { name: '攻击', value: '+15', icon: 'fas fa-sword' },
        { name: '生命', value: '+20', icon: 'fas fa-heart' }
      ],
      effects: ['攻击力 +15%', '生命值 +20', '特性: 狂暴']
    },
    {
      id: '2',
      offerId: '2',
      itemId: '2',
      name: '圣骑士',
      type: 'character',
      category: 'characters',
      description: '防御力强悍的坦克角色',
      icon: 'fas fa-shield-alt',
      rarity: 'epic',
      price: 300,
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
      offerId: '3',
      itemId: '3',
      name: '炎龙之剑',
      type: 'weapon',
      category: 'equipment',
      description: '带有火焰伤害的传说武器',
      icon: 'fas fa-fire',
      rarity: 'legendary',
      price: 500,
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
      offerId: '4',
      itemId: '4',
      name: '生命药水',
      type: 'consumable',
      category: 'consumables',
      description: '恢复50点生命值',
      icon: 'fas fa-flask',
      rarity: 'common',
      currentPrice: 20,
      price: 20,
      stock: 20,
      limitPerPlayer: 10,
      purchased: false,
      effects: ['立即恢复 50 生命值']
    },
    // 礼包
    {
      id: '5',
      offerId: '5',
      itemId: '5',
      name: '新手礼包',
      type: '礼包',
      category: 'bundles',
      description: '包含多种道具的新手福利',
      longDescription: '适合刚开始冒险的新手玩家，包含基础装备和消耗品。',
      icon: 'fas fa-gift',
      rarity: 'rare',
      price: 100,
      currentPrice: 100,
      originalPrice: 150,
      discount: 33,
      stock: 10,
      limitPerPlayer: 1,
      purchased: false,
      effects: ['随机角色卡 x1', '生命药水 x5', '金币 x100']
    }
  ]
  loadingShop.value = false
}

async function buy(item: ShopItem) {
  purchaseOffer(item)
}

function purchaseOffer(offer: ShopItem) {
  if (!canAffordOffer(offer)) {
    buyMsg.value = '金币不足'
    setTimeout(() => { buyMsg.value = null }, 3000)
    return
  }
  
  if (offer.stock !== undefined && offer.stock <= 0) {
    buyMsg.value = '商品已售罄'
    setTimeout(() => { buyMsg.value = null }, 3000)
    return
  }

  if (offer.limitPerPlayer && (offer.purchasedCount || 0) >= offer.limitPerPlayer) {
    buyMsg.value = '已达到购买上限'
    setTimeout(() => { buyMsg.value = null }, 3000)
    return
  }
  
  // 执行购买
  const price = offer.currentPrice || offer.price
  // 这里应该从钱包扣除金币，暂时简化处理
  offer.purchased = true
  offer.purchasedCount = (offer.purchasedCount || 0) + 1
  if (offer.stock !== undefined) {
    offer.stock -= 1
  }
  
  buyMsg.value = `成功购买 ${offer.name}!`
  closeOfferDetails()
  setTimeout(() => {
    buyMsg.value = null
  }, 3000)
}

async function refreshShop() {
  isRefreshing.value = true
  buyMsg.value = null
  
  setTimeout(() => {
    // 模拟刷新商店 - 随机调整折扣
    shop.value.forEach(offer => {
      if (!offer.purchased) {
        offer.discount = Math.random() > 0.7 ? Math.floor(Math.random() * 30) + 10 : undefined
        if (offer.discount) {
          offer.originalPrice = offer.currentPrice || offer.price
          offer.currentPrice = Math.floor((offer.currentPrice || offer.price) * (1 - offer.discount / 100))
        }
      }
    })
    
    isRefreshing.value = false
    buyMsg.value = '商店已刷新'
    setTimeout(() => {
      buyMsg.value = null
    }, 3000)
  }, 1500)
}

onMounted(async () => {
  await wallet.loadWallets().catch(() => {})
  await loadShop()
  await loadBag()
      await loadUserCards()
  await loadTraits()
  loadPlayerClassInfo()
})
</script>

<style scoped>
.camp-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0a0a 0%, #1a1a2e 50%, #16213e 100%);
  color: #ffffff;
}

/* 顶部标题栏 */
.camp-header {
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 2px solid rgba(212, 175, 55, 0.3);
  padding: 24px 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
}

.title-text {
  font-size: 2rem;
  font-weight: bold;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.title-subtitle {
  font-size: 0.95rem;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

/* 职业信息横幅 */
.class-info-banner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 32px;
}

.banner-content {
  background: linear-gradient(135deg, rgba(212, 175, 55, 0.15), rgba(212, 175, 55, 0.05));
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.class-icon-large {
  font-size: 5rem;
  flex-shrink: 0;
  filter: drop-shadow(0 0 20px rgba(212, 175, 55, 0.5));
}

.class-details {
  flex: 1;
}

.class-name {
  font-size: 2rem;
  font-weight: bold;
  color: #d4af37;
  margin: 0 0 8px 0;
}

.class-description {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0 0 16px 0;
  line-height: 1.6;
}

.class-stats-row {
  display: flex;
  gap: 16px;
}

.stat-badge {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 12px;
  padding: 10px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.stat-value {
  font-size: 1.1rem;
  font-weight: bold;
  color: #d4af37;
}

/* 主内容区域 */
.camp-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 32px 32px;
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 24px;
}

/* 通用卡片样式 */
.section-card {
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(212, 175, 55, 0.3);
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.card-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
}

/* 按钮样式 */
.btn-sync,
.btn-refresh {
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 10px;
  padding: 8px 16px;
  color: #d4af37;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-sync:hover,
.btn-refresh:hover {
  background: rgba(212, 175, 55, 0.2);
  border-color: rgba(212, 175, 55, 0.5);
}

.btn-icon {
  font-size: 1rem;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.5);
  text-align: center;
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-text {
  font-size: 1rem;
  margin: 0;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.7);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(212, 175, 55, 0.3);
  border-top-color: #d4af37;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 卡牌分类 */
.card-category {
  margin-bottom: 32px;
}

.card-category:last-child {
  margin-bottom: 0;
}

.category-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
}

.category-icon {
  font-size: 1.25rem;
}

.category-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #ffffff;
}

.category-count {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

/* 卡牌网格 */
.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.card-item {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s ease;
}

.card-item:hover {
  border-color: rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.card-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-name {
  font-size: 1.1rem;
  font-weight: bold;
  color: #ffffff;
}

.card-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.character-badge {
  background: rgba(255, 152, 0, 0.2);
  border: 1px solid rgba(255, 152, 0, 0.4);
  color: #ff9800;
}

.spell-badge {
  background: rgba(156, 39, 176, 0.2);
  border: 1px solid rgba(156, 39, 176, 0.4);
  color: #9c27b0;
}

.equipment-badge {
  background: rgba(63, 81, 181, 0.2);
  border: 1px solid rgba(63, 81, 181, 0.4);
  color: #3f51b5;
}

.card-item-body {
  margin-bottom: 12px;
}

.card-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
}

.stat-icon {
  font-size: 1rem;
}

.card-trait {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
  font-style: italic;
}

.card-effect {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
}

.card-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.card-quantity {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.btn-combine {
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid rgba(212, 175, 55, 0.4);
  border-radius: 8px;
  padding: 6px 12px;
  color: #d4af37;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-combine:hover {
  background: rgba(212, 175, 55, 0.3);
  border-color: rgba(212, 175, 55, 0.6);
}

/* 商店网格 */
.shop-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shop-item {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
}

.shop-item:hover {
  border-color: rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
}

.shop-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.shop-item-name {
  font-size: 1rem;
  font-weight: bold;
  color: #ffffff;
}

.shop-item-type {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.badge-red {
  background: rgba(244, 67, 54, 0.2);
  border: 1px solid rgba(244, 67, 54, 0.4);
  color: #f44336;
}

.badge-blue {
  background: rgba(33, 150, 243, 0.2);
  border: 1px solid rgba(33, 150, 243, 0.4);
  color: #2196f3;
}

.badge-green {
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
  color: #4caf50;
}

.badge-purple {
  background: rgba(156, 39, 176, 0.2);
  border: 1px solid rgba(156, 39, 176, 0.4);
  color: #9c27b0;
}

.badge-amber {
  background: rgba(255, 152, 0, 0.2);
  border: 1px solid rgba(255, 152, 0, 0.4);
  color: #ff9800;
}

.badge-gray {
  background: rgba(158, 158, 158, 0.2);
  border: 1px solid rgba(158, 158, 158, 0.4);
  color: #9e9e9e;
}

.shop-item-description {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
  line-height: 1.5;
}

.shop-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.shop-item-price {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
  color: #d4af37;
}

.price-icon {
  font-size: 1.1rem;
}

.price-value {
  font-size: 1.1rem;
}

.btn-buy {
  background: linear-gradient(135deg, #d4af37, #ffd700);
  border: none;
  border-radius: 8px;
  padding: 8px 16px;
  color: #1a1a2e;
  font-size: 0.875rem;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-buy:hover {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

/* 消息提示 */
.message-box {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.875rem;
  text-align: center;
}

.message-success {
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
  color: #4caf50;
}

.message-error {
  background: rgba(244, 67, 54, 0.2);
  border: 1px solid rgba(244, 67, 54, 0.4);
  color: #f44336;
}

/* 背包网格 */
.bag-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bag-item {
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
}

.bag-item:hover {
  border-color: rgba(212, 175, 55, 0.4);
  transform: translateY(-2px);
}

.bag-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.bag-item-name {
  font-size: 1rem;
  font-weight: bold;
  color: #ffffff;
}

.bag-item-type {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
}

.bag-item-description {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 8px;
}

.bag-item-effects {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.bag-item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.bag-item-quantity {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.6);
}

.btn-use {
  background: rgba(76, 175, 80, 0.2);
  border: 1px solid rgba(76, 175, 80, 0.4);
  border-radius: 8px;
  padding: 6px 12px;
  color: #4caf50;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-use:hover:not(:disabled) {
  background: rgba(76, 175, 80, 0.3);
  border-color: rgba(76, 175, 80, 0.6);
}

.btn-use:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.selected-char-info {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  padding: 6px 12px;
  background: rgba(212, 175, 55, 0.1);
  border: 1px solid rgba(212, 175, 55, 0.3);
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .camp-content {
    grid-template-columns: 1fr;
  }
  
  .cards-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

/* 商城分类 */
.shop-categories {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding: 8px;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(212, 175, 55, 0.2);
  border-radius: 12px;
  flex-wrap: wrap;
}

.category-btn {
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  font-size: 0.875rem;
}

.category-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

.category-btn.active {
  background: rgba(212, 175, 55, 0.2);
  border: 1px solid rgba(212, 175, 55, 0.4);
  color: #d4af37;
}

.category-btn .item-count {
  background: rgba(0, 0, 0, 0.3);
  color: rgba(255, 255, 255, 0.8);
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 0.75rem;
  margin-left: 4px;
}

/* 商品卡片增强 */
.shop-item.discount {
  border-color: rgba(76, 175, 80, 0.5);
}

.shop-item.purchased {
  opacity: 0.6;
  border-color: rgba(255, 255, 255, 0.2);
}

.shop-item.locked {
  opacity: 0.4;
  cursor: not-allowed;
}

.shop-item-body {
  margin-bottom: 12px;
}

.offer-icon {
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.8);
  margin-right: 8px;
}

.offer-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 8px 0;
}

.offer-stats .stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.7);
}

.rarity-indicator {
  display: flex;
  gap: 4px;
  margin-top: 8px;
}

.rarity-indicator.common .star { color: #9e9e9e; }
.rarity-indicator.rare .star { color: #2196f3; }
.rarity-indicator.epic .star { color: #9c27b0; }
.rarity-indicator.legendary .star { color: #ff9800; }

.discount-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #4caf50;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.stock-info {
  padding: 8px 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.6);
}

.original-price {
  text-decoration: line-through;
  color: rgba(255, 255, 255, 0.5);
  font-size: 0.875rem;
  margin-right: 8px;
}

.current-price {
  font-weight: bold;
  font-size: 1.1rem;
  color: #d4af37;
}

/* 商品详情模态框 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(5px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.offer-modal {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 2px solid rgba(212, 175, 55, 0.5);
  border-radius: 20px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid rgba(212, 175, 55, 0.2);
}

.modal-header h2 {
  font-size: 1.5rem;
  color: #ffffff;
  margin: 0;
}

.close-btn {
  background: transparent;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  font-size: 1.5rem;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
}

.modal-content {
  padding: 24px;
}

.offer-preview {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.preview-icon {
  font-size: 3rem;
  color: rgba(255, 255, 255, 0.9);
}

.preview-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rarity-badge {
  padding: 6px 12px;
  border-radius: 15px;
  font-size: 0.875rem;
  font-weight: bold;
  text-align: center;
  width: fit-content;
}

.rarity-badge.common { background: #9e9e9e; color: white; }
.rarity-badge.rare { background: #2196f3; color: white; }
.rarity-badge.epic { background: #9c27b0; color: white; }
.rarity-badge.legendary { background: #ff9800; color: white; }

.offer-description {
  margin-bottom: 20px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
}

.offer-effects {
  margin-bottom: 20px;
}

.offer-effects h4 {
  color: #ffffff;
  margin-bottom: 12px;
  font-size: 1rem;
}

.offer-effects ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.offer-effects li {
  padding: 6px 0;
  color: rgba(255, 255, 255, 0.7);
}

.offer-effects li::before {
  content: "✓ ";
  color: #4caf50;
  font-weight: bold;
  margin-right: 8px;
}

.purchase-details {
  margin-bottom: 20px;
}

.price-breakdown {
  background: rgba(0, 0, 0, 0.3);
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(212, 175, 55, 0.2);
}

.price-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.8);
}

.price-row.discount {
  color: #4caf50;
}

.price-row.total {
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding-top: 12px;
  font-weight: bold;
  font-size: 1.1rem;
  color: #ffffff;
}

.modal-actions {
  display: flex;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid rgba(212, 175, 55, 0.2);
}

.cancel-btn {
  flex: 1;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
}

.confirm-purchase-btn {
  flex: 2;
  background: linear-gradient(135deg, #d4af37, #ffd700);
  color: #1a1a2e;
  border: none;
  padding: 12px;
  border-radius: 10px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.confirm-purchase-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(212, 175, 55, 0.3);
}

.confirm-purchase-btn:disabled {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
  cursor: not-allowed;
}

/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .offer-modal,
.modal-leave-active .offer-modal {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .offer-modal,
.modal-leave-to .offer-modal {
  transform: scale(0.9) translateY(-20px);
  opacity: 0;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .camp-header {
    padding: 16px 20px;
  }
  
  .class-info-banner {
    padding: 16px 20px;
  }
  
  .banner-content {
    flex-direction: column;
    text-align: center;
  }
  
  .class-stats-row {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .camp-content {
    padding: 0 20px 20px;
  }
  
  .section-card {
    padding: 20px;
  }
  
  .cards-grid {
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
