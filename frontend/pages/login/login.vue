<script setup lang="ts">
import { ref, watch } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

// uni-app 类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
  reLaunch: (options: { url: string }) => void
}

const auth = useAuthStore()
const email = ref('')
const password = ref('')
const accountName = ref('')
const confirmPassword = ref('')
const isRegister = ref(false)

// 页面加载时初始化
onLoad(() => {
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
    uni.reLaunch({
      url: '/pages/home/home'
    })
  }
})

async function submit() {
  console.log('[Login] 点击登录/注册按钮', {
    mode: isRegister.value ? 'register' : 'login',
    email: email.value,
    hasPassword: !!password.value
  })
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
  <view class="login-container">
    <view class="login-card">
      <!-- 游戏标题和图标 -->
      <view class="title-section">
        <text class="game-icon">🎮</text>
        <text class="game-title">暗黑地牢肉鸽</text>
        <text class="game-subtitle">{{ isRegister ? '创建账户，开启冒险' : '欢迎回来，勇士' }}</text>
      </view>

      <!-- 注意：在小程序端，不使用原生 form 提交，直接用按钮点击触发 submit 方法 -->
      <view class="form-section">
        <!-- 注册模式显示额外字段 -->
        <view v-if="isRegister" class="form-item">
          <text class="form-label">账户名称</text>
          <input
            v-model="accountName"
            type="text"
            placeholder="请输入账户名称"
            class="form-input"
            required
          />
        </view>

        <view class="form-item">
          <text class="form-label">邮箱地址</text>
          <input
            v-model="email"
            type="email"
            placeholder="请输入邮箱地址"
            class="form-input"
            :required="!isRegister"
          />
        </view>

        <view class="form-item">
          <text class="form-label">密码</text>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码（至少6位）"
            class="form-input"
            required
            minlength="6"
          />
        </view>

        <!-- 注册模式显示确认密码 -->
        <view v-if="isRegister" class="form-item">
          <text class="form-label">确认密码</text>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            class="form-input"
            required
            minlength="6"
          />
        </view>

        <!-- 错误提示 -->
        <view v-if="auth.errorMsg" class="error-message">
          <text>{{ auth.errorMsg }}</text>
        </view>

        <!-- 提交按钮：直接点击触发 submit 方法 -->
        <button 
          type="button"
          class="submit-button" 
          :disabled="auth.loading"
          @click="submit"
        >
          <view v-if="auth.loading" class="loading-content">
            <text class="loading-text">{{ isRegister ? '注册中...' : '登录中...' }}</text>
          </view>
          <text v-else class="button-text">
            {{ isRegister ? '立即注册' : '立即登录' }}
          </text>
        </button>

        <!-- 切换登录/注册 -->
        <view class="switch-mode">
          <button type="button" 
                  class="switch-button" 
                  @click="isRegister = !isRegister">
            <text>{{ isRegister ? '已有账户？去登录' : '没有账户？去注册' }}</text>
          </button>
        </view>
      </view>

      <!-- 使用说明 -->
      <view class="help-section">
        <text class="help-text">
          注册：填写账户名 / 邮箱 / 密码即可创建真实账号；登录：支持邮箱或账户名 + 密码
        </text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(to bottom, #0f172a, #020617);
  color: white;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 448px;
  background-color: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 32px;
  border: 1px solid rgba(51, 65, 85, 0.5);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.title-section {
  text-align: center;
  margin-bottom: 24px;
}

.game-icon {
  font-size: 60px;
  display: block;
  margin-bottom: 16px;
}

.game-title {
  font-size: 24px;
  font-weight: bold;
  display: block;
  margin-bottom: 8px;
}

.game-subtitle {
  color: #cbd5e1;
  display: block;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border-radius: 8px;
  background-color: #1e293b;
  border: 1px solid #334155;
  color: white;
  font-size: 16px;
}

.form-input:focus {
  border-color: #0ea5e9;
  outline: none;
  box-shadow: 0 0 0 1px #0ea5e9;
}

.error-message {
  background-color: rgba(239, 68, 68, 0.2);
  border: 1px solid rgba(239, 68, 68, 0.5);
  border-radius: 8px;
  padding: 12px;
  color: #fca5a5;
  font-size: 14px;
}

.submit-button {
  border-radius: 8px;
  padding: 12px 24px;
  width: 100%;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  cursor: pointer;
  transition: opacity 0.3s;
}

.submit-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.loading-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.loading-text {
  color: white;
}

.button-text {
  color: white;
}

.switch-mode {
  text-align: center;
  font-size: 14px;
  color: #cbd5e1;
  margin-top: 16px;
}

.switch-button {
  background: none;
  border: none;
  color: #38bdf8;
  text-decoration: underline;
  padding: 0;
  font-size: 14px;
}

.help-section {
  margin-top: 24px;
  padding: 16px;
  background-color: rgba(30, 41, 59, 0.5);
  border-radius: 8px;
  border: 1px solid #334155;
}

.help-text {
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
  display: block;
}
</style>
