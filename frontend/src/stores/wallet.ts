import { defineStore } from 'pinia'
import { ref } from 'vue'
import { walletApi } from '@/lib/api'
import { useAuthStore } from './auth'

export const useWalletStore = defineStore('wallet', () => {
  const gold = ref<number>(0)
  const loading = ref<boolean>(false)
  const errorMsg = ref<string | null>(null)

  async function init() {
    loading.value = true
    errorMsg.value = null
    try {
      const authStore = useAuthStore()
      if (!authStore.session) {
        gold.value = 0
        return
      }
      // 读取钱包
      const response = await walletApi.getGold()
      if (response.code === 200 && response.data !== undefined) {
        gold.value = response.data
      } else {
        errorMsg.value = response.message || '获取金币失败'
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || error.message || '获取金币失败'
      gold.value = 0
    } finally {
      loading.value = false
    }
  }

  async function spend(amount: number) {
    errorMsg.value = null
    if (amount <= 0) return true
    if (gold.value < amount) {
      errorMsg.value = '金币不足'
      return false
    }
    const authStore = useAuthStore()
    if (!authStore.session) {
      errorMsg.value = '请先登录'
      return false
    }
    try {
      const response = await walletApi.spend(amount)
      if (response.code === 200 && response.data) {
        gold.value -= amount
        return true
      } else {
        errorMsg.value = response.message || '消费失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || error.message || '消费失败'
      return false
    }
  }

  async function add(amount: number) {
    errorMsg.value = null
    if (amount <= 0) return true
    const authStore = useAuthStore()
    if (!authStore.session) {
      errorMsg.value = '请先登录'
      return false
    }
    try {
      const response = await walletApi.add(amount)
      if (response.code === 200 && response.data !== undefined) {
        gold.value = response.data
        return true
      } else {
        errorMsg.value = response.message || '增加金币失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || error.message || '增加金币失败'
      return false
    }
  }

  return { gold, loading, errorMsg, init, spend, add }
})