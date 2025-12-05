<script setup lang="ts">
import { computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/game'
import { useCampStore } from '@/stores/camp'

const game = useGameStore()
const campStore = useCampStore()
const { heroHP, mana, manaMax, turn } = storeToRefs(game)

// 从营地数据获取最大血量，如果没有则使用默认值100
const heroHPMax = computed(() => campStore.playerCharacter?.maxHp || 100)

const healthPct = computed(() => Math.max(0, Math.min(100, Math.round((heroHP.value / heroHPMax.value) * 100))))

// 获取HP颜色
const healthColor = computed(() => {
  if (healthPct.value > 60) return 'healthy'
  if (healthPct.value > 30) return 'warning'
  return 'danger'
})

// 获取法力颜色
const manaColor = computed(() => {
  const percent = (mana.value / manaMax.value) * 100
  if (percent > 70) return 'high'
  if (percent > 40) return 'medium'
  return 'low'
})
</script>

<template>
  <header class="status-bar">
    <div class="status-content">
      <!-- 左侧：角色信息 -->
      <div class="character-info">
        <div class="character-avatar">
          <div class="avatar-glow"></div>
          <span class="avatar-icon">⚔️</span>
        </div>
        <div class="character-details">
          <div class="character-name">冒险者</div>
          <div class="character-level">等级 5</div>
        </div>
      </div>

      <!-- 中间：状态条 -->
      <div class="status-meters">
        <!-- 生命值条 -->
        <div class="meter-item hp-meter">
          <div class="meter-label">
            <span class="label-icon">❤️</span>
            <span class="label-text">生命值</span>
          </div>
          <div class="meter-bar-container">
            <div class="meter-bar-bg">
              <div 
                class="meter-bar-fill"
                :class="'hp-' + healthColor"
                :style="{ width: healthPct + '%' }"
              >
                <div class="bar-shine"></div>
              </div>
            </div>
            <div class="meter-value">
              <span class="value-current">{{ heroHP }}</span>
              <span class="value-separator">/</span>
              <span class="value-max">{{ heroHPMax }}</span>
            </div>
          </div>
        </div>

        <!-- 法力值条 -->
        <div class="meter-item mana-meter">
          <div class="meter-label">
            <span class="label-icon">💎</span>
            <span class="label-text">法力值</span>
          </div>
          <div class="meter-bar-container">
            <div class="meter-bar-bg">
              <div 
                class="meter-bar-fill"
                :class="'mana-' + manaColor"
                :style="{ width: (mana / manaMax) * 100 + '%' }"
              >
                <div class="bar-shine"></div>
              </div>
            </div>
            <div class="meter-value">
              <span class="value-current">{{ mana }}</span>
              <span class="value-separator">/</span>
              <span class="value-max">{{ manaMax }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：回合指示器 -->
      <div class="turn-indicator" :class="{ 'player-turn': turn === 'player', 'opponent-turn': turn === 'opponent' }">
        <div class="turn-icon-wrapper">
          <span class="turn-icon">{{ turn === 'player' ? '⚡' : '⏸️' }}</span>
        </div>
        <div class="turn-text">
          <span class="turn-label">{{ turn === 'player' ? '你的回合' : '对手回合' }}</span>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.status-bar {
  background: rgba(15, 23, 42, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.2);
  padding: 12px 24px;
  position: relative;
  z-index: 10;
}

.status-content {
  max-width: 1600px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* 角色信息 */
.character-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.character-avatar {
  position: relative;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.3), transparent);
  border-radius: 50%;
  animation: glow-pulse 2s infinite;
}

@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}

