import { defineStore } from 'pinia'
import { ref } from 'vue'
import { supabase } from '@/lib/supabase'

export const useAuthStore = defineStore('auth', () => {
  const session = ref<import('@supabase/supabase-js').Session | null>(null)
  const loading = ref(false)
  const errorMsg = ref<string | null>(null)

  async function init() {
    const { data } = await supabase.auth.getSession()
    session.value = data.session ?? null
    // 订阅会话变化
    supabase.auth.onAuthStateChange((_event, s) => {
      session.value = s ?? null
    })
  }

  async function signInWithEmail(email: string) {
    loading.value = true
    errorMsg.value = null
    // 魔法链接（email）
    const { error } = await supabase.auth.signInWithOtp({
      email,
      options: {
        // 确保在 Supabase Auth 设置中配置 SITE_URL，magic link 将重定向回来
        emailRedirectTo: window.location.origin
      }
    })
    if (error) {
      errorMsg.value = error.message
    }
    loading.value = false
  }

  async function signUp(email: string, password: string) {
    loading.value = true
    errorMsg.value = null
    const { error } = await supabase.auth.signUp({ email, password })
    if (error) errorMsg.value = error.message
    loading.value = false
  }

  async function signInWithPassword(email: string, password: string) {
    loading.value = true
    errorMsg.value = null
    const { error } = await supabase.auth.signInWithPassword({ email, password })
    if (error) errorMsg.value = error.message
    loading.value = false
  }

  async function signOut() {
    loading.value = true
    errorMsg.value = null
    const { error } = await supabase.auth.signOut()
    if (error) errorMsg.value = error.message
    loading.value = false
  }

  return { session, loading, errorMsg, init, signUp, signInWithPassword, signInWithEmail, signOut }
})