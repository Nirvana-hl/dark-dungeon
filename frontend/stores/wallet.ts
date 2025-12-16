import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/api/request'
import { useAuthStore } from './auth'
import type { UserWallet } from '@/types'
import { CurrencyType } from '@/types'

export const useWalletStore = defineStore('wallet', () => {
  // 状态
  const wallets = ref<UserWallet[]>([])
  const loading = ref(false)
  const errorMsg = ref<string | null>(null)

  // 计算属性
  const isAuthenticated = computed(() => {
    const authStore = useAuthStore()
    return authStore.isAuthenticated
  })

  // 获取指定货币的钱包余额
  const getBalance = (currencyType: CurrencyType): bigint => {
    const wallet = wallets.value.find(w => w.currencyType === currencyType)
    return wallet?.balance || 0n
  }

  // 金币余额
  const goldBalance = computed(() => getBalance(CurrencyType.GOLD))
  
  // 魂晶余额
  const soulstoneBalance = computed(() => getBalance(CurrencyType.SOULSTONE))

  // 加载钱包信息
  async function loadWallets(): Promise<void> {
    if (!isAuthenticated.value) return

    loading.value = true
    errorMsg.value = null

    try {
      const response = await apiClient.get<ApiResponse<UserWallet[]>>(
        API_ENDPOINTS.WALLET.INFO
      )

      if (response.data.code === 200 && response.data.data) {
        wallets.value = response.data.data
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '加载钱包信息失败'
      console.error('Load wallets error:', error)
    } finally {
      loading.value = false
    }
  }

  // 增加货币
  async function addCurrency(
    currencyType: CurrencyType, 
    amount: bigint, 
    reason?: string
  ): Promise<boolean> {
    if (!isAuthenticated.value) return false

    loading.value = true
    errorMsg.value = null

    try {
      const response = await apiClient.post<ApiResponse<UserWallet>>(
        API_ENDPOINTS.WALLET.ADD,
        {
          currencyType,
          amount: amount.toString(),
          reason
        }
      )

      if (response.data.code === 200 && response.data.data) {
        // 更新本地状态
        const existingIndex = wallets.value.findIndex(
          w => w.currencyType === currencyType
        )
        if (existingIndex >= 0) {
          wallets.value[existingIndex] = response.data.data
        } else {
          wallets.value.push(response.data.data)
        }
        return true
      } else {
        errorMsg.value = response.data.message || '增加货币失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '增加货币失败'
      console.error('Add currency error:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 消费货币
  async function consumeCurrency(
    currencyType: CurrencyType, 
    amount: bigint, 
    reason?: string
  ): Promise<boolean> {
    if (!isAuthenticated.value) return false

    // 检查余额是否足够
    if (getBalance(currencyType) < amount) {
      errorMsg.value = '余额不足'
      return false
    }

    loading.value = true
    errorMsg.value = null

    try {
      const response = await apiClient.post<ApiResponse<UserWallet>>(
        API_ENDPOINTS.WALLET.CONSUME,
        {
          currencyType,
          amount: amount.toString(),
          reason
        }
      )

      if (response.data.code === 200 && response.data.data) {
        // 更新本地状态
        const existingIndex = wallets.value.findIndex(
          w => w.currencyType === currencyType
        )
        if (existingIndex >= 0) {
          wallets.value[existingIndex] = response.data.data
        } else {
          wallets.value.push(response.data.data)
        }
        return true
      } else {
        errorMsg.value = response.data.message || '消费货币失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '消费货币失败'
      console.error('Consume currency error:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 检查是否可以消费指定金额
  function canConsume(currencyType: CurrencyType, amount: bigint): boolean {
    return getBalance(currencyType) >= amount
  }

  // 格式化余额显示
  function formatBalance(balance: bigint): string {
    if (balance >= 1000000n) {
      return `${(balance / 1000000n).toString()}M`
    } else if (balance >= 1000n) {
      return `${(balance / 1000n).toString()}K`
    } else {
      return balance.toString()
    }
  }

  // 简单的添加方法（用于Explore.vue）
  async function add(amount: number): Promise<boolean> {
    // 调用真实 API 增加货币
    if (amount > 0) {
      try {
        const success = await addCurrency('gold', BigInt(amount), 'explore_reward')
        return success
      } catch (error) {
        console.error('增加货币失败:', error)
        return false
      }
    }
    return false
  }

  // 初始化时尝试加载
  loadWallets().catch(() => {})

  return {
    // 状态
    wallets: readonly(wallets),
    loading: readonly(loading),
    errorMsg: readonly(errorMsg),
    
    // 计算属性
    goldBalance,
    soulstoneBalance,
    
    // 方法
    getBalance,
    loadWallets,
    addCurrency,
    consumeCurrency,
    canConsume,
    formatBalance,
    add // 添加这个方法供Explore.vue使用
  }
})