<template>
  <view class="inventory-page">
    <!-- 导航栏 -->
    <view class="inventory-tabs">
      <view
        v-for="tab in inventoryTabs"
        :key="tab.key"
        :class="['inventory-tab', { active: activeTab === tab.key }]"
        @click="activeTab = tab.key"
      >
        <i :class="tab.icon"></i>
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 内容区域 -->
    <view class="inventory-content">
      <!-- 卡牌标签页 -->
      <view v-if="activeTab === 'cards'" class="tab-content">
        <view class="tab-header">
          <h2>卡牌管理</h2>
          <p class="subtitle">管理你的卡牌收藏与上阵卡组，点击卡牌以上阵/移除。</p>
        </view>

        <CardDeckManager
          :userCards="combinedCards"
          :equippedCards="equippedCards"
          :deckLimit="deckLimit"
          @equipCard="handleEquipCard"
          @unequipCard="handleUnequipCard"
        />
      </view>

      <!-- 道具标签页 -->
      <view v-if="activeTab === 'items'" class="tab-content">
        <view class="tab-header">
          <h2>道具管理</h2>
          <p class="subtitle">查看和管理从商店购买的道具物品。</p>
        </view>

        <InventoryPanel
          :inventory="inventoryItems"
          @useItem="handleUseItem"
        />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import CardDeckManager from '@/components/CardDeckManager.vue'
import InventoryPanel from '@/components/InventoryPanel.vue'
import { useGameStore } from '@/stores/game'
import { useCampStore } from '@/stores/camp'
import { useAuthStore } from '@/stores/auth'
import { userCardApi, campApi } from '@/api/request'

// uni-app API 类型声明
declare const uni: {
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
  reLaunch: (options: { url: string }) => void
  clearStorageSync: () => void
}

const game = useGameStore()
const camp = useCampStore()
const auth = useAuthStore()

// 导航标签
const inventoryTabs: Array<{ key: 'cards' | 'items'; label: string; icon: string }> = [
  { key: 'cards', label: '卡牌', icon: 'fas fa-cards' },
  { key: 'items', label: '道具', icon: 'fas fa-flask' }
]

// 当前激活的标签
const activeTab = ref<'cards' | 'items'>('cards')

// deckLimit 可根据需求调整（界面图中为16/16）
const deckLimit = ref<number>(16)

// 卡牌相关计算属性
const userCards = computed(() => {
  // 优先使用营地中的收藏卡牌（包含 quantity 等信息）
  return (camp.userCards ?? []) as any[]
})

// 合并角色卡（userCardCharacters）与普通 userCards，避免在管理区漏掉角色卡
const combinedCards = computed(() => {
  const base = (camp.userCards ?? []) as any[]
  const chars = (camp.userCardCharacters ?? []) as any[]

  // 先把 base 里的条目标准化到同一格式
  const normalize = (c: any, forcedType?: string) => ({
    id: c.id ?? c.userCardId ?? c.cardId ?? c.cardCharacterId ?? null,
    name: c.name || c.cardName || c.characterName || c.cardCharacterName || '未知卡',
    description: c.cardDescription || c.description || c.characterDescription || '',
    cardType: forcedType ?? (c.cardType || c.type || (c.cardCharacterId ? 'character' : 'unknown')),
    rarity: c.cardRarity || c.rarity || c.characterRarity || 'common',
    manaCost: c.manaCost ?? c.actionPointCost ?? 1,
    hp: c.hp ?? c.health ?? c.currentHp ?? c.baseHp ?? null,
    attack: c.attack ?? c.atk ?? c.baseAttack ?? null,
    quantity: (c.quantity ?? c.count) == null ? 1 : Number(c.quantity ?? c.count),
    raw: c
  })

  const entries: Map<string, any> = new Map()

  // add base items
  base.forEach((b: any) => {
    const n = normalize(b)
    const key = String(n.id)
    if (!key) return
    if (!entries.has(key)) entries.set(key, { ...n })
    else entries.get(key).quantity += n.quantity
  })

  // add chars (as character type)
  chars.forEach((c: any) => {
    const n = normalize(c, 'character')
    const key = String(n.id)
    if (!key) return
    // If the same id already exists in userCards (entries), prefer the userCards entry as the
    // authoritative inventory source and DO NOT sum the character entry's quantity to avoid double-counting.
    if (!entries.has(key)) entries.set(key, { ...n })
    // else: skip adding/summing to avoid inflating counts when the same card appears in both arrays
  })

  const result = Array.from(entries.values())
  // 调试日志
  console.log('[CardsPage] combinedCards built, base=', base.length, 'chars=', chars.length, 'result=', result.length)
  // 检查角色卡牌的稀有度获取情况
  const characterCards = result.filter(card => card.cardType === 'character')
  if (characterCards.length > 0) {
    console.log('[CardsPage] 角色卡牌稀有度检查:', characterCards.map(card => ({
      name: card.name,
      rarity: card.rarity,
      rawRarity: card.raw?.characterRarity,
      allRarityFields: {
        cardRarity: card.raw?.cardRarity,
        rarity: card.raw?.rarity,
        characterRarity: card.raw?.characterRarity
      }
    })))
  }
  return result
})

