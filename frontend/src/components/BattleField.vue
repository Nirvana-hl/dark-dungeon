<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/game'
import { useCampStore } from '@/stores/camp'

const game = useGameStore()
const campStore = useCampStore()
const { heroHP, enemyHP, board, enemyBoard, enemyHand, enemyDeck, enemyMana, enemyManaMax, enemyDeckExhausted, hasEnemyPlayedCards, currentEnemyId, enemyPanel, turn, logs, mana, manaMax, hand, deck, deckExhausted } = storeToRefs(game)

// 受击反馈状态
const hitEffects = ref<Map<string, { timestamp: number; damage: number }>>(new Map())
const playerHitEffect = ref<{ timestamp: number; damage: number } | null>(null)
const enemyBossHitEffect = ref<{ timestamp: number; damage: number } | null>(null)

// 攻击动画状态 - 存储攻击者的绝对位置信息
const attackingMinions = ref<Map<string, { targetId: string; isBoss: boolean; x: number; y: number; targetX: number; targetY: number }>>(new Map())

// 计算攻击位移 - 使用固定定位，计算从攻击者到目标的绝对位置
function calculateAttackOffset(attackerId: string, targetId: string | null, isBoss: boolean): Promise<{ x: number; y: number; targetX: number; targetY: number }> {
  // 等待DOM更新后获取元素位置
  return new Promise<{ x: number; y: number; targetX: number; targetY: number }>((resolve) => {
    setTimeout(() => {
      const attackerEl = document.querySelector(`[data-attacker-id="${attackerId}"]`) as HTMLElement
      let targetEl: HTMLElement | null = null
      
      if (isBoss) {
        // 攻击敌人本体：找到敌人头像
        targetEl = document.querySelector('.enemy-panel .player-avatar-container') as HTMLElement
      } else if (targetId) {
        // 攻击敌方角色：找到目标角色卡片
        targetEl = document.querySelector(`[data-minion-id="${targetId}"]`) as HTMLElement
      }
      
      if (attackerEl && targetEl) {
        const attackerRect = attackerEl.getBoundingClientRect()
        const targetRect = targetEl.getBoundingClientRect()
        
        // 计算中心点位置（相对于视口）
        const attackerX = attackerRect.left + attackerRect.width / 2
        const attackerY = attackerRect.top + attackerRect.height / 2
        const targetX = targetRect.left + targetRect.width / 2
        const targetY = targetRect.top + targetRect.height / 2
        
        resolve({ x: attackerX, y: attackerY, targetX, targetY })
      } else {
        // 如果找不到元素，使用估算值
        if (isBoss) {
          resolve({ x: 200, y: 400, targetX: 1000, targetY: 400 })
        } else {
          resolve({ x: 200, y: 400, targetX: 800, targetY: 400 })
        }
      }
    }, 50)
  })
}

// 监听攻击事件
async function handleAttackStart(event: CustomEvent) {
  const { attackerId, targetId, isBoss } = event.detail
  if (attackerId && (targetId || isBoss)) {
    const positions = await calculateAttackOffset(attackerId, targetId || null, !!isBoss)
    // 通过新 Map 触发响应式更新，确保动画立即生效
    const next = new Map(attackingMinions.value)
    next.set(attackerId, { 
      targetId: targetId || '', 
      isBoss: !!isBoss,
      x: positions.x,
      y: positions.y,
      targetX: positions.targetX,
      targetY: positions.targetY
    })
    attackingMinions.value = next
  }
}

function handleAttackEnd(event: CustomEvent) {
  const { attackerId } = event.detail
  if (attackerId) {
    const next = new Map(attackingMinions.value)
    next.delete(attackerId)
    attackingMinions.value = next
  }
}

// 检查是否正在攻击
function isAttacking(minionId: string): boolean {
  return attackingMinions.value.has(minionId)
}

function getAttackTarget(minionId: string): { targetId: string; isBoss: boolean; x: number; y: number; targetX: number; targetY: number } | undefined {
  return attackingMinions.value.get(minionId)
}

// 获取攻击样式 - 使用固定定位跳出方框
function getAttackStyle(minionId: string): string {
  const target = getAttackTarget(minionId)
  if (!target) return ''
  return `
    position: fixed;
    left: ${target.x}px;
    top: ${target.y}px;
    --start-x: ${target.x}px;
    --start-y: ${target.y}px;
    --target-x: ${target.targetX}px;
    --target-y: ${target.targetY}px;
    z-index: 10000;
  `
}

// 监听受击事件
function handleEnemyHit(event: CustomEvent) {
  const { minionId, damage, beforeHealth, afterHealth } = event.detail
  const next = new Map(hitEffects.value)
  next.set(minionId, { timestamp: Date.now(), damage })
  hitEffects.value = next
  // 3秒后清除效果
  setTimeout(() => {
    const cleared = new Map(hitEffects.value)
    cleared.delete(minionId)
    hitEffects.value = cleared
  }, 3000)
  
  // 如果有血量变化信息，可以用于显示更详细的反馈
  if (beforeHealth !== undefined && afterHealth !== undefined) {
    console.log(`[BattleField] ${minionId} 受击: ${beforeHealth} -> ${afterHealth} (伤害: ${damage})`)
  }
}

function handleAllyHit(event: CustomEvent) {
  const { minionId, damage } = event.detail
  const next = new Map(hitEffects.value)
  next.set(minionId, { timestamp: Date.now(), damage })
  hitEffects.value = next
  setTimeout(() => {
    const cleared = new Map(hitEffects.value)
    cleared.delete(minionId)
    hitEffects.value = cleared
  }, 3000)
}

function handlePlayerHit(event: CustomEvent) {
  const { damage } = event.detail
  playerHitEffect.value = { timestamp: Date.now(), damage }
  setTimeout(() => {
    playerHitEffect.value = null
  }, 2000)
}

function handleEnemyBossHit(event: CustomEvent) {
  const { damage } = event.detail
  enemyBossHitEffect.value = { timestamp: Date.now(), damage }
  setTimeout(() => {
    enemyBossHitEffect.value = null
  }, 2000)
}

// 检查是否有受击效果
function hasHitEffect(minionId: string): boolean {
  return hitEffects.value.has(minionId)
}

function getHitEffect(minionId: string): { timestamp: number; damage: number } | undefined {
  return hitEffects.value.get(minionId)
}

