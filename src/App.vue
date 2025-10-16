<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

onMounted(() => {
  auth.init()
})

function goLogin() {
  router.push('/login')
}
async function signOut() {
  await auth.signOut()
  alert('已登出')
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- 全局导航（与整体风格一致的暗色条） -->
    <nav class="status-bar py-3 px-6 flex items-center gap-3 flex-wrap">
      <RouterLink to="/" class="action-button px-3 py-2 rounded-button">战斗</RouterLink>
      <RouterLink to="/camp" class="action-button px-3 py-2 rounded-button">营地</RouterLink>
      <RouterLink to="/explore" class="action-button px-3 py-2 rounded-button">闯关</RouterLink>
      <RouterLink to="/summary" class="action-button px-3 py-2 rounded-button">结算</RouterLink>
      <RouterLink to="/skills" class="action-button px-3 py-2 rounded-button">技能树</RouterLink>
      <RouterLink to="/settings" class="action-button px-3 py-2 rounded-button">设置</RouterLink>

      <span class="flex-1"></span>
      <span class="text-sm text-gray-300">{{ auth.session ? '已登录' : '未登录' }}</span>
      <button v-if="!auth.session" class="action-button px-3 py-2 rounded-button" @click="goLogin">登录</button>
      <button v-else class="action-button px-3 py-2 rounded-button" @click="signOut">登出</button>
    </nav>
    <RouterView />
  </div>
</template>