const equippedCards = computed(() => {
  // 以当前战斗 deck 作为已上阵卡组的展示（不会修改后端持久化）
  return game.initialDeck?.slice() ?? game.deck?.slice() ?? []
})

// 道具相关计算属性
const inventoryItems = computed(() => {
  return (camp.inventoryItems ?? []) as any[]
})

// 在页面挂载时主动从后端加载营地数据和当前装备状态
onMounted(async () => {
  // 检查登录状态
  if (!auth.isAuthenticated) {
    console.warn('[InventoryPage] 用户未登录，跳转到登录页')
    uni.reLaunch({
      url: '/pages/login/login'
    })
    return
  }

  try {
    console.log('[InventoryPage] 页面挂载，开始清理本地数据并从数据库加载...')

    // 清理所有本地卡牌数据，确保完全依赖数据库
    game.initialDeck = []
    game.deck = []
    game.hand = []
    game.board = []

    // 清理本地存储数据，但保留认证信息
    try {
      if (typeof uni !== 'undefined') {
        console.log('[InventoryPage] 清理本地存储数据（保留认证信息）...')

        // 保留认证相关的数据
        let savedToken = null
        let savedUser = null
        try {
          savedToken = uni.getStorageSync('token')
          savedUser = uni.getStorageSync('user')
        } catch (e) {
          // 忽略获取错误
        }

        // 清理所有存储
        uni.clearStorageSync()

        // 恢复认证数据
        if (savedToken) {
          try {
            uni.setStorageSync('token', savedToken)
            console.log('[InventoryPage] token已恢复')
          } catch (e) {
            console.warn('[InventoryPage] 恢复token失败:', e)
          }
        }
        if (savedUser) {
          try {
            uni.setStorageSync('user', savedUser)
            console.log('[InventoryPage] user已恢复')
          } catch (e) {
            console.warn('[InventoryPage] 恢复user失败:', e)
          }
        }

        console.log('[InventoryPage] 本地存储数据清理完成，认证信息已保留')
      }
    } catch (e) {
      console.log('[InventoryPage] 本地存储清理跳过（非uni-app环境）')
    }

    console.log('[InventoryPage] 开始从后端加载营地数据...')
    await camp.fetchCampData()
    console.log('[InventoryPage] 营地数据加载完成，userCards count=', (camp.userCards || []).length, 'inventory count=', (camp.inventoryItems || []).length)

    // 加载当前装备状态，如果内存中没有数据才从数据库获取
    console.log('[InventoryPage] 检查当前装备状态...')
    const loadoutId = 1 // 使用默认卡组ID

    // 如果内存中没有装备数据，才从后端加载
    if (!game.initialDeck || game.initialDeck.length === 0) {
      console.log('[InventoryPage] 内存中没有装备数据，从数据库获取')
      await game.loadUserDeckFromDB(loadoutId)
      console.log('[InventoryPage] 装备状态加载完成，equipped count=', (game.initialDeck || game.deck || []).length)
    } else {
      console.log('[InventoryPage] 使用内存中的装备数据，equipped count=', game.initialDeck.length)
    }
  } catch (err) {
    console.error('[InventoryPage] 加载数据失败:', err)
  }
})

