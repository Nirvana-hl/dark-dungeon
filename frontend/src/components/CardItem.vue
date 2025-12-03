<script setup lang="ts">
import type { Card } from '@/stores/game'
import { computed, ref } from 'vue'

const props = defineProps<{
  card: Card
  canAfford?: boolean
}>()

const emit = defineEmits<{
  (e: 'play', id: string): void
  (e: 'start-equip-drag', card: Card): void
  (e: 'end-equip-drag'): void
  // 装备点击时，仅查看详情，不直接打出
  (e: 'show-equipment', card: Card): void
}>()

const isHovered = ref(false)
const isPlaying = ref(false)

// 计算冷却时间（示例：根据卡牌类型和费用）
const cooldown = computed(() => {
  // 这里可以根据实际游戏逻辑设置冷却时间
  // 示例：法术卡有1回合冷却，角色卡无冷却
  if (props.card.type === 'spell') return 1
  return 0
})

const currentCooldown = ref(0)

// 卡牌是否可用
const isPlayable = computed(() => {
  return (props.canAfford ?? true) && currentCooldown.value === 0
})

// 获取卡牌类型图标
const typeIcon = computed(() => {
  switch (props.card.type) {
    case 'character': return '⚔️'
    case 'spell': return '✨'
    case 'equipment': return '🛡️'
    default: return '❓'
  }
})

// 获取卡牌类型颜色
const typeColor = computed(() => {
  switch (props.card.type) {
    case 'character': return 'character'
    case 'spell': return 'spell'
    case 'equipment': return 'equipment'
    default: return 'default'
  }
})

// 获取效果描述
const effectDescription = computed(() => {
  if (props.card.type === 'character') {
    return `ATK ${props.card.attack} · HP ${props.card.health}`
  } else if (props.card.type === 'spell') {
    if (props.card.effect === 'fireball3') return '造成3点伤害'
    return '法术效果'
  } else {
    if (props.card.effect === 'teamBuffAtk1') return '友方随从+1攻击'
    return '装备效果'
  }
})

function handleClick() {
  if (!isPlayable.value) return

  // 装备卡：点击只查看详情，不直接打出
  if (props.card.type === 'equipment') {
    emit('show-equipment', props.card)
    return
  }

  isPlaying.value = true
  setTimeout(() => {
    isPlaying.value = false
  }, 300)
  // 触发冷却
  if (cooldown.value > 0) {
    currentCooldown.value = cooldown.value
    // 这里应该由游戏状态管理来处理冷却，这里只是UI展示
  }
  // 通知父组件打出这张牌
  emit('play', props.card.id)
}

function handleDragStart(e: DragEvent) {
  if (props.card.type !== 'equipment') return
  try {
    e.dataTransfer?.setData('text/plain', String(props.card.id))
    if (e.dataTransfer) {
      e.dataTransfer.effectAllowed = 'move'
    }
  } catch {}
  emit('start-equip-drag', props.card)
}

function handleDragEnd() {
  if (props.card.type !== 'equipment') return
  emit('end-equip-drag')
}
</script>

<template>
  <div 
    class="card-item"
    :class="[
      typeColor,
      { 
        'playable': isPlayable,
        'unaffordable': !canAfford,
        'on-cooldown': currentCooldown > 0,
        'hovered': isHovered,
        'playing': isPlaying
      }
    ]"
    :draggable="card.type === 'equipment'"
    @click="handleClick"
    @dragstart="handleDragStart"
    @dragend="handleDragEnd"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- 冷却遮罩 -->
    <div v-if="currentCooldown > 0" class="cooldown-overlay">
      <div class="cooldown-content">
        <div class="cooldown-icon">⏱️</div>
        <div class="cooldown-text">{{ currentCooldown }}</div>
      </div>
    </div>

    <!-- 费用显示 -->
    <div class="card-cost" :class="{ 'unaffordable': !canAfford }">
      <span class="cost-value">{{ card.cost }}</span>
    </div>

    <!-- 卡牌头部 -->
    <div class="card-header">
      <div class="card-type-badge" :class="typeColor">
        <span class="type-icon">{{ typeIcon }}</span>
        <span class="type-text">{{ card.type === 'character' ? '角色' : card.type === 'spell' ? '法术' : '装备' }}</span>
      </div>
    </div>

    <!-- 卡牌名称 -->
    <div class="card-name">{{ card.name }}</div>

    <!-- 卡牌效果 -->
    <div class="card-effect">
      {{ effectDescription }}
    </div>

    <!-- 悬停时的详细信息 -->
    <div v-if="isHovered" class="card-tooltip">
      <div class="tooltip-content">
        <div class="tooltip-name">{{ card.name }}</div>
        <div class="tooltip-type">{{ card.type === 'character' ? '角色卡' : card.type === 'spell' ? '法术卡' : '装备卡' }}</div>
        <div class="tooltip-effect">{{ effectDescription }}</div>
        <div class="tooltip-cost">费用: {{ card.cost }}</div>
        <div v-if="cooldown > 0" class="tooltip-cooldown">冷却: {{ cooldown }} 回合</div>
      </div>
    </div>

    <!-- 播放动画 -->
    <div v-if="isPlaying" class="play-effect"></div>
  </div>
</template>

<style scoped>
.card-item {
  position: relative;
  min-width: 140px;
  width: 140px;
  height: 200px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.8));
  border: 2px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
}

