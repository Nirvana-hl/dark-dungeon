<script setup lang="ts">
import { computed, onMounted, watch, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BattleField from '@/components/BattleField.vue'
import CardItem from '@/components/CardItem.vue'
import { useGameStore } from '@/stores/game'
import type { Card } from '@/stores/game'
import { storeToRefs } from 'pinia'

const route = useRoute()
const router = useRouter()

const level = computed(() => Number(route.query.level ?? 0))
const chapter = computed(() => (level.value ? Math.floor((level.value - 1) / 5) + 1 : 0))

const game = useGameStore()
const { hand, canPlay, winner, mana, manaMax, deckExhausted, deck } = storeToRefs(game)

const isEndingTurn = ref(false)
const showExitConfirm = ref(false)
const draggingEquipCard = ref<Card | null>(null)
const selectedEquipCard = ref<Card | null>(null)
const remainingDeck = computed(() => deck.value.length)

function onPlay(id: string) {
  game.playCard(id)
}

function startEquipDrag(card: Card) {
  // 仅记录装备卡的拖拽状态
  if (card.type !== 'equipment') return
  draggingEquipCard.value = card
  // 开始拖拽时关闭详情面板，避免遮挡
  selectedEquipCard.value = null
}

function endEquipDrag() {
  draggingEquipCard.value = null
}

function handleEquipToMinion(payload: { minionId: string }) {
  if (!draggingEquipCard.value) return
  game.equipCardToMinion(draggingEquipCard.value.id, payload.minionId)
  draggingEquipCard.value = null
}

function showEquipDetails(card: Card) {
  if (card.type !== 'equipment') return
  selectedEquipCard.value = card
}

function closeEquipDetails() {
  selectedEquipCard.value = null
}

// 退出战斗
function exitBattle() {
  showExitConfirm.value = true
}

function confirmExit() {
  showExitConfirm.value = false
  const lv = level.value || 1
  router.push({ path: '/explore', query: { level: String(lv) } })
}

function cancelExit() {
  showExitConfirm.value = false
}

// 结束回合 - 带视觉反馈
async function endTurn() {
  if (!canPlay.value || isEndingTurn.value) return
  isEndingTurn.value = true
  game.endTurn()
  // 添加延迟以提供视觉反馈
  await new Promise(resolve => setTimeout(resolve, 500))
  isEndingTurn.value = false
}

// 根据关卡参数配置敌方难度并开局（从数据库加载玩家/敌方手牌）
onMounted(async () => {
  const lv = level.value || 1
  const diff = lv <= 10 ? '普通' : lv <= 20 ? '困难' : '噩梦'
  game.configureEncounter(diff as any)

  // 从 Spring Boot API 加载用户数据
  await game.loadUserDeckFromDB()
  await game.loadEnemyDeck(lv)
  game.reset()
})

// 若关卡参数变化，重新配置并重新加载敌方手牌
watch(level, async (lv) => {
  const diff = lv && lv <= 10 ? '普通' : lv && lv <= 20 ? '困难' : '噩梦'
  game.configureEncounter(diff as any)
  await game.loadEnemyDeck(lv || 1)
  game.reset()
})

// 监听胜负，均回到闯关并显示奖励（失败也按需求标记通关并待领取奖励）
watch(winner, (w) => {
  const lv = String(route.query.level ?? '1')
  if (w === 'player' || w === 'enemy') {
    router.push({ path: '/explore', query: { victory: '1', level: lv } })
  }
})
</script>

<template>
  <div class="battle-container">
    
    <!-- 顶部信息栏 -->
    <div v-if="level" class="battle-header">
      <div class="header-left">
        <div class="level-badge">
          <span class="level-icon">⚔️</span>
          <div class="level-info">
            <span class="level-text">第 {{ level }} 关</span>
            <span class="chapter-text">第 {{ chapter }} 章</span>
          </div>
        </div>
      </div>
      
      <div class="header-center">
        <div class="info-chip">
          <span class="chip-label">法力</span>
          <span class="chip-value">{{ mana }}/{{ manaMax }}</span>
        </div>
        <div class="info-chip">
          <span class="chip-label">手牌</span>
          <span class="chip-value">{{ hand.length }}/10</span>
        </div>
        <div class="info-chip">
          <span class="chip-label">牌库</span>
          <span class="chip-value">{{ remainingDeck }}</span>
        </div>
      </div>
      
      <div class="header-right">
        <button 
          class="exit-battle-btn"
          @click="exitBattle"
          title="退出战斗"
        >
          <span class="btn-icon">←</span>
          <span class="btn-text">退出</span>
        </button>
      </div>
    </div>

    <!-- 战斗场地 -->
    <div class="battle-main">
      <BattleField 
        :dragging-equip-card="draggingEquipCard"
        @equip-to-minion="handleEquipToMinion"
      />
    </div>

    <!-- 底部操作区 -->
    <div class="battle-footer">
      <!-- 手牌区域 -->
      <div class="hand-section">
        <div class="hand-header">
          <div class="hand-title">
            <span class="hand-icon">🃏</span>
            <span>手牌 ({{ hand.length }}/10)</span>
          </div>
          <div class="hand-helpers">
            <div class="hand-hint">点击卡牌打出</div>
            <div v-if="deckExhausted" class="deck-empty-badge" title="本场战斗无法再抽牌">
              <span class="badge-icon">⚠️</span>
              <span class="badge-text">牌库已耗尽</span>
            </div>
          </div>
        </div>
        <div class="hand-cards">
          <CardItem 
            v-for="c in hand" 
            :key="c.id" 
            :card="c" 
            @play="onPlay"
            @start-equip-drag="startEquipDrag"
            @end-equip-drag="endEquipDrag"
            @show-equipment="showEquipDetails"
            :can-afford="mana >= c.cost"
          />
          <div v-if="hand.length === 0" class="empty-hand">
            <span class="empty-icon">📭</span>
            <span class="empty-text">手牌为空</span>
          </div>
        </div>

        <!-- 装备详情面板（点击装备卡时展示） -->
        <div v-if="selectedEquipCard" class="equip-details">
          <div class="equip-header">
            <span class="equip-title">装备详情</span>
            <button class="equip-close" @click="closeEquipDetails">✕</button>
          </div>
          <div class="equip-body">
            <div class="equip-name">{{ selectedEquipCard.name }}</div>
            <div class="equip-type">类型：装备 · 费用 {{ selectedEquipCard.cost }}</div>
            <div class="equip-effects">
              <span v-if="(selectedEquipCard as any).bonusAttack">
                攻击 +{{ (selectedEquipCard as any).bonusAttack }}
              </span>
              <span v-if="(selectedEquipCard as any).bonusHp">
                生命 +{{ (selectedEquipCard as any).bonusHp }}
              </span>
              <span v-if="(selectedEquipCard as any).bonusDefense">
                防御 +{{ (selectedEquipCard as any).bonusDefense }}
              </span>
              <span 
                v-if="!(selectedEquipCard as any).bonusAttack 
                      && !(selectedEquipCard as any).bonusHp 
                      && !(selectedEquipCard as any).bonusDefense"
              >
                暂无数值加成，可能为特殊效果装备
              </span>
            </div>
            <div class="equip-hint">
              提示：按住此装备拖到己方角色卡牌上，即可为该角色穿戴装备。
            </div>
          </div>
        </div>
      </div>

      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <button 
          class="end-turn-btn"
          :class="{ 
            'disabled': !canPlay || isEndingTurn,
            'processing': isEndingTurn
          }"
          :disabled="!canPlay || isEndingTurn"
          @click="endTurn"
        >
          <span class="btn-content">
            <span class="btn-icon-large">⏭️</span>
            <span class="btn-label">{{ isEndingTurn ? '处理中...' : '结束回合' }}</span>
          </span>
          <div v-if="isEndingTurn" class="btn-loading"></div>
        </button>
      </div>
    </div>

    <!-- 退出确认弹窗 -->
    <div v-if="showExitConfirm" class="modal-overlay" @click="cancelExit">
      <div class="exit-modal" @click.stop>
        <div class="modal-header">
          <h3>退出战斗</h3>
        </div>
        <div class="modal-content">
          <p>确定要退出当前战斗吗？</p>
          <p class="modal-warning">退出后将返回闯关界面，当前战斗进度将丢失。</p>
        </div>
        <div class="modal-actions">
          <button class="modal-btn cancel-btn" @click="cancelExit">取消</button>
          <button class="modal-btn confirm-btn" @click="confirmExit">确认退出</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.battle-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
  position: relative;
  overflow-y: auto;
}