// 解析装备属性加成
function parseStatModifiers(statModifiers?: string): { atk: number; hp: number; def: number } {
  if (!statModifiers) return { atk: 0, hp: 0, def: 0 }
  try {
    const parsed = JSON.parse(statModifiers)
    const atk = Number(parsed.attack ?? 0) || 0
    const hp = Number(parsed.hp ?? 0) || 0
    const def = Number(parsed.defense ?? 0) || 0
    return { atk, hp, def }
  } catch {
    return { atk: 0, hp: 0, def: 0 }
  }
}

// 创建装备卡牌对象，与 loadUserDeckFromDB 的格式保持一致
function createEquippedCard(card: any): any {
  // 确保我们有正确的原始数据
  const rawData = card.raw || card
  const isCharacterType = (card.cardType === 'character') || (rawData.cardCharacterId !== undefined)

  if (isCharacterType) {
    // 角色卡格式 - 使用正确的后端ID
    const attack = Math.max(1, Number(rawData.baseAttack ?? 1))
    const health = Math.max(1, Number(rawData.currentHp ?? rawData.baseHp ?? 1))
    return {
      id: Math.random().toString(36).slice(2, 10), // 前端唯一ID
      // 保持显示名：优先使用传入 card 的显示名，其次使用后端 raw 的 characterName/name，最后回退到占位文本
      name: card?.name ?? rawData.characterName ?? rawData.name ?? '营地角色',
      cost: 2,
      type: 'character' as const,
      attack,
      health,
      cardCharacterId: rawData.cardCharacterId ?? rawData.id,
      raw: rawData // 保留完整的原始数据
    }
  } else {
    // 普通卡牌格式（法术或装备）
    const cardType = rawData.cardType || rawData.type || 'spell'
    const manaCost = Math.max(1, Number(rawData.manaCost ?? rawData.actionPointCost ?? (cardType === 'spell' ? 2 : 3)))
    const attack = typeof rawData.attack === 'number' ? rawData.attack : undefined
    const health = typeof rawData.health === 'number' ? rawData.health : undefined
    const name = rawData.cardName || rawData.name || '未命名卡牌'
    const effect = rawData.effect || undefined
    const { atk, hp, def } = parseStatModifiers(rawData.statModifiers)

    return {
      id: Math.random().toString(36).slice(2, 10), // 前端唯一ID
      name,
      cost: manaCost,
      type: cardType,
      attack,
      health,
      bonusAttack: atk,
      bonusHp: hp,
      bonusDefense: def,
      effect,
      effectPayload: rawData.effectPayload ?? (typeof rawData.effect === 'string' ? rawData.effect : undefined),
      cardCharacterId: rawData.cardCharacterId ?? rawData.id,
      unique_play: !!(rawData as any).unique_play,
      raw: rawData // 保留完整的原始数据
    }
  }
}

