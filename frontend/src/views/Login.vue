<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const email = ref('')
const password = ref('')
const accountName = ref('')
const confirmPassword = ref('')
const isRegister = ref(false)

// 仅初始化，不做自动跳转或数据联动，避免影响开发
onMounted(() => {
  auth.init()
  // 预填充演示账户
  email.value = 'admin@example.com'
  password.value = '123456'
})

// 监听登录/注册模式切换，清空相关字段
watch(isRegister, (newValue) => {
  if (newValue) {
    // 切换到注册模式，保留邮箱但清空密码相关字段
    password.value = ''
    confirmPassword.value = ''
  } else {
    // 切换到登录模式，清空注册特有字段
    accountName.value = ''
    confirmPassword.value = ''
  }
})

// 监听登录状态，成功后自动跳转
watch(() => auth.isAuthenticated, (isAuthenticated) => {
  if (isAuthenticated) {
    router.push('/')
  }
})

async function submit() {
  // 基础验证
  if (!password.value) {
    auth.setError('请输入密码')
    return
  }
  
  if (isRegister.value) {
    // 注册验证
    if (!accountName.value) {
      auth.setError('请输入账户名称')
      return
    }
    if (!email.value) {
      auth.setError('请输入邮箱地址')
      return
    }
    if (!confirmPassword.value) {
      auth.setError('请确认密码')
      return
    }
    if (password.value !== confirmPassword.value) {
      auth.setError('两次输入的密码不一致')
      return
    }
    
    const success = await auth.register({ 
      accountName: accountName.value,
      email: email.value, 
      password: password.value,
      confirmPassword: confirmPassword.value
    })
    if (success) {
      // 注册成功，切换到登录模式
      auth.setError('注册成功，请使用邮箱和密码登录')
      isRegister.value = false
      // 清空注册字段
      accountName.value = ''
      confirmPassword.value = ''
    }
  } else {
    // 登录验证
    if (!email.value) {
      auth.setError('请输入邮箱地址')
      return
    }
    
    const success = await auth.login({ email: email.value, password: password.value })
    if (!success && auth.errorMsg) {
      console.error('登录失败:', auth.errorMsg)
    }
    // 登录成功会通过watch自动跳转
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-b from-slate-900 to-slate-950 text-white p-6">
    <div class="w-full max-w-md card rounded-2xl p-8 border border-slate-700 shadow-2xl">
      <!-- 游戏标题和图标 -->
      <div class="text-center mb-6">
        <div class="text-6xl mb-4">🎮</div>
        <h1 class="text-2xl font-bold mb-2">暗黑地牢肉鸽</h1>
        <p class="text-gray-300">{{ isRegister ? '创建账户，开启冒险' : '欢迎回来，勇士' }}</p>
      </div>

      <form @submit.prevent="submit" class="space-y-4">
        <!-- 注册模式显示额外字段 -->
        <div v-if="isRegister">
          <label class="block text-sm font-medium mb-2">账户名称</label>
          <input
            v-model="accountName"
            type="text"
            placeholder="请输入账户名称"
            class="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            required
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-2">邮箱地址</label>
          <input
            v-model="email"
            type="email"
            placeholder="请输入邮箱地址"
            class="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            :required="!isRegister"
          />
        </div>

        <div>
          <label class="block text-sm font-medium mb-2">密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码（至少6位）"
            class="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            required
            minlength="6"
          />
        </div>

        <!-- 注册模式显示确认密码 -->
        <div v-if="isRegister">
          <label class="block text-sm font-medium mb-2">确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            class="w-full px-4 py-3 rounded-lg bg-slate-800 border border-slate-700 focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all"
            required
            minlength="6"
          />
        </div>

        <!-- 错误提示 -->
        <div v-if="auth.errorMsg" 
             class="bg-red-500/20 border border-red-500/50 rounded-lg p-3 text-red-300 text-sm">
          {{ auth.errorMsg }}
        </div>

        <!-- 提交按钮 -->
        <button 
          type="submit"
          class="action-button rounded-button px-6 py-3 w-full text-lg font-semibold disabled:opacity-50 disabled:cursor-not-allowed" 
          :disabled="auth.loading"
        >
          <span v-if="auth.loading" class="flex items-center justify-center">
            <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            {{ isRegister ? '注册中...' : '登录中...' }}
          </span>
          <span v-else>
            {{ isRegister ? '立即注册' : '立即登录' }}
          </span>
        </button>

        <!-- 切换登录/注册 -->
        <div class="text-center text-sm text-gray-300 mt-4">
          <button type="button" 
                  class="text-sky-400 hover:text-sky-300 underline transition-colors" 
                  @click="isRegister = !isRegister">
            {{ isRegister ? '已有账户？去登录' : '没有账户？去注册' }}
          </button>
        </div>
      </form>

      <!-- 后端状态提示 -->
      <div class="mt-6 p-4 bg-slate-800/50 rounded-lg border border-slate-700">
        <p class="text-sm text-gray-300 text-center">
          📡 <strong>后端连接提示</strong>：请确保 <code>npm run dev</code> + 后端 <code>8080</code> 服务已启动
        </p>
        <p class="text-xs text-gray-400 text-center mt-2">
          注册：填写账户名 / 邮箱 / 密码即可创建真实账号；登录：支持邮箱或账户名 + 密码
        </p>
      </div>
    </div>
  </div>
</template>