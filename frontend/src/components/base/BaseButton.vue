<template>
  <button 
    :class="buttonClasses" 
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="loading"></span>
    <span v-else>{{ text }}</span>
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  text: string
  type?: 'primary' | 'secondary' | 'success' | 'warning' | 'danger'
  size?: 'small' | 'medium' | 'large'
  disabled?: boolean
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'primary',
  size: 'medium',
  disabled: false,
  loading: false
})

interface Emits {
  (e: 'click'): void
}

const emit = defineEmits<Emits>()

const buttonClasses = computed(() => [
  'btn',
  `btn-${props.type}`,
  `btn-${props.size}`,
  {
    'btn-disabled': props.disabled,
    'btn-loading': props.loading
  }
])

function handleClick() {
  if (!props.disabled && !props.loading) {
    emit('click')
  }
}
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-decoration: none;
  font-family: inherit;
  cursor: pointer;
  border: none;
  border-radius: 4px;
  transition: all 0.3s ease;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  position: relative;
  overflow: hidden;
}

.btn-small {
  padding: 8px 16px;
  font-size: 12px;
}

.btn-medium {
  padding: 12px 24px;
  font-size: 14px;
}

.btn-large {
  padding: 16px 32px;
  font-size: 16px;
}

.btn-primary {
  background: linear-gradient(135deg, var(--text-accent), #ff5722);
  color: white;
}

.btn-secondary {
  background: var(--tertiary-bg);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}

.btn-success {
  background: var(--success);
  color: white;
}

.btn-warning {
  background: var(--warning);
  color: white;
}

.btn-danger {
  background: var(--error);
  color: white;
}

.btn:hover:not(.btn-disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-medium);
}

.btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  transition: left 0.5s ease;
}

.btn:hover:not(.btn-disabled)::before {
  left: 100%;
}

.btn-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-disabled:hover {
  transform: none;
  box-shadow: none;
}

.btn-loading {
  cursor: wait;
}

.loading {
  width: 16px;
  height: 16px;
  border: 2px solid transparent;
  border-top: 2px solid currentColor;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>