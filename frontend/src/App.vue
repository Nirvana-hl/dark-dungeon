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
      <!-- 全局导航（仅登录后显示，战斗界面除外） -->
      <nav v-if="auth.isAuthenticated && !isBattlePage" 
           class="status-bar py-3 px-6 flex items-center gap-3 flex-wrap bg-slate-800/90 border-b border-slate-700">
        <RouterLink to="/" class="action-button px-3 py-2">🏠 首页</RouterLink>
        <RouterLink to="/camp" class="action-button px-3 py-2">🏕️ 营地</RouterLink>
        <RouterLink to="/explore" class="action-button px-3 py-2">⚡ 闯关</RouterLink>
        <RouterLink to="/skills" class="action-button px-3 py-2">🌟 技能</RouterLink>
        <RouterLink to="/achievements" class="action-button px-3 py-2">🏆 成就</RouterLink>

        <RouterLink to="/settings" class="action-button px-3 py-2">⚙️ 设置</RouterLink>

        <span class="flex-1"></span>
        <span class="text-sm text-gray-300">✅ {{ auth.user?.accountName || '已登录' }}</span>
        <button class="action-button px-3 py-2" @click="signOut">登出</button>
      </nav>
      
      <!-- 路由视图内容 -->
      <RouterView />
    </template>
  </div>
</template>