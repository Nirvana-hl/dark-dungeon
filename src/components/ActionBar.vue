<script setup lang="ts">
import { useRouter } from 'vue-router'
const emit = defineEmits<{ (e: 'open-items'): void }>()
const router = useRouter()

function go(path: string) {
  router.push(path)
}
</script>

<template>
  <footer class="bg-gray-900 py-4 px-6">
    <div class="flex justify-between items-center mb-4">
      <div class="flex gap-3">
        <button class="action-button rounded-button px-6 py-3 font-bold whitespace-nowrap flex items-center" @click="$emit('open-items')">
          <i class="fas fa-flask mr-2"></i>道具
        </button>
        <button class="action-button rounded-button px-6 py-3 font-bold whitespace-nowrap flex items-center" @click="go('/skills')">
          <i class="fas fa-magic mr-2"></i>技能树
        </button>
      </div>
    </div>

    <!-- 技能图标区域 -->
    <div class="flex space-x-4 overflow-x-auto pb-2">
      <div class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-crosshairs text-xl"></i>
      </div>
      <div class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-wind text-xl"></i>
      </div>
      <div class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-skull-crossbones text-xl"></i>
      </div>
      <div class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-bolt text-xl"></i>
      </div>
      <div class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-ghost text-xl"></i>
      </div>
    </div>
  </footer>
</template>