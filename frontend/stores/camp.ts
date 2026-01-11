import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { campApi, gameApi } from '@/api/request'

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
  const inventoryItems = computed(() => {
    const raw = campData.value?.inventory || []
    return raw.map((it: any) => {
      const name =
        it.name ||
        it.itemName ||
        it.item?.name ||
        it.item_name ||
        it.item?.itemName ||
        it.item?.item_name ||
        '未知道具'
      const description =
        it.description || it.item?.description || it.item_description || ''
      const itemType =
        it.itemType || it.type || it.item?.itemType || it.item?.type || 'consumable'
      const quantity = Number(it.quantity ?? it.count ?? 1) || 1
      const bindStatus =
        (it.bindStatus && (it.bindStatus === 'bound' ? 'bound' : 'unbound')) ||
        (it.bound ? 'bound' : it.bind_status) ||
        'unbound'
      const tags = it.tags || it.item?.tags || []
      return {
        id: it.id ?? it.inventoryId ?? it.itemId ?? null,
        name,
        description,
        itemType,
        quantity,
        bindStatus,
        tags,
        raw: it
      }
    })
  })
  const shopOffers = computed(() => campData.value?.shopOffers || [])
  const wallets = computed(() => campData.value?.wallets || [])
  const userCardCharacters = computed(() => {
    const raw = campData.value?.userCardCharacters || []
    // 统一映射角色卡牌字段到前端通用字段，与userCards保持一致
    return raw.map((char: any) => {
      return {
        id: char.id ?? char.userCardCharacterId ?? char.cardCharacterId ?? null,
        name: char.characterName ?? char.name ?? '未知角色',
        description: char.characterDescription ?? char.lore ?? '',
        cardType: 'character', // 角色卡牌类型固定为character
        rarity: char.characterRarity ?? 'common',
        manaCost: 3, // 角色卡牌默认3费
        hp: char.currentHp ?? char.baseHp ?? null, // 优先使用当前血量，否则使用基础血量
        attack: char.baseAttack ?? null, // 角色卡牌使用基础攻击力
        quantity: char.quantity ?? 1,
        // 角色特有字段
        characterName: char.characterName,
        characterDescription: char.characterDescription,
        characterRarity: char.characterRarity,
        characterClassType: char.characterClassType,
        characterCode: char.characterCode,
        baseHp: char.baseHp,
        baseAttack: char.baseAttack,
        baseArmor: char.baseArmor,
        currentHp: char.currentHp,
        currentArmor: char.currentArmor,
        currentStarLevel: char.currentStarLevel,
        isDeployed: char.isDeployed,
        deployedRound: char.deployedRound,
        // 保留原始数据以备特殊字段使用
        raw: char
      }
    })
  })
  const userCards = computed(() => {
    const raw = campData.value?.userCards || []
    // 统一映射后端字段到前端通用字段
    return raw.map((card: any) => {
      return {
        id: card.id ?? card.userCardId ?? card.cardId ?? null,
        name: card.cardName ?? card.name ?? card.card_title ?? '未知卡牌',
        description: card.cardDescription ?? card.description ?? card.card_desc ?? '',
        cardType: card.cardType ?? card.type ?? 'unknown',
        rarity: card.cardRarity ?? card.rarity ?? card.tier ?? 'common',
        manaCost: card.manaCost ?? card.actionPointCost ?? card.cost ?? 1,
        hp: card.hp ?? card.health ?? card.currentHp ?? null,
        attack: card.attack ?? card.atk ?? null,
        quantity: (card.quantity ?? card.count) == null ? 1 : Number(card.quantity ?? card.count),
        // 保留原始数据以备特殊字段使用
        raw: card
      }
    })
  })
  const hasUnclaimedRewards = computed(() => {
    if (!campData.value) return false
    // 检查是否有未领取的奖励（暂时简化逻辑，避免访问不存在的属性）
    return false
  })
  // 临时保存被移除的 userCards（当数量被减到 0 时），以便撤下时能准确恢复原始对象
  const removedUserCards = ref(new Map<string, any>())

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
          
          // 兼容不同后端字段命名：优先使用驼峰（data.userCards / data.userCardCharacters），
          // 如果后端使用下划线（user_cards / user_card_characters），也兼容并填充到相应字段
          campData.value = {
            userPlayerCharacter: data.userPlayerCharacter || data.user_player_character || null,
            userCardCharacters: data.userCardCharacters || data.user_card_characters || data.user_cards || data.userCardCharacters || [],
            userCards: data.userCards || data.user_cards || [],
            inventory: data.inventory || data.inventory_items || [],
            wallets: data.wallets || [],
            shopOffers: data.shopOffers || data.shop_offers || [],
            availableSkills: data.availableSkills || [],
            unlockedSkills: data.unlockedSkills || []
          }
          
          // 如果后端把角色/法术/装备等卡牌统一返回在 user_cards 下，合并入 userCards，并把角色卡分流到 userCardCharacters
          if (Array.isArray(data.user_cards) && data.user_cards.length > 0) {
            console.log('[CampStore] 发现 data.user_cards，合并到 userCards，count=', data.user_cards.length)
            // 避免重复：按 id 去重（支持多种 id 字段名）
            const existingIds = new Set((campData.value.userCards || []).map((c: any) => String(c.id ?? c.userCardId ?? c.cardId ?? '')))
            const toAdd = data.user_cards.filter((c: any) => {
              const id = String(c.id ?? c.userCardId ?? c.cardId ?? '')
              return id && !existingIds.has(id)
            })
            campData.value.userCards = [...(campData.value.userCards || []), ...toAdd]

            // 将类型为 character 的卡牌也加入 userCardCharacters（保持与其它类型处理一致）
            const charCandidates = toAdd.filter((c: any) => {
              const t = (c.cardType || c.type || '').toString().toLowerCase()
              return t === 'character' || t === '角色' || c.cardCharacterId || c.card_character_id
            })
            if (charCandidates.length > 0) {
              campData.value.userCardCharacters = [...(campData.value.userCardCharacters || []), ...charCandidates]
              console.log('[CampStore] 从 user_cards 中提取角色卡: count=', charCandidates.length)
            }
          }
          
          // 诊断日志：打印卡牌数组长度，帮助定位后端未返回角色卡牌的问题
          console.log('[CampStore] 后端返回卡牌统计:', {
            userCardsLength: Array.isArray(data.userCards) ? data.userCards.length : 0,
            userCardCharactersLength: Array.isArray(data.userCardCharacters) ? data.userCardCharacters.length : 0
          })

          // 如果后端在聚合接口中没有返回角色卡牌，尝试降级获取单独接口的数据（/camp/card-characters）
          if ((!data.userCardCharacters || data.userCardCharacters.length === 0)) {
            try {
              console.warn('[CampStore] 聚合接口未包含 userCardCharacters，尝试调用 /camp/card-characters 作为降级方案')
              const altResp = await campApi.getAvailableCardCharacters()
              console.log('[CampStore] /camp/card-characters 响应:', {
                code: altResp.data?.code,
                hasData: !!altResp.data?.data,
                sampleCount: Array.isArray(altResp.data?.data) ? altResp.data.data.length : 0
              })
              if (altResp.data?.code === 200 && Array.isArray(altResp.data.data) && altResp.data.data.length > 0) {
                campData.value.userCardCharacters = altResp.data.data
                console.log('[CampStore] 已用 /camp/card-characters 填充 userCardCharacters，count=', altResp.data.data.length)
              }
            } catch (fallbackErr) {
              console.warn('[CampStore] 降级获取角色卡牌失败:', fallbackErr)
            }
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

  // 修改用户卡牌数量（减少），用于上阵时乐观/持久化更新
  const decrementUserCardQuantity = (cardId: string | number, amount: number = 1) => {
    if (!campData.value) return
    const findAndAdjust = (arrName: keyof CampData) => {
      const arr: any[] = (campData.value as any)[arrName] || []
      const idx = arr.findIndex(item => String(item.id) === String(cardId) || String(item.userCardId) === String(cardId) || String(item.cardId) === String(cardId))
      if (idx >= 0) {
        const item = arr[idx]
        const curQty = Number(item.quantity ?? item.count ?? 1) || 0
        const newQty = curQty - amount
        console.log('[CampStore] decrementUserCardQuantity found in', String(arrName), 'id=', cardId, 'currentQty=', curQty, 'decrement=', amount, 'newQty=', newQty)
        if (newQty > 0) {
          item.quantity = newQty
        } else {
          // 保存被移除的完整对象以备恢复使用（避免丢失显示字段）
          try {
            const removed = arr[idx]
            // collect all possible id variants to index the cache so restores by any id succeed
            const ids = [
              removed?.id,
              removed?.userCardId,
              removed?.cardId,
              removed?.cardCharacterId,
              removed?.userCardCharacterId
            ].filter(i => i !== undefined && i !== null && String(i) !== '')
            if (ids.length > 0) {
              const stored = { ...removed, __keys: ids.map(i => String(i)) }
              ids.forEach(i => {
                try { removedUserCards.value.set(String(i), stored) } catch (e) {}
              })
              console.log('[CampStore] cached removed userCard for restore keys=', stored.__keys)
            }
          } catch (e) {
            console.warn('[CampStore] failed to cache removed userCard for restore', e)
          }
          arr.splice(idx, 1)
        }
        return true
      }
      return false
    }

    // 优先在 userCards 中查找
    if (!findAndAdjust('userCards')) {
      // 再尝试在 userCardCharacters 中查找
      findAndAdjust('userCardCharacters')
    }
  }

  // 恢复用户卡牌数量（增加），用于撤下时回滚/持久化更新
  const incrementUserCardQuantity = (cardId: string | number, amount: number = 1, insertIndex?: number, idCandidates?: (string|number)[]) => {
    if (!campData.value) return
    const arr: any[] = campData.value.userCards || []
    const idx = arr.findIndex(item => String(item.id) === String(cardId) || String(item.userCardId) === String(cardId) || String(item.cardId) === String(cardId))
    if (idx >= 0) {
      const item = arr[idx]
      const oldQty = Number(item.quantity ?? item.count ?? 0) || 0
      item.quantity = oldQty + amount
      console.log('[CampStore] incrementUserCardQuantity adjusted existing userCards id=', cardId, 'oldQty=', oldQty, 'inc=', amount, 'newQty=', item.quantity)
    } else {
      // 如果不存在，先尝试从已被移除的缓存中恢复（当卡牌被减到 0 时我们会缓存原始对象）
      const candidates = [String(cardId)].concat((idCandidates || []).map(i => String(i)))
      let foundStored: any = null
      let foundKey: string | null = null
      for (const c of candidates) {
        if (removedUserCards.value.has(c)) {
          foundStored = removedUserCards.value.get(c)
          foundKey = c
          break
        }
      }
      if (foundStored) {
        const stored = foundStored
        const restored = { ...stored }
        // ensure display fields exist to avoid '未知卡牌'
        restored.id = restored.id ?? restored.userCardId ?? restored.cardId ?? restored.cardCharacterId ?? restored.userCardCharacterId ?? null
        restored.name = restored.name || restored.cardName || restored.characterName || restored.cardCharacterName || '未知卡牌'
        restored.cardName = restored.cardName || restored.name
        restored.cardType = restored.cardType || restored.type || (restored.cardCharacterId || restored.userCardCharacterId ? 'character' : 'unknown')
        restored.rarity = restored.rarity || restored.cardRarity || restored.characterRarity || 'common'
        restored.manaCost = restored.manaCost ?? restored.actionPointCost ?? restored.cost ?? 1
        restored.hp = restored.hp ?? restored.health ?? restored.currentHp ?? restored.baseHp ?? null
        restored.attack = restored.attack ?? restored.atk ?? restored.baseAttack ?? null
        restored.raw = restored.raw || stored
        restored.quantity = amount
        if (typeof insertIndex === 'number' && insertIndex >= 0 && insertIndex <= arr.length) {
          const newArr = [...arr]
          newArr.splice(insertIndex, 0, restored)
          campData.value.userCards = newArr
        } else {
          campData.value.userCards = [...arr, restored]
        }
        // remove all cache entries for this stored object
        try {
          const keys: string[] = stored?.__keys || [String(foundKey)]
          keys.forEach(k => removedUserCards.value.delete(String(k)))
        } catch (e) {}
        console.log('[CampStore] incrementUserCardQuantity restored from removed cache id=', cardId, 'qty=', restored.quantity, 'matchedKey=', foundKey)
        return
      }
      // 如果不存在，尝试在角色卡数组中增加或加入 userCards（如果提供 insertIndex 则插回原位）
      const chars = campData.value.userCardCharacters || []
      const cidx = chars.findIndex(item => String(item.id) === String(cardId) || String(item.cardCharacterId) === String(cardId))
      if (cidx >= 0) {
        const char = { ...chars[cidx] }
        // normalize some display fields to avoid '未知卡牌' when moving between arrays
        char.name = char.name || char.cardName || char.characterName || char.cardCharacterName || '未知卡牌'
        char.cardName = char.cardName || char.name
        char.cardType = char.cardType || char.type || 'character'
        // ensure rarity and stats are properly restored
        char.rarity = char.rarity || char.cardRarity || char.characterRarity || 'common'
        char.hp = char.hp ?? char.health ?? char.currentHp ?? char.baseHp ?? null
        char.attack = char.attack ?? char.atk ?? char.baseAttack ?? null
        char.raw = char.raw || chars[cidx]
        char.quantity = Number(char.quantity ?? 0) + amount
        // Insert into userCards at requested index or append
        if (typeof insertIndex === 'number' && insertIndex >= 0 && insertIndex <= arr.length) {
          const newArr = [...arr]
          newArr.splice(insertIndex, 0, char)
          campData.value.userCards = newArr
          console.log('[CampStore] incrementUserCardQuantity inserted from userCardCharacters at index=', insertIndex, 'id=', cardId, 'qty=', char.quantity)
        } else {
          campData.value.userCards = [...arr, char]
          console.log('[CampStore] incrementUserCardQuantity added from userCardCharacters id=', cardId, 'qty=', char.quantity)
        }

        // Also decrement or remove the original entry in userCardCharacters to avoid double-counting
        try {
          const newChars = [...chars]
          const originalQty = Number(newChars[cidx].quantity ?? newChars[cidx].count ?? 0) || 0
          const remaining = originalQty - amount
          if (remaining > 0) {
            newChars[cidx] = { ...newChars[cidx], quantity: remaining }
          } else {
            newChars.splice(cidx, 1)
          }
          campData.value.userCardCharacters = newChars
          console.log('[CampStore] adjusted userCardCharacters after incrementUserCardQuantity for id=', cardId, 'remaining=', remaining)
        } catch (e) {
          console.warn('[CampStore] failed to adjust userCardCharacters after increment', e)
        }
      } else {
        // 如果完全没找到，不能恢复；仅记录日志
        console.warn('[CampStore] incrementUserCardQuantity: 未找到卡片以增加数量', cardId)
      }
    }
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
    ,
    // 调整用户卡牌数量的工具方法（用于上阵/撤下的乐观更新或回滚）
    decrementUserCardQuantity,
    incrementUserCardQuantity
  }
})