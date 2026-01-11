<script setup lang="ts">
import type { Card } from '@/stores/game'
import { computed, ref, onMounted, onUnmounted } from 'vue'
import cardImageMap from '@/static/cardImageMap.json'

const props = defineProps<{
  card: Card
  canAfford?: boolean
  showTypeText?: boolean
}>()

// whether to show the textual type label (default true)
const showTypeText = (props as any).showTypeText === false ? false : true

// card art url (prefer mapping, fallback to shop images by id)
const artUrl = computed(() => {
  try {
    const nameKey = String((props.card as any).name || '').trim()
    if (nameKey && (cardImageMap as any)[nameKey]) return (cardImageMap as any)[nameKey]
    const normalized = nameKey.toString().trim().toLowerCase().replace(/\s+/g, '_')
    if (normalized && (cardImageMap as any)[normalized]) return (cardImageMap as any)[normalized]
    const idKey = String((props.card as any).id || '')
    if (idKey) return `static/images/shop/cards/${idKey}.png`
    return null
  } catch (e) {
    return null
  }
})

const emit = defineEmits<{
  (e: 'play', id: string): void
  (e: 'start-equip-drag', card: Card): void
  (e: 'end-equip-drag'): void
  (e: 'start-character-drag', card: Card): void
  (e: 'end-character-drag'): void
  (e: 'start-spell-drag', card: Card): void
  (e: 'end-spell-drag', event?: DragEvent): void
  // 装备点击时，仅查看详情，不直接打出
  (e: 'show-equipment', card: Card): void
  // 角色卡点击时，仅查看详情，不直接打出
  (e: 'show-character', card: Card): void
  // 法术卡点击时，仅查看详情，不直接打出
  (e: 'show-spell', card: Card): void
  // deploy/equip events (for touch drop)
  (e: 'deploy-card', payload: { cardId: string; position: number }): void
  (e: 'equip-to-minion', payload: { minionId: string }): void
  // touch drag event (for mini-program compatibility)
  (e: 'touch-drag-end', payload: { cardId: string; cardType: string; x: number; y: number; canAfford: boolean }): void
}>()

const isHovered = ref(false)
const isPlaying = ref(false)
// mobile touch-to-slide state
const touchStartX = ref<number | null>(null)
const touchStartY = ref<number | null>(null)
const touchMoved = ref(false)
const touchDragging = ref(false)
const suppressClick = ref(false)

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
  if (suppressClick.value) {
    suppressClick.value = false
    return
  }
  // 为兼容父组件（如 battle.vue）中原有的 show-* 事件，仍旧触发这些事件
  if (props.card.type === 'equipment') {
    emit('show-equipment', props.card)
    return
  } else if (props.card.type === 'character') {
    emit('show-character', props.card)
    return
  } else if (props.card.type === 'spell') {
    emit('show-spell', props.card)
    return
  }
}
// click-preview functionality removed per request

function onTouchStart(e: TouchEvent) {
  if (!e.touches || e.touches.length === 0 || !e.touches[0]) return
  // support touch-drag for spells, equipment, and character cards
  if (props.card.type !== 'spell' && props.card.type !== 'equipment' && props.card.type !== 'character') return
  console.log('[CardItem] onTouchStart', { id: props.card.id, type: props.card.type, x: e.touches[0].clientX, y: e.touches[0].clientY })
  touchStartX.value = e.touches[0].clientX
  touchStartY.value = e.touches[0].clientY
  touchMoved.value = false
  touchDragging.value = true
  // notify parent that drag started (for consistency)
  if (props.card.type === 'equipment') {
    console.log('[CardItem] Starting equipment drag for card:', props.card.id)
    emit('start-equip-drag', props.card)
  } else if (props.card.type === 'character') {
    emit('start-character-drag', props.card)
  } else if (props.card.type === 'spell') {
    emit('start-spell-drag', props.card)
  }
}

function onTouchMove(e: TouchEvent) {
  if (!touchDragging.value || touchStartX.value == null || touchStartY.value == null || !e.touches[0]) return
  console.log('[CardItem] onTouchMove', { id: props.card.id, x: e.touches[0].clientX, y: e.touches[0].clientY })
  const dx = Math.abs(e.touches[0].clientX - touchStartX.value)
  const dy = Math.abs(e.touches[0].clientY - touchStartY.value)
  if (dx > 8 || dy > 8) {
    touchMoved.value = true
  }
}