onMounted(() => {
  window.addEventListener('enemy-hit', handleEnemyHit as EventListener)
  window.addEventListener('ally-hit', handleAllyHit as EventListener)
  window.addEventListener('player-hit', handlePlayerHit as EventListener)
  window.addEventListener('enemy-boss-hit', handleEnemyBossHit as EventListener)
  window.addEventListener('attack-start', handleAttackStart as unknown as EventListener)
  window.addEventListener('attack-end', handleAttackEnd as EventListener)
})

onUnmounted(() => {
  window.removeEventListener('enemy-hit', handleEnemyHit as EventListener)
  window.removeEventListener('ally-hit', handleAllyHit as EventListener)
  window.removeEventListener('player-hit', handlePlayerHit as EventListener)
  window.removeEventListener('enemy-boss-hit', handleEnemyBossHit as EventListener)
  window.removeEventListener('attack-start', handleAttackStart as unknown as EventListener)
  window.removeEventListener('attack-end', handleAttackEnd as EventListener)
})

// 调试：监听 enemyPanel 变化
import { watch } from 'vue'
watch(enemyPanel, (newVal) => {
  console.log('[BattleField] enemyPanel 变化:', newVal)
}, { immediate: true, deep: true })

const props = defineProps<{
  draggingEquipCard: import('@/stores/game').Card | null
}>()

const emit = defineEmits<{
  (e: 'equip-to-minion', payload: { minionId: string }): void
  (e: 'deploy-card', payload: { cardId: string; position: number }): void
}>()

// 从营地数据获取最大血量，如果没有则使用默认值100
const heroHPMax = computed(() => campStore.playerCharacter?.maxHp || 100)
// 敌人最大HP从敌人面板获取，如果没有则使用默认值100
const enemyHPMax = computed(() => enemyPanel.value?.hp || 100)
// 获取玩家角色名称
const playerCharacterName = computed(() => campStore.playerCharacter?.playerCharacterName || '冒险者')
// 获取玩家角色图标
const playerCharacterIcon = computed(() => {
  const name = playerCharacterName.value
  return iconFor(name, 'ally')
})
// 获取敌方角色名称
const enemyCharacterName = computed(() => enemyPanel.value?.name || '敌方')
// 获取敌方角色图标
const enemyCharacterIcon = computed(() => {
  const name = enemyCharacterName.value
  return iconFor(name, 'enemy')
})

const heroHPPercent = computed(() => Math.max(0, Math.min(100, (heroHP.value / heroHPMax.value) * 100)))
const enemyHPPercent = computed(() => Math.max(0, Math.min(100, (enemyHP.value / enemyHPMax.value) * 100)))

// 简易图标映射（如未引入字体图标，将显示 emoji）
function iconFor(name: string, side: 'enemy' | 'ally') {
  const lower = name.toLowerCase()
  if (side === 'enemy') {
    if (lower.includes('守门者')) return { emoji: '🛡️', color: 'text-red-400' }
    if (lower.includes('龙')) return { emoji: '🐉', color: 'text-red-400' }
    if (lower.includes('猎手') || lower.includes('刺客')) return { emoji: '🗡️', color: 'text-purple-400' }
    return { emoji: '👹', color: 'text-rose-400' }
  } else {
    if (lower.includes('骑士')) return { emoji: '🛡️', color: 'text-yellow-400' }
    if (lower.includes('弓') || lower.includes('游侠')) return { emoji: '🏹', color: 'text-blue-400' }
    if (lower.includes('祭司') || lower.includes('法师')) return { emoji: '✨', color: 'text-indigo-400' }
    return { emoji: '⚔️', color: 'text-emerald-400' }
  }
}

// 计算角色HP百分比
function getHPPercent(current: number, max: number) {
  return Math.max(0, Math.min(100, (current / max) * 100))
}

// 获取HP颜色类
function getHPColorClass(percent: number) {
  if (percent > 60) return 'hp-healthy'
  if (percent > 30) return 'hp-warning'
  return 'hp-danger'
}

// 获取指定位置的角色
function getMinionAtPosition(position: number) {
  return board.value.find(m => m.position === position)
}

// 获取指定位置的敌方角色
function getEnemyMinionAtPosition(position: number) {
  return enemyBoard.value.find(m => m.position === position)
}

// 处理位置槽的拖拽悬停
function handleSlotDragOver(event: DragEvent, position: number) {
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'move'
  }
}

// 处理位置槽的拖拽放置
function handleSlotDrop(event: DragEvent, position: number) {
  const cardId = event.dataTransfer?.getData('text/plain')
  if (cardId) {
    emit('deploy-card', { cardId, position })
  }
}
</script>

