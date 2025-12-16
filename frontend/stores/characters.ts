import { defineStore } from 'pinia'
import { ref, computed, readonly } from 'vue'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/api/request'
import { useAuthStore } from './auth'
import type { 
  UserPlayerCharacter, 
  CardCharacter, 
  UserCardCharacter,
  CharacterClass,
  Faction,
  Rarity 
} from '@/types'

export const useCharactersStore = defineStore('characters', () => {
  // 状态
  const playerCharacter = ref<UserPlayerCharacter | null>(null)
  const cardCharacters = ref<UserCardCharacter[]>([])
  const availableCardCharacterTemplates = ref<CardCharacter[]>([])
  const selectedCardCharacterId = ref<string | null>(null)
  const loading = ref<boolean>(false)
  const errorMsg = ref<string | null>(null)

  // 计算属性
  const selectedCardCharacter = computed(() => 
    cardCharacters.value.find(c => c.id === selectedCardCharacterId.value) || null
  )
  
  // 为Explore.vue提供selected属性
  const selected = computed(() => {
    // 返回当前选中的角色，如果没有则返回playerCharacter
    return selectedCardCharacter.value || playerCharacter.value || {
      attrs: {
        exp: 0,
        level: 1
      }
    }
  })

  const isAuthenticated = computed(() => {
    const authStore = useAuthStore()
    return authStore.isAuthenticated
  })

  // 加载玩家角色信息
  async function loadPlayerCharacter(): Promise<void> {
    if (!isAuthenticated.value) return

    loading.value = true
    errorMsg.value = null

    try {
      const response = await apiClient.get<ApiResponse<UserPlayerCharacter>>(
        API_ENDPOINTS.CHARACTER.PLAYER_INSTANCE
      )

      if (response.data.code === 200 && response.data.data) {
        playerCharacter.value = response.data.data
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '加载玩家角色失败'
      console.error('Load player character error:', error)
    } finally {
      loading.value = false
    }
  }

  // 加载卡牌角色列表
  async function loadCardCharacters(): Promise<void> {
    if (!isAuthenticated.value) return

    loading.value = true
    errorMsg.value = null

    try {
      const response = await apiClient.get<ApiResponse<UserCardCharacter[]>>(
        API_ENDPOINTS.CHARACTER.CARD_INSTANCE
      )

      if (response.data.code === 200 && response.data.data) {
        cardCharacters.value = response.data.data
        if (cardCharacters.value.length > 0 && !selectedCardCharacterId.value) {
          selectedCardCharacterId.value = cardCharacters.value[0].id
        }
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '加载卡牌角色失败'
      console.error('Load card characters error:', error)
    } finally {
      loading.value = false
    }
  }

  // 加载可购买的卡牌角色模板
  async function loadCardCharacterTemplates(): Promise<void> {
    loading.value = true
    errorMsg.value = null

    try {
      const response = await apiClient.get<ApiResponse<CardCharacter[]>>(
        API_ENDPOINTS.CHARACTER.CARD
      )

      if (response.data.code === 200 && response.data.data) {
        availableCardCharacterTemplates.value = response.data.data
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '加载卡牌角色模板失败'
      console.error('Load card character templates error:', error)
    } finally {
      loading.value = false
    }
  }

  // 选择卡牌角色
  function selectCardCharacter(id: string): void {
    selectedCardCharacterId.value = id
  }

  // 上阵/下阵卡牌角色
  async function toggleDeploy(cardCharacterId: string, round: number): Promise<boolean> {
    const cardCharacter = cardCharacters.value.find(c => c.id === cardCharacterId)
    if (!cardCharacter || !playerCharacter.value) return false

    // 检查行动点是否足够
    if (!cardCharacter.isDeployed) {
      // 需要上阵地，检查行动点
      const template = availableCardCharacterTemplates.value.find(
        t => t.id === cardCharacter.cardCharacterId
      )
      if (template && playerCharacter.value.currentActionPoints < template.actionPointCost) {
        errorMsg.value = '行动点不足'
        return false
      }
    }

    try {
      const response = await apiClient.post<ApiResponse<boolean>>(
        `${API_ENDPOINTS.CHARACTER.CARD_INSTANCE}/${cardCharacterId}/toggle-deploy`,
        { round }
      )

      if (response.data.code === 200 && response.data.data) {
        // 更新本地状态
        cardCharacter.isDeployed = !cardCharacter.isDeployed
        cardCharacter.deployedRound = cardCharacter.isDeployed ? round : 0

        // 更新玩家行动点
        if (cardCharacter.isDeployed) {
          const template = availableCardCharacterTemplates.value.find(
            t => t.id === cardCharacter.cardCharacterId
          )
          if (template) {
            playerCharacter.value.currentActionPoints -= template.actionPointCost
          }
        }

        errorMsg.value = null
        return true
      } else {
        errorMsg.value = response.data.message || '操作失败'
        return false
      }
    } catch (error: any) {
      errorMsg.value = error.response?.data?.message || '操作失败'
      console.error('Toggle deploy error:', error)
      return false
    }
  }

  // 重置行动点（新回合开始时调用）
  function resetActionPoints(): void {
    if (playerCharacter.value) {
      playerCharacter.value.currentActionPoints = playerCharacter.value.maxActionPoints
    }
  }

  // 重置卡牌上阵状态（新回合开始时调用）
  function resetDeployStatus(round: number): void {
    cardCharacters.value.forEach(card => {
      if (card.deployedRound < round) {
        card.isDeployed = false
        card.deployedRound = 0
      }
    })
  }

  // 加载所有角色数据
  async function loadAll(): Promise<void> {
    if (!isAuthenticated.value) return

    await Promise.all([
      loadPlayerCharacter(),
      loadCardCharacters(),
      loadCardCharacterTemplates()
    ])
  }

  // 初始化
  loadAll().catch(() => {})

  return {
    // 状态
    playerCharacter: readonly(playerCharacter),
    cardCharacters: readonly(cardCharacters),
    availableCardCharacterTemplates: readonly(availableCardCharacterTemplates),
    selectedCardCharacterId: readonly(selectedCardCharacterId),
    selectedCardCharacter,
    selected, // 添加这个属性供Explore.vue使用
    loading: readonly(loading),
    errorMsg: readonly(errorMsg),

    // 方法
    loadPlayerCharacter,
    loadCardCharacters,
    loadCardCharacterTemplates,
    selectCardCharacter,
    toggleDeploy,
    resetActionPoints,
    resetDeployStatus,
    loadAll
  }
})