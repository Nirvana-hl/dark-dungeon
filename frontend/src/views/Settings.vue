<script setup lang="ts">
import { ref } from 'vue'

/**
 * 设置页面（演示）
 * - 声音开关
 * - 特效开关
 * - 重置为默认
 */
const soundOn = ref(true)
const fxOn = ref(true)
function resetAll() {
  soundOn.value = true
  fxOn.value = true
  alert('已恢复默认设置')
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <div class="p-4"><RouterLink to="/" class="text-blue-400">&larr; 返回战斗</RouterLink></div>
    <div class="flex-1 flex items-center justify-center">
      <div class="card p-8 rounded-2xl w-full max-w-xl">
        <div class="text-2xl font-bold mb-4">设置</div>

        <div class="space-y-4">
          <label class="flex items-center justify-between">
            <span>声音</span>
            <input type="checkbox" class="h-5 w-5" v-model="soundOn" />
          </label>
          <label class="flex items-center justify-between">
            <span>特效</span>
            <input type="checkbox" class="h-5 w-5" v-model="fxOn" />
          </label>
        </div>

        <div class="mt-6 flex gap-3">
          <button class="action-button rounded-button px-6 py-3" @click="resetAll">恢复默认</button>
          <button class="action-button rounded-button px-6 py-3" @click="$router.push('/')">保存并返回</button>
        </div>
      </div>
    </div>
  </div>
</template>