<template>
  <main class="battle-field">
    <!-- 左右：战斗区域 -->
    <div class="battle-arena">
      <!-- 我方区域（左） -->
      <div class="ally-zone" :class="{ 'ally-turn-active': turn === 'player' }">
        <!-- 左侧竖排信息区 -->
        <div class="vertical-info-panel ally-panel">
          <div class="vertical-info-content">
            <!-- 圆形头像框 -->
            <div class="player-avatar-container" :class="{ 'hit-effect': playerHitEffect }">
              <div class="vertical-name-text">{{ playerCharacterName }}</div>
              <div class="player-avatar-circle">
                <span :class="['player-avatar-icon', playerCharacterIcon.color]">
                  {{ playerCharacterIcon.emoji }}
                </span>
              </div>
              <!-- 玩家受击伤害数字 -->
              <div v-if="playerHitEffect" class="damage-number player-damage">
                -{{ playerHitEffect.damage }}
              </div>
            </div>
            <!-- 血条 -->
            <div class="hp-display ally-hp horizontal">
              <div class="hp-bar-container horizontal">
                <div class="hp-bar-bg horizontal">
                  <div 
                    class="hp-bar-fill"
                    :class="getHPColorClass(heroHPPercent)"
                    :style="{ width: heroHPPercent + '%' }"
                  ></div>
                </div>
              </div>
              <div class="hp-text horizontal">
                <span class="hp-value">{{ heroHP }}</span>
                <span class="hp-separator">/</span>
                <span class="hp-max">{{ heroHPMax }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div class="info-divider"></div>

        <!-- 中间对战区 -->
        <div class="battle-area">
          <div class="battle-slots">
            <!-- 左列：位置 3, 4, 5 -->
            <div class="battle-column left-column">
              <div
                v-for="slotIndex in 3"
                :key="slotIndex + 2"
                class="battle-slot"
                :class="{ 'occupied': getMinionAtPosition(slotIndex + 2) }"
                @dragover.prevent="handleSlotDragOver($event, slotIndex + 2)"
                @drop.prevent="handleSlotDrop($event, slotIndex + 2)"
              >
              <!-- 原位置的角色卡片（攻击时半透明） -->
              <div 
                v-if="getMinionAtPosition(slotIndex + 2)"
                class="character-card ally-card"
                :class="{ 
                  'hit-effect': hasHitEffect(getMinionAtPosition(slotIndex + 2)!.id),
                  'attacking': isAttacking(getMinionAtPosition(slotIndex + 2)!.id)
                }"
                :data-attacker-id="getMinionAtPosition(slotIndex + 2)!.id"
                @dragover.prevent
                @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(slotIndex + 2)!.id })"
              >
                <!-- 攻击时的克隆体（跳出方框，Teleport 到 body，避免任何裁剪/层级影响） -->
                <Teleport to="body">
                  <div 
                    v-if="isAttacking(getMinionAtPosition(slotIndex + 2)!.id)"
                    class="character-card-attack-clone"
                    :style="getAttackStyle(getMinionAtPosition(slotIndex + 2)!.id)"
                  >
                    <div class="character-header">
                      <div class="character-name">{{ getMinionAtPosition(slotIndex + 2)!.name }}</div>
                    </div>
                    <div class="character-avatar">
                      <span :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').color]">
                        {{ iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').emoji }}
                      </span>
                    </div>
                    <div class="character-stats">
                      <div class="stat-item attack-stat">
                        <span class="stat-icon">⚔️</span>
                        <span class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.attack }}</span>
                      </div>
                      <div class="stat-item hp-stat">
                        <span class="stat-icon">❤️</span>
                        <span class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.health }}</span>
                      </div>
                    </div>
                  </div>
                </Teleport>
                <!-- 我方受击伤害数字 -->
                <div 
                  v-if="hasHitEffect(getMinionAtPosition(slotIndex + 2)!.id)"
                  class="damage-number"
                  :key="getHitEffect(getMinionAtPosition(slotIndex + 2)!.id)?.timestamp"
                >
                  -{{ getHitEffect(getMinionAtPosition(slotIndex + 2)!.id)?.damage }}
                </div>
                <div class="character-header">
                  <div class="character-name">{{ getMinionAtPosition(slotIndex + 2)!.name }}</div>
                  <div class="header-badges">
                    <div v-if="getMinionAtPosition(slotIndex + 2)!.shield && getMinionAtPosition(slotIndex + 2)!.shield! > 0" class="shield-badge">
                      <span class="shield-icon">🛡️</span>
                      <span class="shield-value">{{ getMinionAtPosition(slotIndex + 2)!.shield }}</span>
                    </div>
                    <div v-if="getMinionAtPosition(slotIndex + 2)!.stars" class="star-badge">
                      <span class="star-icon">⭐</span>
                      <span class="star-value">{{ getMinionAtPosition(slotIndex + 2)!.stars }}</span>
                    </div>
                    <div v-if="getMinionAtPosition(slotIndex + 2)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                      <span class="status-icon">😴</span>
                    </div>
                  </div>
                </div>
                
                <div class="character-avatar">
                  <span :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').emoji }}
                  </span>
                </div>
                
                <div class="character-stats">
                  <div class="stat-item attack-stat">
                    <span class="stat-icon">⚔️</span>
                    <span class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.attack }}</span>
                  </div>
                  <div class="stat-item hp-stat">
                    <span class="stat-icon">❤️</span>
                    <span class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.health }}</span>
                  </div>
                </div>
                <!-- 装备圆形标记 -->
                <div v-if="getMinionAtPosition(slotIndex + 2)!.equipmentNames && getMinionAtPosition(slotIndex + 2)!.equipmentNames!.length" class="equipment-dots">
                  <span 
                    v-for="(eq, idx) in getMinionAtPosition(slotIndex + 2)!.equipmentNames" 
                    :key="idx" 
                    class="equipment-dot"
                    :title="`已装备：${eq}`"
                  ></span>
                </div>
              </div>
              <div v-else class="empty-slot">
                <span class="slot-hint">位置 {{ slotIndex + 3 }}</span>
              </div>
              </div>
            </div>
            <!-- 右列：位置 0, 1, 2 -->
            <div class="battle-column right-column">
              <div
                v-for="slotIndex in 3"
                :key="slotIndex - 1"
                class="battle-slot"
                :class="{ 'occupied': getMinionAtPosition(slotIndex - 1) }"
                @dragover.prevent="handleSlotDragOver($event, slotIndex - 1)"
                @drop.prevent="handleSlotDrop($event, slotIndex - 1)"
              >
              <!-- 原位置的角色卡片（攻击时半透明） -->
              <div 
                v-if="getMinionAtPosition(slotIndex - 1)"
                class="character-card ally-card"
                :class="{ 
                  'hit-effect': hasHitEffect(getMinionAtPosition(slotIndex - 1)!.id),
                  'attacking': isAttacking(getMinionAtPosition(slotIndex - 1)!.id)
                }"
                :data-attacker-id="getMinionAtPosition(slotIndex - 1)!.id"
                @dragover.prevent
                @drop.prevent="emit('equip-to-minion', { minionId: getMinionAtPosition(slotIndex - 1)!.id })"
              >
                <!-- 攻击时的克隆体（Teleport 到 body，避免裁剪/层级问题） -->
                <Teleport to="body">
                  <div 
                    v-if="isAttacking(getMinionAtPosition(slotIndex - 1)!.id)"
                    class="character-card-attack-clone"
                    :style="getAttackStyle(getMinionAtPosition(slotIndex - 1)!.id)"
                  >
                    <div class="character-header">
                      <div class="character-name">{{ getMinionAtPosition(slotIndex - 1)!.name }}</div>
                    </div>
                    <div class="character-avatar">
                      <span :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').color]">
                        {{ iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').emoji }}
                      </span>
                    </div>
                    <div class="character-stats">
                      <div class="stat-item attack-stat">
                        <span class="stat-icon">⚔️</span>
                        <span class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.attack }}</span>
                      </div>
                      <div class="stat-item hp-stat">
                        <span class="stat-icon">❤️</span>
                        <span class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.health }}</span>
                      </div>
                    </div>
                  </div>
                </Teleport>
                <!-- 我方受击伤害数字 -->
                <div 
                  v-if="hasHitEffect(getMinionAtPosition(slotIndex - 1)!.id)"
                  class="damage-number"
                  :key="getHitEffect(getMinionAtPosition(slotIndex - 1)!.id)?.timestamp"
                >
                  -{{ getHitEffect(getMinionAtPosition(slotIndex - 1)!.id)?.damage }}
                </div>
                <div class="character-header">
                  <div class="character-name">{{ getMinionAtPosition(slotIndex - 1)!.name }}</div>
                  <div class="header-badges">
                    <div v-if="getMinionAtPosition(slotIndex - 1)!.shield && getMinionAtPosition(slotIndex - 1)!.shield! > 0" class="shield-badge">
                      <span class="shield-icon">🛡️</span>
                      <span class="shield-value">{{ getMinionAtPosition(slotIndex - 1)!.shield }}</span>
                    </div>
                    <div v-if="getMinionAtPosition(slotIndex - 1)!.stars" class="star-badge">
                      <span class="star-icon">⭐</span>
                      <span class="star-value">{{ getMinionAtPosition(slotIndex - 1)!.stars }}</span>
                    </div>
                    <div v-if="getMinionAtPosition(slotIndex - 1)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                      <span class="status-icon">😴</span>
                    </div>
                  </div>
                </div>
                
                <div class="character-avatar">
                  <span :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').emoji }}
                  </span>
                </div>
                
                <div class="character-stats">
                  <div class="stat-item attack-stat">
                    <span class="stat-icon">⚔️</span>
                    <span class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.attack }}</span>
                  </div>
                  <div class="stat-item hp-stat">
                    <span class="stat-icon">❤️</span>
                    <span class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.health }}</span>
                  </div>
                </div>
                <!-- 装备圆形标记 -->
                <div v-if="getMinionAtPosition(slotIndex - 1)!.equipmentNames && getMinionAtPosition(slotIndex - 1)!.equipmentNames!.length" class="equipment-dots">
                  <span 
                    v-for="(eq, idx) in getMinionAtPosition(slotIndex - 1)!.equipmentNames" 
                    :key="idx" 
                    class="equipment-dot"
                    :title="`已装备：${eq}`"
                  ></span>
                </div>
              </div>
              <div v-else class="empty-slot">
                <span class="slot-hint">位置 {{ slotIndex }}</span>
              </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中央分割 -->
      <div class="battle-divider vertical">
        <div class="divider-line vertical"></div>
        <div class="divider-icon">⚔️</div>
        <div class="divider-line vertical"></div>
      </div>

      <!-- 敌方区域（右） -->
      <div class="enemy-zone" :class="{ 'enemy-turn-active': turn === 'opponent' }">
        <!-- 中间对战区 -->
        <div class="battle-area">
          <div class="battle-slots">
            <!-- 左列：位置 0, 1, 2 -->
            <div class="battle-column left-column">
              <div
                v-for="slotIndex in 3"
                :key="slotIndex - 1"
                class="battle-slot"
                :class="{ 'occupied': getEnemyMinionAtPosition(slotIndex - 1) }"
              >
                <div 
                  v-if="getEnemyMinionAtPosition(slotIndex - 1)"
                  class="character-card enemy-card"
                  :class="{ 
                    'hit-effect': hasHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id),
                    'attack-target': Array.from(attackingMinions.values()).some(a => a.targetId === getEnemyMinionAtPosition(slotIndex - 1)!.id)
                  }"
                  :data-minion-id="getEnemyMinionAtPosition(slotIndex - 1)!.id"
                >
                  <!-- 受击伤害数字 -->
                  <div 
                    v-if="hasHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id)"
                    class="damage-number enemy-damage"
                    :key="getHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id)?.timestamp"
                  >
                    -{{ getHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id)?.damage }}
                  </div>
                  <div class="character-header">
                    <div class="character-name">{{ getEnemyMinionAtPosition(slotIndex - 1)!.name }}</div>
                    <div class="header-badges">
                      <div v-if="getEnemyMinionAtPosition(slotIndex - 1)!.shield && getEnemyMinionAtPosition(slotIndex - 1)!.shield! > 0" class="shield-badge">
                        <span class="shield-icon">🛡️</span>
                        <span class="shield-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.shield }}</span>
                      </div>
                      <div v-if="getEnemyMinionAtPosition(slotIndex - 1)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                        <span class="status-icon">😴</span>
                        <span class="status-text">准备中</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="character-avatar">
                    <span :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(slotIndex - 1)!.name, 'enemy').color]">
                      {{ iconFor(getEnemyMinionAtPosition(slotIndex - 1)!.name, 'enemy').emoji }}
                    </span>
                  </div>
                  
                  <div class="character-stats">
                    <div class="stat-item attack-stat">
                      <span class="stat-icon">⚔️</span>
                      <span class="stat-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.attack }}</span>
                    </div>
                    <div class="stat-item hp-stat">
                      <span class="stat-icon">❤️</span>
                      <span class="stat-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.health }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-slot">
                  <span class="slot-hint">位置 {{ slotIndex }}</span>
                </div>
              </div>
            </div>
            <!-- 右列：位置 3, 4, 5 -->
            <div class="battle-column right-column">
              <div
                v-for="slotIndex in 3"
                :key="slotIndex + 2"
                class="battle-slot"
                :class="{ 'occupied': getEnemyMinionAtPosition(slotIndex + 2) }"
              >
                <div 
                  v-if="getEnemyMinionAtPosition(slotIndex + 2)"
                  class="character-card enemy-card"
                  :class="{ 
                    'hit-effect': hasHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id),
                    'attack-target': Array.from(attackingMinions.values()).some(a => a.targetId === getEnemyMinionAtPosition(slotIndex + 2)!.id)
                  }"
                  :data-minion-id="getEnemyMinionAtPosition(slotIndex + 2)!.id"
                >
                  <!-- 受击伤害数字 -->
                  <div 
                    v-if="hasHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id)"
                    class="damage-number enemy-damage"
                    :key="getHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id)?.timestamp"
                  >
                    -{{ getHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id)?.damage }}
                  </div>
                  <div class="character-header">
                    <div class="character-name">{{ getEnemyMinionAtPosition(slotIndex + 2)!.name }}</div>
                    <div class="header-badges">
                      <div v-if="getEnemyMinionAtPosition(slotIndex + 2)!.shield && getEnemyMinionAtPosition(slotIndex + 2)!.shield! > 0" class="shield-badge">
                        <span class="shield-icon">🛡️</span>
                        <span class="shield-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.shield }}</span>
                      </div>
                      <div v-if="getEnemyMinionAtPosition(slotIndex + 2)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                        <span class="status-icon">😴</span>
                        <span class="status-text">准备中</span>
                      </div>
                    </div>
                  </div>
                  
                  <div class="character-avatar">
                    <span :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(slotIndex + 2)!.name, 'enemy').color]">
                      {{ iconFor(getEnemyMinionAtPosition(slotIndex + 2)!.name, 'enemy').emoji }}
                    </span>
                  </div>
                  
                  <div class="character-stats">
                    <div class="stat-item attack-stat">
                      <span class="stat-icon">⚔️</span>
                      <span class="stat-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.attack }}</span>
                    </div>
                    <div class="stat-item hp-stat">
                      <span class="stat-icon">❤️</span>
                      <span class="stat-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.health }}</span>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-slot">
                  <span class="slot-hint">位置 {{ slotIndex + 3 }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 分隔线 -->
        <div class="info-divider"></div>
        
        <!-- 右侧竖排信息区 -->
        <div class="vertical-info-panel enemy-panel">
          <div class="vertical-info-content">
            <!-- 圆形头像框 -->
            <div class="player-avatar-container" :class="{ 'hit-effect': enemyBossHitEffect }">
              <div class="vertical-name-text">{{ enemyCharacterName }}</div>
              <div class="player-avatar-circle enemy-avatar">
                <span :class="['player-avatar-icon', enemyCharacterIcon.color]">
                  {{ enemyCharacterIcon.emoji }}
                </span>
              </div>
              <!-- 敌人本体受击伤害数字 -->
              <div v-if="enemyBossHitEffect" class="damage-number enemy-boss-damage">
                -{{ enemyBossHitEffect.damage }}
              </div>
            </div>
            <div class="hp-display enemy-hp horizontal">
              <div class="hp-bar-container horizontal">
                <div class="hp-bar-bg horizontal">
                  <div 
                    class="hp-bar-fill"
                    :class="getHPColorClass(enemyHPPercent)"
                    :style="{ width: enemyHPPercent + '%' }"
                  ></div>
                </div>
              </div>
              <div class="hp-text horizontal">
                <span class="hp-value">{{ enemyHP }}</span>
                <span class="hp-separator">/</span>
                <span class="hp-max">{{ enemyHPMax }}</span>
              </div>
            </div>
            <div v-if="enemyPanel" class="enemy-panel-info horizontal">
              <div class="panel-stat">
                <span class="panel-stat-icon">⚔️</span>
                <span class="panel-stat-value">{{ enemyPanel.attack ?? 0 }}</span>
              </div>
              <div v-if="enemyPanel.armor" class="panel-stat">
                <span class="panel-stat-icon">🛡️</span>
                <span class="panel-stat-value">{{ enemyPanel.armor }}</span>
              </div>
              <div v-if="enemyPanel.difficulty" class="enemy-difficulty-badge" :class="`difficulty-${enemyPanel.difficulty}`">
                {{ enemyPanel.difficulty === 'normal' ? '普通' : enemyPanel.difficulty === 'hard' ? '困难' : enemyPanel.difficulty === 'boss' ? '首领' : enemyPanel.difficulty }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：消息日志 -->
    <div class="battle-log">
      <div class="log-header">
        <span class="log-icon">📜</span>
        <span class="log-title">战斗日志</span>
        <span class="log-count">({{ logs.length }})</span>
      </div>
      <div class="log-content">
        <div 
          v-for="(l, i) in logs" 
          :key="i" 
          class="log-entry"
          :style="{ animationDelay: `${i * 0.05}s` }"
        >
          {{ l }}
        </div>
        <div v-if="logs.length === 0" class="log-empty">
          <span class="empty-icon">📝</span>
          <span class="empty-text">尚无消息</span>
        </div>
      </div>
    </div>
  </main>
</template>

<style scoped>
.battle-field {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 20px;
  padding: 20px;
  position: relative;
  z-index: 1;
  overflow: visible;
}

/* 战斗区域：左右对称，中间分隔 */
.battle-arena {
  display: grid;
  grid-template-columns: 1fr 48px 1fr;
  gap: 16px;
  align-items: stretch;
  height: 100%;
  overflow: visible;
}

/* 区域通用样式 */
.enemy-zone, .ally-zone {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s ease;
  flex: 1;
  min-height: 0;
  position: relative;
  display: flex;
  gap: 12px;
  overflow: hidden;
}

.enemy-zone {
  border-color: rgba(239, 68, 68, 0.3);
}

.enemy-zone.enemy-turn-active {
  border-color: rgba(239, 68, 68, 0.8);
  border-width: 3px;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4), inset 0 0 30px rgba(239, 68, 68, 0.1);
  animation: zone-highlight 2s ease-in-out infinite;
}

.ally-zone {
  border-color: rgba(59, 130, 246, 0.3);
}

.ally-zone.ally-turn-active {
  border-color: rgba(59, 130, 246, 0.8);
  border-width: 3px;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.4), inset 0 0 30px rgba(59, 130, 246, 0.1);
  animation: zone-highlight 2s ease-in-out infinite;
}

