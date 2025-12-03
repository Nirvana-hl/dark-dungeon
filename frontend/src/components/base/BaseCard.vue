<template>
  <div 
    :class="cardClasses" 
    @click="handleClick"
    @mouseenter="showTooltip = true"
    @mouseleave="showTooltip = false"
  >
    <div class="card-header">
      <span class="card-name">{{ name }}</span>
      <span v-if="rarity" :class="['rarity-tag', `rarity-${rarity}`]">
        {{ rarityDisplay }}
      </span>
    </div>
    
    <div class="card-content">
      <slot name="content">
        <div v-if="imageUrl" class="card-image">
          <img :src="imageUrl" :alt="name" />
        </div>
        <div v-if="description" class="card-description">
          {{ description }}
        </div>
      </slot>
    </div>
    
    <div v-if="showStats" class="card-stats">
      <div v-if="hp" class="stat-item">
        <span class="stat-label">HP:</span>
        <span class="stat-value">{{ hp }}</span>
      </div>
      <div v-if="attack" class="stat-item">
        <span class="stat-label">ATK:</span>
        <span class="stat-value">{{ attack }}</span>
      </div>
      <div v-if="actionPoints" class="stat-item">
        <span class="stat-label">AP:</span>
        <span class="stat-value">{{ actionPoints }}</span>
      </div>
      <div v-if="stars" class="stat-item stars">
        <span class="stat-label">★</span>
        <span class="stat-value">{{ stars }}</span>
      </div>
    </div>
    
    <!-- 工具提示 -->
    <div v-if="tooltip && showTooltip" class="tooltip-content">
      {{ tooltip }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { Rarity } from '@/types'

interface Props {
  name: string
  rarity?: Rarity
  imageUrl?: string
  description?: string
  hp?: number
  attack?: number
  actionPoints?: number
  stars?: number
  tooltip?: string
  showStats?: boolean
  clickable?: boolean
  selected?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showStats: true,
  clickable: false,
  selected: false,
  disabled: false
})

interface Emits {
  (e: 'click'): void
}

const emit = defineEmits<Emits>()

const showTooltip = ref(false)

const cardClasses = computed(() => [
  'card',
  'base-card',
  {
    'card-clickable': props.clickable && !props.disabled,
    'card-selected': props.selected,
    'card-disabled': props.disabled,
    [`card-${props.rarity}`]: props.rarity
  }
])

const rarityDisplay = computed(() => {
  switch (props.rarity) {
    case 'common': return '普通'
    case 'rare': return '稀有'
    case 'epic': return '史诗'
    case 'legendary': return '传说'
    default: return ''
  }
})

function handleClick() {
  if (props.clickable && !props.disabled) {
    emit('click')
  }
}
</script>

<style scoped>
.base-card {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  cursor: default;
  min-height: 200px;
  display: flex;
  flex-direction: column;
}

.base-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-medium);
}

.card-clickable {
  cursor: pointer;
}

.card-clickable:hover {
  border-color: var(--text-accent);
}

.card-selected {
  border-color: var(--text-accent);
  box-shadow: 0 0 10px rgba(255, 107, 53, 0.3);
}

.card-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.card-disabled:hover {
  transform: none;
  box-shadow: none;
}

.card-common {
  background: linear-gradient(135deg, var(--secondary-bg), #2a2a2a);
}

.card-rare {
  background: linear-gradient(135deg, var(--secondary-bg), #1a3a52);
}

.card-epic {
  background: linear-gradient(135deg, var(--secondary-bg), #4a1a4a);
}

.card-legendary {
  background: linear-gradient(135deg, var(--secondary-bg), #4a2a1a);
  animation: legendary-glow 2s ease-in-out infinite alternate;
}

@keyframes legendary-glow {
  0% { box-shadow: 0 0 5px rgba(255, 152, 0, 0.5); }
  100% { box-shadow: 0 0 20px rgba(255, 152, 0, 0.8); }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-name {
  font-weight: bold;
  font-size: 16px;
  color: var(--text-primary);
}

.rarity-tag {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 10px;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.rarity-common { background: var(--common); color: white; }
.rarity-rare { background: var(--rare); color: white; }
.rarity-epic { background: var(--epic); color: white; }
.rarity-legendary { 
  background: linear-gradient(135deg, var(--legendary), #ff5722);
  color: white;
}

.card-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card-image {
  width: 100%;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 4px;
  background: var(--tertiary-bg);
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-description {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.4;
  flex: 1;
}

.card-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(60px, 1fr));
  gap: 8px;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
}

.stat-label {
  color: var(--text-muted);
}

.stat-value {
  color: var(--text-primary);
}

.stat-item.stars .stat-value {
  color: var(--warning);
}

.tooltip-content {
  position: absolute;
  bottom: 100%;
  left: 50%;
  transform: translateX(-50%);
  background: var(--tertiary-bg);
  color: var(--text-primary);
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 14px;
  white-space: nowrap;
  opacity: 0;
  visibility: hidden;
  transition: all 0.15s ease;
  z-index: 1000;
  margin-bottom: 8px;
  box-shadow: var(--shadow-medium);
  max-width: 200px;
  white-space: normal;
}

.tooltip-content::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: var(--tertiary-bg);
}

.base-card:hover .tooltip-content {
  opacity: 1;
  visibility: visible;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .base-card {
    padding: 12px;
    min-height: 160px;
  }
  
  .card-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stat-item {
    font-size: 11px;
  }
}
</style>