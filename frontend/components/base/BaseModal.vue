<template>
  <Transition name="modal">
    <view v-if="show" class="modal-overlay" @click="handleOverlayClick">
        <view 
          class="modal-container" 
          :class="modalClasses"
          @click.stop
        >
          <view class="modal-header">
            <h3 class="modal-title">{{ title }}</h3>
            <button 
              class="modal-close" 
              @click="handleClose"
              aria-label="关闭"
            >
              ✕
            </button>
          </view>
          
          <view class="modal-content">
            <slot>
              {{ content }}
            </slot>
          </view>
          
          <view v-if="showFooter" class="modal-footer">
            <slot name="footer">
              <BaseButton 
                v-if="showCancelButton"
                text="取消"
                type="secondary"
                @click="handleCancel"
              />
              <BaseButton 
                v-if="showConfirmButton"
                text="确认"
                :type="confirmButtonType"
                :loading="loading"
                @click="handleConfirm"
              />
            </slot>
          </view>
        </view>
      </view>
    </Transition>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import BaseButton from './BaseButton.vue'

interface Props {
  show: boolean
  title: string
  content?: string
  size?: 'small' | 'medium' | 'large' | 'fullscreen'
  showFooter?: boolean
  showCancelButton?: boolean
  showConfirmButton?: boolean
  confirmButtonType?: 'primary' | 'success' | 'warning' | 'danger'
  loading?: boolean
  closeOnOverlay?: boolean
  closeOnEscape?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  size: 'medium',
  showFooter: true,
  showCancelButton: true,
  showConfirmButton: true,
  confirmButtonType: 'primary',
  loading: false,
  closeOnOverlay: true,
  closeOnEscape: true
})

interface Emits {
  (e: 'close'): void
  (e: 'cancel'): void
  (e: 'confirm'): void
}

const emit = defineEmits<Emits>()

const modalClasses = computed(() => [
  'modal',
  `modal-${props.size}`
])

// ESC 键关闭
function handleKeydown(event: KeyboardEvent) {
  if (props.closeOnEscape && event.key === 'Escape') {
    handleClose()
  }
}

// 监听键盘事件
watch(() => props.show, (show) => {
  // 仅在浏览器环境中处理DOM事件
  if (typeof document !== 'undefined') {
    if (show) {
      document.addEventListener('keydown', handleKeydown)
      if (document.body) {
        document.body.style.overflow = 'hidden'
      }
    } else {
      document.removeEventListener('keydown', handleKeydown)
      if (document.body) {
        document.body.style.overflow = ''
      }
    }
  }
})

function handleOverlayClick() {
  if (props.closeOnOverlay) {
    handleClose()
  }
}

function handleClose() {
  emit('close')
}

function handleCancel() {
  emit('cancel')
}

function handleConfirm() {
  emit('confirm')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  padding: 20px;
}

.modal-container {
  background: var(--secondary-bg);
  border: 2px solid var(--border-color);
  border-radius: 12px;
  box-shadow: var(--shadow-heavy);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-small {
  width: 400px;
  max-width: 90vw;
}

.modal-medium {
  width: 600px;
  max-width: 90vw;
}

.modal-large {
  width: 800px;
  max-width: 90vw;
}

.modal-fullscreen {
  width: 100vw;
  height: 100vh;
  max-width: 100vw;
  max-height: 100vh;
  border-radius: 0;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  flex-shrink: 0;
}

.modal-title {
  margin: 0;
  font-size: 20px;
  font-weight: bold;
  color: var(--text-primary);
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-muted);
  font-size: 24px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.modal-close:hover {
  background: var(--tertiary-bg);
  color: var(--text-primary);
}

.modal-content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
  color: var(--text-primary);
  line-height: 1.6;
}

.modal-footer {
  padding: 20px 24px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  flex-shrink: 0;
}

.modal-fullscreen .modal-content {
  height: calc(100vh - 140px);
}

/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-active .modal-container,
.modal-leave-active .modal-container {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.9) translateY(-50px);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 10px;
  }
  
  .modal-small,
  .modal-medium,
  .modal-large {
    width: 100vw;
    max-width: 100vw;
    border-radius: 12px 12px 0 0;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .modal-content {
    padding: 20px;
  }
  
  .modal-footer {
    padding: 16px 20px;
  }
  
  .modal-title {
    font-size: 18px;
  }
}

/* 确保内容不会溢出 */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-track {
  background: var(--tertiary-bg);
}

.modal-content::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: var(--text-muted);
}
</style>