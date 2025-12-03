<template>
  <div class="stress-indicator">
    <div class="stress-bar">
      <div 
        class="stress-fill"
        :class="stressLevelClass"
        :style="{ width: `${stressPercentage}%` }"
      ></div>
    </div>
    <div class="stress-info">
      <span class="stress-level-text">{{ stressLevelText }}</span>
      <div class="stress-dots">
        <div 
          v-for="level in 4" 
          :key="level"
          class="stress-dot"
          :class="{ 
            'active': level <= stressLevel,
            [`level-${level}`]: true
          }"
        ></div>
      </div>
    </div>
    
    <!-- 压力详情弹窗 -->
    <div v-if="showDetails" class="stress-details">
      <h4>压力状态详情</h4>
      <div class="stress-stats">
        <div class="stat-item">
          <span>当前压力值:</span>
          <span>{{ currentStress }}/100</span>
        </div>
        <div class="stat-item">
          <span>压力等级:</span>
          <span :class="stressLevelClass">{{ stressLevelText }}</span>
        </div>
      </div>
      
      <!-- 激活的debuff -->
      <div v-if="activeDebuffs.length > 0" class="debuffs-section">
        <h5>当前负面效果</h5>
        <div class="debuffs-list">
          <div 
            v-for="debuff in activeDebuffs" 
            :key="debuff.id"
            class="debuff-item"
            :class="debuff.type"
          >
            <div class="debuff-icon">
              <i :class="getDebuffIcon(debuff.type)"></i>
            </div>
            <div class="debuff-info">
              <div class="debuff-name">{{ debuff.name }}</div>
              <div class="debuff-description">{{ debuff.description }}</div>
            </div>
            <div class="debuff-chance">{{ Math.round(debuff.triggerChance * 100) }}%</div>
          </div>
        </div>
      </div>
      
      <!-- 缓解建议 -->
      <div class="stress-relief-suggestions">
        <h5>缓解建议</h5>
        <div class="suggestions-list">
          <div 
            v-for="suggestion in reliefSuggestions" 
            :key="suggestion.facility"
            class="suggestion-item"
            @click="$emit('useFacility', suggestion.facility)"
          >
            <div class="suggestion-icon">
              <i :class="suggestion.icon"></i>
            </div>
            <div class="suggestion-info">
              <div class="suggestion-name">{{ suggestion.name }}</div>
              <div class="suggestion-effect">-{{ suggestion.reliefAmount }} 压力</div>
            </div>
            <div class="suggestion-cost">
              <i class="fas fa-coins"></i>
              {{ suggestion.cost }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StressDebuffConfig } from '@/types'

interface Props {
  stressLevel: number
  currentStress?: number
  maxStress?: number
  showDetails?: boolean
  activeDebuffs?: StressDebuffConfig[]
}

interface Emits {
  (e: 'useFacility', facility: string): void
}

const props = withDefaults(defineProps<Props>(), {
  currentStress: 25,
  maxStress: 100,
  showDetails: false,
  activeDebuffs: () => []
})

const emit = defineEmits<Emits>()

// 计算属性
const stressPercentage = computed(() => {
  return Math.min(100, (props.currentStress / props.maxStress) * 100)
})

const stressLevelClass = computed(() => {
  switch (props.stressLevel) {
    case 1: return 'stress-low'
    case 2: return 'stress-medium'
    case 3: return 'stress-high'
    case 4: return 'stress-max'
    default: return 'stress-none'
  }
})

const stressLevelText = computed(() => {
  switch (props.stressLevel) {
    case 1: return '轻松'
    case 2: return '紧张'
    case 3: return '焦虑'
    case 4: return '崩溃'
    default: return '平静'
  }
})

// 缓解建议
const reliefSuggestions = computed(() => [
  {
    facility: 'tavern',
    name: '酒馆',
    icon: 'fas fa-beer',
    reliefAmount: 20,
    cost: 50
  },
  {
    facility: 'chapel',
    name: '教堂',
    icon: 'fas fa-church',
    reliefAmount: 15,
    cost: 30
  },
  {
    facility: 'sanctum',
    name: '圣所',
    icon: 'fas fa-spa',
    reliefAmount: 30,
    cost: 100
  }
])

