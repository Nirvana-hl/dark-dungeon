import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/lib/api'
import type { User, LoginRequest, RegisterRequest, AuthResponse } from '@/types'
import { UserStatus } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)
  const errorMsg = ref<string | null>(null)

  // 计算属性
  const isAuthenticated = computed(() => !!token.value && !!user.value)
  const isLoggedIn = computed(() => isAuthenticated.value)

  async function init() {
    // 从本地存储恢复会话
    const savedToken = localStorage.getItem('token')
    const savedUser = localStorage.getItem('user')
    
    if (savedToken && savedUser) {
      token.value = savedToken
      try {
        user.value = JSON.parse(savedUser)
      } catch (error) {
        console.error('Failed to parse user data:', error)
        clearAuth()
      }
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
      errorMsg.value = error.response?.data?.message || error.message || '登录失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function logout(): Promise<void> {
    loading.value = true
    errorMsg.value = null
    
    try {
      // 调用后端登出接口
      if (token.value) {
        await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT)
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      // 无论后端调用成功与否，都清除本地认证信息
      clearAuth()
      loading.value = false
    }
  }

  function saveAuth(authData: AuthResponse): void {
    token.value = authData.token
    user.value = normalizeUser(authData)
    localStorage.setItem('token', authData.token)
    localStorage.setItem('user', JSON.stringify(user.value))
  }

  function clearAuth(): void {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function updateUser(userData: Partial<User>): void {
    if (user.value) {
      user.value = { ...user.value, ...userData }
      localStorage.setItem('user', JSON.stringify(user.value))
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