@keyframes zone-highlight {
  0%, 100% {
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.4), inset 0 0 30px rgba(59, 130, 246, 0.1);
  }
  50% {
    box-shadow: 0 0 30px rgba(59, 130, 246, 0.6), inset 0 0 40px rgba(59, 130, 246, 0.2);
  }
}

/* 移除旧的 zone-header 和 info-row 样式，因为已经移到竖排区域 */

.stats {
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 竖排信息面板 */
.vertical-info-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 110px;
  max-width: 130px;
  width: 110px;
  padding: 10px 6px;
}

.vertical-name-text {
  font-weight: 700;
  font-size: 0.9rem;
  letter-spacing: 0.5px;
  color: #e2e8f0;
  padding: 0;
  white-space: nowrap;
  text-align: center;
  width: 100%;
  margin-bottom: 6px;
}

.vertical-info-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex: 1;
  width: 100%;
  box-sizing: border-box;
  padding: 0;
  overflow: hidden;
}

/* 玩家头像容器 */
.player-avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 8px;
}

.player-avatar-circle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.3), rgba(139, 92, 246, 0.3));
  border: 3px solid rgba(59, 130, 246, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: all 0.3s ease;
}

.player-avatar-circle:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

/* 敌方头像框 */
.player-avatar-circle.enemy-avatar {
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.3), rgba(220, 38, 38, 0.3));
  border: 3px solid rgba(239, 68, 68, 0.5);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.player-avatar-circle.enemy-avatar:hover {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
}

