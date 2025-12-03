<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/game'
import { ref } from 'vue'

const game = useGameStore()
const { heroHP, enemyHP, board, enemyBoard, turn, logs, mana, manaMax } = storeToRefs(game)

const props = defineProps<{
  draggingEquipCard: import('@/stores/game').Card | null
}>()

const emit = defineEmits<{
  (e: 'equip-to-minion', payload: { minionId: string }): void
}>()

const heroHPMax = 100
const enemyHPMax = 100

const heroHPPercent = computed(() => Math.max(0, Math.min(100, (heroHP.value / heroHPMax) * 100)))
const enemyHPPercent = computed(() => Math.max(0, Math.min(100, (enemyHP.value / enemyHPMax) * 100)))

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
</script>

<template>
  <main class="battle-field">
    <!-- 左侧：战斗区域 -->
    <div class="battle-arena">
      <!-- 敌方区域 -->
      <div class="enemy-zone">
        <div class="zone-header">
          <div class="zone-title">
            <span class="title-icon">👹</span>
            <span class="title-text">敌方</span>
          </div>
          <div class="hp-display enemy-hp">
            <div class="hp-bar-container">
              <div class="hp-bar-bg">
                <div 
                  class="hp-bar-fill"
                  :class="getHPColorClass(enemyHPPercent)"
                  :style="{ width: enemyHPPercent + '%' }"
                ></div>
              </div>
              <div class="hp-text">
                <span class="hp-value">{{ enemyHP }}</span>
                <span class="hp-separator">/</span>
                <span class="hp-max">{{ enemyHPMax }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="turn-indicator" :class="{ 'enemy-turn': turn === 'opponent' }">
          <span class="turn-icon">{{ turn === 'opponent' ? '⚡' : '⏸️' }}</span>
          <span class="turn-text">{{ turn === 'opponent' ? '敌方回合' : '等待中' }}</span>
        </div>

        <div class="characters-grid">
          <div 
            v-for="e in enemyBoard" 
            :key="e.id" 
            class="character-card enemy-card"
          >
            <div class="character-header">
              <div class="character-name">{{ e.name }}</div>
              <div v-if="e.shield && e.shield > 0" class="shield-badge">
                <span class="shield-icon">🛡️</span>
                <span class="shield-value">{{ e.shield }}</span>
              </div>
            </div>
            
            <div class="character-avatar">
              <span :class="['avatar-icon', iconFor(e.name, 'enemy').color]">
                {{ iconFor(e.name, 'enemy').emoji }}
              </span>
            </div>
            
            <div class="character-stats">
              <div class="stat-item attack-stat">
                <span class="stat-icon">⚔️</span>
                <span class="stat-value">{{ e.attack }}</span>
              </div>
              <div class="stat-item hp-stat">
                <div class="hp-mini-bar">
                  <div 
                    class="hp-mini-fill"
                    :class="getHPColorClass(getHPPercent(e.health, e.health + 10))"
                    :style="{ width: getHPPercent(e.health, e.health + 10) + '%' }"
                  ></div>
                </div>
                <span class="stat-icon">❤️</span>
                <span class="stat-value">{{ e.health }}</span>
              </div>
            </div>
          </div>
          
          <div v-if="enemyBoard.length === 0" class="empty-zone">
            <span class="empty-icon">💀</span>
            <span class="empty-text">敌方角色已被击败</span>
            <span class="empty-hint">可直接对敌方造成伤害</span>
          </div>
        </div>
      </div>

      <!-- 战斗分隔线 -->
      <div class="battle-divider">
        <div class="divider-line"></div>
        <div class="divider-icon">⚔️</div>
        <div class="divider-line"></div>
      </div>

      <!-- 我方区域 -->
      <div class="ally-zone">
        <div class="zone-header">
          <div class="zone-title">
            <span class="title-icon">⚔️</span>
            <span class="title-text">我方</span>
          </div>
          <div class="hp-display ally-hp">
            <div class="hp-bar-container">
              <div class="hp-bar-bg">
                <div 
                  class="hp-bar-fill"
                  :class="getHPColorClass(heroHPPercent)"
                  :style="{ width: heroHPPercent + '%' }"
                ></div>
              </div>
              <div class="hp-text">
                <span class="hp-value">{{ heroHP }}</span>
                <span class="hp-separator">/</span>
                <span class="hp-max">{{ heroHPMax }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="turn-indicator" :class="{ 'ally-turn': turn === 'player' }">
          <span class="turn-icon">{{ turn === 'player' ? '⚡' : '⏸️' }}</span>
          <span class="turn-text">{{ turn === 'player' ? '你的回合' : '等待中' }}</span>
        </div>

        <div class="characters-grid">
          <div 
            v-for="m in board" 
            :key="m.id" 
            class="character-card ally-card"
            @dragover.prevent
            @drop.prevent="emit('equip-to-minion', { minionId: m.id })"
          >
            <div class="character-header">
              <div class="character-name">{{ m.name }}</div>
              <div v-if="m.shield && m.shield > 0" class="shield-badge">
                <span class="shield-icon">🛡️</span>
                <span class="shield-value">{{ m.shield }}</span>
              </div>
              <div v-if="m.stars" class="star-badge">
                <span class="star-icon">⭐</span>
                <span class="star-value">{{ m.stars }}</span>
              </div>
            </div>
            
            <div class="character-avatar">
              <span :class="['avatar-icon', iconFor(m.name, 'ally').color]">
                {{ iconFor(m.name, 'ally').emoji }}
              </span>
            </div>
            
            <div class="character-stats">
              <div class="stat-item attack-stat">
                <span class="stat-icon">⚔️</span>
                <span class="stat-value">{{ m.attack }}</span>
              </div>
              <div class="stat-item hp-stat">
                <div class="hp-mini-bar">
                  <div 
                    class="hp-mini-fill"
                    :class="getHPColorClass(getHPPercent(m.health, m.health + 10))"
                    :style="{ width: getHPPercent(m.health, m.health + 10) + '%' }"
                  ></div>
                </div>
                <span class="stat-icon">❤️</span>
                <span class="stat-value">{{ m.health }}</span>
              </div>
            </div>
            <!-- 装备圆形标记 -->
            <div v-if="m.equipmentNames && m.equipmentNames.length" class="equipment-dots">
              <span 
                v-for="(eq, idx) in m.equipmentNames" 
                :key="idx" 
                class="equipment-dot"
                :title="`已装备：${eq}`"
              ></span>
            </div>
          </div>
          
          <div v-if="board.length === 0" class="empty-zone">
            <span class="empty-icon">📭</span>
            <span class="empty-text">暂无随从</span>
            <span class="empty-hint">打出手牌召唤角色</span>
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
  overflow: hidden;
}

/* 战斗区域 */
.battle-arena {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 区域通用样式 */
.enemy-zone, .ally-zone {
  background: rgba(30, 41, 59, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  padding: 20px;
  transition: all 0.3s ease;
}

.enemy-zone {
  border-color: rgba(239, 68, 68, 0.3);
}

.ally-zone {
  border-color: rgba(59, 130, 246, 0.3);
}

.zone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.zone-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.125rem;
  font-weight: 700;
  color: #e2e8f0;
}

.title-icon {
  font-size: 1.5rem;
}

.title-text {
  font-size: 1.125rem;
}

/* HP显示 */
.hp-display {
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

.hp-bar-fill {
  height: 100%;
  border-radius: 12px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.hp-bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.hp-bar-fill.hp-healthy {
  background: linear-gradient(90deg, #10b981, #059669);
}

.hp-bar-fill.hp-warning {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.hp-bar-fill.hp-danger {
  background: linear-gradient(90deg, #ef4444, #dc2626);
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

/* 回合指示器 */
.turn-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(71, 85, 105, 0.3);
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 0.875rem;
  color: #94a3b8;
  transition: all 0.3s ease;
}

.turn-indicator.enemy-turn {
  background: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.4);
  color: #f87171;
  animation: pulse-turn 2s infinite;
}

.turn-indicator.ally-turn {
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
  color: #60a5fa;
  animation: pulse-turn 2s infinite;
}

@keyframes pulse-turn {
  0%, 100% { box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.4); }
  50% { box-shadow: 0 0 0 8px rgba(59, 130, 246, 0); }
}

.turn-icon {
  font-size: 1rem;
}

.turn-text {
  font-weight: 600;
}

/* 角色网格 */
.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
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

.character-name {
  font-size: 0.8125rem;
  font-weight: 600;
  color: #e2e8f0;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shield-badge, .star-badge {
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

.shield-icon, .star-icon {
  font-size: 0.75rem;
}

.shield-value, .star-value {
  font-weight: 700;
  color: #e2e8f0;
}

.character-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 60px;
  margin: 8px 0;
}

.avatar-icon {
  font-size: 2.5rem;
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
  gap: 8px;
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
  flex: 1;
  display: flex;
  align-items: center;
  gap: 4px;
}

.hp-mini-bar {
  flex: 1;
  height: 4px;
  background: rgba(15, 23, 42, 0.8);
  border-radius: 2px;
  overflow: hidden;
}

.hp-mini-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s ease;
}

.hp-mini-fill.hp-healthy {
  background: #10b981;
}

.hp-mini-fill.hp-warning {
  background: #f59e0b;
}

.hp-mini-fill.hp-danger {
  background: #ef4444;
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
  margin: 8px 0;
}

.divider-line {
  flex: 1;
  height: 2px;
  background: linear-gradient(90deg, transparent, rgba(148, 163, 184, 0.3), transparent);
}

.divider-icon {
  font-size: 1.5rem;
  opacity: 0.5;
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

  .hp-display {
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

  .hp-display {
    min-width: 120px;
  }
}
</style>
