<template>
  <view class="character-panel">
    <!-- 主角卡片 -->
    <view class="character-card premium">
      <view class="character-header">
        <view class="character-avatar-section">
          <view class="avatar-glow"></view>
          <image 
            class="character-avatar"
            src="/static/touxiang.png" 
            :alt="playerCharacter?.playerCharacterName || playerCharacter?.name"
            @error="handleImageError"
          />
          <view class="level-ring">
            <text class="level-text">1</text>
          </view>
        </view>
        <view class="character-info">
          <h3 class="character-name">{{ playerCharacter?.playerCharacterName || playerCharacter?.name || '无名英雄' }}</h3>
          <view class="character-title">{{ getCharacterTitle(1) }}</view>
        </view>
      </view>
      
      <!-- 核心状态 -->
      <view class="core-stats">
        <view class="stat-item hp">
          <view class="stat-icon"><i class="fas fa-heart"></i></view>
          <view class="stat-content">
            <view class="stat-bar">
              <view class="stat-fill hp-fill" :style="{ width: hpPercentage + '%' }"></view>
            </view>
            <text class="stat-value">{{ playerCharacter?.currentHp || 0 }}/{{ playerCharacter?.maxHp || 100 }}</text>
          </view>
        </view>
        
        <view class="stat-item ap">
          <view class="stat-icon"><i class="fas fa-bolt"></i></view>
          <view class="stat-content">
            <view class="stat-bar">
              <view class="stat-fill ap-fill" :style="{ width: apPercentage + '%' }"></view>
            </view>
            <text class="stat-value">{{ playerCharacter?.currentActionPoints || 0 }}/{{ playerCharacter?.maxActionPoints || 5 }}</text>
          </view>
        </view>
        
        <view class="stat-item stress" :class="stressLevelClass" @click="$emit('showStressDetails')">
          <view class="stat-icon"><i class="fas fa-brain"></i></view>
          <view class="stat-content">
            <view class="stat-bar">
              <view class="stat-fill stress-fill" :style="{ width: stressPercentage + '%' }"></view>
            </view>
            <text class="stat-value">{{ playerCharacter?.currentStress || 0 }}% (Lv.{{ playerCharacter?.stressLevel || 1 }})</text>
          </view>
        </view>
      </view>
      
      <!-- 快速行动 -->
      <view class="quick-actions">
        <button class="action-btn primary" @click="goSkills">
          <i class="fas fa-graduation-cap"></i>
          <text>技能树</text>
          <text class="action-count">{{ unlockedSkills.length }}/{{ totalSkills }}</text>
        </button>
        <button class="action-btn secondary" @click="$emit('manageCards')">
          <i class="fas fa-layer-group"></i>
          <text>卡组</text>
          <text class="action-count">{{ equippedCards.length }}</text>
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  playerCharacter: any
  unlockedSkills: any[]
  equippedCards: any[]
  totalSkills?: number
}

const props = withDefaults(defineProps<Props>(), {
  totalSkills: 15
})

const emit = defineEmits<{
  manageCards: []
  showStressDetails: []
}>()

// uni-app 全局对象类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
}

function goSkills() {
  uni.navigateTo({ url: '/pages/skills/skills' })
}

// 计算属性
const hpPercentage = computed(() => {
  if (!props.playerCharacter?.maxHp) return 0
  return (props.playerCharacter.currentHp / props.playerCharacter.maxHp) * 100
})

const apPercentage = computed(() => {
  if (!props.playerCharacter?.maxActionPoints) return 0
  return (props.playerCharacter.currentActionPoints / props.playerCharacter.maxActionPoints) * 100
})

const stressPercentage = computed(() => {
  return Math.min((props.playerCharacter?.currentStress || 0), 100)
})

const stressLevelClass = computed(() => {
  const level = props.playerCharacter?.stressLevel || 1
  return `stress-level-${level}`
})

// 方法
function handleImageError(event: Event) {
  const img = event.target as HTMLImageElement
  img.src = '/images/default-avatar.png'
}

function getCharacterTitle(level: number): string {
  if (level >= 20) return '传说英雄'
  if (level >= 15) return '史诗战士'
  if (level >= 10) return '资深冒险者'
  if (level >= 5) return '见习战士'
  return '新手冒险者'
}
</script>

<style scoped>
.character-panel {
  width: 100%;
}

.character-card {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.1), rgba(255, 255, 255, 0.05));
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 1.5rem;
  position: relative;
  overflow: hidden;
}

.character-card.premium::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(255, 215, 0, 0.1), transparent);
  animation: shimmer 3s infinite;
}

.character-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.character-avatar-section {
  position: relative;
  width: 80px;
  height: 80px;
}

.avatar-glow {
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: radial-gradient(circle, rgba(76, 175, 80, 0.3), transparent);
  border-radius: 50%;
  animation: glow-pulse 2s infinite;
}

.character-avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
  position: relative;
  z-index: 2;
}

.level-ring {
  position: absolute;
  bottom: -5px;
  right: -5px;
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #4caf50, #45a049);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.1);
  z-index: 3;
}

.level-text {
  font-size: 0.7rem;
  font-weight: bold;
  color: white;
}

.character-info {
  flex: 1;
}

.character-name {
  font-size: 1.3rem;
  font-weight: bold;
  color: #ffffff;
  margin: 0 0 0.25rem 0;
}

.character-title {
  font-size: 0.9rem;
  color: #9ca3af;
  background: rgba(255, 255, 255, 0.1);
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  display: inline-block;
}

.core-stats {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s;
}


.stat-icon {
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9rem;
}

.stat-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.stat-bar {
  flex: 1;
  height: 6px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
  overflow: hidden;
  position: relative;
}

.stat-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.5s ease;
  position: relative;
}

.stat-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer-horizontal 2s infinite;
}

.hp-fill { background: linear-gradient(90deg, #4caf50, #45a049); }
.ap-fill { background: linear-gradient(90deg, #2196f3, #1976d2); }
.stress-fill { background: linear-gradient(90deg, #ff9800, #f44336); }

.stat-value {
  min-width: 80px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #e8e8e8;
}

.quick-actions {
  display: flex;
  gap: 0.75rem;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
  color: #e8e8e8;
}

.action-btn:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.action-btn.primary {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.3), rgba(76, 175, 80, 0.1));
  border-color: rgba(76, 175, 80, 0.5);
}

.action-btn.secondary {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.3), rgba(33, 150, 243, 0.1));
  border-color: rgba(33, 150, 243, 0.5);
}

.action-count {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.125rem 0.5rem;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: bold;
  min-width: 20px;
  text-align: center;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}

@keyframes shimmer-horizontal {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(200%); }
}

@keyframes glow-pulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.1); opacity: 0.6; }
}

@media (max-width: 768px) {
  .character-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
  }
  
  .quick-actions {
    flex-direction: column;
  }
}
</style>