<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'

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
    <!-- 返回首页按钮 -->
    <RouterLink to="/" class="back-to-home">
      <i class="fas fa-home"></i>
      返回首页
    </RouterLink>
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

<style scoped>
/* 返回首页按钮 */
.back-to-home {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  text-decoration: none;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
}

.back-to-home:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.back-to-home i {
  font-size: 1rem;
}
</style>