.card-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.05), transparent);
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.card-item:hover::before {
  opacity: 1;
}

/* 卡牌类型颜色 */
.card-item.character {
  border-color: rgba(59, 130, 246, 0.4);
  background: linear-gradient(145deg, rgba(30, 58, 138, 0.3), rgba(15, 23, 42, 0.8));
}

.card-item.spell {
  border-color: rgba(139, 92, 246, 0.4);
  background: linear-gradient(145deg, rgba(76, 29, 149, 0.3), rgba(15, 23, 42, 0.8));
}

.card-item.equipment {
  border-color: rgba(245, 158, 11, 0.4);
  background: linear-gradient(145deg, rgba(120, 53, 15, 0.3), rgba(15, 23, 42, 0.8));
}

/* 可玩状态 */
.card-item.playable:hover {
  transform: translateY(-8px) scale(1.05);
  box-shadow: 0 12px 32px rgba(59, 130, 246, 0.4);
  border-color: rgba(59, 130, 246, 0.8);
}

.card-item.playable:active {
  transform: translateY(-4px) scale(1.02);
}

/* 不可支付状态 */
.card-item.unaffordable {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.5);
}

.card-item.unaffordable:hover {
  transform: none;
  box-shadow: none;
}

/* 冷却状态 */
.card-item.on-cooldown {
  opacity: 0.6;
  cursor: not-allowed;
  filter: grayscale(0.3);
}

/* 冷却遮罩 */
.cooldown-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  z-index: 10;
  animation: cooldown-pulse 1s infinite;
}

@keyframes cooldown-pulse {
  0%, 100% { opacity: 0.7; }
  50% { opacity: 0.9; }
}

.cooldown-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.cooldown-icon {
  font-size: 2rem;
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.cooldown-text {
  font-size: 1.5rem;
  font-weight: 700;
  color: #fbbf24;
  text-shadow: 0 0 10px rgba(251, 191, 36, 0.5);
}

/* 费用显示 */
.card-cost {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.875rem;
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.4);
  z-index: 5;
}

.card-cost.unaffordable {
  background: linear-gradient(135deg, #64748b, #475569);
  box-shadow: 0 2px 8px rgba(100, 116, 139, 0.3);
}

.cost-value {
  font-size: 0.875rem;
  font-weight: 700;
}

/* 卡牌头部 */
.card-header {
  margin-bottom: 8px;
}

.card-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 0.6875rem;
  font-weight: 600;
  text-transform: uppercase;
}

.card-type-badge.character {
  background: rgba(59, 130, 246, 0.2);
  color: #60a5fa;
  border: 1px solid rgba(59, 130, 246, 0.4);
}

.card-type-badge.spell {
  background: rgba(139, 92, 246, 0.2);
  color: #a78bfa;
  border: 1px solid rgba(139, 92, 246, 0.4);
}

.card-type-badge.equipment {
  background: rgba(245, 158, 11, 0.2);
  color: #fbbf24;
  border: 1px solid rgba(245, 158, 11, 0.4);
}

.type-icon {
  font-size: 0.75rem;
}

.type-text {
  font-size: 0.6875rem;
}

/* 卡牌名称 */
.card-name {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #e2e8f0;
  margin-bottom: 8px;
  line-height: 1.3;
  min-height: 2.6em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 卡牌效果 */
.card-effect {
  flex: 1;
  font-size: 0.75rem;
  color: #cbd5e1;
  line-height: 1.4;
  display: flex;
  align-items: flex-end;
}

/* 工具提示 */
.card-tooltip {
  position: absolute;
  bottom: calc(100% + 12px);
  left: 50%;
  transform: translateX(-50%);
  background: rgba(15, 23, 42, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 8px;
  padding: 12px;
  min-width: 200px;
  z-index: 100;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
  animation: tooltip-fade-in 0.2s ease;
  pointer-events: none;
}

@keyframes tooltip-fade-in {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.tooltip-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tooltip-name {
  font-size: 1rem;
  font-weight: 700;
  color: #e2e8f0;
}

.tooltip-type {
  font-size: 0.75rem;
  color: #94a3b8;
  text-transform: uppercase;
}

.tooltip-effect {
  font-size: 0.8125rem;
  color: #cbd5e1;
  margin: 4px 0;
}

.tooltip-cost, .tooltip-cooldown {
  font-size: 0.75rem;
  color: #94a3b8;
}

.tooltip-cooldown {
  color: #fbbf24;
}

/* 播放特效 */
.play-effect {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.4), transparent);
  border-radius: 12px;
  animation: play-flash 0.3s ease;
  pointer-events: none;
  z-index: 1;
}

@keyframes play-flash {
  0% {
    opacity: 0;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
  100% {
    opacity: 0;
    transform: scale(1);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .card-item {
    min-width: 120px;
    width: 120px;
    height: 180px;
    padding: 10px;
  }

  .card-name {
    font-size: 0.875rem;
    min-height: 2.4em;
  }

  .card-effect {
    font-size: 0.6875rem;
  }

  .card-cost {
    width: 28px;
    height: 28px;
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .card-item {
    min-width: 100px;
    width: 100px;
    height: 160px;
    padding: 8px;
  }

  .card-tooltip {
    min-width: 180px;
    padding: 10px;
  }
}
</style>