.player-avatar-icon {
  font-size: 2rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}


/* 分隔线（不到两头） */
.info-divider {
  width: 1px;
  height: 100%;
  background: linear-gradient(
    to bottom,
    transparent 20px,
    rgba(148, 163, 184, 0.4) 20px,
    rgba(148, 163, 184, 0.4) calc(100% - 20px),
    transparent calc(100% - 20px)
  );
  flex-shrink: 0;
  margin: 0 8px;
}

/* 竖排布局的HP条 */
.hp-bar-container.vertical {
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.hp-bar-bg.vertical {
  width: 20px;
  height: 120px;
  flex: none;
  border-radius: 10px;
}

.hp-bar-container.vertical .hp-bar-bg {
  position: relative;
  display: flex;
  align-items: flex-end;
}

.hp-bar-container.vertical .hp-bar-fill {
  width: 100%;
  border-radius: 10px;
  transition: height 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.hp-bar-container.vertical .hp-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(180deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer-vertical 2s infinite;
}

@keyframes shimmer-vertical {
  0% { transform: translateY(-100%); }
  100% { transform: translateY(100%); }
}

.hp-text.vertical {
  flex-direction: column;
  gap: 2px;
  font-size: 0.75rem;
  min-width: auto;
  text-align: center;
}

/* 横排布局的HP条 */
.hp-display.horizontal {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 100%;
  max-width: 100%;
  padding: 0;
  margin: 0;
  box-sizing: border-box;
  overflow: hidden;
}

.hp-bar-container.horizontal {
  width: 100%;
  max-width: 100%;
  display: flex;
  align-items: center;
  box-sizing: border-box;
  overflow: hidden;
}

.hp-bar-bg.horizontal {
  width: 100%;
  max-width: 100%;
  height: 14px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 7px;
  overflow: hidden;
  position: relative;
  box-sizing: border-box;
  flex-shrink: 0;
}

.hp-bar-container.horizontal .hp-bar-fill {
  height: 100%;
  width: 100%;
  border-radius: 8px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.hp-bar-container.horizontal .hp-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer-horizontal 2s infinite;
}

@keyframes shimmer-horizontal {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.hp-text.horizontal {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 2px;
  font-size: 0.7rem;
  font-weight: 700;
  text-align: center;
  white-space: nowrap;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

/* 竖排布局的统计信息 */
.stats.vertical {
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.stat-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  font-size: 0.7rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  width: 100%;
  box-sizing: border-box;
}

/* 竖排布局的敌人面板信息 */
.enemy-panel-info.vertical {
  flex-direction: column;
  gap: 8px;
  width: 100%;
  padding: 8px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
}

.panel-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  font-size: 0.75rem;
}

.panel-stat-icon {
  font-size: 1rem;
}

.panel-stat-label {
  color: #94a3b8;
  font-weight: 500;
  font-size: 0.65rem;
}

.panel-stat-value {
  color: #e2e8f0;
  font-weight: 700;
  font-size: 0.875rem;
}

/* 对战区域 */
.battle-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  justify-content: center;
  align-items: center;
  padding: 8px 6px; /* 缩小上下内边距 */
  overflow: hidden;
}

.stat-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  font-size: 0.75rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.stat-badge.exhausted {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
}

.stat-icon {
  font-size: 0.875rem;
}

.stat-value {
  font-weight: 600;
  color: #e8e8e8;
}

.zone-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.125rem;
  font-weight: 700;
  color: #e2e8f0;
  flex-wrap: wrap;
}

.title-icon {
  font-size: 1.5rem;
}

.enemy-difficulty-badge {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: 600;
  margin-left: 8px;
}

.enemy-difficulty-badge.difficulty-normal {
  background: rgba(148, 163, 184, 0.2);
  color: #cbd5e1;
  border: 1px solid rgba(148, 163, 184, 0.4);
}

.enemy-difficulty-badge.difficulty-hard {
  background: rgba(251, 191, 36, 0.2);
  color: #fcd34d;
  border: 1px solid rgba(251, 191, 36, 0.4);
}

.enemy-difficulty-badge.difficulty-boss {
  background: rgba(239, 68, 68, 0.2);
  color: #f87171;
  border: 1px solid rgba(239, 68, 68, 0.4);
}

.enemy-panel-info {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 8px;
  padding: 8px 12px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 8px;
  flex-wrap: wrap;
}

.panel-stat {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 0.875rem;
}

.panel-stat-icon {
  font-size: 1rem;
}

.panel-stat-label {
  color: #94a3b8;
  font-weight: 500;
}

.panel-stat-value {
  color: #e2e8f0;
  font-weight: 700;
  min-width: 24px;
  text-align: right;
}

.title-text {
  font-size: 1.125rem;
}

/* HP显示 - 仅用于非horizontal的情况 */
.hp-display:not(.horizontal) {
  min-width: 200px;
}

.hp-bar-container {
  position: relative;
  display: flex;
  align-items: center;
  gap: 12px;
}

.hp-bar-bg {
  flex: 1;
  height: 24px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

/* HP条填充样式已在竖排区域定义 */

/* 横排 HP 条渐变 */
.hp-bar-container.horizontal .hp-bar-fill.hp-healthy {
  background: linear-gradient(90deg, #10b981, #059669);
}

.hp-bar-container.horizontal .hp-bar-fill.hp-warning {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.hp-bar-container.horizontal .hp-bar-fill.hp-danger {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* 竖排 HP 条渐变 */
.hp-bar-container.vertical .hp-bar-fill.hp-healthy {
  background: linear-gradient(180deg, #10b981, #059669);
}

.hp-bar-container.vertical .hp-bar-fill.hp-warning {
  background: linear-gradient(180deg, #f59e0b, #d97706);
}

.hp-bar-container.vertical .hp-bar-fill.hp-danger {
  background: linear-gradient(180deg, #ef4444, #dc2626);
}

.hp-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.875rem;
  font-weight: 700;
  min-width: 60px;
  text-align: right;
}

.hp-value {
  color: #e2e8f0;
}

.hp-separator {
  color: #94a3b8;
}

.hp-max {
  color: #94a3b8;
}

.enemy-hp .hp-value {
  color: #f87171;
}

.ally-hp .hp-value {
  color: #60a5fa;
}


/* 角色网格 */
.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}

/* 固定位置槽 */
.battle-slots {
  display: flex;
  flex-direction: row;
  gap: 12px;
  width: 100%;
  max-width: 100%;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-height: 0;
  flex-wrap: wrap;
}

.battle-column {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: center;
  justify-content: center;
  min-width: 0;
  min-height: 0;
}

/* 左列和右列样式已通过 battle-column 统一设置 */

.battle-slot {
  width: 150px;
  height: 150px;
  min-width: 150px;
  min-height: 150px;
  border: 2px dashed rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  position: relative;
  overflow: visible;
  box-sizing: border-box;
  flex-shrink: 0;
}

.battle-slot.occupied {
  border: 2px solid rgba(148, 163, 184, 0.5);
  padding: 0;
}

.battle-slot:not(.occupied):hover {
  border-color: rgba(59, 130, 246, 0.6);
  background: rgba(59, 130, 246, 0.1);
}

.empty-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: rgba(148, 163, 184, 0.5);
  font-size: 0.75rem;
}

.slot-hint {
  opacity: 0.6;
}

.character-card {
  background: rgba(15, 23, 42, 0.6);
  border: 2px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  width: 150px;
  height: 150px;
  min-width: 150px;
  box-sizing: border-box;
  flex-shrink: 0;
  z-index: 10;
}

.equipment-dots {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
}

.equipment-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  border: 2px solid rgba(248, 250, 252, 0.9);
  background: radial-gradient(circle at 30% 30%, #fbbf24, #92400e);
  box-shadow: 0 0 6px rgba(251, 191, 36, 0.8);
}

.character-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s;
  pointer-events: none;
  /* 限制滑动光效只在卡牌内部，避免超出边界 */
  clip-path: inset(0 round 12px);
}

.character-card:hover::before {
  left: 100%;
}

.character-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.enemy-card {
  border-color: rgba(239, 68, 68, 0.4);
}

.enemy-card:hover {
  border-color: rgba(239, 68, 68, 0.6);
  box-shadow: 0 8px 24px rgba(239, 68, 68, 0.2);
}

.ally-card {
  border-color: rgba(59, 130, 246, 0.4);
}

.ally-card:hover {
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.2);
}

.character-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  flex-wrap: wrap;
}

.header-badges {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}

.character-name {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #e2e8f0;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.3;
  min-height: 2.6em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}

.shield-badge, .star-badge, .status-badge {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 2px 6px;
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  border-radius: 6px;
  font-size: 0.6875rem;
}

.shield-badge {
  background: rgba(139, 92, 246, 0.2);
  border-color: rgba(139, 92, 246, 0.4);
}

.status-badge.summoning-sickness {
  background: rgba(55, 65, 81, 0.35); /* 更暗的灰色，表示准备中 */
  border-color: rgba(55, 65, 81, 0.55);
  color: #9ca3af;
  opacity: 0.9;
}

.shield-icon, .star-icon, .status-icon {
  font-size: 0.75rem;
}

.shield-value, .star-value, .status-text {
  font-weight: 700;
  color: #e2e8f0;
}

.status-text {
  font-size: 0.625rem;
}

.character-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  margin: 4px 0;
  flex-shrink: 0;
}

.avatar-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
}

