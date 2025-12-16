<template>
  <view class="loading-container" :class="{ 'fullscreen': fullscreen, 'inline': !fullscreen }">
    <view class="loading-content">
      <!-- 加载动画 -->
      <view class="loading-spinner" :class="size">
        <view class="spinner-ring"></view>
        <view v-if="showLogo" class="spinner-logo">
          <i class="fas fa-dragon"></i>
        </view>
      </view>
      
      <!-- 加载文字 -->
      <view v-if="message" class="loading-message">
        {{ message }}
      </view>
      
      <!-- 进度条 -->
      <view v-if="showProgress" class="loading-progress">
        <view class="progress-bar">
          <view 
            class="progress-fill" 
            :style="{ width: `${progress}%` }"
          ></view>
        </view>
        <view class="progress-text">
          {{ Math.round(progress) }}%
        </view>
      </view>
      
      <!-- 加载步骤 -->
      <view v-if="steps && steps.length > 0" class="loading-steps">
        <view 
          v-for="(step, index) in steps" 
          :key="index"
          class="step-item"
          :class="{
            'active': index === currentStep,
            'completed': index < currentStep,
            'pending': index > currentStep
          }"
        >
          <view class="step-icon">
            <i 
              v-if="index < currentStep" 
              class="fas fa-check"
            ></i>
            <view 
              v-else-if="index === currentStep" 
              class="step-loading"
            ></view>
            <text v-else>{{ index + 1 }}</text>
          </view>
          <text class="step-text">{{ step }}</text>
        </view>
      </view>
      
      <!-- 加载提示 -->
      <view v-if="showTips && currentTip" class="loading-tip">
        <i class="fas fa-lightbulb"></i>
        <text>{{ currentTip }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'

interface Props {
  message?: string
  size?: 'small' | 'medium' | 'large'
  fullscreen?: boolean
  showLogo?: boolean
  showProgress?: boolean
  progress?: number
  steps?: string[]
  currentStep?: number
  showTips?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  fullscreen: false,
  showLogo: false,
  showProgress: false,
  progress: 0,
  currentStep: 0,
  showTips: false
})

// 游戏提示
const gameTips = [
  '合理搭配角色卡牌可以提高战斗胜率',
  '压力值过高会影响角色表现，记得及时缓解',
  '在营地休息可以恢复角色状态',
  '升级技能树可以解锁强大能力',
  '不同难度的地牢有不同奖励',
  '收集稀有度更高的卡牌获得更强属性',
  '装备合适的武器可以提升战斗效率',
  '观察敌人的攻击模式制定相应策略'
]

const currentTip = ref('')
const tipInterval = ref<NodeJS.Timeout | null>(null)

function selectRandomTip() {
  const randomIndex = Math.floor(Math.random() * gameTips.length)
  currentTip.value = gameTips[randomIndex]
}

function startTipRotation() {
  if (props.showTips) {
    selectRandomTip()
    tipInterval.value = setInterval(() => {
      selectRandomTip()
    }, 5000)
  }
}

function stopTipRotation() {
  if (tipInterval.value) {
    clearInterval(tipInterval.value)
    tipInterval.value = null
  }
}

onMounted(() => {
  startTipRotation()
})

onUnmounted(() => {
  stopTipRotation()
})
</script>

<style scoped>
.loading-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.loading-container.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(26, 26, 26, 0.95);
  z-index: 9999;
  padding: 0;
}

.loading-container.inline {
  padding: 40px 20px;
}

.loading-content {
  text-align: center;
  max-width: 300px;
}

.loading-spinner {
  position: relative;
  display: inline-block;
}

.loading-spinner.small {
  width: 32px;
  height: 32px;
}

.loading-spinner.medium {
  width: 48px;
  height: 48px;
}

.loading-spinner.large {
  width: 64px;
  height: 64px;
}

.spinner-ring {
  width: 100%;
  height: 100%;
  border: 3px solid var(--border-color);
  border-top: 3px solid var(--text-accent);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.spinner-logo {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: var(--text-accent);
  font-size: 20px;
  animation: pulse 2s ease-in-out infinite;
}

.loading-spinner.small .spinner-logo {
  font-size: 14px;
}

.loading-spinner.large .spinner-logo {
  font-size: 24px;
}

.loading-message {
  margin-top: 16px;
  font-size: 16px;
  color: var(--text-primary);
  font-weight: 500;
}

.loading-progress {
  margin-top: 16px;
  width: 100%;
}

.progress-bar {
  width: 100%;
  height: 6px;
  background: var(--tertiary-bg);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, var(--text-accent), #ff5722);
  border-radius: 3px;
  transition: width 0.3s ease;
  position: relative;
}

.progress-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  animation: shimmer 1.5s infinite;
}

.progress-text {
  font-size: 14px;
  color: var(--text-secondary);
  font-weight: bold;
}

.loading-steps {
  margin-top: 20px;
  text-align: left;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  opacity: 0.6;
  transition: all 0.3s ease;
}

.step-item.active {
  opacity: 1;
}

.step-item.completed {
  opacity: 0.8;
}

.step-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: var(--tertiary-bg);
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

.step-item.active .step-icon {
  border-color: var(--text-accent);
  background: rgba(229, 62, 62, 0.1);
}

.step-item.completed .step-icon {
  border-color: var(--success);
  background: var(--success);
  color: white;
}

.step-loading {
  width: 8px;
  height: 8px;
  border: 2px solid var(--text-accent);
  border-top: 2px solid transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.step-text {
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.step-item.active .step-text {
  color: var(--text-primary);
  font-weight: 500;
}

.step-item.completed .step-text {
  color: var(--success);
}

.loading-tip {
  margin-top: 20px;
  padding: 12px 16px;
  background: rgba(33, 150, 243, 0.1);
  border: 1px solid rgba(33, 150, 243, 0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  animation: fadeIn 0.5s ease;
}

.loading-tip i {
  color: var(--info);
  font-size: 16px;
  flex-shrink: 0;
}

.loading-tip span {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.4;
  text-align: left;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { transform: translate(-50%, -50%) scale(1); opacity: 1; }
  50% { transform: translate(-50%, -50%) scale(1.1); opacity: 0.8; }
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 768px) {
  .loading-content {
    max-width: 280px;
  }
  
  .loading-message {
    font-size: 14px;
  }
  
  .step-text {
    font-size: 13px;
  }
  
  .loading-tip span {
    font-size: 12px;
  }
}
</style>