.avatar-icon {
  position: relative;
  z-index: 2;
  font-size: 1.75rem;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.character-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.character-name {
  font-size: 1rem;
  font-weight: 700;
  color: #e2e8f0;
}

.character-level {
  font-size: 0.75rem;
  color: #94a3b8;
}

/* 状态条 */
.status-meters {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}

.meter-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.meter-label {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 80px;
}

.label-icon {
  font-size: 1rem;
}

.label-text {
  font-size: 0.875rem;
  font-weight: 600;
  color: #cbd5e1;
}

.meter-bar-container {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.meter-bar-bg {
  flex: 1;
  height: 20px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.meter-bar-fill {
  height: 100%;
  border-radius: 10px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.bar-shine {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shine 2s infinite;
}

@keyframes shine {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* HP颜色 */
.meter-bar-fill.hp-healthy {
  background: linear-gradient(90deg, #10b981, #059669);
}

.meter-bar-fill.hp-warning {
  background: linear-gradient(90deg, #f59e0b, #d97706);
}

.meter-bar-fill.hp-danger {
  background: linear-gradient(90deg, #ef4444, #dc2626);
  animation: danger-pulse 1s infinite;
}

@keyframes danger-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

/* 法力颜色 */
.meter-bar-fill.mana-high {
  background: linear-gradient(90deg, #8b5cf6, #7c3aed);
}

.meter-bar-fill.mana-medium {
  background: linear-gradient(90deg, #6366f1, #4f46e5);
}

.meter-bar-fill.mana-low {
  background: linear-gradient(90deg, #3b82f6, #2563eb);
}

.meter-value {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 60px;
  font-size: 0.875rem;
  font-weight: 700;
  text-align: right;
}

.value-current {
  color: #e2e8f0;
}

.value-separator {
  color: #94a3b8;
}

.value-max {
  color: #94a3b8;
}

.hp-meter .value-current {
  color: #10b981;
}

.mana-meter .value-current {
  color: #8b5cf6;
}

/* 回合指示器 */
.turn-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 20px;
  background: rgba(71, 85, 105, 0.3);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 12px;
  transition: all 0.3s ease;
  flex-shrink: 0;
}

.turn-indicator.player-turn {
  background: rgba(59, 130, 246, 0.2);
  border-color: rgba(59, 130, 246, 0.5);
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.3);
  animation: turn-pulse 2s infinite;
}

.turn-indicator.opponent-turn {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.5);
  box-shadow: 0 0 20px rgba(239, 68, 68, 0.3);
}

@keyframes turn-pulse {
  0%, 100% {
    box-shadow: 0 0 20px rgba(59, 130, 246, 0.3);
  }
  50% {
    box-shadow: 0 0 30px rgba(59, 130, 246, 0.5);
  }
}

.turn-icon-wrapper {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.turn-icon {
  font-size: 1.25rem;
  animation: turn-icon-spin 2s linear infinite;
}

.turn-indicator.opponent-turn .turn-icon {
  animation: none;
}

@keyframes turn-icon-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.turn-text {
  display: flex;
  flex-direction: column;
}

.turn-label {
  font-size: 0.875rem;
  font-weight: 700;
  color: #e2e8f0;
}

.turn-indicator.player-turn .turn-label {
  color: #60a5fa;
}

.turn-indicator.opponent-turn .turn-label {
  color: #f87171;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .status-content {
    gap: 16px;
  }

  .meter-label {
    min-width: 70px;
  }
}

@media (max-width: 992px) {
  .status-content {
    flex-wrap: wrap;
    gap: 12px;
  }

  .status-meters {
    width: 100%;
    order: 3;
  }

  .turn-indicator {
    order: 2;
  }
}

@media (max-width: 768px) {
  .status-bar {
    padding: 10px 16px;
  }

  .character-avatar {
    width: 40px;
    height: 40px;
  }

  .avatar-icon {
    font-size: 1.5rem;
  }

  .character-name {
    font-size: 0.875rem;
  }

  .character-level {
    font-size: 0.6875rem;
  }

  .meter-label {
    min-width: 60px;
  }

  .label-text {
    font-size: 0.75rem;
  }

  .meter-bar-bg {
    height: 16px;
  }

  .meter-value {
    min-width: 50px;
    font-size: 0.75rem;
  }

  .turn-indicator {
    padding: 8px 16px;
  }

  .turn-label {
    font-size: 0.75rem;
  }
}

@media (max-width: 480px) {
  .status-content {
    flex-direction: column;
    align-items: stretch;
  }

  .character-info {
    justify-content: center;
  }

  .turn-indicator {
    justify-content: center;
  }

  .meter-label {
    min-width: 50px;
  }

  .label-text {
    display: none;
  }
}
</style>