.battle-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 50%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
  pointer-events: none;
  z-index: 0;
}

/* 顶部信息栏 */
.battle-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  position: relative;
  z-index: 10;
}

.header-left, .header-center, .header-right {
  flex: 1;
  display: flex;
  align-items: center;
}

.header-center {
  justify-content: center;
  gap: 12px;
}

.header-right {
  justify-content: flex-end;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 12px;
}

.level-icon {
  font-size: 1.5rem;
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.level-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: #e2e8f0;
}

.chapter-text {
  font-size: 0.75rem;
  color: #94a3b8;
}

.info-chip {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 14px;
  background: rgba(148, 163, 184, 0.12);
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 12px;
  min-width: 90px;
}

.chip-label {
  font-size: 0.75rem;
  color: #94a3b8;
  letter-spacing: 0.05em;
}

.chip-value {
  font-size: 1rem;
  font-weight: 700;
  color: #f4f4f5;
}

.exit-battle-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: rgba(239, 68, 68, 0.1);
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 8px;
  color: #f87171;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.exit-battle-btn:hover {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.exit-battle-btn:active {
  transform: translateY(0);
}

.exit-battle-btn .btn-icon {
  font-size: 1rem;
  font-weight: bold;
}

.battle-main {
  flex: 1;
  min-height: 0;
  padding: 12px 24px;
  position: relative;
  z-index: 1;
  overflow-y: auto;
}

.battle-main :deep(.battle-field) {
  min-height: 100%;
  box-sizing: border-box;
}