.character-stats {
  display: flex;
  justify-content: space-between;
  gap: 4px;
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.attack-stat {
  color: #fbbf24;
}

.hp-stat {
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-icon {
  font-size: 0.75rem;
}

.stat-value {
  font-size: 0.8125rem;
}

/* 空区域 */
.empty-zone {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px;
  color: #64748b;
  text-align: center;
}

.empty-icon {
  font-size: 2rem;
  opacity: 0.5;
}

.empty-text {
  font-size: 0.875rem;
  font-weight: 600;
}

.empty-hint {
  font-size: 0.75rem;
  opacity: 0.7;
}

/* 战斗分隔线 */
.battle-divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 8px;
}

.battle-divider.vertical {
  flex-direction: column;
  justify-content: center;
  min-width: 48px;
}

.divider-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.3), transparent);
}

.divider-line.vertical {
  width: 2px;
  height: 100%;
  min-height: 200px;
  background: linear-gradient(180deg, transparent, rgba(148, 163, 184, 0.3), transparent);
}

.divider-icon {
  font-size: 1.5rem;
  opacity: 0.6;
  animation: rotate 4s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 战斗日志 */
.battle-log {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 200px);
}

.log-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
}

.log-icon {
  font-size: 1.25rem;
}

.log-title {
  font-size: 1rem;
  font-weight: 700;
  color: #e2e8f0;
}

