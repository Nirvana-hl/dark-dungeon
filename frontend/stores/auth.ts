import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/api/request'
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types'
import { UserStatus } from '@/types'

// uni-app 类型声明
declare const uni: {
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
}

export const useAuthStore = defineStore('auth', () => {
  // 是否允许在开发环境下自动降级为本地演示登录（仅开发时设为 true）
  const ALLOW_DEMO_AUTH = false
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)
  const errorMsg = ref<string | null>(null)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isLoggedIn = computed(() => isAuthenticated.value)

  async function init() {
    // 从本地存储恢复会话
    try {
      const savedToken = uni.getStorageSync('token')
      const savedUser = uni.getStorageSync('user')
      
      if (savedToken && savedUser) {
        token.value = savedToken
        try {
          user.value = typeof savedUser === 'string' ? JSON.parse(savedUser) : savedUser
        } catch (error) {
          console.error('Failed to parse user data:', error)
          clearAuth()
        }
      }
    } catch (error) {
      console.error('Failed to get storage:', error)
      clearAuth()
    }
  }

  async function register(payload: RegisterRequest): Promise<boolean> {
    loading.value = true
    errorMsg.value = null
    
    try {
      const { accountName, email, password, confirmPassword } = payload
      if (confirmPassword !== undefined && confirmPassword !== password) {
        errorMsg.value = '两次输入的密码不一致'
        return false
      }
      
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        API_ENDPOINTS.AUTH.REGISTER,
        { accountName, email, password }
      )
      
      if (response.data.code === 200 && response.data.data) {
        const authData = response.data.data
        saveAuth(authData)
        return true
      } else {
        errorMsg.value = response.data.message || '注册失败'
        return false
      }
    } catch (error: any) {
      console.error('Registration error:', error)
      errorMsg.value = error.response?.data?.message || error.message || '注册失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function login(payload: LoginRequest): Promise<boolean> {
    loading.value = true
    errorMsg.value = null
    
    try {
      const requestBody: LoginRequest = {
        accountName: payload.accountName,
        email: payload.email,
        password: payload.password
      }
      if (!requestBody.accountName && !requestBody.email) {
        errorMsg.value = '请输入邮箱或账号名称'
        return false
      }
      const response = await apiClient.post<ApiResponse<AuthResponse>>(
        API_ENDPOINTS.AUTH.LOGIN,
        requestBody
      )
      
      if (response.data.code === 200 && response.data.data) {
        const authData = response.data.data
        saveAuth(authData)
        return true
      } else {
        errorMsg.value = response.data.message || '登录失败'
        return false
      }
    } catch (error: any) {
      console.error('Login error:', error)
      // 不再使用 demo-token，所有环境统一要求真实后端登录
      const networkMsg: string | undefined =
        error?.userMessage ||
        error?.response?.data?.message ||
        error?.errMsg ||
        error?.message

      errorMsg.value = networkMsg || '登录失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function logout(): Promise<void> {
    loading.value = true
    errorMsg.value = null
    
    try {
      // 调用后端登出接口（如果token存在）
      if (token.value) {
        try {
          await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT)
          console.log('[Auth] 登出接口调用成功')
        } catch (error: any) {
          // 登出接口失败不影响登出流程（主要是前端清除token）
          // 404错误说明接口可能不存在，但这是正常的，因为JWT是无状态的
          if (error?.response?.status === 404) {
            console.warn('[Auth] 登出接口不存在（404），继续执行登出流程')
          } else {
            console.warn('[Auth] 登出接口调用失败，继续执行登出流程:', error?.message)
          }
        }
      }
    } catch (error) {
      console.warn('[Auth] 登出过程出错，继续清除本地认证信息:', error)
    } finally {
      // 无论后端调用成功与否，都清除本地认证信息
      clearAuth()
      loading.value = false
    }
  }

  function saveAuth(authData: AuthResponse): void {
    token.value = authData.token
    user.value = normalizeUser(authData)
    try {
      uni.setStorageSync('token', authData.token)
      uni.setStorageSync('user', JSON.stringify(user.value))
    } catch (error) {
      console.error('Failed to save auth data:', error)
    }
  }

  function clearAuth(): void {
    token.value = null
    user.value = null
    try {
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
    } catch (error) {
      console.error('Failed to clear auth data:', error)
    }
  }

  function updateUser(userData: Partial<User>): void {
    if (user.value) {
      user.value = { ...user.value, ...userData }
      try {
        uni.setStorageSync('user', JSON.stringify(user.value))
      } catch (error) {
        console.error('Failed to update user data:', error)
      }
    }
  }

  function normalizeUser(authData: AuthResponse): User {
    return {
      id: authData.userId ?? authData.accountName,
      accountName: authData.accountName,
      email: authData.email,
      playerLevel: user.value?.playerLevel ?? 1,
      playerExp: user.value?.playerExp ?? 0,
      status: user.value?.status ?? UserStatus.ACTIVE
    }
  }

  function setError(message: string | null) {
    errorMsg.value = message
  }

  function clearErrorMessage() {
    setError(null)
  }

  return {
    // 状态
    user: readonly(user),
    token: readonly(token),
    loading: readonly(loading),
    errorMsg,
    
    // 计算属性
    isAuthenticated,
    isLoggedIn,
    
    // 方法
    init,
    register,
    login,
    logout,
    updateUser,
    setError,
    clearError: clearErrorMessage
  }
})