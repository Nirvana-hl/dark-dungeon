import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { campApi } from '@/lib/api'
import type { ApiResponse } from '@/lib/api'

// 商品接口定义
export interface ShopOffer {
  id: string
  name: string
  itemType: 'consumable' | 'material' | 'equipment' | 'special'
  description: string
  price: number
  discount?: number
  currencyType: 'gold' | 'soulstone' | 'crystal'
  tags: string[]
  stock?: number
  isLimitedTime?: boolean
  endTime?: string
  isNew?: boolean
}

// 购买结果接口定义
export interface PurchaseResult {
  success: boolean
  message: string
  item?: ShopOffer
  newBalance?: {
    [key: string]: number
  }
}

export const useShopStore = defineStore('shop', () => {
  // 状态
  const offers = ref<ShopOffer[]>([])
  const loading = ref(false)
  const purchasing = ref(false)
  const error = ref<string | null>(null)
  const lastUpdated = ref<Date | null>(null)

  // 计算属性
  const consumableOffers = computed(() => 
    offers.value.filter(offer => offer.itemType === 'consumable')
  )
  
  const materialOffers = computed(() => 
    offers.value.filter(offer => offer.itemType === 'material')
  )
  
  const equipmentOffers = computed(() => 
    offers.value.filter(offer => offer.itemType === 'equipment')
  )
  
  const specialOffers = computed(() => 
    offers.value.filter(offer => offer.itemType === 'special')
  )
  
  const discountedOffers = computed(() => 
    offers.value.filter(offer => offer.discount && offer.discount > 0)
  )
  
  const limitedTimeOffers = computed(() => 
    offers.value.filter(offer => offer.isLimitedTime)
  )
  
  const offerCountByType = computed(() => ({
    consumable: consumableOffers.value.length,
    material: materialOffers.value.length,
    equipment: equipmentOffers.value.length,
    special: specialOffers.value.length
  }))

  const getDiscountedPrice = (offer: ShopOffer): number => {
    if (!offer.discount) return offer.price
    return Math.floor(offer.price * (1 - offer.discount / 100))
  }

  // 动作
  const fetchShopOffers = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await campApi.getShopOffers()
      
      if (response.data.code === 200) {
        // 将后端返回的 ShopOfferDetailDTO 转换为前端的 ShopOffer 格式
        const backendOffers = response.data.data || []
        offers.value = backendOffers.map((backendOffer: any) => {
          // 根据 offerType 获取关联的商品信息
          let name = '未知商品'
          let description = ''
          let itemType: 'consumable' | 'material' | 'equipment' | 'special' = 'consumable'
          const tags: string[] = []
          
          if (backendOffer.item) {
            name = backendOffer.item.name || name
            description = backendOffer.item.description || description
            // 根据道具类型映射到前端分类
            const itemTypeMap: { [key: string]: 'consumable' | 'material' | 'equipment' | 'special' } = {
              'consumable': 'consumable',
              'material': 'material',
              'equipment': 'equipment',
              'special': 'special'
            }
            itemType = itemTypeMap[backendOffer.item.itemType] || 'consumable'
            if (backendOffer.item.tags) {
              tags.push(...backendOffer.item.tags)
            }
          } else if (backendOffer.card) {
            name = backendOffer.card.name || name
            description = backendOffer.card.description || description
            itemType = 'equipment' // 卡牌归类为装备
            tags.push('卡牌')
          } else if (backendOffer.cardCharacter) {
            name = backendOffer.cardCharacter.name || name
            description = backendOffer.cardCharacter.description || description
            itemType = 'special' // 角色归类为特殊商品
            tags.push('角色')
          }
          
          return {
            id: String(backendOffer.id),
            name: name,
            itemType: itemType,
            offerType: backendOffer.offerType || null, // 保留原始 offerType 字段
            description: description,
            price: Number(backendOffer.price || 0),
            discount: undefined, // 后端暂无折扣字段
            currencyType: 'gold' as const, // 默认使用金币，后端暂无货币类型字段
            tags: tags,
            stock: undefined, // 后端暂无库存字段
            isLimitedTime: false,
            endTime: undefined,
            isNew: false,
            // 保留后端原始数据，用于筛选
            cardCharacter: backendOffer.cardCharacter,
            item: backendOffer.item,
            card: backendOffer.card
          }
        })
        lastUpdated.value = new Date()
        console.log('[ShopStore] 商品数据已转换:', {
          count: offers.value.length,
          offers: offers.value
        })
      } else {
        throw new Error(response.data.message || '获取商城数据失败')
      }
    } catch (err) {
      console.error('[ShopStore] 获取商城数据失败:', err)
      error.value = err instanceof Error ? err.message : '获取商城数据失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const purchaseItem = async (offerId: string, quantity: number = 1): Promise<PurchaseResult> => {
    try {
      console.log('[ShopStore] 开始购买流程:', { offerId, quantity })
      purchasing.value = true
      error.value = null
      
      console.log('[ShopStore] 调用 campApi.purchaseItem...')
      const response = await campApi.purchaseItem(offerId, quantity)
      
      console.log('[ShopStore] 购买API响应:', {
        code: response.data.code,
        message: response.data.message,
        data: response.data.data
      })
      
      if (response.data.code === 200) {
        const result: PurchaseResult = {
          success: true,
          message: response.data.message || '购买成功',
          ...response.data.data
        }
        
        console.log('[ShopStore] ✓ 购买成功:', result)
        return result
      } else {
        console.error('[ShopStore] ✗ 购买失败，响应码不是200:', response.data)
        throw new Error(response.data.message || '购买失败')
      }
    } catch (err: any) {
      console.error('[ShopStore] ✗ 购买异常:', {
        error: err,
        message: err?.message,
        response: err?.response?.data,
        status: err?.response?.status,
        userMessage: err?.userMessage
      })
      const result: PurchaseResult = {
        success: false,
        message: err?.userMessage || err?.response?.data?.message || err?.message || '购买失败'
      }
      
      error.value = result.message
      return result
    } finally {
      purchasing.value = false
      console.log('[ShopStore] 购买流程结束')
    }
  }

  const refreshOffers = () => {
    fetchShopOffers()
  }

  const canAfford = (offer: ShopOffer, userBalance: { [key: string]: number }): boolean => {
    const price = getDiscountedPrice(offer)
    const balance = userBalance[offer.currencyType] || 0
    return balance >= price
  }

  const getAffordabilityStatus = (offer: ShopOffer, userBalance: { [key: string]: number }) => {
    const price = getDiscountedPrice(offer)
    const balance = userBalance[offer.currencyType] || 0
    
    if (balance >= price) return 'affordable'
    if (balance > 0) return 'insufficient'
    return 'no_currency'
  }

  const addOffer = (newOffer: ShopOffer) => {
    const existingIndex = offers.value.findIndex(offer => offer.id === newOffer.id)
    if (existingIndex >= 0) {
      offers.value[existingIndex] = { ...newOffer, isNew: false }
    } else {
      offers.value.push({ ...newOffer, isNew: true })
    }
  }

  const removeOffer = (offerId: string) => {
    const index = offers.value.findIndex(offer => offer.id === offerId)
    if (index >= 0) {
      offers.value.splice(index, 1)
    }
  }

  const updateOffer = (offerId: string, updates: Partial<ShopOffer>) => {
    const index = offers.value.findIndex(offer => offer.id === offerId)
    if (index >= 0) {
      offers.value[index] = {
        ...offers.value[index],
        ...updates
      }
    }
  }

  const clearError = () => {
    error.value = null
  }

  const reset = () => {
    offers.value = []
    loading.value = false
    purchasing.value = false
    error.value = null
    lastUpdated.value = null
  }

  return {
    // 状态
    offers,
    loading,
    purchasing,
    error,
    lastUpdated,
    
    // 计算属性
    consumableOffers,
    materialOffers,
    equipmentOffers,
    specialOffers,
    discountedOffers,
    limitedTimeOffers,
    offerCountByType,
    
    // 动作
    fetchShopOffers,
    purchaseItem,
    refreshOffers,
    getDiscountedPrice,
    canAfford,
    getAffordabilityStatus,
    addOffer,
    removeOffer,
    updateOffer,
    clearError,
    reset
  }
})