.log-count {
  font-size: 0.75rem;
  color: #94a3b8;
  margin-left: auto;
}

.log-content {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.3) transparent;
}

.log-content::-webkit-scrollbar {
  width: 6px;
}

.log-content::-webkit-scrollbar-track {
  background: transparent;
}

.log-content::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}

.log-content::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}

.log-entry {
  font-size: 0.8125rem;
  color: #cbd5e1;
  padding: 6px 10px;
  background: rgba(15, 23, 42, 0.4);
  border-radius: 6px;
  line-height: 1.5;
  animation: slide-in 0.3s ease;
}

@keyframes slide-in {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.log-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 32px;
  color: #64748b;
}

.log-empty .empty-icon {
  font-size: 2rem;
  opacity: 0.5;
}

.log-empty .empty-text {
  font-size: 0.875rem;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .battle-field {
    grid-template-columns: 1fr 280px;
  }
}

@media (max-width: 992px) {
  .battle-field {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .battle-log {
    max-height: 300px;
  }
}

@media (max-width: 768px) {
  .battle-field {
    padding: 12px;
  }

  .characters-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 12px;
  }

  .hp-display:not(.horizontal) {
    min-width: 150px;
  }

  .hp-text {
    min-width: 50px;
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .characters-grid {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .character-card {
    padding: 8px;
  }

  .avatar-icon {
    font-size: 2rem;
  }

  .hp-display:not(.horizontal) {
    min-width: 120px;
  }
}

/* 受击反馈效果 */
.hit-effect {
  animation: hit-flash 0.5s ease;
}

@keyframes hit-flash {
  0% {
    transform: scale(1);
    filter: brightness(1);
  }
  15% {
    transform: scale(1.05);
    filter: brightness(1.5);
  }
  30% {
    transform: scale(0.98);
    filter: brightness(0.8);
  }
  45% {
    transform: scale(1.02);
    filter: brightness(1.3);
  }
  60% {
    transform: scale(0.99);
    filter: brightness(0.9);
  }
  100% {
    transform: scale(1);
    filter: brightness(1);
  }
}

/* 伤害数字显示 */
.damage-number {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.5rem;
  font-weight: 700;
  color: #ef4444;
  text-shadow: 
    0 0 10px rgba(239, 68, 68, 0.8),
    0 2px 4px rgba(0, 0, 0, 0.8);
  z-index: 1000;
  pointer-events: none;
  animation: damage-float 1s ease-out forwards;
  user-select: none;
}

.enemy-damage {
  color: #fbbf24;
  text-shadow: 
    0 0 10px rgba(251, 191, 36, 0.8),
    0 2px 4px rgba(0, 0, 0, 0.8);
}

.player-damage {
  color: #ef4444;
  font-size: 1.25rem;
}

.enemy-boss-damage {
  color: #fbbf24;
  font-size: 1.25rem;
}

@keyframes damage-float {
  0% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -80%) scale(1.2);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -120%) scale(0.9);
  }
}

