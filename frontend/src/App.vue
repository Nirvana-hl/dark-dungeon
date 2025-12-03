<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const isLoginPage = computed(() => router.currentRoute.value.path === '/login')
const isBattlePage = computed(() => router.currentRoute.value.path === '/battle')

function goLogin() {
  router.push('/login')
}

async function signOut() {
  await auth.logout()
  // 登出后跳转到登录页
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- 未登录且不在登录页面时，显示全屏提示 -->
    <div v-if="!auth.isAuthenticated && !isLoginPage" 
         class="fixed inset-0 bg-gradient-to-b from-slate-900 to-slate-950 flex items-center justify-center z-50">
      <div class="text-center space-y-6 p-8 bg-slate-800/90 rounded-2xl border border-slate-600 max-w-md">
        <div class="text-6xl mb-4">🎮</div>
        <h1 class="text-3xl font-bold text-white mb-2">暗黑地牢肉鸽</h1>
        <p class="text-gray-300 mb-6">请先登录以开始您的冒险之旅</p>
        <button class="action-button px-6 py-3 text-lg w-full" @click="goLogin">
          立即登录
        </button>
      </div>
    </div>

    <!-- 已登录或在登录页面时，显示正常内容 -->
    <template v-else>
      <!-- 路由视图内容（导航栏已移到各页面内部） -->
      <RouterView />
    </template>
  </div>
</template>