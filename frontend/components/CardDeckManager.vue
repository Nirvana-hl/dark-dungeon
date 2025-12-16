<template>
  <view class="card-deck-manager">

    
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
        @click="toggleEquipCard(card)"
        :style="{ '--delay': index * 0.05 + 's' }"
      >
        <view class="card-frame" :class="card.rarity">
          <view class="card-top">
            <text class="mana-cost">{{ card.manaCost || 1 }}</text>
            <view class="rarity-indicator">
              <i v-for="i in getRarityStars(card.rarity)" :key="i" class="fas fa-star"></i>
            </view>
          </view>
          <view class="card-artwork">
            <i :class="getCardIcon(card.cardType)" class="card-artwork-icon"></i>
          </view>
          <view class="card-bottom">
            <h4 class="card-title">{{ card.name }}</h4>
            <view class="card-stats-mini" v-if="card.cardType === 'character'">
              <text class="stat-mini">
                <i class="fas fa-heart"></i>
                {{ card.hp || 1 }}
              </text>
              <text class="stat-mini">
                <i class="fas fa-sword"></i>
                {{ card.attack || 1 }}
              </text>
            </view>
          </view>
        </view>
        <view v-if="isCardEquipped(card.id)" class="equipped-overlay">
          <i class="fas fa-check-circle"></i>
        </view>
        <view class="card-quantity-badge">{{ card.quantity || 1 }}</view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

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
  unequipCard: [cardId: string]
}>()

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

// 计算属性
const filteredCards = computed(() => {
  let cards = [...props.userCards].filter(card => card && card.id)
  
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
  
  return cards
})

// 方法
const isCardEquipped = (cardId: string) => {
  return props.equippedCards.some(card => card.id === cardId)
}

const toggleEquipCard = (card: any) => {
  if (isCardEquipped(card.id)) {
    emit('unequipCard', card.id)
  } else if (props.equippedCards.length < props.deckLimit) {
    emit('equipCard', card)
  }
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
  border: 2px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.card-frame.common { border-color: #9e9e9e; }
.card-frame.rare { border-color: #2196f3; box-shadow: 0 0 20px rgba(33, 150, 243, 0.3); }
.card-frame.epic { border-color: #9c27b0; box-shadow: 0 0 20px rgba(156, 39, 176, 0.3); }
.card-frame.legendary { 
  border-color: #ff9800; 
  box-shadow: 0 0 30px rgba(255, 152, 0, 0.5);
  background: linear-gradient(145deg, rgba(255, 152, 0, 0.2), rgba(255, 152, 0, 0.05));
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

.rarity-indicator {
  display: flex;
  gap: 0.125rem;
}

.rarity-indicator i {
  color: #ffd700;
  font-size: 0.6rem;
}

.card-artwork {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 1rem 0;
  position: relative;
}

.card-artwork-icon {
  font-size: 3rem;
  color: rgba(255, 255, 255, 0.7);
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

.card-bottom {
  text-align: center;
}

.card-title {
  font-size: 0.9rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.5rem 0;
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
  bottom: -8px;
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