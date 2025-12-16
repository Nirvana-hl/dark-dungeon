<template>
  <transition name="toast" appear>
    <view class="toast-notification" :class="`toast-${type}`">
      <view class="toast-icon">
        <i :class="getIcon()"></i>
      </view>
      <view class="toast-content">
        <view class="toast-message">{{ message }}</view>
        <view v-if="detail" class="toast-detail">{{ detail }}</view>
      </view>
      <button class="toast-close" @click="$emit('close')">
        <i class="fas fa-times"></i>
      </button>
      <view 
        v-if="autoClose && duration > 0" 
        class="toast-progress"
        :style="{ animationDuration: `${duration}ms` }"
      ></view>
    </view>
  </transition>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

interface Props {
  message: string
  type?: 'success' | 'error' | 'info' | 'warning'
  detail?: string
  duration?: number
  autoClose?: boolean
}

interface Emits {
  (e: 'close'): void
}

const props = withDefaults(defineProps<Props>(), {
  type: 'info',
  duration: 3000,
  autoClose: true
})

const emit = defineEmits<Emits>()

let timer: NodeJS.Timeout | null = null

function getIcon(): string {
  switch (props.type) {
    case 'success':
      return 'fas fa-check-circle'
    case 'error':
      return 'fas fa-exclamation-circle'
    case 'warning':
      return 'fas fa-exclamation-triangle'
    case 'info':
      return 'fas fa-info-circle'
    default:
      return 'fas fa-info-circle'
  }
}

function startTimer() {
  if (props.autoClose && props.duration > 0) {
    timer = setTimeout(() => {
      emit('close')
    }, props.duration)
  }
}

onMounted(() => {
  startTimer()
})
</script>

<style scoped>
.toast-notification {
  position: fixed;
  top: 20px;
  right: 20px;
  min-width: 300px;
  max-width: 500px;
  background: var(--secondary-bg);
  border-radius: 8px;
  box-shadow: var(--shadow-heavy);
  border-left: 4px solid var(--border-color);
  padding: 16px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  z-index: 9999;
  position: relative;
  overflow: hidden;
}

.toast-notification.toast-success {
  border-left-color: var(--success);
}

.toast-notification.toast-error {
  border-left-color: var(--error);
}

.toast-notification.toast-warning {
  border-left-color: var(--warning);
}

.toast-notification.toast-info {
  border-left-color: var(--info);
}

.toast-icon {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}

.toast-notification.toast-success .toast-icon {
  color: var(--success);
}

.toast-notification.toast-error .toast-icon {
  color: var(--error);
}

.toast-notification.toast-warning .toast-icon {
  color: var(--warning);
}

.toast-notification.toast-info .toast-icon {
  color: var(--info);
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-message {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 500;
  line-height: 1.4;
  margin-bottom: 2px;
}

.toast-detail {
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.3;
}

.toast-close {
  background: transparent;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-size: 12px;
  flex-shrink: 0;
}

.toast-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.toast-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background: var(--text-accent);
  animation: toast-progress linear forwards;
}

.toast-notification.toast-success .toast-progress {
  background: var(--success);
}

.toast-notification.toast-error .toast-progress {
  background: var(--error);
}

.toast-notification.toast-warning .toast-progress {
  background: var(--warning);
}

.toast-notification.toast-info .toast-progress {
  background: var(--info);
}

/* 进入和离开动画 */
.toast-enter-active {
  transition: all 0.3s ease-out;
}

.toast-leave-active {
  transition: all 0.3s ease-in;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 进度条动画 */
@keyframes toast-progress {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toast-notification {
    top: 10px;
    right: 10px;
    left: 10px;
    min-width: auto;
    max-width: none;
  }
  
  .toast-message {
    font-size: 13px;
  }
  
  .toast-detail {
    font-size: 11px;
  }
}

/* 多个toast堆叠时的样式调整 */
.toast-notification:nth-child(2) {
  top: 80px;
}

.toast-notification:nth-child(3) {
  top: 140px;
}

.toast-notification:nth-child(4) {
  top: 200px;
}

@media (max-width: 768px) {
  .toast-notification:nth-child(2) {
    top: 70px;
  }
  
  .toast-notification:nth-child(3) {
    top: 130px;
  }
  
  .toast-notification:nth-child(4) {
    top: 190px;
  }
}
</style>