/* 玩家头像受击效果 */
.player-avatar-container.hit-effect {
  animation: avatar-hit-shake 0.5s ease;
}

@keyframes avatar-hit-shake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-3px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(3px);
  }
}

/* 攻击动画效果 - 原卡片半透明 */
.character-card.attacking {
  opacity: 0.3;
  pointer-events: none;
}

/* 攻击克隆体 - 跳出方框攻击 */
.character-card-attack-clone {
  position: fixed;
  width: 120px;   /* 缩小克隆体尺寸，减小覆盖面积 */
  height: 160px;
  min-width: 120px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.95));
  border: 2px solid rgba(59, 130, 246, 0.8);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  /* 置于最顶层，避免被任何战场层级覆盖 */
  z-index: 2147483647 !important; /* 接近浏览器上限 */
  pointer-events: none;
  animation: attack-move-clone 0.9s ease-in-out forwards; /* 稍加速，贴合新高度 */
  transform-origin: center center;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.6), 0 0 20px rgba(59, 130, 246, 0.4);
  will-change: transform, left, top;
  margin-left: -70px; /* 减去卡片宽度的一半，使中心对齐 */
  margin-top: -90px; /* 减去卡片高度的一半，使中心对齐 */
}

.character-card-attack-clone .character-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 4px;
  flex-wrap: wrap;
}

.character-card-attack-clone .character-name {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #e2e8f0;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.character-card-attack-clone .character-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  margin: 4px 0;
  flex-shrink: 0;
}

.character-card-attack-clone .avatar-icon {
  font-size: 1.8rem;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
}

.character-card-attack-clone .character-stats {
  display: flex;
  justify-content: space-between;
  gap: 4px;
  flex-shrink: 0;
}

.character-card-attack-clone .stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  font-weight: 600;
}

.character-card-attack-clone .attack-stat {
  color: #fbbf24;
}

.character-card-attack-clone .hp-stat {
  color: #10b981;
  display: flex;
  align-items: center;
  gap: 4px;
}

.character-card-attack-clone .stat-icon {
  font-size: 0.75rem;
}

.character-card-attack-clone .stat-value {
  font-size: 0.8125rem;
}

/* 允许攻击克隆体/伤害数字跨越容器，不被裁剪 */
.battle-field,
.battle-arena,
.ally-zone,
.enemy-zone,
.battle-area,
.battle-slots,
.battle-slot {
  overflow: visible !important;
  position: relative;
}

/* 卡牌本体保持裁剪，限制内部光效不出界（克隆体已 Teleport，不受此限制） */
.character-card {
  overflow: hidden;
}

@keyframes attack-move-clone {
  0% {
    left: var(--start-x, 200px);
    top: var(--start-y, 400px);
    transform: translate(0, 0) scale(1);
    opacity: 1;
  }
  40% {
    /* 移动到目标位置 */
    left: var(--target-x, 800px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.1);
    opacity: 1;
  }
  50% {
    /* 攻击动作 - 向前突进 */
    left: calc(var(--target-x, 800px) + 15px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.3) rotate(12deg);
    opacity: 1;
  }
  60% {
    /* 攻击后稍微后退 */
    left: var(--target-x, 800px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.1);
    opacity: 1;
  }
  100% {
    /* 返回原位置并淡出 */
    left: var(--start-x, 200px);
    top: var(--start-y, 400px);
    transform: translate(0, 0) scale(0.9);
    opacity: 0;
  }
}

@keyframes attack-glow {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1.2);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(1.5);
  }
}

/* 攻击目标高亮 */
.character-card.attack-target {
  animation: target-flash 0.3s ease-out;
  box-shadow: 0 0 20px rgba(251, 191, 36, 0.6);
}

@keyframes target-flash {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.5);
  }
}

/* 敌人本体受击目标 */
.player-avatar-container.hit-effect[data-is-boss="true"] {
  animation: avatar-hit-shake 0.5s ease, boss-target-flash 0.3s ease-out;
}

@keyframes boss-target-flash {
  0%, 100% {
    filter: brightness(1);
  }
  50% {
    filter: brightness(1.3);
  }
}
</style>

