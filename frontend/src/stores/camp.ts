import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { campApi } from '@/lib/api'

// 营地数据接口定义
export interface CampData {
  userPlayerCharacter: any
  userCardCharacters: any[]
  userCards: any[]
  inventory: any[]
  wallets: any[]
  shopOffers: any[]
  availableSkills?: any[]
  unlockedSkills?: any[]
}

export const useCampStore = defineStore('camp', () => {
  // 状态
  const campData = ref<CampData | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)
  const lastUpdated = ref<Date | null>(null)

  // 计算属性
  const playerCharacter = computed(() => {
    const result = campData.value?.userPlayerCharacter || null
    // 只在调试时输出日志，避免过多日志
    if (process.env.NODE_ENV === 'development' && Math.random() < 0.1) {
    console.log('[CampStore] playerCharacter computed 被调用:', {
      hasCampData: !!campData.value,
      hasUserPlayerCharacter: !!campData.value?.userPlayerCharacter,
        result: result ? { 
          id: result.id, 
          name: result.playerCharacterName,
          currentHp: result.currentHp,
          maxHp: result.maxHp
        } : null
    })
    }
    return result
  })
  const availableCards = computed(() => campData.value?.userCards || [])
  const inventoryItems = computed(() => campData.value?.inventory || [])
  const shopOffers = computed(() => campData.value?.shopOffers || [])
  const wallets = computed(() => campData.value?.wallets || [])
  const userCardCharacters = computed(() => campData.value?.userCardCharacters || [])
  const userCards = computed(() => campData.value?.userCards || [])
  const hasUnclaimedRewards = computed(() => {
    if (!campData.value) return false
    // 检查是否有未领取的奖励
    return inventoryItems.value.some(item => item.isNew) || 
           availableCards.value.some(card => card.isNew)
  })

  // 动作
  const fetchCampData = async () => {
    try {
      loading.value = true
      error.value = null
      
      console.log('[CampStore] 开始获取营地数据...')
      const response = await campApi.getCampData()
      
      console.log('[CampStore] API响应:', {
        code: response.data.code,
        hasData: !!response.data.data,
        dataKeys: response.data.data ? Object.keys(response.data.data) : []
      })
      
      if (response.data.code === 200) {
        const data = response.data.data || null
        if (data) {
          console.log('[CampStore] 解析数据:', {
            hasUserPlayerCharacter: !!data.userPlayerCharacter,
            userPlayerCharacter: data.userPlayerCharacter ? {
              id: data.userPlayerCharacter.id,
              name: data.userPlayerCharacter.playerCharacterName,
              code: data.userPlayerCharacter.playerCharacterCode
            } : null
          })
          
          campData.value = {
            userPlayerCharacter: data.userPlayerCharacter || null,
            userCardCharacters: data.userCardCharacters || [],
            userCards: data.userCards || [],
            inventory: data.inventory || [],
            wallets: data.wallets || [],
            shopOffers: data.shopOffers || [],
            availableSkills: data.availableSkills || [],
            unlockedSkills: data.unlockedSkills || []
          }
          
          console.log('[CampStore] 数据已更新到store:', {
            hasCampData: !!campData.value,
            hasPlayerCharacter: !!campData.value?.userPlayerCharacter,
            playerCharacterId: campData.value?.userPlayerCharacter?.id
          })
        } else {
          console.warn('[CampStore] API返回的数据为空')
          campData.value = null
        }
        lastUpdated.value = new Date()
        return campData.value
      } else {
        throw new Error(response.data.message || '获取营地数据失败')
      }
    } catch (err: any) {
      console.error('[CampStore] 获取营地数据失败:', err)
      
      // 提取更详细的错误信息
      let errorMessage = '获取营地数据失败'
      if (err?.userMessage) {
        errorMessage = err.userMessage
      } else if (err?.response?.data?.message) {
        errorMessage = err.response.data.message
      } else if (err?.message) {
        errorMessage = err.message
      } else if (err?.code === 'ECONNREFUSED' || err?.code === 'ERR_NETWORK') {
        errorMessage = '无法连接到后端服务器，请检查服务是否正常运行'
      }
      
      error.value = errorMessage
      console.error('[CampStore] 错误详情:', {
        message: errorMessage,
        code: err?.code,
        status: err?.response?.status,
        url: err?.config?.url
      })
      throw err
    } finally {
      loading.value = false
    }
  }

  const updatePlayerCharacter = (updatedCharacter: any) => {
    if (campData.value) {
      campData.value.userPlayerCharacter = {
        ...campData.value.userPlayerCharacter,
        ...updatedCharacter
      }
    }
  }

  const updateInventory = (newInventory: any[]) => {
    if (campData.value) {
        campData.value.inventory = newInventory
    }
  }

  const addToInventory = (item: any) => {
    if (campData.value) {
      const existingIndex = campData.value.inventory.findIndex(inv => inv.id === item.id)
      if (existingIndex >= 0) {
        campData.value.inventory[existingIndex].quantity += item.quantity
      } else {
        campData.value.inventory.push(item)
      }
    }
  }

  const removeFromInventory = (itemId: string, quantity: number = 1) => {
    if (campData.value) {
      const index = campData.value.inventory.findIndex(inv => inv.id === itemId)
      if (index >= 0) {
        const item = campData.value.inventory[index]
        item.quantity -= quantity
        if (item.quantity <= 0) {
          campData.value.inventory.splice(index, 1)
        }
      }
    }
  }

  const updateShopOffers = (offers: any[]) => {
    if (campData.value) {
      campData.value.shopOffers = offers
    }
  }

  const clearError = () => {
    error.value = null
  }

  const reset = () => {
    campData.value = null
    loading.value = false
    error.value = null
    lastUpdated.value = null
  }

  return {
    // 状态
    campData,
    loading,
    error,
    lastUpdated,
    
    // 计算属性
    playerCharacter,
    availableCards,
    inventoryItems,
    shopOffers,
    hasUnclaimedRewards,
    wallets,
    userCardCharacters,
    userCards,
    
    // 动作
    fetchCampData,
    updatePlayerCharacter,
    updateInventory,
    addToInventory,
    removeFromInventory,
    updateShopOffers,
    clearError,
    reset
  }
})