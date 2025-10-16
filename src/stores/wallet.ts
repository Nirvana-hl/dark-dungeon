import { defineStore } from 'pinia'
import { ref } from 'vue'
import { supabase } from '@/lib/supabase'

export const useWalletStore = defineStore('wallet', () => {
  const gold = ref<number>(0)
  const loading = ref<boolean>(false)
  const errorMsg = ref<string | null>(null)

  async function init() {
    loading.value = true
    errorMsg.value = null
    try {
      const { data: userData } = await supabase.auth.getUser()
      const uid = userData.user?.id
      if (!uid) {
        gold.value = 0
        return
      }
      // 读取钱包
      const { data, error } = await supabase.from('user_wallets').select('gold').single()
      if (error) {
        // 不存在则初始化
        const { error: upErr } = await supabase.from('user_wallets').upsert({ user_id: uid, gold: 500 })
        if (upErr) {
          errorMsg.value = upErr.message
          return
        }
        gold.value = 500
      } else {
        gold.value = Number(data?.gold ?? 0)
      }
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
    const { data: userData } = await supabase.auth.getUser()
    const uid = userData.user?.id
    if (!uid) {
      errorMsg.value = '请先登录'
      return false
    }
    const newGold = gold.value - amount
    const { error } = await supabase.from('user_wallets').update({ gold: newGold }).eq('user_id', uid)
    if (error) {
      errorMsg.value = error.message
      return false
    }
    gold.value = newGold
    return true
  }

  async function add(amount: number) {
    errorMsg.value = null
    if (amount <= 0) return true
    const { data: userData } = await supabase.auth.getUser()
    const uid = userData.user?.id
    if (!uid) {
      errorMsg.value = '请先登录'
      return false
    }
    const newGold = gold.value + amount
    const { error } = await supabase.from('user_wallets').update({ gold: newGold }).eq('user_id', uid)
    if (error) {
      errorMsg.value = error.message
      return false
    }
    gold.value = newGold
    return true
  }

  return { gold, loading, errorMsg, init, spend, add }
})