async function handleEquipCard(card: any) {
  if (!card || !card.id) return

  // 检查登录状态
  if (!auth.isAuthenticated) {
    console.warn('[InventoryPage] 用户未登录，无法装备卡牌')
    uni.reLaunch({
      url: '/pages/login/login'
    })
    return
  }

  // 重新从数据库获取当前装备状态，确保准确的上限检查
  console.log('[InventoryPage] 检查当前装备状态...')
  const loadoutId = 1 // 使用默认卡组ID
  await game.loadUserDeckFromDB(loadoutId)

  // 检查是否已经装备了同名卡牌
  const currentEquippedCards = game.initialDeck ?? game.deck ?? []
  const cardIsCharacter = (card.cardType === 'character') || (card.raw && (card.raw.cardType === 'character' || card.raw.cardCharacterId))

  let hasSameCardEquipped = false
  if (cardIsCharacter) {
    // 角色卡：检查是否已经装备了相同的角色
    const cardCharacterId = card.raw?.cardCharacterId ?? card.raw?.id ?? card.id
    hasSameCardEquipped = currentEquippedCards.some((equippedCard: any) => {
      const equippedCharacterId = equippedCard.raw?.cardCharacterId ?? equippedCard.raw?.id ?? equippedCard.id
      return String(equippedCharacterId) === String(cardCharacterId)
    })
  } else {
    // 普通卡牌：检查是否已经装备了相同名称的卡牌
    hasSameCardEquipped = currentEquippedCards.some((equippedCard: any) => {
      return equippedCard.name === card.name
    })
  }

  if (hasSameCardEquipped) {
    console.warn('同名卡牌只能上阵一个')
    return
  }

  if ((game.initialDeck?.length ?? game.deck.length) >= deckLimit.value) {
    // 超过上阵限制，简单提示（可替换为 Toast）
    console.warn('已达到上阵上限')
    return
  }
  console.log('[InventoryPage] handleEquipCard called, card=', card)
  // 优先尝试持久化到后端，再更新本地 state
  const isCharacterTypeType = (card.cardType === 'character') || (card.raw && (card.raw.cardType === 'character' || card.raw.cardCharacterId))

  try {
    if (isCharacterTypeType) {
      // 角色卡使用营地部署接口 - 优先使用 id 作为 userCardCharacterId
      const userCardCharacterId = card.raw?.id ?? card.raw?.userCardCharacterId ?? card.raw?.cardCharacterId
      console.log('[InventoryPage] deploying character to camp:', userCardCharacterId)
      console.log('[InventoryPage] equip character raw data:', card.raw)

      // 手动更新 campStore 中对应角色的部署状态（立即UI更新）
      const charIndex = camp.userCardCharacters.findIndex((c: any) =>
        String(c.id) === String(userCardCharacterId) ||
        String(c.userCardCharacterId) === String(userCardCharacterId) ||
        String(c.cardCharacterId) === String(userCardCharacterId)
      )
      if (charIndex >= 0 && camp.userCardCharacters && camp.userCardCharacters[charIndex]) {
        camp.userCardCharacters[charIndex].isDeployed = true
        console.log('[InventoryPage] manually updated character deployment status to true')
      }

      // 创建装备卡牌对象并立即添加到装备列表（立即UI更新）
      const equippedCard = createEquippedCard(card)
      game.initialDeck = [...(game.initialDeck ?? game.deck ?? []), equippedCard]
      game.deck = [...(game.deck ?? []), equippedCard]
      console.log('[InventoryPage] immediately added character to equipped deck, new length=', game.initialDeck?.length)

        // 异步调用后端API和同步状态
      try {
        const characterEquipResponse = await campApi.deployCardCharacter(userCardCharacterId, true)
        console.log('[InventoryPage] character equip API response:', characterEquipResponse)
        console.log('[InventoryPage] character equip API response data:', characterEquipResponse.data)
        await camp.fetchCampData()
        console.log('[InventoryPage] character equip synced with backend')
      } catch (err) {
        console.error('[InventoryPage] backend sync failed, reverting local changes')
        // 如果后端失败，回滚本地更改
        game.initialDeck = (game.initialDeck ?? []).filter(c => c.id !== equippedCard.id)
        game.deck = (game.deck ?? []).filter(c => c.id !== equippedCard.id)
        if (charIndex >= 0 && camp.userCardCharacters && camp.userCardCharacters[charIndex]) {
          camp.userCardCharacters[charIndex].isDeployed = false
        }
      }
    } else {
      // 普通卡牌通过更新 userCard 的 loadoutId 来加入卡组 - 优先使用后端 ID
      const userCardId = card.raw?.userCardId ?? card.raw?.cardId ?? card.raw?.id
      console.log('[InventoryPage] updating userCard loadout:', userCardId, '->', loadoutId)
      console.log('[InventoryPage] equip card raw data:', card.raw)

      // 手动更新 campStore 中对应卡牌的 loadoutId（立即UI更新）
      const cardIndex = camp.userCards.findIndex((c: any) =>
        String(c.id) === String(userCardId) ||
        String(c.userCardId) === String(userCardId) ||
        String(c.cardId) === String(userCardId)
      )
      if (cardIndex >= 0 && camp.userCards && camp.userCards[cardIndex]) {
        // 更新原始数据中的 loadoutId，用于后续显示
        if (camp.userCards[cardIndex].raw) {
          camp.userCards[cardIndex].raw.loadoutId = loadoutId
        }
        console.log('[InventoryPage] manually updated card loadoutId in raw data')
      }

      // 创建装备卡牌对象并立即添加到装备列表（立即UI更新）
      const equippedCard = createEquippedCard(card)
      game.initialDeck = [...(game.initialDeck ?? game.deck ?? []), equippedCard]
      game.deck = [...(game.deck ?? []), equippedCard]
      console.log('[InventoryPage] immediately added card to equipped deck, new length=', game.initialDeck?.length)

      // 异步调用后端API和同步状态
      try {
        const cardEquipResponse = await userCardApi.updateUserCard(userCardId, { loadoutId })
        console.log('[InventoryPage] card equip API response:', cardEquipResponse)
        console.log('[InventoryPage] card equip API response data:', cardEquipResponse.data)
        console.log('[InventoryPage] card equip API request data:', { loadoutId })
        await camp.fetchCampData()
        console.log('[InventoryPage] card equip synced with backend')
      } catch (err) {
        console.error('[InventoryPage] backend sync failed, reverting local changes')
        // 如果后端失败，回滚本地更改
        game.initialDeck = (game.initialDeck ?? []).filter(c => c.id !== equippedCard.id)
        game.deck = (game.deck ?? []).filter(c => c.id !== equippedCard.id)
        if (cardIndex >= 0 && camp.userCards && camp.userCards[cardIndex] && camp.userCards[cardIndex].raw) {
          camp.userCards[cardIndex].raw.loadoutId = null
        }
      }
    }

    // 本地同步数量（在后端已成功持久化后再做本地调整）
    try {
      camp.decrementUserCardQuantity(card.id, 1)
      console.log('[InventoryPage] decremented card quantity in store for id=', card.id)
    } catch (e) {
      console.warn('[InventoryPage] failed to decrement card quantity', e)
    }
  } catch (err) {
    console.error('[InventoryPage] persist equip failed:', err)
    // 回退：提示用户（此处使用 console.warn，UI 可替换为 Toast）
    console.warn('上阵失败，请检查网络或稍后重试')
  }
}

