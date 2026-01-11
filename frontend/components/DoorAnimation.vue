<template>
  <view class="door-container" :class="{ open: open }" :style="containerStyleAny">
    <svg class="door-svg" :width="width" :height="height" viewBox="0 0 400 320" preserveAspectRatio="xMidYMid meet">
      <!-- left door -->
      <g class="door-part left" :style="{ transformOrigin: '100% 50%' }">
        <rect x="0" y="0" width="200" :height="320" rx="6" fill="url(#grad)" stroke="rgba(0,0,0,0.6)" stroke-width="2"/>
      </g>
      <!-- right door -->
      <g class="door-part right" :style="{ transformOrigin: '0% 50%' }">
        <rect x="200" y="0" width="200" :height="320" rx="6" fill="url(#grad)" stroke="rgba(0,0,0,0.6)" stroke-width="2"/>
      </g>
      <defs>
        <linearGradient id="grad" x1="0" x2="1" y1="0" y2="1">
          <stop offset="0%" stop-color="#1b1b24" stop-opacity="1"/>
          <stop offset="100%" stop-color="#2b2b35" stop-opacity="1"/>
        </linearGradient>
      </defs>
    </svg>
  </view>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'
const props = defineProps({
  open: { type: Boolean, default: false },
  width: { type: Number, default: 360 },
  height: { type: Number, default: 320 }
})

// compute container style to size the svg properly
const containerStyle = computed(() => {
  return {
    width: `${props.width}px`,
    height: `${props.height}px`,
    pointerEvents: 'none'
  }
})
// TS 类型兼容：在模板绑定 style 时强制 any
const containerStyleAny = computed(() => (containerStyle.value as any))
</script>

<style scoped>
.door-container {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  perspective: 1000px;
  will-change: transform;
}
.door-svg {
  display: block;
}
.door-part {
  transform: rotateY(0deg);
  transition: transform 1s cubic-bezier(.22,.9,.34,1);
  transform-style: preserve-3d;
}
.door-part.left {
  transform-origin: 100% 50%;
}
.door-part.right {
  transform-origin: 0% 50%;
}
.door-container.open .door-part.left {
  transform: rotateY(-80deg);
}
.door-container.open .door-part.right {
  transform: rotateY(80deg);
}
</style>


