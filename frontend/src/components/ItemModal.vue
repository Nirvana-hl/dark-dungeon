<script setup lang="ts">
const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ (e: 'close'): void }>()
</script>

<template>
  <div v-show="open" class="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-10">
    <div class="item-grid rounded-2xl p-6 w-full max-w-2xl">
      <div class="flex justify-between items-center mb-6">
        <h3 class="text-2xl font-bold">使用道具</h3>
        <button class="text-gray-400 hover:text-white" @click="$emit('close')">
          <i class="fas fa-times text-2xl"></i>
        </button>
      </div>

      <div class="grid grid-cols-4 gap-4">
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-red-900 flex items-center justify-center mb-2">
            <i class="fas fa-heart text-red-400 text-2xl"></i>
          </div>
          <div class="font-bold">生命药水</div>
          <div class="text-sm text-gray-400">x3</div>
        </div>
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-blue-900 flex items-center justify-center mb-2">
            <i class="fas fa-bolt text-blue-400 text-2xl"></i>
          </div>
          <div class="font-bold">能量药剂</div>
          <div class="text-sm text-gray-400">x2</div>
        </div>
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-purple-900 flex items-center justify-center mb-2">
            <i class="fas fa-shield-alt text-purple-400 text-2xl"></i>
          </div>
          <div class="font-bold">防护卷轴</div>
          <div class="text-sm text-gray-400">x1</div>
        </div>
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-yellow-900 flex items-center justify-center mb-2">
            <i class="fas fa-fire text-yellow-400 text-2xl"></i>
          </div>
          <div class="font-bold">火焰炸弹</div>
          <div class="text-sm text-gray-400">x2</div>
        </div>

        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-green-900 flex items-center justify-center mb-2">
            <i class="fas fa-wind text-green-400 text-2xl"></i>
          </div>
          <div class="font-bold">疾风符文</div>
          <div class="text-sm text-gray-400">x1</div>
        </div>
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-indigo-900 flex items-center justify-center mb-2">
            <i class="fas fa-eye text-indigo-400 text-2xl"></i>
          </div>
          <div class="font-bold">洞察药剂</div>
          <div class="text-sm text-gray-400">x1</div>
        </div>
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-pink-900 flex items-center justify-center mb-2">
            <i class="fas fa-skull text-pink-400 text-2xl"></i>
          </div>
          <div class="font-bold">毒刃</div>
          <div class="text-sm text-gray-400">x2</div>
        </div>
        <div class="bg-gray-800 rounded-xl p-4 flex flex-col items-center">
          <div class="w-16 h-16 rounded-full bg-teal-900 flex items-center justify-center mb-2">
            <i class="fas fa-snowflake text-teal-400 text-2xl"></i>
          </div>
          <div class="font-bold">冰霜陷阱</div>
          <div class="text-sm text-gray-400">x1</div>
        </div>
      </div>

      <div class="flex justify-center mt-6">
        <button class="action-button rounded-button px-8 py-3 font-bold" @click="$emit('close')">关闭</button>
      </div>
    </div>
  </div>
</template>