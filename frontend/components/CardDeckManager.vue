<template>
  <view class="card-deck-manager">
  
    <!-- 上阵卡组区域 -->
    <view class="lineup-area">
      <view class="lineup-header">
        <h3>上阵卡组</h3>
        <span class="lineup-count">{{ props.equippedCards.length }}/{{ props.deckLimit }}</span>
      </view>
      <view class="lineup-grid">
        <view
          v-for="(slot, idx) in lineupSlots"
          :key="idx"
          class="slot"
          :class="{ empty: !slot }"
          @click="slotClicked(slot)"
        >
          <view v-if="slot" class="slot-card">
            <view class="card-frame equipped" :class="slot.rarity">
              <!-- 上阵卡牌只显示图片和名字，占满整个卡牌 -->
              <view class="equipped-card-content">
                <view class="equipped-card-artwork">
                  <template v-if="getCardImage(slot.name)">
                    <img :src="getCardImage(slot.name)" class="equipped-card-img" />
                  </template>
                  <template v-else>
                    <i :class="getCardIcon(slot.cardType)" class="equipped-card-icon"></i>
                  </template>
                </view>
                <view class="equipped-card-name">
                  <text class="equipped-card-title">{{ slot.name }}</text>
                </view>
              </view>
            </view>
          </view>
          <view v-else class="slot-placeholder">
            <i class="fas fa-plus"></i>
          </view>
        </view>
      </view>
    </view>

    <!-- 筛选器 -->
    <view class="filters-section">
      <view class="filter-group">
        <button 
          v-for="filter in cardTypes" 
          :key="filter.value"
          :class="['filter-btn', { active: activeFilter.type === filter.value }]"
          @click="activeFilter.type = filter.value"
        >
          <i :class="filter.icon"></i>
          {{ filter.label }}
        </button>
      </view>
      
      <view class="rarity-filters">
        <button 
          v-for="rarity in rarities" 
          :key="rarity.value"
          :class="['rarity-filter', { active: activeFilter.rarity === rarity.value }]"
          @click="activeFilter.rarity = rarity.value"
        >
          <view class="rarity-dot" :class="rarity.value"></view>
          {{ rarity.label }}
        </button>
      </view>
    </view>
    
    <!-- 卡牌网格 -->
    <view class="cards-grid">
      <view
        v-for="(card, index) in filteredCards"
        :key="card.id"
        class="card-item"
        :class="{
          equipped: isCardEquipped(card.id),
          [card.rarity]: true
        }"
        @click="equipFromGrid(card)"
        :style="{ '--delay': index * 0.05 + 's' }"
      >
        <view class="card-frame" :class="card.rarity">
          <!-- 卡牌图片区域（占据大部分空间） -->
          <view class="card-artwork-large">
            <template v-if="getCardImage(card.name)">
              <img :src="getCardImage(card.name)" class="card-artwork-img-large" />
            </template>
            <template v-else>
              <i :class="getCardIcon(card.cardType)" class="card-artwork-icon-large"></i>
            </template>
            <!-- 角色卡的攻击力和生命值圆圈 -->
            <view v-if="card.cardType === 'character'" class="stat-circle stat-circle-left">
              <view class="stat-circle-content">
                <i class="fas fa-heart stat-icon"></i>
                <text class="stat-text">{{ card.hp ?? '-' }}</text>
              </view>
            </view>
            <view v-if="card.cardType === 'character'" class="stat-circle stat-circle-right">
              <view class="stat-circle-content">
                <i class="fas fa-sword stat-icon"></i>
                <text class="stat-text">{{ card.attack ?? '-' }}</text>
              </view>
            </view>
          </view>
          <!-- 卡牌名字区域 -->
          <view class="card-name-bottom">
            <h4 class="card-title">{{ card.name }}</h4>
          </view>
        </view>
        <view v-if="isCardEquipped(card.id)" class="equipped-overlay">
          <i class="fas fa-check-circle"></i>
        </view>
        <view class="card-quantity-badge">{{ (card.quantity !== undefined && card.quantity !== null) ? card.quantity : 1 }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
// card image mapping
import cardImageMap from '@/static/cardImageMap.json'
import { useCampStore } from '@/stores/camp'

interface Props {
  userCards: any[]
  equippedCards: any[]
  deckLimit?: number
}

const props = withDefaults(defineProps<Props>(), {
  deckLimit: 10
})

const emit = defineEmits<{
  equipCard: [card: any]
  unequipCard: [payload: any]
}>()

// 调试：当传入的 userCards 变化时打印样例，帮助在小程序控制台确认是否从后端获取到数据
onMounted(() => {
  console.log('[CardDeckManager] mounted, initial userCards count=', (props.userCards || []).length)
  if (Array.isArray(props.userCards) && props.userCards.length > 0) {
    console.log('[CardDeckManager] sample userCards (first 3):', props.userCards.slice(0, 3))
  }
})

watch(() => props.userCards, (newVal) => {
  console.log('[CardDeckManager] props.userCards updated, count=', (newVal || []).length)
  if (Array.isArray(newVal) && newVal.length > 0) {
    console.log('[CardDeckManager] sample userCards (first 3):', newVal.slice(0, 3))
  }
})

// 乐观上阵计数：防止快速重复上阵突破数量限制（id -> pending count）
const optimisticEquipped = ref(new Map<string, number>())

// 当父组件的 equippedCards 同步更新后，清理乐观集合中已被后端/父组件确认的 id
watch(() => props.equippedCards, (newVal) => {
  if (!Array.isArray(newVal)) return
  const present = new Set(newVal.map((c: any) => String(c.id)))
  for (const k of Array.from(optimisticEquipped.value.keys())) {
    if (present.has(k)) optimisticEquipped.value.delete(k)
  }
})


// 当父组件的 equippedCards 同步更新后，清理乐观上阵计数（确认后清空）
watch(() => props.equippedCards, (newVal) => {
  // 清空所有 pending，因为后端/父组件已回复最新状态
  optimisticEquipped.value.clear()
})

// 筛选状态
const activeFilter = ref({
  type: 'all',
  rarity: 'all',
  sort: 'name'
})

// 筛选选项
const cardTypes = ref([
  { value: 'all', label: '全部', icon: 'fas fa-th' },
  { value: 'character', label: '角色', icon: 'fas fa-user' },
  { value: 'spell', label: '法术', icon: 'fas fa-magic' },
  { value: 'equipment', label: '装备', icon: 'fas fa-shield-alt' }
])

const rarities = ref([
  { value: 'all', label: '全部' },
  { value: 'common', label: '普通' },
  { value: 'rare', label: '稀有' },
  { value: 'epic', label: '史诗' },
  { value: 'legendary', label: '传说' }
])

// 计算属性（会同时过滤已上阵卡与乐观上阵的卡）
const filteredCards = computed(() => {
  let cards = [...props.userCards].filter(card => card && card.id)

  // 调试：打印 equipped 与 userCards 的 id 变体，便于排查匹配问题
  try {
    const equippedIds = (props.equippedCards || []).map((ec: any) => ([
      String(ec.id ?? ''),
      String(ec.raw?.userCardId ?? ''),
      String(ec.raw?.cardId ?? ''),
      String(ec.raw?.userCardCharacterId ?? ''),
      String(ec.raw?.cardCharacterId ?? ''),
      String(ec.raw?.id ?? '')
    ].filter(s => s && s !== '')))

    const userIds = cards.map((c: any) => ([
      String(c.id ?? ''),
      String(c.userCardId ?? ''),
      String(c.cardId ?? ''),
      String(c.userCardCharacterId ?? ''),
      String(c.cardCharacterId ?? ''),
      String(c.raw?.id ?? ''),
      String(c.raw?.cardCharacterId ?? ''),
      String(c.raw?.userCardCharacterId ?? '')
    ].filter(s => s && s !== '')))

    console.log('[CardDeckManager] DEBUG equippedIds=', equippedIds, 'userIds=', userIds)
  } catch (e) {}

  // 类型筛选
  if (activeFilter.value.type !== 'all') {
    cards = cards.filter(card => card && card.cardType === activeFilter.value.type)
  }

  // 稀有度筛选
  if (activeFilter.value.rarity !== 'all') {
    cards = cards.filter(card => card && card.rarity === activeFilter.value.rarity)
  }

  // 排序
  cards.sort((a, b) => {
    if (!a || !b) return 0
    switch (activeFilter.value.sort) {
      case 'name':
        return (a.name || '').localeCompare(b.name || '')
      case 'rarity':
        const rarityOrder = { common: 1, rare: 2, epic: 3, legendary: 4 }
        const aRarity = a.rarity as keyof typeof rarityOrder
        const bRarity = b.rarity as keyof typeof rarityOrder
        return (rarityOrder[aRarity] || 0) - (rarityOrder[bRarity] || 0)
      case 'cost':
        return (a.manaCost || 1) - (b.manaCost || 1)
      default:
        return 0
    }
  })

  // 隐藏已无库存的卡牌（quantity 表示剩余可上阵数量）
  // 同时隐藏已装备的卡牌（防止重复上阵）
  // 计算时考虑乐观上阵(pending)计数：pending 会临时占用库存
  const result = cards.filter(card => {
    if (!card) return false

    // 检查是否已经装备（更鲁棒的匹配：比较所有 id 变体的交集，最后回退到名称匹配）
    const isEquipped = props.equippedCards.some((equippedCard: any) => {
      const eqVariants = [
        equippedCard.id,
        equippedCard.userCardId,
        equippedCard.cardId,
        equippedCard.userCardCharacterId,
        equippedCard.cardCharacterId,
        equippedCard.raw?.id,
        equippedCard.raw?.userCardId,
        equippedCard.raw?.cardId,
        equippedCard.raw?.userCardCharacterId,
        equippedCard.raw?.cardCharacterId
      ].filter((v: any) => v !== undefined && v !== null && String(v) !== '').map((v: any) => String(v))

      const cardVariants = [
        card.id,
        (card as any).userCardId,
        (card as any).cardId,
        (card as any).userCardCharacterId,
        (card as any).cardCharacterId,
        card.raw?.id,
        card.raw?.userCardId,
        card.raw?.cardId,
        card.raw?.userCardCharacterId,
        card.raw?.cardCharacterId
      ].filter((v: any) => v !== undefined && v !== null && String(v) !== '').map((v: any) => String(v))

      // 如果任一变体相等则认为已装备
      for (const a of eqVariants) {
        if (cardVariants.includes(a)) {
          return true
        }
      }

      // 最后回退到按名称匹配（避免 id 字段不一致时出现漏判）
      if ((equippedCard.name || '').trim() !== '' && (card.name || '').trim() !== '' && String(equippedCard.name) === String(card.name)) {
        return true
      }

      return false
    })

    // 如果已经装备，不显示
    if (isEquipped) return false

    const qty = Number(card.quantity ?? 0) || 0
    const idKey = String(card.id ?? card.userCardId ?? card.cardId ?? '')
    const pending = optimisticEquipped.value.get(idKey) || 0
    const effectiveQty = qty - pending
    return effectiveQty > 0
  })

  return result
})

// 生成固定长度的上阵插槽数组（空位为 null）
const lineupSlots = computed(() => {
  const limit = props.deckLimit || 10
  return Array.from({ length: limit }).map((_, i) => {
    return props.equippedCards && props.equippedCards[i] ? props.equippedCards[i] : null
  })
})

const slotClicked = (slot: any|null) => {
  if (!slot) return
  console.log('[CardDeckManager] slotClicked -> emitting unequipCard for id=', slot.id)
  // 清除乐观上阵状态（如果之前为该 id 添加过）
  try { optimisticEquipped.value.delete(String(slot.id)) } catch (e) {}
  // emit both normalized id and all backend id variants (if available) so parent can accurately restore quantity
  const idVariants = [
    slot.id,
    slot.raw?.userCardId,
    slot.raw?.cardId,
    slot.raw?.userCardCharacterId,
    slot.raw?.cardCharacterId
  ].filter(i => i !== undefined && i !== null && String(i) !== '').map(i => String(i))
  // 优先使用原始数据的 id 字段，通常就是后端 ID，然后才是其他变体
  const rawId = slot.raw?.id ? String(slot.raw.id) : (idVariants[1] ?? idVariants[2] ?? null)
  // include originalIndex, idVariants and raw data so parent can access backend IDs
  emit('unequipCard', { id: slot.id, rawId, idVariants, raw: slot.raw })
}

// 方法
const isCardEquipped = (cardId: string) => {
  return props.equippedCards.some(card => card.id === cardId)
}

const toggleEquipCard = (card: any) => {
  console.log('[CardDeckManager] card clicked -> id=', card?.id, 'isEquipped=', isCardEquipped(card.id))
  if (isCardEquipped(card.id)) {
    console.log('[CardDeckManager] emitting unequipCard for id=', card.id)
    emit('unequipCard', card.id)
  } else if (props.equippedCards.length < props.deckLimit) {
    console.log('[CardDeckManager] emitting equipCard for id=', card.id)
    // 不做额外的乐观隐藏（父组件会更新 store 并触发 props 变化）
    emit('equipCard', card)
  } else {
    console.warn('[CardDeckManager] cannot equip, deck limit reached', props.deckLimit)
  }
}

// 对于卡牌管理区的卡片点击：始终作为“上阵”动作（不会切换为撤下）
const equipFromGrid = (card: any) => {
  console.log('[CardDeckManager] equipFromGrid clicked id=', card?.id, 'quantity=', card?.quantity)
  if (!card || !card.id) return
  // 获取最新的数量信息直接从 store，避免使用可能已过期的 prop 对象
  const camp = useCampStore()
  const findQtyInStore = () => {
    const arr = (camp.userCards || []) as any[]
    const chars = (camp.userCardCharacters || []) as any[]
    const key = String(card.id)
    const found = arr.find(i => String(i.id) === key || String(i.userCardId) === key || String(i.cardId) === key)
    if (found) return Number(found.quantity ?? found.count ?? 0) || 0
    const foundChar = chars.find(i => String(i.id) === key || String(i.cardCharacterId) === key || String(i.userCardCharacterId) === key)
    if (foundChar) return Number(foundChar.quantity ?? foundChar.count ?? 0) || 0
    return Number(card.quantity ?? 0) || 0
  }
  const qty = findQtyInStore()
  if (qty <= 0) {
    console.warn('[CardDeckManager] cannot equip, no quantity left for id=', card.id)
    return
  }
  if ((props.equippedCards?.length ?? 0) >= props.deckLimit) {
    console.warn('[CardDeckManager] cannot equip from grid, deck full')
    return
  }
  // 计算当前已上阵该 id 的数量
  const actualEquipped = (props.equippedCards || []).filter((c: any) => String(c.id) === String(card.id)).length
  const pending = optimisticEquipped.value.get(String(card.id)) || 0
  if (actualEquipped + pending >= qty) {
    console.warn('[CardDeckManager] cannot equip, would exceed available quantity for id=', card.id, 'actual=', actualEquipped, 'pending=', pending, 'qty=', qty)
    return
  }
  // 增加乐观计数并 emit
  optimisticEquipped.value.set(String(card.id), pending + 1)
  console.log('[CardDeckManager] emitting equipCard for id=', card.id, 'from grid (pending now=', pending + 1, ')')
  emit('equipCard', card)
}

const getCardIcon = (type: string) => {
  const icons: { [key: string]: string } = {
    character: 'fas fa-user',
    spell: 'fas fa-magic',
    equipment: 'fas fa-shield-alt'
  }
  return icons[type] || 'fas fa-question'
}

const getRarityStars = (rarity: string) => {
  const stars: { [key: string]: number } = {
    common: 1,
    rare: 2,
    epic: 3,
    legendary: 4
  }
  return stars[rarity] || 1
}

// 返回卡牌对应的图片路径（按卡牌名字匹配），优先使用映射文件
const getCardImage = (cardName: string | undefined | null) => {
  if (!cardName) return null
  try {
    // 直接按名字匹配映射（确保中文名或英文名均可）
    const key = String(cardName)
    if ((cardImageMap as any)[key]) return (cardImageMap as any)[key]
    // fallback: 尝试按小写无空格匹配
    const normalized = key.toString().trim()
    if ((cardImageMap as any)[normalized]) return (cardImageMap as any)[normalized]
    return null
  } catch (e) {
    return null
  }
}
</script>

<style scoped>
.card-deck-manager {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}







.filters-section {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.lineup-area {
  background: rgba(255,255,255,0.03);
  padding: 0.75rem;
  border-radius: 12px;
  border: 1px solid rgba(255,255,255,0.04);
  margin-bottom: 0.75rem;
}
.lineup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.lineup-header h3 {
  margin: 0;
  color: #ffd78b;
  font-size: 1rem;
}
.lineup-count {
  color: rgba(255,255,255,0.7);
}
.lineup-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
}
.slot {
  height: 140px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255,255,255,0.02);
  border-radius: 8px;
  border: 1px dashed rgba(255,255,255,0.03);
  cursor: pointer;
  transition: all 0.15s;
  overflow: hidden;
}
.slot-card {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.slot-placeholder {
  color: rgba(255,255,255,0.35);
  font-size: 1.25rem;
}
.card-panel .card-desc {
  font-size: 0.75rem;
  color: #aab6c4;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.small-panel .card-desc {
  font-size: 0.7rem;
  color: #9aa3b0;
}

.filter-group {
  display: flex;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  padding: 0.5rem;
  border-radius: 12px;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #9ca3af;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
}

.filter-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.filter-btn.active {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
  border: 1px solid rgba(76, 175, 80, 0.4);
}

.rarity-filters {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.rarity-filter {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.85rem;
}

.rarity-filter:hover {
  background: rgba(255, 255, 255, 0.1);
}

.rarity-filter.active {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.rarity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.rarity-dot.common { background: #9e9e9e; }
.rarity-dot.rare { background: #2196f3; }
.rarity-dot.epic { background: #9c27b0; }
.rarity-dot.legendary { background: #ff9800; }

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 1.25rem;
}

.card-item {
  position: relative;
  cursor: pointer;
  transition: all 0.4s;
  transform-style: preserve-3d;
  animation: fadeInUp 0.6s ease;
  animation-delay: var(--delay);
}

.card-item:hover {
  transform: translateY(-8px) rotateX(5deg);
}

.card-frame {
  width: 100%;
  height: 240px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border: 3px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 0.5rem;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  /* 默认阴影效果 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 稀有度边框样式 - 高亮设计 */
.card-frame.common {
  border-color: #9e9e9e;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 8px rgba(158, 158, 158, 0.2);
}

.card-frame.rare {
  border-color: #2196f3;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 15px rgba(33, 150, 243, 0.4), 0 0 30px rgba(33, 150, 243, 0.2);
  background: linear-gradient(145deg, rgba(33, 150, 243, 0.08), rgba(33, 150, 243, 0.03));
}

.card-frame.epic {
  border-color: #9c27b0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 20px rgba(156, 39, 176, 0.5), 0 0 40px rgba(156, 39, 176, 0.3);
  background: linear-gradient(145deg, rgba(156, 39, 176, 0.1), rgba(156, 39, 176, 0.04));
}

.card-frame.legendary {
  border-color: #ff9800;
  border-width: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 25px rgba(255, 152, 0, 0.6), 0 0 50px rgba(255, 152, 0, 0.4), 0 0 75px rgba(255, 152, 0, 0.2);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.15), rgba(255, 152, 0, 0.06));
  animation: legendary-glow 3s ease-in-out infinite alternate;
}

@keyframes legendary-glow {
  0% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 20px rgba(255, 152, 0, 0.5), 0 0 40px rgba(255, 152, 0, 0.3), 0 0 60px rgba(255, 152, 0, 0.15);
  }
  100% {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 0 30px rgba(255, 152, 0, 0.7), 0 0 60px rgba(255, 152, 0, 0.5), 0 0 90px rgba(255, 152, 0, 0.3);
  }
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.mana-cost {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #4a90e2, #357abd);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 0.8rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

/* hide the top-left mana cost in this manager view (we'll show quantity on top-right instead) */
.mana-cost {
  display: none;
}

.rarity-indicator {
  display: flex;
  gap: 0.125rem;
}

.rarity-indicator i {
  color: #ffd700;
  font-size: 0.6rem;
}

/* 大的图片区域 */
.card-artwork-large {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 8px;
}

/* 原来的小图片区域样式 */
.card-artwork {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden; /* ensure image doesn't overflow card area */
}

.card-artwork-icon {
  font-size: 3rem;
  color: rgba(255, 255, 255, 0.7);
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

/* 大图标样式 */
.card-artwork-icon-large {
  font-size: 4rem;
  color: rgba(255, 255, 255, 0.7);
  filter: drop-shadow(0 6px 12px rgba(0, 0, 0, 0.4));
}

.card-artwork-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover; /* fill the area */
  border-radius: 8px;
  box-shadow: 0 6px 18px rgba(0,0,0,0.35);
}

/* 大图片样式 */
.card-artwork-img-large {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.4);
}

/* 上阵卡牌样式 */
.card-frame.equipped {
  padding: 4px;
  height: 140px;
  border-width: 2px;
}

/* 上阵卡牌的稀有度边框 */
.card-frame.equipped.common {
  border-color: #9e9e9e;
  box-shadow: 0 2px 8px rgba(158, 158, 158, 0.3);
}

.card-frame.equipped.rare {
  border-color: #2196f3;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.4);
}

.card-frame.equipped.epic {
  border-color: #9c27b0;
  box-shadow: 0 2px 8px rgba(156, 39, 176, 0.4);
}

.card-frame.equipped.legendary {
  border-color: #ff9800;
  border-width: 3px;
  box-shadow: 0 2px 12px rgba(255, 152, 0, 0.5);
}

.equipped-card-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
  background: rgba(0, 0, 0, 0.3);
}

.equipped-card-artwork {
  flex: 1;
  position: relative;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 6px 6px 0 0;
}

.equipped-card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 6px 6px 0 0;
}

.equipped-card-icon {
  font-size: 2.5rem;
  color: rgba(255, 255, 255, 0.7);
}

.equipped-card-name {
  padding: 2px 4px;
  background: rgba(0, 0, 0, 0.8);
  border-radius: 0 0 6px 6px;
  text-align: center;
  margin-top: auto;
}

.equipped-card-title {
  font-size: 0.75rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
  line-height: 1.2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 红色圆圈属性显示 */
.stat-circle {
  position: absolute;
  bottom: 8px;
  width: 32px;
  height: 32px;
  background: #f44336;
  border: 2px solid rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(244, 67, 54, 0.4);
}

.stat-circle-left {
  left: 8px;
}

.stat-circle-right {
  right: 8px;
}

.stat-circle-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1px;
}

.stat-icon {
  font-size: 0.7rem;
  color: white;
  margin: 0;
}

.stat-text {
  font-size: 0.6rem;
  font-weight: bold;
  color: white;
  margin: 0;
  line-height: 1;
}

.card-bottom {
  text-align: center;
  /* reduce bottom reserve so artwork occupies more space */
  padding-bottom: 8px;
}

/* 底部名字区域 */
.card-name-bottom {
  text-align: center;
  padding: 0.5rem 0.25rem 0.25rem 0.25rem;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 0 0 8px 8px;
  margin-top: auto;
}

.card-title {
  font-size: 0.85rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0;
  line-height: 1.2;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-stats-mini {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

.stat-mini {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.7rem;
  color: #9ca3af;
}

/* 旧的底部属性样式（保留用于上阵区域的小卡牌） */
.stat-bottom {
  position: absolute;
  bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 8px;
  background: rgba(0,0,0,0.35);
  color: #ffffff;
  font-size: 0.8rem;
  z-index: 8;
  box-shadow: 0 2px 6px rgba(0,0,0,0.4);
}
.stat-left { left: 10px; }
.stat-right { right: 10px; }
.stat-bottom i { font-size: 0.9rem; color: #ffd78b; }
.stat-bottom .stat-value { font-weight: 600; margin-left: 4px; }

/* Adjustments for small (lineup) card frames to avoid overlap */
.card-frame.small {
  padding: 0.6rem;
}
.card-frame.small .card-bottom {
  padding-bottom: 26px; /* less space needed for small cards */
}
.lineup-area .card-title {
  font-size: 0.85rem;
}
.lineup-area .stat-bottom {
  bottom: 6px;
  padding: 2px 6px;
  font-size: 0.76rem;
  gap: 4px;
  box-shadow: none;
  background: rgba(0,0,0,0.45);
}
.lineup-area .stat-bottom i { font-size: 0.8rem; color: #ffd78b; }
.lineup-area .stat-bottom .stat-value { font-weight: 600; margin-left: 3px; }

/* Slightly lift the title for small slot so it won't be covered */
.slot .card-title {
  margin-bottom: 6px;
}

.equipped-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(76, 175, 80, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: #4caf50;
}

.card-quantity-badge {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #f44336;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.7rem;
  font-weight: bold;
  border: 2px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
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

@media (max-width: 768px) {

  
  .cards-grid {
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 1rem;
  }
}
</style>