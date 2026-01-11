<template>
  <main class="battle-field">
    <!-- 上下：战斗区域 -->
    <view class="battle-arena">
      <!-- 敌人面板区域 -->
      <view class="enemy-panel-zone" :class="{ 'enemy-turn-active': turn === 'opponent' }">
        <view class="player-avatar-container enemy-avatar-container" :class="{ 'hit-effect': enemyBossHitEffect }" :data-attacker-id="'enemy-boss'">
          <view class="vertical-name-text">{{ enemyCharacterName }}</view>
          <view class="player-avatar-circle enemy-avatar">
            <text :class="['player-avatar-icon', enemyCharacterIcon.color]">
              {{ enemyCharacterIcon.emoji }}
            </text>
          </view>
          <view v-if="enemyBossHitEffect" class="damage-number enemy-boss-damage">
            -{{ enemyBossHitEffect.damage }}
          </view>
        </view>
        <view class="hp-display enemy-hp horizontal">
          <view class="hp-bar-container horizontal">
            <view class="hp-bar-bg horizontal">
              <view class="hp-bar-fill" :class="getHPColorClass(enemyHPPercent)" :style="{ width: enemyHPPercent + '%' }"></view>
            </view>
          </view>
          <view class="hp-text horizontal">
            <text class="hp-value">{{ enemyHP }}</text>
            <text class="hp-separator">/</text>
            <text class="hp-max">{{ enemyHPMax }}</text>
          </view>
        </view>
        <view v-if="enemyPanel" class="enemy-panel-info horizontal">
          <view class="panel-stat">
            <text class="panel-stat-icon">⚔️</text>
            <text class="panel-stat-value">{{ enemyPanel.attack ?? 0 }}</text>
          </view>
          <view v-if="enemyPanel.armor" class="panel-stat">
            <text class="panel-stat-icon">🛡️</text>
            <text class="panel-stat-value">{{ enemyPanel.armor }}</text>
          </view>
          <view v-if="enemyPanel.difficulty" class="enemy-difficulty-badge" :class="`difficulty-${enemyPanel.difficulty}`">
            {{ enemyPanel.difficulty === 'normal' ? '普通' : enemyPanel.difficulty === 'hard' ? '困难' : enemyPanel.difficulty === 'boss' ? '首领' : enemyPanel.difficulty }}
          </view>
        </view>
      </view>

      <!-- 敌人对战区域 -->
      <view class="enemy-battle-zone">
        <view class="battle-slots enemy-slots">
          <!-- 两行三列：前排（靠近中间）位置0,1,2，后排位置3,4,5 -->
          <view class="battle-row front-row">
            <view
              v-for="slotIndex in 3"
              :key="slotIndex - 1"
              class="battle-slot enemy-slot"
              :class="{ 'occupied': getEnemyMinionAtPosition(slotIndex - 1) }"
            >
              <view
                v-if="getEnemyMinionAtPosition(slotIndex - 1)"
                class="character-card enemy-card"
                :class="{
                  'hit-effect': hasHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id),
                  'attack-target': Array.from(attackingMinions.values()).some(a => a.targetId === getEnemyMinionAtPosition(slotIndex - 1)!.id),
                  'attacking': isAttacking(getEnemyMinionAtPosition(slotIndex - 1)!.id)
                }"
                :data-minion-id="getEnemyMinionAtPosition(slotIndex - 1)!.id"
                :data-attacker-id="getEnemyMinionAtPosition(slotIndex - 1)!.id"
              >
                <view
                  v-if="isAttacking(getEnemyMinionAtPosition(slotIndex - 1)!.id)"
                  class="character-card-attack-clone enemy-attack-clone"
                  :style="getAttackStyle(getEnemyMinionAtPosition(slotIndex - 1)!.id)"
                >
                  <view class="character-header">
                    <view class="character-name">{{ getEnemyMinionAtPosition(slotIndex - 1)!.name }}</view>
                  </view>
                  <view class="character-avatar">
                    <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(slotIndex - 1)!.name, 'enemy').color]">
                      {{ iconFor(getEnemyMinionAtPosition(slotIndex - 1)!.name, 'enemy').emoji }}
                    </text>
                  </view>
                  <view class="character-stats">
                    <view class="stat-item attack-stat">
                      <text class="stat-icon">⚔️</text>
                      <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.attack }}</text>
                    </view>
                    <view class="stat-item hp-stat">
                      <text class="stat-icon">❤️</text>
                      <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.health }}</text>
                    </view>
                  </view>
                </view>
                <view
                  v-if="hasHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id)"
                  class="damage-number enemy-damage"
                  :key="getHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id)?.timestamp"
                >
                  -{{ getHitEffect(getEnemyMinionAtPosition(slotIndex - 1)!.id)?.damage }}
                </view>
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(slotIndex - 1)!.name }}</view>
                  <view class="header-badges">
                    <view v-if="getEnemyMinionAtPosition(slotIndex - 1)!.shield && getEnemyMinionAtPosition(slotIndex - 1)!.shield! > 0" class="shield-badge">
                      <text class="shield-icon">🛡️</text>
                      <text class="shield-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.shield }}</text>
                    </view>
                    <view v-if="getEnemyMinionAtPosition(slotIndex - 1)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                      <text class="status-icon">😴</text>
                    </view>
                  </view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(slotIndex - 1)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(slotIndex - 1)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex - 1)!.health }}</text>
                  </view>
                </view>
              </view>
              <view v-else class="empty-slot">
                <text class="slot-hint">{{ slotIndex }}</text>
              </view>
            </view>
          </view>
          <view class="battle-row back-row">
            <view
              v-for="slotIndex in 3"
              :key="slotIndex + 2"
              class="battle-slot enemy-slot"
              :class="{ 'occupied': getEnemyMinionAtPosition(slotIndex + 2) }"
            >
              <view
                v-if="getEnemyMinionAtPosition(slotIndex + 2)"
                class="character-card enemy-card"
                :class="{
                  'hit-effect': hasHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id),
                  'attack-target': Array.from(attackingMinions.values()).some(a => a.targetId === getEnemyMinionAtPosition(slotIndex + 2)!.id),
                  'attacking': isAttacking(getEnemyMinionAtPosition(slotIndex + 2)!.id)
                }"
                :data-minion-id="getEnemyMinionAtPosition(slotIndex + 2)!.id"
                :data-attacker-id="getEnemyMinionAtPosition(slotIndex + 2)!.id"
              >
                <view
                  v-if="isAttacking(getEnemyMinionAtPosition(slotIndex + 2)!.id)"
                  class="character-card-attack-clone enemy-attack-clone"
                  :style="getAttackStyle(getEnemyMinionAtPosition(slotIndex + 2)!.id)"
                >
                  <view class="character-header">
                    <view class="character-name">{{ getEnemyMinionAtPosition(slotIndex + 2)!.name }}</view>
                  </view>
                  <view class="character-avatar">
                    <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(slotIndex + 2)!.name, 'enemy').color]">
                      {{ iconFor(getEnemyMinionAtPosition(slotIndex + 2)!.name, 'enemy').emoji }}
                    </text>
                  </view>
                  <view class="character-stats">
                    <view class="stat-item attack-stat">
                      <text class="stat-icon">⚔️</text>
                      <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.attack }}</text>
                    </view>
                    <view class="stat-item hp-stat">
                      <text class="stat-icon">❤️</text>
                      <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.health }}</text>
                    </view>
                  </view>
                </view>
                <view
                  v-if="hasHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id)"
                  class="damage-number enemy-damage"
                  :key="getHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id)?.timestamp"
                >
                  -{{ getHitEffect(getEnemyMinionAtPosition(slotIndex + 2)!.id)?.damage }}
                </view>
                <view class="character-header">
                  <view class="character-name">{{ getEnemyMinionAtPosition(slotIndex + 2)!.name }}</view>
                  <view class="header-badges">
                    <view v-if="getEnemyMinionAtPosition(slotIndex + 2)!.shield && getEnemyMinionAtPosition(slotIndex + 2)!.shield! > 0" class="shield-badge">
                      <text class="shield-icon">🛡️</text>
                      <text class="shield-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.shield }}</text>
                    </view>
                    <view v-if="getEnemyMinionAtPosition(slotIndex + 2)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                      <text class="status-icon">😴</text>
                    </view>
                  </view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getEnemyMinionAtPosition(slotIndex + 2)!.name, 'enemy').color]">
                    {{ iconFor(getEnemyMinionAtPosition(slotIndex + 2)!.name, 'enemy').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getEnemyMinionAtPosition(slotIndex + 2)!.health }}</text>
                  </view>
                </view>
              </view>
              <view v-else class="empty-slot">
                <text class="slot-hint">{{ slotIndex + 3 }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 玩家对战区域 -->
      <view class="player-battle-zone" :class="{ 'ally-turn-active': turn === 'player' }">
        <view class="battle-slots player-slots">
          <!-- 两行三列：前排（靠近中间）位置0,1,2，后排位置3,4,5 -->
          <view class="battle-row front-row">
            <view
              v-for="slotIndex in 3"
              :key="slotIndex - 1"
              class="battle-slot player-slot"
              :class="{ 'occupied': getMinionAtPosition(slotIndex - 1) }"
              @dragover.prevent="handleSlotDragOver($event, slotIndex - 1)"
              @drop.prevent="handleSlotDrop($event, slotIndex - 1)"
            >
              <view
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
                <view
                  v-if="isAttacking(getMinionAtPosition(slotIndex - 1)!.id)"
                  class="character-card-attack-clone"
                  :style="getAttackStyle(getMinionAtPosition(slotIndex - 1)!.id)"
                >
                  <view class="character-header">
                    <view class="character-name">{{ getMinionAtPosition(slotIndex - 1)!.name }}</view>
                  </view>
                  <view class="character-avatar">
                    <text :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').color]">
                      {{ iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').emoji }}
                    </text>
                  </view>
                  <view class="character-stats">
                    <view class="stat-item attack-stat">
                      <text class="stat-icon">⚔️</text>
                      <text class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.attack }}</text>
                    </view>
                    <view class="stat-item hp-stat">
                      <text class="stat-icon">❤️</text>
                      <text class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.health }}</text>
                    </view>
                  </view>
                </view>
                <view
                  v-if="hasHitEffect(getMinionAtPosition(slotIndex - 1)!.id)"
                  class="damage-number"
                  :key="getHitEffect(getMinionAtPosition(slotIndex - 1)!.id)?.timestamp"
                >
                  -{{ getHitEffect(getMinionAtPosition(slotIndex - 1)!.id)?.damage }}
                </view>
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(slotIndex - 1)!.name }}</view>
                  <view class="header-badges">
                    <view v-if="getMinionAtPosition(slotIndex - 1)!.shield && getMinionAtPosition(slotIndex - 1)!.shield! > 0" class="shield-badge">
                      <text class="shield-icon">🛡️</text>
                      <text class="shield-value">{{ getMinionAtPosition(slotIndex - 1)!.shield }}</text>
                    </view>
                    <view v-if="getMinionAtPosition(slotIndex - 1)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                      <text class="status-icon">😴</text>
                    </view>
                  </view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(slotIndex - 1)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(slotIndex - 1)!.health }}</text>
                  </view>
                </view>
                <view v-if="getMinionAtPosition(slotIndex - 1)!.equipmentNames && getMinionAtPosition(slotIndex - 1)!.equipmentNames!.length" class="equipment-dots">
                  <text
                    v-for="(eq, idx) in getMinionAtPosition(slotIndex - 1)!.equipmentNames"
                    :key="idx"
                    class="equipment-dot"
                    :title="`已装备：${eq}`"
                  ></text>
                </view>
              </view>
              <view v-else class="empty-slot">
                <text class="slot-hint">{{ slotIndex }}</text>
              </view>
            </view>
          </view>
          <view class="battle-row back-row">
            <view
              v-for="slotIndex in 3"
              :key="slotIndex + 2"
              class="battle-slot player-slot"
              :class="{ 'occupied': getMinionAtPosition(slotIndex + 2) }"
              @dragover.prevent="handleSlotDragOver($event, slotIndex + 2)"
              @drop.prevent="handleSlotDrop($event, slotIndex + 2)"
            >
              <view
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
                <view
                  v-if="isAttacking(getMinionAtPosition(slotIndex + 2)!.id)"
                  class="character-card-attack-clone"
                  :style="getAttackStyle(getMinionAtPosition(slotIndex + 2)!.id)"
                >
                  <view class="character-header">
                    <view class="character-name">{{ getMinionAtPosition(slotIndex + 2)!.name }}</view>
                  </view>
                  <view class="character-avatar">
                    <text :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').color]">
                      {{ iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').emoji }}
                    </text>
                  </view>
                  <view class="character-stats">
                    <view class="stat-item attack-stat">
                      <text class="stat-icon">⚔️</text>
                      <text class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.attack }}</text>
                    </view>
                    <view class="stat-item hp-stat">
                      <text class="stat-icon">❤️</text>
                      <text class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.health }}</text>
                    </view>
                  </view>
                </view>
                <view
                  v-if="hasHitEffect(getMinionAtPosition(slotIndex + 2)!.id)"
                  class="damage-number"
                  :key="getHitEffect(getMinionAtPosition(slotIndex + 2)!.id)?.timestamp"
                >
                  -{{ getHitEffect(getMinionAtPosition(slotIndex + 2)!.id)?.damage }}
                </view>
                <view class="character-header">
                  <view class="character-name">{{ getMinionAtPosition(slotIndex + 2)!.name }}</view>
                  <view class="header-badges">
                    <view v-if="getMinionAtPosition(slotIndex + 2)!.shield && getMinionAtPosition(slotIndex + 2)!.shield! > 0" class="shield-badge">
                      <text class="shield-icon">🛡️</text>
                      <text class="shield-value">{{ getMinionAtPosition(slotIndex + 2)!.shield }}</text>
                    </view>
                    <view v-if="getMinionAtPosition(slotIndex + 2)!.canAttack === false" class="status-badge summoning-sickness" title="召唤疲劳：下回合才能攻击">
                      <text class="status-icon">😴</text>
                    </view>
                  </view>
                </view>
                <view class="character-avatar">
                  <text :class="['avatar-icon', iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').color]">
                    {{ iconFor(getMinionAtPosition(slotIndex + 2)!.name, 'ally').emoji }}
                  </text>
                </view>
                <view class="character-stats">
                  <view class="stat-item attack-stat">
                    <text class="stat-icon">⚔️</text>
                    <text class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.attack }}</text>
                  </view>
                  <view class="stat-item hp-stat">
                    <text class="stat-icon">❤️</text>
                    <text class="stat-value">{{ getMinionAtPosition(slotIndex + 2)!.health }}</text>
                  </view>
                </view>
                <view v-if="getMinionAtPosition(slotIndex + 2)!.equipmentNames && getMinionAtPosition(slotIndex + 2)!.equipmentNames!.length" class="equipment-dots">
                  <text
                    v-for="(eq, idx) in getMinionAtPosition(slotIndex + 2)!.equipmentNames"
                    :key="idx"
                    class="equipment-dot"
                    :title="`已装备：${eq}`"
                  ></text>
                </view>
              </view>
              <view v-else class="empty-slot">
                <text class="slot-hint">{{ slotIndex + 3 }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 玩家信息区域 -->
      <view class="player-info-zone">
        <view class="player-avatar-container ally-avatar-container" :class="{ 'hit-effect': playerHitEffect }">
          <view class="vertical-name-text">{{ playerCharacterName }}</view>
          <view class="player-avatar-circle">
            <text :class="['player-avatar-icon', playerCharacterIcon.color]">
              {{ playerCharacterIcon.emoji }}
            </text>
          </view>
          <view v-if="playerHitEffect" class="damage-number player-damage">
            -{{ playerHitEffect.damage }}
          </view>
        </view>
        <view class="hp-display ally-hp horizontal">
          <view class="hp-bar-container horizontal">
            <view class="hp-bar-bg horizontal">
              <view class="hp-bar-fill" :class="getHPColorClass(heroHPPercent)" :style="{ width: heroHPPercent + '%' }"></view>
            </view>
          </view>
          <view class="hp-text horizontal">
            <text class="hp-value">{{ heroHP }}</text>
            <text class="hp-separator">/</text>
            <text class="hp-max">{{ heroHPMax }}</text>
          </view>
        </view>
      </view>
    </view>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
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
function calculateAttackOffset(attackerId: string, targetId: string | null, isBoss: boolean, isEnemyAttacker: boolean = false): Promise<{ x: number; y: number; targetX: number; targetY: number }> {
  // 等待DOM更新后获取元素位置
  return new Promise<{ x: number; y: number; targetX: number; targetY: number }>((resolve) => {
    setTimeout(() => {
      // 优先查找 data-attacker-id，如果没有则查找 data-minion-id（用于敌方卡牌）
      let attackerEl = document.querySelector(`[data-attacker-id="${attackerId}"]`) as HTMLElement
      if (!attackerEl) {
        attackerEl = document.querySelector(`[data-minion-id="${attackerId}"]`) as HTMLElement
      }

      if (isEnemyAttacker && !attackerEl) {
        console.warn('[BattleField] 找不到敌人攻击者元素:', attackerId)
      }

      let targetEl: HTMLElement | null = null

      if (isBoss) {
        // 攻击本体：根据攻击者类型找到对应的本体头像
        if (isEnemyAttacker) {
          // 敌人攻击我方本体
          targetEl = document.querySelector('.ally-zone .player-avatar-container') as HTMLElement
        } else {
          // 我方攻击敌人本体
          targetEl = document.querySelector('.enemy-zone .player-avatar-container') as HTMLElement
        }
      } else if (targetId) {
        // 攻击角色：根据攻击者类型找到对应的目标卡片
        if (isEnemyAttacker) {
          // 敌人攻击我方角色：查找我方角色卡片（使用 data-attacker-id 或 data-minion-id）
          targetEl = document.querySelector(`[data-attacker-id="${targetId}"]`) as HTMLElement
          if (!targetEl) {
            targetEl = document.querySelector(`[data-minion-id="${targetId}"]`) as HTMLElement
          }
        } else {
          // 我方攻击敌方角色：查找敌方角色卡片
          targetEl = document.querySelector(`[data-minion-id="${targetId}"]`) as HTMLElement
        }
      }

      if (attackerEl && targetEl) {
        const attackerRect = attackerEl.getBoundingClientRect()
        const targetRect = targetEl.getBoundingClientRect()

        // 计算中心点位置（相对于视口）- 确保从卡牌中心出发
        const attackerX = attackerRect.left + attackerRect.width / 2
        const attackerY = attackerRect.top + attackerRect.height / 2
        const targetX = targetRect.left + targetRect.width / 2
        const targetY = targetRect.top + targetRect.height / 2

        if (isEnemyAttacker) {
          console.log('[BattleField] 敌人攻击位置计算:', {
            attackerId,
            attackerEl: attackerEl.className,
            attackerRect: { left: attackerRect.left, top: attackerRect.top, width: attackerRect.width, height: attackerRect.height },
            attackerCenter: { x: attackerX, y: attackerY },
            targetId,
            targetEl: targetEl.className,
            targetRect: { left: targetRect.left, top: targetRect.top, width: targetRect.width, height: targetRect.height },
            targetCenter: { x: targetX, y: targetY }
          })
        }

        resolve({ x: attackerX, y: attackerY, targetX, targetY })
      } else {
        // 如果找不到元素，使用估算值
        if (isBoss) {
          if (isEnemyAttacker) {
            // 敌人攻击我方本体：从右侧到左侧
            resolve({ x: 1200, y: 400, targetX: 200, targetY: 400 })
          } else {
            // 我方攻击敌人本体：从左侧到右侧
            resolve({ x: 200, y: 400, targetX: 1000, targetY: 400 })
          }
        } else {
          if (isEnemyAttacker) {
            // 敌人攻击我方角色：从右侧到左侧
            resolve({ x: 1000, y: 400, targetX: 400, targetY: 400 })
          } else {
            // 我方攻击敌方角色：从左侧到右侧
            resolve({ x: 200, y: 400, targetX: 800, targetY: 400 })
          }
        }
      }
    }, 50)
  })
}

// 监听攻击事件
async function handleAttackStart(event: CustomEvent) {
  const { attackerId, targetId, isBoss } = event.detail
  if (attackerId && (targetId || isBoss)) {
    // 跳过敌人本体攻击（不需要动画）
    if (attackerId === 'enemy-boss') {
      return
    }

    // 判断是否是敌人攻击（通过检查 attackerId 是否在 enemyBoard 中）
    const isEnemyAttacker = enemyBoard.value.some(m => m.id === attackerId)

    if (isEnemyAttacker) {
      const eventTime = Date.now()
      console.log(`[BattleField] 敌人卡牌攻击动画开始: ${attackerId} 在 ${eventTime}`)

      // 检查是否已经有其他攻击正在进行
      if (attackingMinions.value.size > 0) {
        const activeAttacks = Array.from(attackingMinions.value.keys())
        console.warn(`[BattleField] 警告：检测到多个攻击同时进行！当前攻击: ${attackerId}, 已有攻击: ${activeAttacks.join(', ')}`)
      }
    }

    const positions = await calculateAttackOffset(attackerId, targetId || null, !!isBoss, isEnemyAttacker)

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

    if (isEnemyAttacker) {
      const setTime = Date.now()
      console.log(`[BattleField] 敌人卡牌攻击动画已设置: ${attackerId} 在 ${setTime}, 位置:`, positions)
    }
  }
}

function handleAttackEnd(event: CustomEvent) {
  const { attackerId } = event.detail
  if (attackerId) {
    const endTime = Date.now()
    const isEnemyAttacker = enemyBoard.value.some(m => m.id === attackerId)
    if (isEnemyAttacker) {
      console.log(`[BattleField] 敌人卡牌攻击动画结束事件: ${attackerId} 在 ${endTime}`)
      console.log(`[BattleField] 当前正在进行的攻击数量: ${attackingMinions.value.size}`)
    }

    const next = new Map(attackingMinions.value)
    next.delete(attackerId)
    attackingMinions.value = next

    if (isEnemyAttacker) {
      console.log(`[BattleField] 攻击已从 attackingMinions 中移除: ${attackerId}, 剩余攻击数: ${attackingMinions.value.size}`)
    }
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

<style scoped>
.battle-field {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 20px;
  position: relative;
  z-index: 1;
  overflow: visible;
}

/* 战斗区域：上下布局，敌方在上，我方在下 */
.battle-arena {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  overflow: visible;
}

/* 区域通用样式 */
.enemy-panel-zone, .enemy-battle-zone, .player-battle-zone, .player-info-zone {
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 16px;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

/* 敌方面板区域 */
.enemy-panel-zone {
  border-color: rgba(239, 68, 68, 0.3);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px 16px;
  min-height: 80px;
}

/* 敌人对战区域 */
.enemy-battle-zone {
  border-color: rgba(239, 68, 68, 0.3);
  flex: 1;
}

/* 玩家对战区域 */
.player-battle-zone {
  border-color: rgba(59, 130, 246, 0.3);
  flex: 1;
}

/* 玩家信息区域 */
.player-info-zone {
  border-color: rgba(59, 130, 246, 0.3);
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 12px 16px;
  min-height: 80px;
}

/* 激活状态 */
.enemy-panel-zone.enemy-turn-active {
  border-color: rgba(239, 68, 68, 0.8);
  border-width: 3px;
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.4), inset 0 0 30px rgba(239, 68, 68, 0.1);
  animation: zone-highlight 2s ease-in-out infinite;
}

.player-battle-zone.ally-turn-active {
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

/* 区域信息面板 */
.zone-info-panel {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 10px 6px;
  min-height: 80px;
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

/* 玩家头像容器 */
.player-avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  margin-bottom: 8px;
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

/* 玩家头像框 */
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

/* 固定位置槽 */
.battle-slots {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 100%;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-height: 0;
  padding: 8px 0;
}

/* 两行布局 */
.battle-row {
  display: flex;
  flex-direction: row;
  gap: 12px;
  width: 100%;
  justify-content: center;
  align-items: center;
  min-height: 160px;
}

/* 前排（靠近中间） */
.front-row {
  order: 2; /* 前排在中间 */
}

/* 后排 */
.back-row {
  order: 1; /* 后排在前面 */
  opacity: 0.8;
}

/* 固定位置槽 */
.battle-slot {
  width: 120px;
  height: 120px;
  min-width: 120px;
  min-height: 120px;
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

/* 角色卡片 */
.character-card {
  background: rgba(15, 23, 42, 0.6);
  border: 2px solid rgba(148, 163, 184, 0.2);
  border-radius: 12px;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  min-width: 120px;
  box-sizing: border-box;
  flex-shrink: 0;
  z-index: 10;
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
  background: rgba(55, 65, 81, 0.35);
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

/* HP条渐变 */
.hp-bar-container.horizontal .hp-bar-fill.hp-healthy {
  background: linear-gradient(90deg, #10b981, #059669);
}

.hp-bar-container.horizontal .hp-bar-fill.hp-warning {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.hp-bar-container.horizontal .hp-bar-fill.hp-danger {
  background: linear-gradient(90deg, #ef4444, #dc2626);
}

/* 敌人面板信息 */
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
  min-width: 24px;
  text-align: right;
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
  width: 120px;
  height: 160px;
  min-width: 120px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.95), rgba(15, 23, 42, 0.95));
  border: 2px solid rgba(59, 130, 246, 0.8);
  border-radius: 12px;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2147483647 !important;
  pointer-events: none;
  animation: attack-move-clone 0.9s ease-in-out forwards;
  transform-origin: center center;
  box-shadow: 0 8px 32px rgba(59, 130, 246, 0.6), 0 0 20px rgba(59, 130, 246, 0.4);
  will-change: transform, left, top;
  margin-left: -70px;
  margin-top: -90px;
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

/* 敌人攻击克隆体 - 使用红色主题 */
.character-card-attack-clone.enemy-attack-clone {
  background: linear-gradient(145deg, rgba(59, 15, 15, 0.95), rgba(42, 8, 8, 0.95));
  border: 2px solid rgba(239, 68, 68, 0.8);
  box-shadow: 0 8px 32px rgba(239, 68, 68, 0.6), 0 0 20px rgba(239, 68, 68, 0.4);
}

/* 敌人本体攻击克隆体 */
.character-card-attack-clone.enemy-boss-attack-clone {
  background: linear-gradient(145deg, rgba(59, 15, 15, 0.95), rgba(42, 8, 8, 0.95));
  border: 2px solid rgba(239, 68, 68, 0.9);
  box-shadow: 0 8px 32px rgba(239, 68, 68, 0.7), 0 0 20px rgba(239, 68, 68, 0.5);
  width: 100px;
  height: 140px;
  min-width: 100px;
  margin-left: -50px;
  margin-top: -70px;
}

/* 允许攻击克隆体/伤害数字跨越容器，不被裁剪 */
.battle-field,
.battle-arena,
.enemy-panel-zone,
.enemy-battle-zone,
.player-battle-zone,
.player-info-zone,
.battle-slots,
.battle-slot {
  overflow: visible !important;
  position: relative;
}

/* 卡牌本体保持裁剪，限制内部光效不出界 */
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
    left: var(--target-x, 800px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.1);
    opacity: 1;
  }
  50% {
    left: calc(var(--target-x, 800px) + 15px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.3) rotate(12deg);
    opacity: 1;
  }
  60% {
    left: var(--target-x, 800px);
    top: var(--target-y, 400px);
    transform: translate(0, 0) scale(1.1);
    opacity: 1;
  }
  100% {
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

/* 响应式设计 */
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
</style>
