<script setup lang="ts">
const emit = defineEmits<{ (e: 'open-items'): void }>()

// uni-app 全局对象类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
}

function go(path: string) {
  uni.navigateTo({ url: path })
}
</script>

<template>
  <footer class="bg-gray-900 py-4 px-6">
    <view class="flex justify-between items-center mb-4">
      <view class="flex gap-3">
        <button class="action-button rounded-button px-6 py-3 font-bold whitespace-nowrap flex items-center" @click="$emit('open-items')">
          <i class="fas fa-flask mr-2"></i>道具
        </button>
        <button class="action-button rounded-button px-6 py-3 font-bold whitespace-nowrap flex items-center" @click="go('/skills')">
          <i class="fas fa-magic mr-2"></i>技能树
        </button>
      </view>
    </view>

    <!-- 技能图标区域 -->
    <view class="flex space-x-4 overflow-x-auto pb-2">
      <view class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-crosshairs text-xl"></i>
      </view>
      <view class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-wind text-xl"></i>
      </view>
      <view class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-skull-crossbones text-xl"></i>
      </view>
      <view class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-bolt text-xl"></i>
      </view>
      <view class="flex-shrink-0 w-16 h-16 rounded-full bg-gray-800 flex items-center justify-center">
        <i class="fas fa-ghost text-xl"></i>
      </view>
    </view>
  </footer>
</template>