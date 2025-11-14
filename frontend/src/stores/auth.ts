import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi, type AuthResponse } from '@/lib/api'

export interface Session {
  token: string
  userId: number
  username: string
  email: string
}

export const useAuthStore = defineStore('auth', () => {
  const session = ref<Session | null>(null)
  const loading = ref(false)
  const errorMsg = ref<string | null>(null)

  async function init() {
    // 从本地存储恢复会话
    const token = localStorage.getItem('token')
    const userId = localStorage.getItem('userId')
    const username = localStorage.getItem('username')
    const email = localStorage.getItem('email')
    
    if (token && userId && username && email) {
      session.value = {
        token,
        userId: Number(userId),
        username,
        email
      }
    }
  }

  async function signUp(username: string, email: string, password: string) {
    loading.value = true
    errorMsg.value = null
    try {
      const response = await authApi.register(username, email, password)
      if (response.code === 200 && response.data) {
        // 保存会话信息
        const authData = response.data
        localStorage.setItem('token', authData.token)
        localStorage.setItem('userId', authData.userId.toString())
        localStorage.setItem('username', authData.username)
        localStorage.setItem('email', authData.email)
        
        session.value = {
          token: authData.token,
          userId: authData.userId,
          username: authData.username,
          email: authData.email
        }
        return true
      } else {
        errorMsg.value = response.message || '注册失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || error.message || '注册失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function signInWithPassword(email: string, password: string) {
    loading.value = true
    errorMsg.value = null
    try {
      const response = await authApi.login(email, password)
      if (response.code === 200 && response.data) {
        // 保存会话信息
        const authData = response.data
        localStorage.setItem('token', authData.token)
        localStorage.setItem('userId', authData.userId.toString())
        localStorage.setItem('username', authData.username)
        localStorage.setItem('email', authData.email)
        
        session.value = {
          token: authData.token,
          userId: authData.userId,
          username: authData.username,
          email: authData.email
        }
        return true
      } else {
        errorMsg.value = response.message || '登录失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || error.message || '登录失败'
      return false
    } finally {
      loading.value = false
    }
  }

  async function signInWithEmail(email: string) {
    // 简化处理：提示用户使用密码登录
    errorMsg.value = '请使用密码登录'
    return false
  }

  async function signOut() {
    loading.value = true
    errorMsg.value = null
    try {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('email')
      session.value = null
    } catch (error: any) {
      errorMsg.value = error.message || '登出失败'
    } finally {
      loading.value = false
    }
  }

  return { session, loading, errorMsg, init, signUp, signInWithPassword, signInWithEmail, signOut }
})