function onTouchEnd(e: TouchEvent) {
  if (!touchDragging.value) return
  touchDragging.value = false
  if (touchMoved.value) {
    const touch = (e.changedTouches && e.changedTouches[0]) || null
    if (!touch) return
    console.log('[CardItem] onTouchEnd - emitting touch-drag-end', { id: props.card.id, type: props.card.type, x: touch.clientX, y: touch.clientY, canAfford: props.canAfford ?? true })
    // For mini-program compatibility, emit coordinates for parent to resolve target using uni.createSelectorQuery.
    emit('touch-drag-end', {
      cardId: props.card.id,
      cardType: props.card.type,
      x: touch.clientX,
      y: touch.clientY,
      canAfford: props.canAfford ?? true
    })
    // suppress immediate click handler
    suppressClick.value = true
  } else {
    console.log('[CardItem] Touch end without move - treating as click')
    // no move: treat as click (handled elsewhere)
  }
  touchStartX.value = null
  touchStartY.value = null
  touchMoved.value = false
}

function handleDragStart(e: DragEvent) {
  // 支持装备卡、角色卡和法术卡的拖拽
  if (props.card.type === 'equipment') {
    try {
      e.dataTransfer?.setData('text/plain', String(props.card.id))
      if (e.dataTransfer) {
        e.dataTransfer.effectAllowed = 'move'
      }
    } catch {}
    emit('start-equip-drag', props.card)
  } else if (props.card.type === 'character') {
    try {
      e.dataTransfer?.setData('text/plain', String(props.card.id))
      if (e.dataTransfer) {
        e.dataTransfer.effectAllowed = 'move'
      }
    } catch {}
    emit('start-character-drag', props.card)
  } else if (props.card.type === 'spell') {
    try {
      e.dataTransfer?.setData('text/plain', String(props.card.id))
      if (e.dataTransfer) {
        e.dataTransfer.effectAllowed = 'move'
      }
    } catch {}
    emit('start-spell-drag', props.card)
  }
}

function handleDragEnd(e: DragEvent) {
  if (props.card.type === 'equipment') {
    emit('end-equip-drag')
  } else if (props.card.type === 'character') {
    emit('end-character-drag')
  } else if (props.card.type === 'spell') {
    emit('end-spell-drag', e)
  }
}
</script>

<template>
  <view 
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
    :draggable="card.type === 'equipment' || card.type === 'character' || card.type === 'spell'"
    @click="handleClick"
    @dragstart="handleDragStart"
    @dragend="handleDragEnd"
    @touchstart.passive="onTouchStart"
    @touchmove.passive="onTouchMove"
    @touchend.passive="onTouchEnd"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- 冷却遮罩 -->
    <view v-if="currentCooldown > 0" class="cooldown-overlay">
      <view class="cooldown-content">
        <view class="cooldown-icon">⏱️</view>
        <view class="cooldown-text">{{ currentCooldown }}</view>
      </view>
    </view>

    <!-- 背景图（铺满卡片） -->
    <view v-if="artUrl" class="card-bg" aria-hidden="true">
      <image :src="artUrl" class="card-bg-image" mode="aspectFill" />
    </view>

    <!-- 费用显示（大徽章，出界） -->
    <view class="card-cost" :class="{ 'unaffordable': !canAfford }">
      <text class="cost-value">{{ card.cost }}</text>
    </view>

    <!-- 卡牌头部（可选） -->
    <view v-if="showTypeText" class="card-header">
      <view class="card-type-badge" :class="typeColor">
        <text class="type-icon">{{ typeIcon }}</text>
        <text class="type-text">{{ card.type === 'character' ? '角色' : card.type === 'spell' ? '法术' : '装备' }}</text>
      </view>
    </view>

    <!-- 卡片可见内容（裁剪） -->
    <view class="card-content">
      <!-- 卡牌效果（可扩展） -->
      <view class="card-effect"></view>

      <!-- 卡牌名称：绝对贴底 -->
      <view class="card-name">{{ card.name }}</view>

      <!-- 悬停时的详细信息 -->
      <view v-if="isHovered" class="card-tooltip">
        <view class="tooltip-content">
          <view class="tooltip-name">{{ card.name }}</view>
          <view class="tooltip-type">{{ card.type === 'character' ? '角色卡' : card.type === 'spell' ? '法术卡' : '装备卡' }}</view>
          <view class="tooltip-effect">{{ effectDescription }}</view>
          <view class="tooltip-cost">费用: {{ card.cost }}</view>
          <view v-if="cooldown > 0" class="tooltip-cooldown">冷却: {{ cooldown }} 回合</view>
        </view>
      </view>
    </view>

    <!-- 播放动画 -->
    <view v-if="isPlaying" class="play-effect"></view>
    
    <!-- 底部左右属性面板（atk / hp） -->
    <view class="card-stats-bottom" aria-hidden="true">
      <view v-if="props.card.type === 'character'" class="stat-panel stat-attack">
        <text class="stat-label">atk</text>
        <text class="stat-value">{{ props.card.attack }}</text>
      </view>
      <view v-if="props.card.type === 'character'" class="stat-panel stat-hp">
        <text class="stat-label">hp</text>
        <text class="stat-value">{{ props.card.health }}</text>
      </view>
    </view>
  </view>

  <!-- card preview removed -->
