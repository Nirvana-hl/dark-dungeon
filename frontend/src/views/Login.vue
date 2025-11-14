<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const email = ref('')
const password = ref('')
const isRegister = ref(false)

// 仅初始化，不做自动跳转或数据联动，避免影响开发
onMounted(() => {
  auth.init()
})

async function submit() {
  if (!email.value || !password.value) {
    auth.errorMsg = '请输入邮箱和密码'
    return
  }
  if (isRegister.value) {
    await auth.signUp(email.value, password.value)
    if (!auth.errorMsg) alert('注册成功，请使用邮箱和密码登录')
  } else {
    await auth.signInWithPassword(email.value, password.value)
    if (!auth.errorMsg) alert('登录成功')
  }
}
</script>

<template>
  <div class="min-h-screen p-6 bg-gradient-to-b from-slate-900 to-slate-950 text-white">
    <div class="max-w-md mx-auto card rounded-2xl p-6 border border-slate-700">
      <h1 class="text-2xl font-bold mb-4">{{ isRegister ? '注册' : '登录' }}</h1>

      <div class="space-y-3">
        <label class="block text-sm">邮箱</label>
        <input
          v-model="email"
          type="email"
          placeholder="you@example.com"
          class="w-full px-3 py-2 rounded bg-slate-800 border border-slate-700"
        />
        <label class="block text-sm mt-2">密码</label>
        <input
          v-model="password"
          type="password"
          placeholder="至少6位"
          class="w-full px-3 py-2 rounded bg-slate-800 border border-slate-700"
        />

        <!-- 暂时不做自动导航、数据拉取等联动，仅提供基础注册/登录 -->
        <button class="action-button rounded-button px-4 py-2 w-full" :disabled="auth.loading" @click="submit">
          {{ auth.loading ? (isRegister ? '注册中...' : '登录中...') : (isRegister ? '注册' : '登录') }}
        </button>

        <p v-if="auth.errorMsg" class="text-red-400 text-sm mt-2">{{ auth.errorMsg }}</p>

        <div class="text-sm text-gray-300 mt-3">
          <button class="underline" @click="isRegister = !isRegister">
            {{ isRegister ? '已有账户？去登录' : '没有账户？去注册' }}
          </button>
        </div>
      </div>

      <div class="mt-6 text-sm text-gray-300">
        提示：需在 Supabase 控制台启用 Email+Password 登录（Authentication → Providers → Email）。
      </div>
    </div>
  </div>
</template>