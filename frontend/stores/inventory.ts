import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { campApi } from '@/api/request'
import type { ApiResponse } from '@/api/request'

// 背包物品接口定义
export interface InventoryItem {
  id: string
  name: string
  itemType: 'consumable' | 'material' | 'equipment' | 'special'
  description: string
  quantity: number
  bindStatus: 'bound' | 'unbound'
  tags: string[]
  lastUpdatedAt?: string
  isNew?: boolean
}

export const useInventoryStore = defineStore('inventory', () => {
  // 状态
  const inventory = ref<InventoryItem[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 计算属性
  const consumableItems = computed(() => 
    inventory.value.filter(item => item.itemType === 'consumable')
  )
  
  const materialItems = computed(() => 
    inventory.value.filter(item => item.itemType === 'material')
  )
  
  const equipmentItems = computed(() => 
    inventory.value.filter(item => item.itemType === 'equipment')
  )
  
  const specialItems = computed(() => 
    inventory.value.filter(item => item.itemType === 'special')
  )
  
  const itemCountByType = computed(() => ({
    consumable: consumableItems.value.length,
    material: materialItems.value.length,
    equipment: equipmentItems.value.length,
    special: specialItems.value.length
  }))
  
  const totalItems = computed(() => inventory.value.length)

  // 动作
  const fetchInventory = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await campApi.getInventory()
      
      if (response.data.code === 200) {
        inventory.value = response.data.data || []
      } else {
        throw new Error(response.data.message || '获取背包数据失败')
      }
    } catch (err) {
      console.error('获取背包数据失败:', err)
      error.value = err instanceof Error ? err.message : '获取背包数据失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const useItem = async (itemId: string) => {
    try {
      loading.value = true
      error.value = null
      
      const response = await campApi.useItem(itemId)
      
      if (response.data.code === 200) {
        // 从背包中移除或减少数量
        const itemIndex = inventory.value.findIndex(item => item.id === itemId)
        if (itemIndex >= 0) {
          const item = inventory.value[itemIndex]
          item.quantity -= 1
          if (item.quantity <= 0) {
            inventory.value.splice(itemIndex, 1)
          }
        }
        return response.data
      } else {
        throw new Error(response.data.message || '使用物品失败')
      }
    } catch (err) {
      console.error('使用物品失败:', err)
      error.value = err instanceof Error ? err.message : '使用物品失败'
      throw err
    } finally {
      loading.value = false
    }
  }

  const addToInventory = (newItem: Partial<InventoryItem>) => {
    const item: InventoryItem = {
      id: newItem.id || `inv-${Date.now()}`,
      name: newItem.name || '',
      itemType: newItem.itemType || 'consumable',
      description: newItem.description || '',
      quantity: newItem.quantity || 1,
      bindStatus: newItem.bindStatus || 'unbound',
      tags: newItem.tags || [],
      lastUpdatedAt: new Date().toISOString(),
      isNew: true,
      ...newItem
    }

    // 检查是否已存在相同物品
    const existingIndex = inventory.value.findIndex(inv => 
      inv.name === item.name && inv.itemType === item.itemType
    )
    
    if (existingIndex >= 0) {
      inventory.value[existingIndex].quantity += item.quantity
    } else {
      inventory.value.push(item)
    }
  }

  const removeFromInventory = (itemId: string, quantity: number = 1) => {
    const index = inventory.value.findIndex(item => item.id === itemId)
    if (index >= 0) {
      const item = inventory.value[index]
      item.quantity -= quantity
      if (item.quantity <= 0) {
        inventory.value.splice(index, 1)
      }
    }
  }

  const updateItem = (itemId: string, updates: Partial<InventoryItem>) => {
    const index = inventory.value.findIndex(item => item.id === itemId)
    if (index >= 0) {
      inventory.value[index] = {
        ...inventory.value[index],
        ...updates,
        lastUpdatedAt: new Date().toISOString()
      }
    }
  }

  const clearError = () => {
    error.value = null
  }

  const reset = () => {
    inventory.value = []
    loading.value = false
    error.value = null
  }

  return {
    // 状态
    inventory,
    loading,
    error,
    
    // 计算属性
    consumableItems,
    materialItems,
    equipmentItems,
    specialItems,
    itemCountByType,
    totalItems,
    
    // 动作
    fetchInventory,
    useItem,
    addToInventory,
    removeFromInventory,
    updateItem,
    clearError,
    reset
  }
})