// 方法
function getDebuffIcon(type: string): string {
  switch (type) {
    case 'mental': return 'fas fa-brain'
    case 'combat': return 'fas fa-sword'
    case 'behavioral': return 'fas fa-walking'
    default: return 'fas fa-exclamation-triangle'
  }
}
</script>

<style scoped>
.stress-indicator {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 120px;
}

.stress-bar {
  width: 100%;
  height: 8px;
  background: var(--tertiary-bg);
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.stress-fill {
  height: 100%;
  transition: width 0.5s ease, background 0.3s ease;
  border-radius: 4px;
}

.stress-fill.stress-none {
  background: var(--text-muted);
}

.stress-fill.stress-low {
  background: var(--stress-low);
}

.stress-fill.stress-medium {
  background: var(--stress-medium);
}

.stress-fill.stress-high {
  background: var(--stress-high);
}

.stress-fill.stress-max {
  background: var(--stress-max);
  animation: pulse 1s infinite;
}

.stress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stress-level-text {
  font-size: 12px;
  font-weight: bold;
  color: var(--text-primary);
}

.stress-dots {
  display: flex;
  gap: 2px;
}

.stress-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--border-color);
  transition: all 0.3s ease;
}

.stress-dot.active.level-1 {
  background: var(--stress-low);
}

.stress-dot.active.level-2 {
  background: var(--stress-medium);
}

.stress-dot.active.level-3 {
  background: var(--stress-high);
  animation: pulse 1.5s infinite;
}

.stress-dot.active.level-4 {
  background: var(--stress-max);
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { 
    opacity: 1;
    transform: scale(1);
  }
  50% { 
    opacity: 0.7;
    transform: scale(1.1);
  }
}

/* 压力详情弹窗 */
.stress-details {
  position: absolute;
  top: 100%;
  left: 0;
  min-width: 300px;
  background: var(--secondary-bg);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 16px;
  margin-top: 8px;
  box-shadow: var(--shadow-heavy);
  z-index: 1000;
  backdrop-filter: blur(10px);
}

.stress-details h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 8px;
}

.stress-stats {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}

.stat-item span:first-child {
  color: var(--text-secondary);
}

.stat-item span:last-child {
  font-weight: bold;
  color: var(--text-primary);
}

.debuffs-section {
  margin-bottom: 16px;
}

.debuffs-section h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
}

.debuffs-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.debuff-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  border-left: 3px solid var(--border-color);
}

.debuff-item.mental {
  border-left-color: #9c27b0;
}

.debuff-item.combat {
  border-left-color: var(--error);
}

.debuff-item.behavioral {
  border-left-color: var(--warning);
}

.debuff-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  font-size: 12px;
}

.debuff-info {
  flex: 1;
  min-width: 0;
}

.debuff-name {
  font-size: 12px;
  font-weight: bold;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.debuff-description {
  font-size: 11px;
  color: var(--text-secondary);
  line-height: 1.2;
}

.debuff-chance {
  font-size: 12px;
  font-weight: bold;
  color: var(--text-accent);
  white-space: nowrap;
}

.stress-relief-suggestions h5 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: bold;
  color: var(--text-primary);
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.suggestion-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--text-accent);
  transform: translateY(-1px);
}

.suggestion-icon {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--tertiary-bg);
  border-radius: 50%;
  color: var(--text-accent);
}

.suggestion-info {
  flex: 1;
  min-width: 0;
}

.suggestion-name {
  font-size: 13px;
  font-weight: bold;
  color: var(--text-primary);
}

.suggestion-effect {
  font-size: 11px;
  color: var(--success);
}

.suggestion-cost {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: bold;
  color: var(--warning);
}

@media (max-width: 768px) {
  .stress-indicator {
    min-width: 100px;
  }
  
  .stress-details {
    min-width: 280px;
    left: -10px;
    right: -10px;
  }
}
</style>