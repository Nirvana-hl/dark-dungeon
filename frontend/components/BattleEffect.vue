<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

export interface EffectProps {
  type: 'damage' | 'heal' | 'buff' | 'debuff' | 'critical' | 'miss'
  value?: number
  position?: { x: number; y: number }
  target?: 'enemy' | 'ally' | 'player' | 'opponent'
  duration?: number
}

const props = withDefaults(defineProps<EffectProps>(), {
  value: 0,
  duration: 1500,
  target: 'enemy'
})

const emit = defineEmits<{
  (e: 'complete'): void
}>()

const visible = ref(true)
let timer: ReturnType<typeof setTimeout> | null = null

onMounted(() => {
  timer = setTimeout(() => {
    visible.value = false
    setTimeout(() => {
      emit('complete')
    }, 300)
  }, props.duration)
})

onUnmounted(() => {
  if (timer) {
    clearTimeout(timer)
  }
})

// 获取效果图标和颜色
const effectConfig = {
  damage: { icon: '💥', color: '#ef4444', label: '伤害' },
  heal: { icon: '💚', color: '#10b981', label: '治疗' },
  buff: { icon: '⬆️', color: '#3b82f6', label: '增益' },
  debuff: { icon: '⬇️', color: '#f59e0b', label: '减益' },
  critical: { icon: '⚡', color: '#fbbf24', label: '暴击' },
  miss: { icon: '❌', color: '#94a3b8', label: '未命中' }
}

const config = effectConfig[props.type]
</script>

<template>
  <Transition name="effect-fade">
    <view 
      v-if="visible"
      class="battle-effect"
      :class="[`effect-${type}`, `target-${target}`]"
      :style="position ? { left: position.x + 'px', top: position.y + 'px' } : {}"
    >
      <view class="effect-content">
        <view class="effect-icon">{{ config.icon }}</view>
        <view v-if="value > 0" class="effect-value" :style="{ color: config.color }">
          {{ type === 'damage' || type === 'critical' ? '-' : '+' }}{{ value }}
        </view>
        <view v-if="type === 'critical'" class="effect-label">{{ config.label }}</view>
      </view>
    </view>
  </Transition>
</template>

<style scoped>
.battle-effect {
  position: absolute;
  pointer-events: none;
  z-index: 1000;
  user-select: none;
}

.effect-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  animation: effect-float 1.5s ease-out forwards;
}

.effect-icon {
  font-size: 2rem;
  animation: effect-bounce 0.6s ease-out;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.5));
}

.effect-value {
  font-size: 1.5rem;
  font-weight: 700;
  text-shadow: 
    0 0 10px currentColor,
    0 2px 4px rgba(0, 0, 0, 0.5);
  animation: effect-scale 0.6s ease-out;
}

.effect-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #fbbf24;
  text-transform: uppercase;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 效果类型样式 */
.effect-damage .effect-value {
  color: #ef4444;
}

.effect-heal .effect-value {
  color: #10b981;
}

.effect-buff .effect-value {
  color: #3b82f6;
}

.effect-debuff .effect-value {
  color: #f59e0b;
}

.effect-critical .effect-value {
  color: #fbbf24;
  font-size: 1.75rem;
  animation: effect-critical 0.8s ease-out;
}

.effect-miss .effect-value {
  color: #94a3b8;
}

/* 目标位置 */
.target-enemy {
  animation: effect-float-up 1.5s ease-out forwards;
}

.target-ally {
  animation: effect-float-down 1.5s ease-out forwards;
}

.target-player {
  animation: effect-float-up 1.5s ease-out forwards;
}

.target-opponent {
  animation: effect-float-down 1.5s ease-out forwards;
}

/* 动画 */
@keyframes effect-float {
  0% {
    opacity: 1;
    transform: translateY(0) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translateY(-30px) scale(1.1);
  }
  100% {
    opacity: 0;
    transform: translateY(-60px) scale(0.9);
  }
}

@keyframes effect-float-up {
  0% {
    opacity: 1;
    transform: translateY(0) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translateY(-40px) scale(1.1);
  }
  100% {
    opacity: 0;
    transform: translateY(-80px) scale(0.9);
  }
}

@keyframes effect-float-down {
  0% {
    opacity: 1;
    transform: translateY(0) scale(0.8);
  }
  50% {
    opacity: 1;
    transform: translateY(40px) scale(1.1);
  }
  100% {
    opacity: 0;
    transform: translateY(80px) scale(0.9);
  }
}

@keyframes effect-bounce {
  0% {
    transform: scale(0);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes effect-scale {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes effect-critical {
  0% {
    transform: scale(0) rotate(-10deg);
    opacity: 0;
  }
  30% {
    transform: scale(1.3) rotate(5deg);
    opacity: 1;
  }
  60% {
    transform: scale(1.1) rotate(-3deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}

/* 过渡动画 */
.effect-fade-enter-active {
  transition: opacity 0.3s ease;
}

.effect-fade-leave-active {
  transition: opacity 0.3s ease;
}

.effect-fade-enter-from,
.effect-fade-leave-to {
  opacity: 0;
}
</style>