/* 底部操作区 */
.battle-footer {
  padding: 12px 24px 18px;
  background: rgba(15, 23, 42, 0.92);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  position: relative;
  z-index: 10;
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.hand-section {
  flex: 1;
}

.hand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.hand-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  color: #e2e8f0;
}

.hand-icon {
  font-size: 1.125rem;
}

.hand-hint {
  font-size: 0.75rem;
  color: #94a3b8;
}

.hand-helpers {
  display: flex;
  align-items: center;
  gap: 8px;
}

.deck-empty-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(248, 113, 113, 0.15);
  border: 1px solid rgba(248, 113, 113, 0.4);
  color: #fecaca;
  font-size: 0.75rem;
  font-weight: 600;
}

.deck-empty-badge .badge-icon {
  font-size: 0.9rem;
}

.hand-cards {
  display: flex;
  gap: 12px;
  overflow-x: auto;
  padding: 8px 0;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.3) transparent;
}

.hand-cards::-webkit-scrollbar {
  height: 6px;
}

.hand-cards::-webkit-scrollbar-track {
  background: transparent;
}

.hand-cards::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.3);
  border-radius: 3px;
}

.hand-cards::-webkit-scrollbar-thumb:hover {
  background: rgba(148, 163, 184, 0.5);
}

.equip-details {
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #e2e8f0;
  max-width: 420px;
}

.equip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.equip-title {
  font-size: 0.9rem;
  font-weight: 600;
}

.equip-close {
  background: transparent;
  border: none;
  color: #9ca3af;
  cursor: pointer;
  font-size: 0.9rem;
}

.equip-close:hover {
  color: #e5e7eb;
}

.equip-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 0.8rem;
}

.equip-name {
  font-weight: 700;
  font-size: 0.95rem;
}

.equip-effects {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 2px;
}

.equip-effects span {
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.15);
  border: 1px solid rgba(34, 197, 94, 0.4);
  color: #bbf7d0;
}

.equip-hint {
  margin-top: 2px;
  font-size: 0.75rem;
  color: #9ca3af;
}

.empty-hand {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 32px;
  color: #64748b;
}

.empty-icon {
  font-size: 2rem;
  opacity: 0.5;
}

.empty-text {
  font-size: 0.875rem;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-shrink: 0;
  width: 220px;
}

.end-turn-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 160px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.end-turn-btn:hover:not(.disabled) {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(59, 130, 246, 0.4);
}

.end-turn-btn:active:not(.disabled) {
  transform: translateY(0);
}

.end-turn-btn.disabled {
  background: rgba(71, 85, 105, 0.5);
  cursor: not-allowed;
  opacity: 0.6;
  box-shadow: none;
}

.end-turn-btn.processing {
  pointer-events: none;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  position: relative;
  z-index: 2;
}

.btn-icon-large {
  font-size: 1.25rem;
}

.btn-label {
  font-size: 1rem;
}

.btn-loading {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: loading-slide 1.5s infinite;
}

@keyframes loading-slide {
  0% { left: -100%; }
  100% { left: 100%; }
}

/* 退出确认弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fade-in 0.2s ease;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.exit-modal {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 16px;
  padding: 24px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  animation: slide-up 0.3s ease;
}

@keyframes slide-up {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  margin-bottom: 16px;
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  color: #e2e8f0;
  margin: 0;
}

.modal-content {
  margin-bottom: 24px;
}

.modal-content p {
  font-size: 0.875rem;
  color: #cbd5e1;
  margin: 0 0 8px 0;
  line-height: 1.6;
}

.modal-warning {
  color: #fbbf24 !important;
  font-size: 0.8125rem !important;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.modal-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn {
  background: rgba(71, 85, 105, 0.5);
  color: #cbd5e1;
}

.cancel-btn:hover {
  background: rgba(71, 85, 105, 0.7);
}

.confirm-btn {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: white;
}

.confirm-btn:hover {
  background: linear-gradient(135deg, #dc2626 0%, #b91c1c 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .battle-header {
    padding: 10px 16px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .header-left, .header-center, .header-right {
    flex: none;
  }

  .header-center {
    order: 3;
    width: 100%;
    justify-content: center;
  }

  .level-badge {
    padding: 6px 12px;
  }

  .exit-battle-btn .btn-text {
    display: none;
  }

  .battle-footer {
    padding: 12px 16px;
  }

  .hand-cards {
    gap: 8px;
  }

  .end-turn-btn {
    min-width: 140px;
    padding: 12px 24px;
  }
}

@media (max-width: 480px) {
  .level-badge {
    padding: 6px 10px;
  }

  .level-icon {
    font-size: 1.25rem;
  }

  .level-text {
    font-size: 0.8125rem;
  }

  .chapter-text {
    font-size: 0.6875rem;
  }

  .mana-display {
    padding: 6px 12px;
  }

  .mana-text {
    font-size: 0.875rem;
  }

  .end-turn-btn {
    min-width: 120px;
    padding: 10px 20px;
  }

  .btn-label {
    font-size: 0.875rem;
  }
}
</style>