</template>

<style scoped>
.card-item {
  position: relative;
  min-width: 140px;
  width: 140px;
  height: 180px; /* 降低手牌高度，减少占用空间 */
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.8), rgba(15, 23, 42, 0.8));
  border: 2px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
}

/* background image styles: show full image (contain) centered, add subtle backdrop */
.card-bg {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  overflow: hidden;
  z-index: 0;
  background: rgba(0,0,0,0.06);
}
.card-bg-image {
  width: 100%;
  height: 100%;
  object-fit: cover; /* fill the card like before */
  object-position: center;
  display: block;
  background-color: transparent;
  transform-origin: center;
}

/* content container above background */
.card-content {
  position: relative;
  z-index: 6;
  display: flex;
  flex-direction: column;
  height: 100%;
  pointer-events: none;
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
  /* move cost badge slightly inside to avoid clipping */
  top: 8px;
  right: 8px;
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 1rem;
  color: white;
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.28);
  z-index: 40;
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
  position: absolute;
  left: 10px;
  right: 10px;
  bottom: 6px;
  font-size: 0.9375rem;
  font-weight: 700;
  color: #e2e8f0;
  line-height: 1.1;
  display: block;
  z-index: 12;
  text-align: center;
  white-space: nowrap;
  text-overflow: ellipsis;
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

/* 卡牌详情弹窗样式 */
.card-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 24px;
}
.card-preview-fixed {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 3000; /* higher than battlefield */
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: auto;
  max-width: 96vw;
  max-height: 92vh;
  padding: 12px;
}
.card-item.modal-preview.centered {
  /* 略微放大原始卡片（比原卡 ~140px 宽稍大），保持正位且居中 */
  width: min(180px, 80vw);
  height: auto;
  min-height: 220px;
  max-height: 86vh;
  padding: 14px;
  border-radius: 12px;
  box-shadow: 0 18px 50px rgba(0,0,0,0.55);
  background: linear-gradient(145deg, rgba(20,27,38,0.98), rgba(12,18,26,0.98));
  overflow: auto;
  -webkit-overflow-scrolling: touch;
  transform: none !important; /* 确保不带旋转或缩放 */
}
.card-modal-overlay {
  /* allow overlay to darken battlefield but still let preview overlap */
  background: rgba(0,0,0,0.32);
}
.card-item.modal-preview {
  width: 60%;
  height: auto;
  min-height: 420px;
  padding: 22px;
  border-radius: 14px;
  transform-origin: center;
  transition: transform 0.22s cubic-bezier(0.2,0,0,1);
  box-shadow: 0 18px 60px rgba(0,0,0,0.7);
}

/* 移除原先的右侧 info 面板，改为单一放大卡片视图 */
.modal-close {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2100;
  background: rgba(0,0,0,0.35);
  border: none;
  color: #e2e8f0;
  font-size: 1.125rem;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
.modal-close:hover {
  background: rgba(0,0,0,0.5);
}

.card-modal-body.single-body {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}
.card-visual.single {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px 24px;
}
.card-item.modal-preview.full {
  width: min(520px, 88vw);
  height: auto;
  min-height: 520px;
  padding: 24px;
}
.card-effect.full-effect {
  font-size: 1rem;
  color: #dbeafe;
  margin-top: 12px;
}
.card-stats {
  display: flex;
  gap: 12px;
  margin-top: 14px;
}
.card-stats .stat {
  background: rgba(255,255,255,0.03);
  padding: 6px 10px;
  border-radius: 8px;
  color: #cfe8ff;
  font-weight: 700;
}

/* bottom outer stat panels (visible on battlefield cards) */
.card-stats-bottom {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 8px; /* move inside to avoid cropping */
  pointer-events: none;
  z-index: 40;
}
.card-stats-bottom .stat-panel {
  position: absolute;
  bottom: 0;
  background: transparent;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0;
  color: #e6eef8;
  font-weight: 700;
  pointer-events: none;
}
.card-stats-bottom .stat-attack {
  left: 8px; /* move inside */
}
.card-stats-bottom .stat-hp {
  right: 8px; /* move inside */
}
.card-stats-bottom .stat-label {
  font-size: 0.62rem;
  color: rgba(255,255,255,0.85);
  text-transform: uppercase;
  letter-spacing: 0.02em;
  margin-bottom: 2px;
}
.card-stats-bottom .stat-value {
  font-size: 0.9rem;
  font-weight: 800;
  text-shadow: 0 1px 2px rgba(0,0,0,0.6);
}
.card-stats-bottom .stat-attack .stat-value {
  color: #ffd54a; /* attack yellow */
}
.card-stats-bottom .stat-hp .stat-value {
  color: #7ef3b6; /* hp green */
}
</style>