async function handleUnequipCard(payload: any) {
  if (!payload) return

  // 检查登录状态
  if (!auth.isAuthenticated) {
    console.warn('[InventoryPage] 用户未登录，无法撤下卡牌')
    uni.reLaunch({
      url: '/pages/login/login'
    })
    return
  }
  // payload may be a string id or an object { id, rawId }
  const id = typeof payload === 'string' ? payload : (payload.id ?? null)
  const rawId = typeof payload === 'object' ? (payload.rawId ?? null) : null
  const originalIndex = typeof payload === 'object' ? (payload.originalIndex ?? null) : null
  const idVariants = typeof payload === 'object' ? (payload.idVariants ?? null) : null
  console.log('[InventoryPage] handleUnequipCard called, id=', id, 'rawId=', rawId)

  try {
    // 同步到后端：如果是角色，调用撤下接口；否则将 loadoutId 设为 null
    const isCharacterType = (payload && payload.cardType === 'character') || (payload && payload.raw && (payload.raw.cardType === 'character' || payload.raw.cardCharacterId))
    // 优先使用 rawId（从 CardDeckManager 传递的真正后端 ID），其次从 idVariants 中获取
    const targetId = rawId || (idVariants && idVariants.length > 1 ? idVariants[1] : null) || (idVariants && idVariants[0] ? idVariants[0] : null)
    const loadoutId = 1 // 使用默认卡组ID，确保与战斗页面一致

    try {
      if (isCharacterType) {
        const userCardCharacterId = rawId || payload.raw?.id || payload.raw?.userCardCharacterId || (idVariants && idVariants.length > 3 ? idVariants[3] : null) || payload.id
        console.log('[InventoryPage] undeploying character:', userCardCharacterId)

        // 手动更新 campStore 中对应角色的部署状态（立即UI更新）
        const charIndex = camp.userCardCharacters.findIndex((c: any) =>
          String(c.id) === String(userCardCharacterId) ||
          String(c.userCardCharacterId) === String(userCardCharacterId) ||
          String(c.cardCharacterId) === String(userCardCharacterId)
        )
        if (charIndex >= 0 && camp.userCardCharacters && camp.userCardCharacters[charIndex]) {
          camp.userCardCharacters[charIndex].isDeployed = false
          console.log('[InventoryPage] manually updated character deployment status to false')
        }

        // 从装备列表中立即移除卡牌（立即UI更新）
        const match = (c: any) => {
          const key = String(id)
          const rkey = rawId ? String(rawId) : null
          return String(c.id) === key || String(c.userCardId) === key || String(c.cardId) === key ||
                 (rkey && (String(c.id) === rkey || String(c.userCardId) === rkey || String(c.cardId) === rkey))
        }
        game.initialDeck = (game.initialDeck ?? []).filter(c => !match(c))
        game.deck = (game.deck ?? []).filter(c => !match(c))
        console.log('[InventoryPage] immediately removed character from equipped deck')

        // 异步调用后端API（撤下操作不重新加载装备状态，避免状态被覆盖）
        try {
          const characterUnequipResponse = await campApi.deployCardCharacter(userCardCharacterId, false)
          console.log('[InventoryPage] character unequip API response:', characterUnequipResponse)
          console.log('[InventoryPage] character unequip API response data:', characterUnequipResponse.data)
          await camp.fetchCampData()
          // 撤下操作不重新加载装备状态，保持本地修改
          console.log('[InventoryPage] character unequip synced with backend')
        } catch (err) {
          console.error('[InventoryPage] backend sync failed, reverting local changes')
          // 如果后端失败，回滚本地更改
          if (charIndex >= 0 && camp.userCardCharacters && camp.userCardCharacters[charIndex]) {
            camp.userCardCharacters[charIndex].isDeployed = true
          }
          // 重新添加回装备列表
          const equippedCard = createEquippedCard({ raw: camp.userCardCharacters[charIndex] })
          game.initialDeck = [...(game.initialDeck ?? []), equippedCard]
          game.deck = [...(game.deck ?? []), equippedCard]
        }
      } else if (targetId) {
        console.log('[InventoryPage] updating userCard remove from loadout:', targetId)
        console.log('[InventoryPage] unequip card raw data:', payload.raw)
        // 确保使用正确的后端ID
        const actualTargetId = payload.raw?.cardId ?? payload.raw?.userCardId ?? payload.raw?.id ?? targetId
        console.log('[InventoryPage] actual target ID for unequip:', actualTargetId)

        // 手动更新 campStore 中对应卡牌的 loadoutId（立即UI更新）
        const cardIndex = camp.userCards.findIndex((c: any) =>
          String(c.id) === String(actualTargetId) ||
          String(c.userCardId) === String(actualTargetId) ||
          String(c.cardId) === String(actualTargetId)
        )
        if (cardIndex >= 0 && camp.userCards && camp.userCards[cardIndex]) {
          // 更新原始数据中的 loadoutId，用于后续显示
          if (camp.userCards[cardIndex].raw) {
            camp.userCards[cardIndex].raw.loadoutId = null
          }
          console.log('[InventoryPage] manually updated card loadoutId to null in raw data')
        }

        // 从装备列表中立即移除卡牌（立即UI更新）
        const match = (c: any) => {
          const key = String(id)
          const rkey = rawId ? String(rawId) : null
          return String(c.id) === key || String(c.userCardId) === key || String(c.cardId) === key ||
                 (rkey && (String(c.id) === rkey || String(c.userCardId) === rkey || String(c.cardId) === rkey))
        }
        game.initialDeck = (game.initialDeck ?? []).filter(c => !match(c))
        game.deck = (game.deck ?? []).filter(c => !match(c))
        console.log('[InventoryPage] immediately removed card from equipped deck')

        // 异步调用后端API（撤下操作不重新加载装备状态，避免状态被覆盖）
        try {
          const unequipResponse = await userCardApi.updateUserCard(actualTargetId, { loadoutId: null })
          console.log('[InventoryPage] unequip API response:', unequipResponse)
          console.log('[InventoryPage] unequip API response data:', unequipResponse.data)
          console.log('[InventoryPage] unequip API request data:', { loadoutId: null })
          await camp.fetchCampData()
          // 撤下操作不重新加载装备状态，保持本地修改
          console.log('[InventoryPage] card unequip synced with backend')
        } catch (err) {
          console.error('[InventoryPage] backend sync failed, reverting local changes')
          // 如果后端失败，回滚本地更改
          if (cardIndex >= 0 && camp.userCards && camp.userCards[cardIndex] && camp.userCards[cardIndex].raw) {
            camp.userCards[cardIndex].raw.loadoutId = 1
          }
          // 重新添加回装备列表
          const equippedCard = createEquippedCard({ raw: camp.userCards[cardIndex] })
          game.initialDeck = [...(game.initialDeck ?? []), equippedCard]
          game.deck = [...(game.deck ?? []), equippedCard]
        }
      }
    } catch (err) {
      console.error('[InventoryPage] persist unequip failed:', err)
      // 如果后端同步失败，尝试回滚本地数量恢复
      try {
        camp.incrementUserCardQuantity(targetId ?? payload.id, 1, originalIndex ?? undefined, idVariants ?? undefined)
      } catch (e) {}
    }
  } catch (e) {
    console.error('[InventoryPage] error while unequipping one instance', e)
  }

  // 恢复用户卡牌数量（乐观回滚），优先使用 rawId（后端的 userCardId/cardId）以便精确匹配
  try {
    const restoreId = rawId ?? id
    // 如果父组件传入 originalIndex 或 idVariants，则传递给 store 做更稳健的恢复匹配
    camp.incrementUserCardQuantity(restoreId, 1, originalIndex ?? undefined, idVariants ?? undefined)
    console.log('[InventoryPage] incremented card quantity in store for id=', restoreId, 'insertIndex=', originalIndex, 'idVariants=', idVariants)
  } catch (e) {
    console.warn('[InventoryPage] failed to increment card quantity', e)
  }
}

function handleUseItem(item: any) {
  console.log('[InventoryPage] 使用道具:', item)
  // 这里可以添加使用道具的逻辑
  // 例如：camp.useItem(item.id)
}
</script>

<style scoped>
.inventory-page {
  padding: 20px;
  min-height: 100vh;
  background: linear-gradient(180deg, #0b1220 0%, #09101a 100%);
  color: #fff;
  box-sizing: border-box;
}

.inventory-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  background: rgba(255, 255, 255, 0.05);
  padding: 0.5rem;
  border-radius: 12px;
}

.inventory-tab {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 0.9rem;
  color: #9ca3af;
  flex: 1;
  justify-content: center;
}

.inventory-tab:hover {
  background: rgba(255, 255, 255, 0.1);
  color: #e8e8e8;
}

.inventory-tab.active {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
  border: 1px solid rgba(76, 175, 80, 0.4);
}


.tab-header {
  margin-bottom: 16px;
}

.tab-header h2 {
  margin: 0;
  font-size: 20px;
  color: #ffd78b;
}

.subtitle {
  margin: 6px 0 0 0;
  color: rgba(255,255,255,0.7);
  font-size: 12px;
}
</style>
