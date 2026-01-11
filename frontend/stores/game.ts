

import { defineStore } from 'pinia'
import { ref, computed, nextTick } from 'vue'
import { gameApi, userCardApi, userCardCharacterApi } from '@/api/request'
import { useAuthStore } from './auth'
import { useCampStore } from './camp'
import type { ApiResponse, UniResponse } from '@/api/request'
import apiClient from '@/api/request'
import { soundManager, SoundType } from '@/utils/soundManager'

export type CardType = 'character' | 'spell' | 'equipment'

export interface Card {
  id: string
  name: string
  cost: number
  type: CardType
  attack?: number
  health?: number
  effect?: 'fireball3' | 'teamBuffAtk1'
  // 装备的属性加成（来自 cards.stat_modifiers）
  bonusAttack?: number
  bonusHp?: number
  bonusDefense?: number
  // 效果配置（JSON字符串，来自后端 effectPayload）
  effectPayload?: string
  // 卡牌角色ID（用于获取特性）
  cardCharacterId?: number | string
}

export interface Minion {
  id: string
  name: string
  attack: number
  health: number
  shield?: number
  stars?: number
  // 已装备的卡牌名称列表，用于在战斗界面展示圆形标记
  equipmentNames?: string[]
  // 是否可以攻击（召唤疲劳：刚打出的随从本回合不能攻击）
  canAttack?: boolean
  // 效果配置（JSON字符串，来自后端 effectPayload）
  effectPayload?: string
  // 卡牌角色ID（用于获取特性）
  cardCharacterId?: number | string
  // 角色特性列表（从后端加载）
  traits?: Array<{ effectPayload?: string; scalingPayload?: string }>
  // 部署位置（0-5，共6个位置，0为最前排）
  position?: number
  // 攻击动画状态
  attackState?: 'idle' | 'attacking' | 'returning'
  attackTargetId?: string // 攻击目标ID
  // 打出时间戳（用于判断攻击优先级）
  createdAt?: number
}

function uid() {
  return Math.random().toString(36).slice(2, 10)
}

// 移除了 baseDeck 函数，完全使用数据库数据

export const useGameStore = defineStore('game', () => {
  // 英雄生命与法力
  const heroHP = ref(100)
  const enemyHP = ref(100)
  const manaMax = ref(1)
  const mana = ref(1)

  // 手牌/牌库/战场
  const deck = ref<Card[]>([])
  const hand = ref<Card[]>([])
  const initialDeck = ref<Card[]>([])
  const deckExhausted = ref(false)
  const board = ref<Minion[]>([])
  
  // 敌人卡牌系统（和玩家对应）
  const enemyDeck = ref<Card[]>([]) // 敌人牌库
  const enemyHand = ref<Card[]>([]) // 敌人手牌
  const enemyBoard = ref<Minion[]>([]) // 敌人战场
  const enemyMana = ref(1) // 敌人法力值
  const enemyManaMax = ref(1) // 敌人最大法力值
  const enemyDeckExhausted = ref(false) // 敌人牌库是否耗尽
  const hasEnemyPlayedCards = ref(false) // 敌人是否已经出过牌（用于区分"游戏刚开始"和"敌人角色被击败"）
  
  // 敌人本体信息
  const currentEnemyId = ref<number | null>(null) // 当前战斗的敌人ID
  const enemyPanel = ref<{ name?: string; attack?: number; hp?: number; armor?: number; difficulty?: string } | null>(null) // 敌人面板信息（名字、攻击力、生命值、护甲、难度）

  // 胜负状态
  const battleOver = ref<boolean>(false)
  const winner = ref<'player' | 'enemy' | null>(null)
  // 濒死机制标记（防止连续触发）
  const hasNearDeathTriggered = ref(false)

  /**
   * 检查胜负条件
   * 当玩家血量为零时，触发濒死机制：增加50压力值，恢复一半血量
   */
  async function checkVictory() {
    if (enemyHP.value <= 0 && !battleOver.value) {
      battleOver.value = true
      winner.value = 'player'
      soundManager.playSound(SoundType.VICTORY)
      log('胜利：敌方生命归零！')
    }
    
    // 玩家血量为零时，触发濒死机制
    if (heroHP.value <= 0 && !battleOver.value && !hasNearDeathTriggered.value) {
      await triggerNearDeath()
    }
  }

  /**
   * 触发濒死机制：增加50压力值，恢复一半血量，然后结束战斗（失败）
   */
  async function triggerNearDeath() {
    if (hasNearDeathTriggered.value) return // 防止重复触发
    
    hasNearDeathTriggered.value = true
    
    const campStore = useCampStore()
    const playerChar = campStore.playerCharacter
    
    if (!playerChar || !playerChar.id) {
      // 如果没有玩家角色数据，直接失败
      battleOver.value = true
      winner.value = 'enemy'
      log('失败：我方生命归零，且无法触发濒死机制（缺少角色数据）。')
      return
    }
    
    // 获取最大血量
    const maxHp = playerChar.maxHp || 100
    const halfHp = Math.floor(maxHp / 2)
    
    // 获取当前压力值
    const currentStress = playerChar.currentStress || 0
    const newStress = Math.min(100, currentStress + 50) // 压力值上限100
    
    log(`⚠️ 濒死触发！增加50压力值（${currentStress} → ${newStress}），恢复一半血量（${halfHp}/${maxHp}），战斗失败`)
    
    try {
      // 更新后端数据：恢复一半血量，增加50压力值
      await apiClient.put(`/user-player-characters/${playerChar.id}`, {
        currentHp: halfHp,
        currentActionPoints: playerChar.currentActionPoints,
        currentStress: newStress
      })
      
      // 更新本地血量
      heroHP.value = halfHp
      
      // 更新营地store
      campStore.updatePlayerCharacter({
        currentHp: halfHp,
        currentStress: newStress
      })
      
      log(`✅ 濒死机制生效：血量已恢复至 ${halfHp}，压力值已增加至 ${newStress}`)
    } catch (error) {
      console.error('[Game] 触发濒死机制失败:', error)
      log('❌ 濒死机制触发失败，但战斗仍将结束')
      // 即使更新失败，也恢复一半血量（本地）
      heroHP.value = halfHp
    }
    
    // 结束战斗，标记为失败
    battleOver.value = true
    winner.value = 'enemy'
    soundManager.playSound(SoundType.DEFEAT)
    log('战斗失败：触发濒死机制')
  }

  // 回合：'player' | 'opponent'
  const turn = ref<'player' | 'opponent'>('player')
  const canPlay = computed(() => turn.value === 'player')



  // 关卡难度（影响敌方出牌/属性）
  const enemyDifficulty = ref<'普通' | '困难' | '噩梦'>('普通')
  function configureEncounter(diff: '普通' | '困难' | '噩梦') {
    enemyDifficulty.value = diff
  }

  // 特性接口定义
  interface CharacterTrait {
    trait_key: string
    base_power: number
    power_per_star: number
    description: string
  }

  // 角色特性（从数据库加载）
  const charTraits = ref<Record<string, CharacterTrait>>({})
  // 卡牌角色特性缓存：按 cardCharacterId 存储后端返回的 traits 数组
  const cardCharacterTraitsCache = ref<Record<string, Array<{ effectPayload?: string; scalingPayload?: string }>>>({})

  async function loadCharacterTraits() {
    try {
      const response: UniResponse<ApiResponse<Record<string, CharacterTrait>>> = await gameApi.getCharacterTraits()
      if (response.data.code === 200 && response.data.data) {
        charTraits.value = Object.fromEntries(
          Object.entries(response.data.data).map(([name, trait]: [string, CharacterTrait]) => [
            name,
            {
              trait_key: trait.trait_key,
              base_power: Number(trait.base_power ?? 1),
              power_per_star: Number(trait.power_per_star ?? 0),
              description: trait.description ?? ''
            }
          ])
        )
        log('已加载角色特性配置')
      }
    } catch (e: any) {
      log('加载角色特性异常：' + e?.message)
    }
  }

  function getTraitPower(name: string, stars: number): number {
    const t = charTraits.value[name]
    if (!t) return 0
    const s = Math.max(1, Number(stars ?? 1))
    return Math.round(t.base_power + t.power_per_star * (s - 1))
  }

  /**
   * 按 cardCharacterId 加载特性（后端接口），带缓存
   */
  async function loadCardCharacterTraits(cardCharacterId?: number | string): Promise<Array<{ effectPayload?: string; scalingPayload?: string }>> {
    if (!cardCharacterId) return []
    const key = String(cardCharacterId)
    if (cardCharacterTraitsCache.value[key]) return cardCharacterTraitsCache.value[key]
    try {
      const res = await gameApi.getCardCharacterTraits(cardCharacterId)
      if (res.data.code === 200 && Array.isArray(res.data.data)) {
        cardCharacterTraitsCache.value[key] = res.data.data
        return res.data.data
      }
    } catch (e) {
      log('加载角色特性失败：' + (e as any)?.message)
    }
    cardCharacterTraitsCache.value[key] = []
    return []
  }

  /**
   * 从角色特性或 effectPayload 中解析治疗量
   */
  function extractHealAmount(minion: Minion): number {
    // 优先从 traits 读取 heal_allies（支持 scalingPayload）
    if (minion.traits && minion.traits.length > 0) {
      for (const trait of minion.traits) {
        if (!trait.effectPayload) continue
        try {
          const effect = JSON.parse(trait.effectPayload)
          if (effect.heal_allies) {
            let healAmount = Number(effect.heal_allies) || 0
            if (healAmount <= 0) continue
            if (trait.scalingPayload && minion.stars && minion.stars > 1) {
              try {
                const scaling = JSON.parse(trait.scalingPayload)
                const starKey = String(minion.stars)
                if (scaling[starKey] && scaling[starKey].heal_allies) {
                  healAmount = Number(scaling[starKey].heal_allies) || healAmount
                }
              } catch {
                // ignore scaling parse errors
              }
            }
            if (healAmount > 0) return healAmount
          }
        } catch {
          // ignore parse errors
        }
      }
    }

    // 兼容：从 effectPayload 读取 heal_allies / heal_amount / heal
    if (minion.effectPayload) {
      try {
        const effect = JSON.parse(minion.effectPayload)
        const healAmount =
          Number(effect.heal_allies ?? effect.heal_amount ?? effect.heal ?? 0) || 0
        if (healAmount > 0) return healAmount
      } catch {
        // ignore parse errors
      }
    }

    return 0
  }

  /**
   * 对友方全体进行治疗（如果有可解析的治疗量）
   */
  function maybeHealAllies(source: Minion, allies: Minion[], reason: string, isEnemy = false) {
    const healAmount = extractHealAmount(source)
    if (healAmount <= 0 || allies.length === 0) return
    let healedCount = 0
    allies.forEach(target => {
      const before = target.health
      target.health = target.health + healAmount
      const actual = target.health - before
      if (actual > 0) healedCount++
    })
    if (healedCount > 0) {
      // 播放治疗音效
      soundManager.playSound(SoundType.HEAL, { volume: 0.4 })
      const side = isEnemy ? '敌方' : '我方'
      log(`治疗：${side} ${source.name} 在${reason}为全体恢复 ${healAmount} 点生命`)
    }
  }

  function applyTraitsAtTurnStart() {
    // 我方每回合开始时，根据在场角色的特性结算
    if (board.value.length === 0) return
    
    // 处理角色特性治疗效果（基于 effectPayload 中的 heal_allies 字段），在回合开始给予群体治疗
    board.value.forEach(m => maybeHealAllies(m, board.value, '回合开始'))
    
    // 然后处理其他特性（旧系统）
    board.value.forEach(m => {
      const t = charTraits.value[m.name]
      if (!t) return
      const power = getTraitPower(m.name, m.stars ?? 1)
      if (power <= 0) return
      switch (t.trait_key) {
        case 'priest_bless':
          // 祭司：为友方全体恢复生命
          soundManager.playSound(SoundType.HEAL, { volume: 0.4 })
          board.value = board.value.map(x => ({ ...x, health: x.health + power }))
          log(`特性：祭司祝福，友方全体恢复 ${power} 点生命`)
          break
        case 'shield_guard':
          // 盾卫：为一个队友提供护盾（选取当前生命最低的友方）
          let idx = 0
          for (let i = 1; i < board.value.length; i++) {
            if (board.value[i].health < board.value[idx].health) idx = i
          }
          const target = board.value[idx]
          target.shield = (target.shield ?? 0) + power
          log(`特性：盾卫守护，为 ${target.name} 提供 ${power} 点护盾`)
          break
        default:
          // 其他特性可拓展
          break
      }
    })
  }

  // 用户卡牌接口
  interface UserCardResponse {
    id?: string | number
    name?: string
    cardName?: string
    type?: CardType
    cardType?: CardType
    quantity?: number
    attack?: number
    health?: number
    manaCost?: number
    cardCode?: string
    effect?: 'fireball3' | 'teamBuffAtk1'
    statModifiers?: string
    effectPayload?: string
    cardCharacterId?: number | string
  }

  interface UserCardCharacterResponse {
    id?: string | number
    characterName?: string
    characterClassType?: string
    baseAttack?: number
    baseHp?: number
    currentHp?: number
    currentArmor?: number
    currentStarLevel?: number
    isDeployed?: boolean
  }

  function normalizeCardTypeValue(type?: string): CardType {
    switch ((type || '').toLowerCase()) {
      case 'character':
        return 'character'
      case 'equipment':
        return 'equipment'
      default:
        return 'spell'
    }
  }

  function inferEffectFromCard(card: UserCardResponse, cardType: CardType): Card['effect'] {
    if (card.effect) return card.effect
    const identifier = (card.cardCode || card.cardName || card.name || '').toLowerCase()
    if (cardType === 'spell' && (identifier.includes('fireball') || identifier.includes('火球'))) {
      return 'fireball3'
    }
    if (cardType === 'equipment' && (identifier.includes('banner') || identifier.includes('战旗'))) {
      return 'teamBuffAtk1'
    }
    return undefined
  }

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

  function mapDeployedCharactersToCards(chars: UserCardCharacterResponse[]): Card[] {
    return chars
      .filter(char => char.isDeployed !== false) // deployed 接口默认只返回已上阵，但防御一下
      .map((char) => {
        const attack = Math.max(1, Number(char.baseAttack ?? 1))
        const health = Math.max(1, Number(char.currentHp ?? char.baseHp ?? 1))
        return {
          id: uid2(),
          // 保持显示名：优先使用后端 characterName，再尝试 name 字段，最后回退占位
          name: char.characterName ?? (char as any).name ?? '营地角色',
          cost: 2,
          type: 'character' as CardType,
          attack,
          health,
          // 优先使用后端提供的 cardCharacterId，缺失时退回记录 id
          cardCharacterId: (char as any).cardCharacterId ?? char.id,
          // 保留原始后端数据，以便撤下时获取正确的后端 ID
          raw: char
        }
      })
  }

  // 从数据库加载玩家手牌（包含营地上阵角色 + 选中卡组）
  async function loadUserDeckFromDB(loadoutId = 1) {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      log('未登录，无法加载玩家手牌')
      return
    }
    try {
      // 获取卡组中的卡牌（普通卡牌）
      const deckResponse = await userCardApi.getDeckCards(loadoutId)
      const deckCardsRaw: UserCardResponse[] = Array.isArray(deckResponse.data?.data) ? deckResponse.data.data : []
      console.log('[GameStore] loadUserDeckFromDB - deck cards response:', deckResponse)
      console.log('[GameStore] loadUserDeckFromDB - deck cards data:', deckCardsRaw)
      console.log('[GameStore] loadUserDeckFromDB - deck cards IDs:', deckCardsRaw.map(c => c.id))
      console.log('[GameStore] loadUserDeckFromDB - deck cards loadoutIds:', deckCardsRaw.map(() => loadoutId))

      // 获取已部署的角色卡（直接从后端获取最新的部署状态）
      const deployedResponse = await userCardCharacterApi.getDeployedCardCharacters()
      const deployedCharsRaw: UserCardCharacterResponse[] = Array.isArray(deployedResponse.data?.data)
        ? deployedResponse.data.data
        : []
      console.log('[GameStore] loadUserDeckFromDB - deployed characters response:', deployedResponse)
      console.log('[GameStore] loadUserDeckFromDB - deployed characters data:', deployedCharsRaw)
      console.log('[GameStore] loadUserDeckFromDB - deployed characters IDs:', deployedCharsRaw.map(c => c.id))

      // 战斗牌库中，每种卡牌只保留一张，不再按 quantity 复制多份
      const deckCards: Card[] = deckCardsRaw.map((card) => {
        const cardType = normalizeCardTypeValue(card.cardType || card.type)
        const manaCost = Math.max(1, Number(card.manaCost ?? (cardType === 'spell' ? 2 : 3)))
        const attack = typeof card.attack === 'number' ? card.attack : undefined
        const health = typeof card.health === 'number' ? card.health : undefined
        const name = card.cardName || card.name || '未命名卡牌'
        const effect = inferEffectFromCard(card, cardType)
        const { atk, hp, def } = parseStatModifiers(card.statModifiers)

        return {
            id: uid2(),
          name,
          cost: manaCost,
          type: cardType,
          attack,
          health,
          // 以 stat_modifiers 作为主来源，统一传递到战斗逻辑
          bonusAttack: atk,
          bonusHp: hp,
          bonusDefense: def,
          effect,
          effectPayload: card.effectPayload ?? (typeof card.effect === 'string' ? card.effect : undefined),
          // 优先使用 cardCharacterId 字段，回退记录 id（后端未返回时也能尝试加载特性）
          cardCharacterId: card.cardCharacterId ?? card.id,
          unique_play: !!(card as any).unique_play,
          // 保留原始后端数据，以便撤下时获取正确的后端 ID
          raw: card
        }
      })

      const characterCards = mapDeployedCharactersToCards(deployedCharsRaw)
      const combined = [...characterCards, ...deckCards]

      deck.value = combined
      initialDeck.value = combined.slice()

      if (combined.length === 0) {
        log('提示：当前未配置上阵卡牌，请返回营地在卡组管理中选择卡牌后再开始战斗')
      } else {
        log(`已从营地上阵卡组加载 ${combined.length} 张卡牌（角色 ${characterCards.length}，技能 ${deckCards.length}）`)
      }

        shuffle(deck.value)
        hand.value = []
        board.value = []
    } catch (e: any) {
      log('加载玩家手牌失败：' + e?.message)
    }
  }

  // 敌方卡牌接口
  interface EnemyCardResponse {
    id?: string | number
    name: string
    type: 'character' | 'spell' | 'equipment'
    attack?: number
    health?: number
    effect?: string
    effectPayload?: string
    cardCharacterId?: number | string
    unique_play?: boolean
  }

  // 从数据库加载敌方手牌（按关卡与难度）
  /**
   * 从后端加载敌人卡牌列表，初始化敌人牌库和手牌
   * 同时获取敌人面板信息（用于敌人本体攻击）
   */
  async function loadEnemyDeck(stageNum: number) {
    const diff = enemyDifficulty.value
    try {
      // 1. 先获取关卡中所有可能的敌人列表
      let selectedEnemyId: number | null = null
      try {
        const enemiesResponse = await gameApi.getStageEnemies(stageNum, diff)
        console.log('[Game] getStageEnemies 响应:', enemiesResponse.data)
        if (enemiesResponse.data.code === 200 && enemiesResponse.data.data) {
          let enemies = enemiesResponse.data.data as Array<{ id: number; name: string; difficulty: string }>
          console.log('[Game] 获取到敌人列表:', enemies)
          
          // 检查关卡号是否是5的倍数，如果不是则过滤掉首领敌人
          const isBossStage = stageNum % 5 === 0
          if (!isBossStage) {
            const beforeFilter = enemies.length
            enemies = enemies.filter(enemy => {
              const difficulty = (enemy.difficulty || '').toLowerCase()
              const isBoss = difficulty === 'boss' || difficulty === '首领' || difficulty.includes('boss')
              if (isBoss) {
                console.log(`[Game] 关卡 ${stageNum} 不是5的倍数，过滤掉首领敌人: ${enemy.name} (difficulty: ${enemy.difficulty})`)
              }
              return !isBoss
            })
            console.log(`[Game] 过滤首领敌人: ${beforeFilter} -> ${enemies.length} (关卡 ${stageNum} 不是Boss关卡)`)
          } else {
            console.log(`[Game] 关卡 ${stageNum} 是Boss关卡，允许出现首领敌人`)
          }
          
          if (enemies.length > 0) {
            // 随机选择一个敌人
            const randomIndex = Math.floor(Math.random() * enemies.length)
            selectedEnemyId = enemies[randomIndex].id
            currentEnemyId.value = selectedEnemyId
            log(`已选择敌人：${enemies[randomIndex].name} (ID: ${selectedEnemyId})`)
          } else {
            console.warn('[Game] 敌人列表为空（可能所有敌人都是首领且当前不是Boss关卡）')
          }
        } else {
          console.warn('[Game] getStageEnemies 返回错误:', enemiesResponse.data)
        }
      } catch (e) {
        console.error('[Game] 获取敌人列表失败:', e)
      }
      
      // 2. 获取敌人卡牌（如果已选择敌人ID，则使用ID查询；否则使用原有逻辑）
      let cardResponse: UniResponse<ApiResponse<EnemyCardResponse[]>>
      if (selectedEnemyId) {
        console.log('[Game] 使用敌人ID获取卡牌:', selectedEnemyId)
        cardResponse = await gameApi.getEnemyCards(selectedEnemyId) as any
      } else {
        console.log('[Game] 使用关卡和难度获取卡牌:', stageNum, diff)
        cardResponse = await gameApi.getEnemyCards(stageNum, diff)
        // 如果使用关卡和难度获取卡牌，尝试再次获取敌人列表以获取敌人ID
        // 因为后端可能已经选择了敌人，我们需要知道是哪个
        if (!selectedEnemyId) {
          try {
            const retryEnemiesResponse = await gameApi.getStageEnemies(stageNum, diff)
            if (retryEnemiesResponse.data.code === 200 && retryEnemiesResponse.data.data) {
              const enemies = retryEnemiesResponse.data.data as Array<{ id: number; name: string; difficulty: string }>
              if (enemies.length > 0) {
                // 选择第一个敌人（因为后端可能已经选择了）
                selectedEnemyId = enemies[0].id
                currentEnemyId.value = selectedEnemyId
                console.log('[Game] 重试后获取到敌人ID:', selectedEnemyId)
              }
            }
          } catch (e) {
            console.warn('[Game] 重试获取敌人列表失败:', e)
          }
        }
      }
      
      // 3. 获取敌人面板信息
      if (selectedEnemyId) {
        try {
          console.log('[Game] 正在获取敌人面板信息，敌人ID:', selectedEnemyId)
          const panelResponse = await gameApi.getEnemyPanel(selectedEnemyId)
          console.log('[Game] 敌人面板API响应:', panelResponse.data)
          if (panelResponse.data.code === 200 && panelResponse.data.data) {
            const panel = panelResponse.data.data as { name?: string; attack?: number; hp?: number; armor?: number; difficulty?: string }
            enemyPanel.value = panel
            console.log('[Game] 敌人面板已设置:', enemyPanel.value)
            
            // 使用敌人面板的HP值设置敌人初始HP
            if (panel.hp && panel.hp > 0) {
              enemyHP.value = panel.hp
              log(`敌人面板：${panel.name ?? '未知'} - 攻击 ${panel.attack ?? 0}，生命 ${panel.hp}，护甲 ${panel.armor ?? 0}`)
              log(`敌人初始HP已设置为：${panel.hp}`)
            } else {
              // 如果没有HP值，保持默认值100
              enemyHP.value = 100
              log(`敌人面板：${panel.name ?? '未知'} - 攻击 ${panel.attack ?? 0}，生命 ${panel.hp ?? 0}，护甲 ${panel.armor ?? 0}`)
            }
          } else {
            console.warn('[Game] 敌人面板API返回错误:', panelResponse.data)
            enemyPanel.value = null
          }
        } catch (e) {
          console.error('[Game] 获取敌人面板失败:', e)
          enemyPanel.value = null
        }
      } else {
        console.warn('[Game] 未选择敌人ID，无法获取敌人面板信息')
        enemyPanel.value = null
      }
      
      if (cardResponse.data.code === 200 && cardResponse.data.data) {
        // 将后端返回的卡牌转换为前端 Card 格式，作为敌人牌库
        const cards: Card[] = cardResponse.data.data.map((r: EnemyCardResponse) => {
          // 根据卡牌类型确定费用
          let cost = 2
          if (r.type === 'character') {
            cost = 2 // 角色卡默认2费
          } else if (r.type === 'spell') {
            cost = 2 // 法术卡默认2费
          } else if (r.type === 'equipment') {
            cost = 3 // 装备卡默认3费
          }
          
          // 解析 statModifiers（如果有）
          let bonusAttack: number | undefined
          let bonusHp: number | undefined
          let bonusDefense: number | undefined
          
          // 如果后端返回了 effect，尝试解析（可能包含 statModifiers 信息）
          if (r.effect) {
            try {
              const effectObj = typeof r.effect === 'string' ? JSON.parse(r.effect) : r.effect
              if (effectObj.attack) bonusAttack = Number(effectObj.attack)
              if (effectObj.hp) bonusHp = Number(effectObj.hp)
              if (effectObj.defense) bonusDefense = Number(effectObj.defense)
            } catch (e) {
              // 解析失败，忽略
            }
          }
          
          return {
          id: uid2(),
          name: r.name,
            cost: cost,
          type: r.type as CardType,
          attack: r.attack ?? undefined,
          health: r.health ?? undefined,
          effect: r.effect ?? undefined,
            effectPayload: r.effectPayload ?? (typeof r.effect === 'string' ? r.effect : undefined),
            bonusAttack: bonusAttack,
            bonusHp: bonusHp,
            bonusDefense: bonusDefense,
          unique_play: !!r.unique_play
          } as Card
        })
        
        // 初始化敌人牌库（打乱顺序）
        enemyDeck.value = [...cards]
        shuffle(enemyDeck.value)
        
        // 初始化敌人手牌（抽前5张）
        enemyHand.value = []
        for (let i = 0; i < Math.min(5, enemyDeck.value.length); i++) {
          const card = enemyDeck.value.shift()
          if (card) {
            enemyHand.value.push(card)
          }
        }
        
        // 初始化敌人法力值（根据难度）
        enemyManaMax.value = diff === '普通' ? 1 : diff === '困难' ? 2 : 3
        enemyMana.value = enemyManaMax.value
        
        log(`已加载敌人卡牌系统（${diff}，关卡 ${stageNum}）：牌库 ${enemyDeck.value.length} 张，手牌 ${enemyHand.value.length} 张`)
      }
    } catch (e: any) {
      log('加载敌人卡牌失败：' + e?.message)
      // 如果加载失败，使用默认敌人
      enemyDeck.value = []
      enemyHand.value = []
    }
  }

  function uid2() { return Math.random().toString(36).slice(2, 10) }



  // 消息日志
  const logs = ref<string[]>([])
  function log(msg: string) {
    logs.value.unshift(new Date().toLocaleTimeString() + ' ' + msg)
    if (logs.value.length > 100) logs.value.pop()
  }

  function shuffle<T>(arr: T[]) {
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[arr[i], arr[j]] = [arr[j], arr[i]]
    }
  }

  // 从营地数据加载玩家血量
  function loadPlayerHealthFromCamp() {
    const campStore = useCampStore()
    const playerChar = campStore.playerCharacter
    if (playerChar && playerChar.currentHp !== undefined && playerChar.currentHp !== null) {
      // 使用营地中的当前血量，确保战斗和营地使用同一个血量
      // 重要：如果 currentHp 超过 maxHp，应该限制为 maxHp（防止数据不一致）
      const maxHp = playerChar.maxHp || 100
      const currentHp = Math.max(0, Math.min(playerChar.currentHp, maxHp))
      heroHP.value = currentHp
      log(`已从营地加载玩家血量：${heroHP.value}/${maxHp}`)
      return heroHP.value
    } else {
      // 如果没有营地数据，使用默认值
    heroHP.value = 100
      log('未找到营地数据，使用默认血量：100')
      return 100
    }
  }

  function reset() {
    // 清理所有本地卡牌数据，完全依赖数据库
    deck.value = []
    initialDeck.value = []
    hand.value = []
    board.value = []

    // 从营地加载玩家血量，而不是重置为100
    // 确保在重置时重新加载营地数据，以获取最新的血量
    const campStore = useCampStore()
    // 如果营地数据不存在，尝试重新获取（但这是异步的，所以这里只是确保数据存在）
    if (!campStore.playerCharacter) {
      log('警告：营地数据未加载，尝试加载...')
      // 注意：这里不能 await，因为 reset() 是同步函数
      // 调用者应该在调用 reset() 之前确保营地数据已加载
    }
    loadPlayerHealthFromCamp()
    // 使用敌人面板的HP值，如果没有面板信息则使用默认值100
    enemyHP.value = enemyPanel.value?.hp && enemyPanel.value.hp > 0 ? enemyPanel.value.hp : 100

    // 从营地数据获取行动点作为初始法力值
    const playerChar = campStore.playerCharacter
    if (playerChar && playerChar.currentActionPoints !== undefined && playerChar.currentActionPoints !== null) {
      // 使用当前行动点作为初始法力值，如果没有则使用4
      manaMax.value = Math.max(4, playerChar.currentActionPoints)
      mana.value = Math.max(4, playerChar.currentActionPoints)
      log(`从营地加载初始法力值：${mana.value}（来自行动点：${playerChar.currentActionPoints}）`)
    } else {
      // 如果没有行动点数据，使用默认值4
      manaMax.value = 4
      mana.value = 4
      log('使用默认初始法力值：4')
    }
    turn.value = 'player'
    battleOver.value = false
    winner.value = null

    // 重置敌人卡牌系统
    // 注意：敌人牌库和手牌应该在 loadEnemyDeck 中初始化
    // 这里只重置战场和状态
    enemyBoard.value = []
    enemyMana.value = enemyManaMax.value
    enemyDeckExhausted.value = false
    hasEnemyPlayedCards.value = false // 重置敌人出牌标记
    currentEnemyId.value = null // 重置敌人ID
    enemyPanel.value = null // 重置敌人面板信息

    logs.value = []
    deckExhausted.value = false

    // 重置濒死机制标记
    hasNearDeathTriggered.value = false

    // 确保血量不超过最大血量（防止数据不一致）
    const maxHp = playerChar?.maxHp || 100
    if (heroHP.value > maxHp) {
      log(`警告：当前血量 ${heroHP.value} 超过最大血量 ${maxHp}，已限制为 ${maxHp}`)
      heroHP.value = maxHp
    }

    log(`战斗重置：玩家生命 ${heroHP.value}/${maxHp}，敌方初始生命 100。牌库将在 loadUserDeckFromDB 时从数据库加载。`)
  }

  function draw(n = 2) {
    let drawnCount = 0
    for (let i = 0; i < n; i++) {
      if (deck.value.length === 0) {
        if (!deckExhausted.value) {
          deckExhausted.value = true
          log('牌库已耗尽，本场战斗不会再补充卡牌。')
        }
        break
      }
      const c = deck.value.shift()
      if (!c) break
      if (hand.value.length < 10) {
        hand.value.push(c)
        drawnCount++
        // 播放发牌音效（延迟播放，避免重叠）
        setTimeout(() => {
          soundManager.playSound(SoundType.DRAW_CARD, { volume: 0.4 })
        }, i * 100)
      } else {
        log('提示：手牌已满，本次抽到的卡牌被丢弃。')
      }
    }
  }

  function startPlayerTurn() {
    turn.value = 'player'
    manaMax.value = Math.min(10, manaMax.value + 1) // 简化：每回合+1上限
    mana.value = manaMax.value
    draw(2)
    
    // 重置所有我方随从的攻击状态（解除召唤疲劳）
    board.value.forEach(m => {
      m.canAttack = true
    })
    
    // 首次需要时加载角色特性配置
    if (Object.keys(charTraits.value).length === 0) {
      // 非阻塞加载
      loadCharacterTraits()
    }
    // 结算在场角色的特性（群体治疗、护盾等）
    applyTraitsAtTurnStart()
  }

  function playCard(id: string, position?: number) {
    if (turn.value !== 'player') return
    if (battleOver.value) return
    const idx = hand.value.findIndex(c => c.id === id)
    if (idx < 0) return
    const card = hand.value[idx]
    if (card.cost > mana.value) return
    // 扣蓝
    mana.value -= card.cost
    // 播放出牌音效
    soundManager.playSound(SoundType.PLAY_CARD)
    // 处理类型
    if (card.type === 'character') {
      // 检查位置是否可用
      if (position !== undefined) {
        if (position < 0 || position >= 6) return // 位置必须在 0-5 之间
        // 检查该位置是否已被占用
        if (board.value.some(m => m.position === position)) return
      } else {
        // 如果没有指定位置，自动找到第一个空位
        const occupiedPositions = new Set(board.value.map(m => m.position).filter(p => p !== undefined))
        let autoPosition = -1
        for (let i = 0; i < 6; i++) {
          if (!occupiedPositions.has(i)) {
            autoPosition = i
            break
          }
        }
        if (autoPosition === -1) {
          log('提示：战场已满（最多6个位置），无法部署更多角色')
          mana.value += card.cost // 返还法力值
          return
        }
        position = autoPosition
      }
      
      if (board.value.length >= 6) {
        log('提示：战场已满（最多6个位置），无法部署更多角色')
        mana.value += card.cost // 返还法力值
        return
      }
      
      // 不再升星，重复卡牌也直接上场为独立单位
        const m: Minion = {
        id: uid2(), // 保证同名随从也有唯一ID
          name: card.name,
          attack: card.attack ?? 0,
          health: card.health ?? 1,
          shield: 0,
        stars: 1,
        canAttack: false, // 召唤疲劳：刚打出的随从本回合不能攻击
        cardCharacterId: card.cardCharacterId,
        effectPayload: card.effectPayload,
        traits: [], // 特性列表将在需要时加载
        position: position,
        attackState: 'idle', // 初始化攻击状态
        createdAt: Date.now() // 记录打出时间，用于攻击优先级判断
        }
        board.value.push(m)
      // 按位置排序，确保显示顺序正确
      board.value.sort((a, b) => (a.position ?? 999) - (b.position ?? 999))
      log(`召唤：${card.name}（ATK ${card.attack ?? 0} / HP ${card.health ?? 1}），下回合才能攻击`)
      // 异步加载角色特性，供后续回合/攻击时使用
      if (card.cardCharacterId) {
        loadCardCharacterTraits(card.cardCharacterId).then(traits => {
          m.traits = traits ?? []
        }).catch(() => {})
      }
        // 注意：召唤时不应该触发治疗特性，只在回合开始时触发
        // 这里只处理非治疗类的即时特性（如护盾）
        const t = charTraits.value[card.name]
        if (t) {
          const power = getTraitPower(card.name, 1)
          if (power > 0) {
            if (t.trait_key === 'shield_guard') {
              // 盾卫：召唤时可以为队友提供护盾
              let idx = 0
              for (let i = 1; i < board.value.length; i++) {
                if (board.value[i].health < board.value[idx].health) idx = i
              }
              const target = board.value[idx]
              target.shield = (target.shield ?? 0) + power
              log(`特性：${card.name} 盾卫守护，为 ${target.name} 提供 ${power} 点护盾`)
          }
            // 注意：priest_bless（祭司祝福）只在回合开始时触发，不在召唤时触发
        }
      }
      hand.value.splice(idx, 1)
    } else if (card.type === 'spell') {
      // 检查法术卡是否有治疗效果（基于 effectPayload 字段）
      let healAmount = 0
      let isHealSpell = false
      
      // 优先从 effectPayload 解析治疗量
      if (card.effectPayload) {
        try {
          const effect = JSON.parse(card.effectPayload)
          // 检查是否有治疗相关字段
          if (effect.heal || effect.heal_amount || effect.heal_single) {
            isHealSpell = true
            healAmount = effect.heal || effect.heal_amount || effect.heal_single || 0
          }
        } catch (e) {
          // 解析失败
        }
      }
      
      // 兼容旧逻辑：检查名字是否包含"治疗"
      if (!isHealSpell && card.name && card.name.includes('治疗')) {
        isHealSpell = true
        // 尝试从 effect 字段解析（旧格式）
        if (card.effect && typeof card.effect === 'string') {
          try {
            const effectObj = JSON.parse(card.effect)
            if (effectObj.heal || effectObj.healAmount) {
              healAmount = effectObj.heal || effectObj.healAmount
            }
          } catch (e) {
            // 解析失败
          }
        }
        
        // 如果还是没有，尝试从 bonusHp 获取
        if (healAmount === 0 && card.bonusHp) {
          healAmount = card.bonusHp
        }
        
        // 如果还是没有，使用默认治疗量 5
        if (healAmount === 0) {
          healAmount = 5
        }
      }
      
      // 执行治疗效果
      if (isHealSpell && healAmount > 0) {
        // 播放治疗音效
        soundManager.playSound(SoundType.HEAL, { volume: 0.5 })
        
        // 治疗我方所有卡牌
        let healedCount = 0
        board.value.forEach(m => {
          const oldHealth = m.health
          m.health = m.health + healAmount
          const actualHeal = m.health - oldHealth
          if (actualHeal > 0) {
            healedCount++
            log(`治疗法术：${card.name} 为 ${m.name} 恢复 ${actualHeal} 点生命（当前HP ${m.health}）`)
          }
        })
        
        if (healedCount === 0) {
          log(`治疗法术：${card.name} 使用，但我方没有需要治疗的卡牌`)
        } else {
          log(`治疗法术：${card.name} 为我方 ${healedCount} 个卡牌恢复了生命`)
        }
      } else if (card.effect === 'fireball3') {
        // 火球优先对敌方角色；若敌方角色已被击败则对敌方HP
        const target = enemyBoard.value[0]
        if (target) {
          // 先消耗护盾
          const absorb = Math.min(target.shield ?? 0, 3)
          target.shield = Math.max(0, (target.shield ?? 0) - absorb)
          const dmg = 3 - absorb
          target.health = Math.max(0, target.health - dmg)
          log(`法术：火球术对 ${target.name} 造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}，盾 ${target.shield}）`)
          if (target.health <= 0) {
            enemyBoard.value.shift()
            log(`击败敌方角色：${target.name}！现在可对敌方造成直接伤害。`)
          }
        } else {
          enemyHP.value = Math.max(0, enemyHP.value - 3)
          log(`法术：火球术对敌方造成 3 伤害（敌方HP ${enemyHP.value}）`)
          checkVictory()
        }
      }
      hand.value.splice(idx, 1)
    } else if (card.type === 'equipment') {
      if (card.effect === 'teamBuffAtk1') {
        board.value = ref(board.value.map(m => ({ ...m, attack: m.attack + 1 }))).value
        log('装备：战旗生效，友方随从攻击 +1')
        // 旧版逻辑：直接群体加攻；兼容保留
      }
      // 默认仍按老逻辑“打出后进入弃牌”，如果想用拖拽装备，请使用 equipCardToMinion
      hand.value.splice(idx, 1)
    }
  }

  /**
   * 将手牌中的装备卡拖拽到某个随从上进行“装备”
   * - 从手牌移除该装备
   * - 给目标随从记录一条 equipmentNames，用于前端显示圆形标记
   * - 根据卡牌效果对该随从进行属性加成（目前仅支持 teamBuffAtk1：该随从攻击+1）
   */
  function equipCardToMinion(cardId: string, minionId: string) {
    const idx = hand.value.findIndex(c => c.id === cardId)
    if (idx < 0) {
      console.log('[GameStore] 装备卡不在手牌中:', cardId)
      return
    }
    const card = hand.value[idx]
    if (card.type !== 'equipment') {
      console.log('[GameStore] 卡牌不是装备类型:', card.type)
      return
    }

    console.log('[GameStore] 装备卡数据:', {
      id: card.id,
      name: card.name,
      type: card.type,
      bonusAttack: card.bonusAttack,
      bonusHp: card.bonusHp,
      bonusDefense: card.bonusDefense,
      hasRaw: !!card.raw,
      rawType: typeof card.raw,
      rawKeys: card.raw ? Object.keys(card.raw) : null,
      statModifiers: (card.raw as any)?.statModifiers,
      effect: card.effect,
      allKeys: Object.keys(card)
    })

    const mIdx = board.value.findIndex(m => m.id === minionId)
    if (mIdx < 0) {
      console.log('[GameStore] 随从不在战场上:', minionId)
      return
    }

    const target = board.value[mIdx]

    // 根据装备的 stat_modifiers 计算属性加成
    // 优先使用装备卡的bonus属性，如果没有则从raw数据重新解析
    let atkBonus = Number(card.bonusAttack ?? 0) || 0
    let hpBonus = Number(card.bonusHp ?? 0) || 0
    let shieldBonus = Number(card.bonusDefense ?? 0) || 0

    // 如果bonus属性都没有设置，尝试从raw数据重新解析
    if (atkBonus === 0 && hpBonus === 0 && shieldBonus === 0 && card.raw) {
      const rawStats = parseStatModifiers((card.raw as any)?.statModifiers)
      atkBonus = rawStats.atk
      hpBonus = rawStats.hp
      shieldBonus = rawStats.def
      console.log('[GameStore] 从raw数据重新解析装备加成:', rawStats)
    }

    console.log('[GameStore] 装备卡加成:', {
      cardName: card.name,
      bonusAttack: card.bonusAttack,
      bonusHp: card.bonusHp,
      bonusDefense: card.bonusDefense,
      rawStatModifiers: (card.raw as any)?.statModifiers,
      finalBonuses: { atkBonus, hpBonus, shieldBonus }
    })

    // 创建全新的随从对象，确保Vue能检测到变化
    const updatedMinion = {
      ...target,
      // 应用属性加成
      attack: target.attack + atkBonus,
      health: target.health + hpBonus,
      shield: (target.shield ?? 0) + shieldBonus,
      // 复制并添加装备名称
      equipmentNames: [...(target.equipmentNames || []), card.name]
    }

    console.log('[GameStore] 创建更新后的随从对象:', {
      original: { attack: target.attack, health: target.health, shield: target.shield },
      bonuses: { atkBonus, hpBonus, shieldBonus },
      updated: { attack: updatedMinion.attack, health: updatedMinion.health, shield: updatedMinion.shield }
    })

    // 直接替换数组中的对象
    board.value[mIdx] = updatedMinion

    console.log('[GameStore] 替换后的board:', board.value[mIdx])

    if (atkBonus || hpBonus || shieldBonus) {
      log(`装备：${card.name} 装备到 ${target.name}，面板变化 ATK +${atkBonus} / HP +${hpBonus} / 盾 +${shieldBonus}`)
    } else {
      log(`装备：${card.name} 装备到 ${target.name}`)
    }

    console.log('[GameStore] 装备状态:', {
      minionName: updatedMinion.name,
      attack: updatedMinion.attack,
      health: updatedMinion.health,
      shield: updatedMinion.shield,
      equipmentNames: updatedMinion.equipmentNames
    })

    // 响应式更新已通过对象替换实现

    // 从手牌中移除该装备
    hand.value.splice(idx, 1)
  }

  /**
   * 执行单个随从的攻击（带动画和延迟）
   */
  /**
   * 选择攻击目标
   * 优先级：
   * 1. 优先攻击位置 0, 1, 2（前排）
   * 2. 如果同一列有多张卡牌，攻击最早打出的（createdAt 最小）
   * 
   * 列的定义：
   * - 第1列：位置 0, 3
   * - 第2列：位置 1, 4
   * - 第3列：位置 2, 5
   */
  function selectAttackTarget(targetBoard: Minion[]): Minion | null {
    if (targetBoard.length === 0) return null
    
    // 优先选择位置 0, 1, 2（前排）的卡牌
    const frontRow = targetBoard.filter(m => m.position !== undefined && m.position < 3)
    
    if (frontRow.length > 0) {
      // 如果前排有卡牌，按列分组，每列选择最早打出的
      // 列0: 位置0, 列1: 位置1, 列2: 位置2
      const byColumn: Minion[][] = [[], [], []]
      
      for (const minion of frontRow) {
        if (minion.position !== undefined && minion.position < 3) {
          byColumn[minion.position].push(minion)
        }
      }
      
      // 找到第一个有卡牌的列
      for (let col = 0; col < 3; col++) {
        if (byColumn[col].length > 0) {
          // 如果该列有多张卡牌，选择最早打出的（createdAt 最小）
          byColumn[col].sort((a, b) => {
            const timeA = a.createdAt ?? 0
            const timeB = b.createdAt ?? 0
            return timeA - timeB
          })
          return byColumn[col][0]
        }
      }
    }
    
    // 如果前排没有卡牌，选择后排（位置 3, 4, 5）
    const backRow = targetBoard.filter(m => m.position !== undefined && m.position >= 3)
    
    if (backRow.length > 0) {
      // 按列分组，每列选择最早打出的
      // 列0: 位置3, 列1: 位置4, 列2: 位置5
      const byColumn: Minion[][] = [[], [], []]
      
      for (const minion of backRow) {
        if (minion.position !== undefined && minion.position >= 3) {
          byColumn[minion.position - 3].push(minion)
        }
      }
      
      // 找到第一个有卡牌的列
      for (let col = 0; col < 3; col++) {
        if (byColumn[col].length > 0) {
          // 如果该列有多张卡牌，选择最早打出的（createdAt 最小）
          byColumn[col].sort((a, b) => {
            const timeA = a.createdAt ?? 0
            const timeB = b.createdAt ?? 0
            return timeA - timeB
          })
          return byColumn[col][0]
        }
      }
    }
    
    // 如果都没有，返回第一个（降级方案）
    return targetBoard[0]
  }

  async function executeAttack(attacker: Minion, target: Minion | null, isBoss: boolean): Promise<void> {
    return new Promise((resolve) => {
      // 1. 设置攻击状态 - 开始攻击
      attacker.attackState = 'attacking'
      if (target) {
        attacker.attackTargetId = target.id
      }
      
      // 触发攻击开始事件
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('attack-start', {
          detail: { attackerId: attacker.id, targetId: target?.id, isBoss }
        }))
      }
      
      // 2. 等待角色移动到目标位置（350ms）
      setTimeout(() => {
        // 3. 播放攻击音效（角色到达目标位置时）
        soundManager.playSound(SoundType.ATTACK, { volume: 0.6 })
        
        // 4. 等待攻击动作（120ms，让音效和动画同步更快）
        setTimeout(() => {
          // 5. 计算伤害
          if (target) {
            const absorb = Math.min(target.shield ?? 0, attacker.attack)
            target.shield = Math.max(0, (target.shield ?? 0) - absorb)
            const dmg = attacker.attack - absorb
            const beforeHealth = target.health
            target.health = Math.max(0, target.health - dmg)
            
            // 6. 立即播放受击音效和触发受击反馈（显示扣血）
            soundManager.playSound(SoundType.HIT, { volume: 0.7 })
            if (typeof window !== 'undefined') {
              window.dispatchEvent(new CustomEvent('enemy-hit', {
                detail: { minionId: target.id, damage: dmg, beforeHealth, afterHealth: target.health }
              }))
            }
          
          log(`我方随从 ${attacker.name} 攻击敌方 ${target.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}，盾 ${target.shield}）`)
          
          if (target.health <= 0) {
            const enemyIndex = enemyBoard.value.findIndex(e => e.id === target.id)
            if (enemyIndex >= 0) {
              enemyBoard.value.splice(enemyIndex, 1)
            }
            log(`敌方角色 ${target.name} 被击败！现在可对敌方造成直接伤害。`)
          }
          } else if (isBoss) {
            // 攻击敌方本体
            soundManager.playSound(SoundType.HIT, { volume: 0.7 })
            if (typeof window !== 'undefined') {
              window.dispatchEvent(new CustomEvent('enemy-boss-hit', {
                detail: { damage: attacker.attack }
              }))
            }
            enemyHP.value = Math.max(0, enemyHP.value - attacker.attack)
          }
          
          // 7. 设置返回状态
          attacker.attackState = 'returning'
          
          // 8. 等待返回动画完成（400ms）
          setTimeout(() => {
            // 9. 重置攻击状态
            attacker.attackState = 'idle'
            attacker.attackTargetId = undefined
            
            // 触发攻击结束事件
            if (typeof window !== 'undefined') {
              window.dispatchEvent(new CustomEvent('attack-end', {
                detail: { attackerId: attacker.id }
              }))
            }
            
            resolve()
          }, 320)
        }, 120) // 攻击动作延迟（减少延迟，让音效更快响应）
      }, 350) // 移动到目标位置延迟
    })
  }

  async function endTurn() {
    if (turn.value !== 'player') return
    if (battleOver.value) return
    
    // 我方随从攻击阶段（只允许可以攻击的随从攻击）
    // 按位置排序，前排优先攻击
    const sortedBoard = [...board.value].sort((a, b) => (a.position ?? 999) - (b.position ?? 999))
    
    let total = 0
    
    // 逐个执行攻击（带延迟和动画），每次重新获取当前敌方最前排，保证每次都有目标且效果展示
    for (const m of sortedBoard) {
      if (battleOver.value) break
      if (m.canAttack === false) continue

      // 每次攻击前重新确定当前目标
      // 优先攻击位置 0, 1, 2（前排），如果同一列有多张卡牌，攻击最早打出的
      const currentEnemyRole = selectAttackTarget(enemyBoard.value)

      if (currentEnemyRole) {
        await executeAttack(m, currentEnemyRole, false)
      } else {
        await executeAttack(m, null, true)
        total += m.attack
      }

      // 攻击后触发治疗（如果有 heal_allies）
      maybeHealAllies(m, board.value, '攻击后')
    }
    
    // 如果敌方没有随从且造成了直接伤害，记录日志
    if (enemyBoard.value.length === 0 && total > 0) {
      log(`我方随从合计对敌方造成 ${total} 伤害（敌方HP ${enemyHP.value}）`)
      checkVictory()
    }

    // 轮到敌方回合
    turn.value = 'opponent'
    setTimeout(() => {
      enemyTurn()
    }, 500)
  }

  /**
   * 敌人回合逻辑
   * 1. 抽牌（从牌库抽1-2张到手牌）
   * 2. 增加法力值
   * 3. 出牌（根据法力值和手牌情况）
   * 4. 攻击阶段
   */
  function enemyTurn() {
              if (battleOver.value) return
    
    // 0. 回合开始：解除已有随从的召唤疲劳（仅对已经在场的随从生效）
    enemyBoard.value.forEach(m => {
      // 仅在之前已经上场过的随从上解除疲劳；新打出的随从会在本回合内保持 canAttack = false
      if (m.canAttack === false) {
        m.canAttack = true
      }
    })
    
    // 1. 抽牌阶段：从敌人牌库抽1-2张到手牌
    const drawCount = Math.min(2, enemyDeck.value.length)
    for (let i = 0; i < drawCount; i++) {
      if (enemyDeck.value.length > 0) {
        const card = enemyDeck.value.shift()
        if (card) {
          if (enemyHand.value.length < 10) {
            enemyHand.value.push(card)
            log(`敌人抽牌：${card.name}`)
          }
            }
          } else {
        if (!enemyDeckExhausted.value) {
          enemyDeckExhausted.value = true
          log('敌人牌库已耗尽')
        }
        break
      }
    }
    
    // 2. 增加法力值（每回合+1，最多到10）
    enemyManaMax.value = Math.min(enemyManaMax.value + 1, 10)
    enemyMana.value = enemyManaMax.value
    log(`敌人回合开始，法力值：${enemyMana.value}/${enemyManaMax.value}`)
    
    // 3. 出牌阶段：根据法力值和手牌情况出牌
    // 优先出角色卡，然后出法术卡，最后出装备卡
    const playableCards = enemyHand.value.filter(card => card.cost <= enemyMana.value)
    
    // 按优先级排序：角色卡 > 法术卡 > 装备卡
    playableCards.sort((a, b) => {
      if (a.type === 'character' && b.type !== 'character') return -1
      if (a.type !== 'character' && b.type === 'character') return 1
      if (a.type === 'spell' && b.type === 'equipment') return -1
      if (a.type === 'equipment' && b.type === 'spell') return 1
      return 0
    })
    
    // 尝试出牌（最多出3张，或直到法力值不足）
    let playedCount = 0
    const maxPlays = 3
    
    for (const card of playableCards) {
      if (playedCount >= maxPlays) break
      if (enemyMana.value < card.cost) break
      
      const handIndex = enemyHand.value.findIndex(c => c.id === card.id)
      if (handIndex < 0) continue
      
      // 消耗法力值
      enemyMana.value -= card.cost
      
      // 根据卡牌类型执行不同逻辑
      if (card.type === 'character') {
        // 角色卡：直接上场为独立单位（不再升星）
        if (enemyBoard.value.length < 6) {
          // 敌人自动选择第一个空位
          const occupiedPositions = new Set(enemyBoard.value.map(m => m.position).filter(p => p !== undefined))
          let autoPosition = -1
          for (let i = 0; i < 6; i++) {
            if (!occupiedPositions.has(i)) {
              autoPosition = i
              break
            }
          }
          if (autoPosition === -1) {
            enemyMana.value += card.cost // 返还法力值
            continue
          }
          
          const minion: Minion = {
            id: uid2(),
            name: card.name,
            attack: card.attack ?? 2,
            health: card.health ?? 20,
            shield: 0,
            stars: 1,
            canAttack: false, // 召唤疲劳：刚打出的随从本回合不能攻击
            effectPayload: card.effectPayload,
            position: autoPosition,
            attackState: 'idle', // 初始化攻击状态
            createdAt: Date.now() // 记录打出时间，用于攻击优先级判断
          }
          enemyBoard.value.push(minion)
          // 按位置排序
          enemyBoard.value.sort((a, b) => (a.position ?? 999) - (b.position ?? 999))
          hasEnemyPlayedCards.value = true // 标记敌人已经出过牌
          log(`敌人打出角色：${minion.name}（ATK ${minion.attack} / HP ${minion.health}），下回合才能攻击`)
        } else {
          // 战场已满，不出牌
          enemyMana.value += card.cost // 返还法力值
          continue
          }
        } else if (card.type === 'spell') {
        // 法术卡：执行效果
        enemyPlaySpell(card)
      } else if (card.type === 'equipment') {
        // 装备卡：装备到敌人角色
        if (enemyBoard.value.length > 0) {
          const targetIndex = Math.floor(Math.random() * enemyBoard.value.length)
          const target = enemyBoard.value[targetIndex]
          if (!target.equipmentNames) {
            target.equipmentNames = []
          }
          target.equipmentNames.push(card.name)
          
          // 应用装备属性加成
          const atkBonus = Number(card.bonusAttack ?? 0) || 0
          const hpBonus = Number(card.bonusHp ?? 0) || 0
          const shieldBonus = Number(card.bonusDefense ?? 0) || 0
          
          if (atkBonus) target.attack += atkBonus
          if (hpBonus) target.health += hpBonus
          if (shieldBonus) target.shield = (target.shield ?? 0) + shieldBonus
          
          log(`敌人装备：${card.name} 装备到 ${target.name}，ATK +${atkBonus} / HP +${hpBonus} / 盾 +${shieldBonus}`)
        } else {
          // 没有角色，不出装备
          enemyMana.value += card.cost // 返还法力值
          continue
        }
      }
      
      // 从手牌移除
      enemyHand.value.splice(handIndex, 1)
      playedCount++
    }
    
    // 4. 攻击阶段：敌人战场上的角色攻击（仅攻击已解除召唤疲劳的随从）
    // 使用异步执行，确保攻击动画逐个完成
    enemyAttackPhase().then(() => {
      // 回到玩家回合
      startPlayerTurn()
    })
  }

  /**
   * 敌人使用法术卡
   */
  function enemyPlaySpell(card: Card) {
    if (!card.effect) {
      log(`敌人使用法术：${card.name}（无效果）`)
      return
    }
    
    try {
      // 尝试解析 effect（可能是 JSON 字符串）
      let effectObj: any = card.effect
      if (typeof card.effect === 'string') {
        try {
          effectObj = JSON.parse(card.effect)
        } catch (e) {
          // 如果不是 JSON，当作普通字符串处理
          effectObj = { type: card.effect }
        }
      }
      
      // 根据效果类型执行
      if (effectObj.type === 'fireball3' || card.effect === 'fireball3') {
        // 火球术：对随机目标造成3点伤害
            if (board.value.length > 0) {
          const targetIndex = Math.floor(Math.random() * board.value.length)
          const target = board.value[targetIndex]
          const absorb = Math.min(target.shield ?? 0, 3)
          target.shield = Math.max(0, (target.shield ?? 0) - absorb)
              const dmg = 3 - absorb
          target.health = Math.max(0, target.health - dmg)
          log(`敌方法术：${card.name} 击中 ${target.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}）`)
          if (target.health <= 0) {
            board.value.splice(targetIndex, 1)
            log(`我方随从 ${target.name} 倒下。`)
              }
            } else {
              heroHP.value = Math.max(0, heroHP.value - 3)
          log(`敌方法术：${card.name} 对我方英雄造成 3 伤害（我方HP ${heroHP.value}）`)
              checkVictory()
        }
      } else if (effectObj.type === 'lightning3' || (typeof card.effect === 'string' && card.effect.includes('lightning3'))) {
        // 雷电之球：对随机目标造成3点伤害
        if (board.value.length > 0) {
          const targetIndex = Math.floor(Math.random() * board.value.length)
          const target = board.value[targetIndex]
          const absorb = Math.min(target.shield ?? 0, 3)
          target.shield = Math.max(0, (target.shield ?? 0) - absorb)
          const dmg = 3 - absorb
          target.health = Math.max(0, target.health - dmg)
          log(`敌方法术：${card.name} 击中 ${target.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}）`)
          if (target.health <= 0) {
            board.value.splice(targetIndex, 1)
            log(`我方随从 ${target.name} 倒下。`)
          }
        } else {
          heroHP.value = Math.max(0, heroHP.value - 3)
          log(`敌方法术：${card.name} 对我方英雄造成 3 伤害（我方HP ${heroHP.value}）`)
          checkVictory()
        }
      } else {
        log(`敌人使用法术：${card.name}（效果：${card.effect}）`)
      }
    } catch (e) {
      log(`敌人使用法术：${card.name}（效果解析失败）`)
        }
      }

  // 敌人攻击阶段锁，防止并发执行
  let isEnemyAttacking = false
  // 当前正在执行的敌人攻击 Promise，用于确保攻击按顺序执行
  let currentEnemyAttackPromise: Promise<void> | null = null

  /**
   * 敌人攻击阶段 - 逐个执行攻击动画
   */
  async function enemyAttackPhase() {
    if (battleOver.value) return
    if (isEnemyAttacking) {
      console.warn('[EnemyAttackPhase] 警告：敌人攻击阶段已在执行中，跳过重复调用')
      return
    }
    
    isEnemyAttacking = true
    const phaseStartTime = Date.now()
    console.log(`[EnemyAttackPhase] 开始敌人攻击阶段，时间: ${phaseStartTime}`)
    
    try {
      // 1. 敌人战场上的角色攻击（只允许可以攻击的随从攻击）
      // 每次攻击前重新获取当前我方前排，确保目标存在并能显示受击效果
      const sortedEnemyBoard = [...enemyBoard.value].sort((a, b) => (a.position ?? 999) - (b.position ?? 999))
      
      // 逐个执行攻击，确保动画逐个显示
      for (let i = 0; i < sortedEnemyBoard.length; i++) {
        const enemyMinion = sortedEnemyBoard[i]
        if (battleOver.value) break
        if (enemyMinion.canAttack === false) continue
        
        // 优先攻击位置 0, 1, 2（前排），如果同一列有多张卡牌，攻击最早打出的
        const target = selectAttackTarget(board.value)
        
        const loopIterationStart = Date.now()
        console.log(`[EnemyAttackPhase] [${i + 1}/${sortedEnemyBoard.length}] 循环迭代开始，准备执行攻击: ${enemyMinion.name} (ID: ${enemyMinion.id}), 目标: ${target?.name || '英雄'}, 时间: ${loopIterationStart}`)
        
        // 等待前一个攻击完全完成（如果有）
        // 必须在调用 executeEnemyAttack 之前等待，确保前一个攻击完全结束
        if (currentEnemyAttackPromise) {
          console.log(`[EnemyAttackPhase] [${i + 1}/${sortedEnemyBoard.length}] 等待前一个攻击完成...`)
          await currentEnemyAttackPromise
          console.log(`[EnemyAttackPhase] [${i + 1}/${sortedEnemyBoard.length}] 前一个攻击已完成，开始新的攻击`)
        }
        
        const beforeAttack = Date.now()
        
        // 执行单个敌人卡牌攻击（等待完成）
        // 将 Promise 保存到 currentEnemyAttackPromise，确保下一个攻击等待
        // 注意：必须在 await 之前调用 executeEnemyAttack，但要在前一个攻击完成后
        currentEnemyAttackPromise = executeEnemyAttack(enemyMinion, target)
        console.log(`[EnemyAttackPhase] [${i + 1}/${sortedEnemyBoard.length}] 已调用 executeEnemyAttack，等待 Promise resolve...`)
        await currentEnemyAttackPromise
        // 攻击完成后，清除 Promise 引用，让下一个攻击可以开始
        currentEnemyAttackPromise = null
        
        const afterAttack = Date.now()
        const waitDuration = afterAttack - beforeAttack
        console.log(`[EnemyAttackPhase] [${i + 1}/${sortedEnemyBoard.length}] await 完成，等待了 ${waitDuration}ms，准备下一个攻击`)
        
        // 攻击后触发治疗（如果有 heal_allies）
        maybeHealAllies(enemyMinion, enemyBoard.value, '攻击后', true)
      }
      
      const phaseEndTime = Date.now()
      const totalDuration = phaseEndTime - phaseStartTime
      console.log(`[EnemyAttackPhase] 敌人攻击阶段完成，总耗时: ${totalDuration}ms`)
    } finally {
      isEnemyAttacking = false
      currentEnemyAttackPromise = null // 确保清理
    }
    
    // 2. 敌人本体攻击（如果敌人面板有攻击力）- 在所有卡牌攻击完成后执行
    if (!battleOver.value && enemyPanel.value && enemyPanel.value.attack && enemyPanel.value.attack > 0) {
      const enemyBaseAttack = enemyPanel.value.attack
      
      // 优先攻击我方随从
      if (board.value.length > 0) {
        const targetIndex = Math.floor(Math.random() * board.value.length)
        const target = board.value[targetIndex]
        
        // 敌人本体攻击不需要动画，直接执行伤害
        // 先消耗护盾
        const absorb = Math.min(target.shield ?? 0, enemyBaseAttack)
        target.shield = Math.max(0, (target.shield ?? 0) - absorb)
        const dmg = enemyBaseAttack - absorb
        
        target.health = Math.max(0, target.health - dmg)
        
        soundManager.playSound(SoundType.ATTACK, { volume: 0.6 })
        setTimeout(() => {
          soundManager.playSound(SoundType.HIT, { volume: 0.7 })
          if (typeof window !== 'undefined') {
            window.dispatchEvent(new CustomEvent('ally-hit', { 
              detail: { minionId: target.id, damage: dmg } 
            }))
          }
        }, 120)
        
        log(`敌人本体发动攻击，对我方 ${target.name} 造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}）`)
        
        if (target.health <= 0) {
          board.value.splice(targetIndex, 1)
          log(`我方随从 ${target.name} 倒下。`)
        }
      } else {
        // 没有随从，直接攻击英雄
        soundManager.playSound(SoundType.ATTACK, { volume: 0.6 })
        setTimeout(() => {
          soundManager.playSound(SoundType.HIT, { volume: 0.7 })
          // 触发玩家受击反馈
          if (typeof window !== 'undefined') {
            window.dispatchEvent(new CustomEvent('player-hit', { 
              detail: { damage: enemyBaseAttack } 
            }))
          }
        }, 120)
        
        heroHP.value = Math.max(0, heroHP.value - enemyBaseAttack)
        log(`敌人本体发动攻击，对我方英雄造成 ${enemyBaseAttack} 伤害（我方HP ${heroHP.value}）`)
        checkVictory()
      }
    }
  }

  /**
   * 执行单个敌人卡牌攻击（带动画）
   */
  async function executeEnemyAttack(enemyMinion: Minion, target: Minion | null): Promise<void> {
    // 关键修复：在创建 Promise 之前，先等待前一个攻击完成
    // 这样可以确保在开始计时前，前一个攻击已经完全结束
    const previousPromise = currentEnemyAttackPromise
    if (previousPromise) {
      console.log(`[EnemyAttack] 等待前一个攻击完成: ${enemyMinion.name} (ID: ${enemyMinion.id})`)
      await previousPromise
      console.log(`[EnemyAttack] 前一个攻击已完成，开始新攻击: ${enemyMinion.name} (ID: ${enemyMinion.id})`)
    }
    
    // 现在前一个攻击已经完成，创建新的 Promise 并开始计时
    return new Promise((resolve) => {
      const attackStartTime = Date.now()
      console.log(`[EnemyAttack] 开始攻击: ${enemyMinion.name} (ID: ${enemyMinion.id}) 在 ${attackStartTime}`)
      
      // 触发攻击开始事件
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new CustomEvent('attack-start', {
          detail: { attackerId: enemyMinion.id, targetId: target?.id, isBoss: false }
        }))
      }
      
      // 使用和我方攻击相同的时间间隔，但需要等待完整的 CSS 动画（900ms）
      // CSS 动画 attack-move-clone 总时长为 900ms
      // 时间线：
      // 0ms: 触发攻击开始事件，开始动画
      // 350ms: 播放攻击音效，执行伤害计算（动画进行到约 39%）
      // 470ms: 播放受击音效（350 + 120）
      // 900ms: 动画完成，触发攻击结束事件，resolve Promise
      
      // 1. 等待角色移动到目标位置（350ms）
      setTimeout(() => {
        // 2. 播放攻击音效（角色到达目标位置时）
        soundManager.playSound(SoundType.ATTACK, { volume: 0.5 })
        
        // 3. 等待攻击动作（120ms，让音效和动画同步更快）
        setTimeout(() => {
          // 4. 计算伤害
          if (target) {
            const targetIndex = board.value.findIndex(m => m.id === target.id)
            
            const absorb = Math.min(target.shield ?? 0, enemyMinion.attack)
            target.shield = Math.max(0, (target.shield ?? 0) - absorb)
            const dmg = enemyMinion.attack - absorb
            
            target.health = Math.max(0, target.health - dmg)
            
            // 5. 立即播放受击音效和触发受击反馈（显示扣血）
            soundManager.playSound(SoundType.HIT, { volume: 0.6 })
            if (typeof window !== 'undefined') {
              window.dispatchEvent(new CustomEvent('ally-hit', { 
                detail: { minionId: target.id, damage: dmg } 
              }))
            }
            
            log(`敌人 ${enemyMinion.name} 攻击我方 ${target.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}）`)
            
            if (target.health <= 0 && targetIndex >= 0) {
              board.value.splice(targetIndex, 1)
              log(`我方随从 ${target.name} 倒下。`)
            }
          } else {
            soundManager.playSound(SoundType.HIT, { volume: 0.6 })
            if (typeof window !== 'undefined') {
              window.dispatchEvent(new CustomEvent('player-hit', { 
                detail: { damage: enemyMinion.attack } 
              }))
            }
            
            heroHP.value = Math.max(0, heroHP.value - enemyMinion.attack)
            log(`敌人 ${enemyMinion.name} 攻击我方英雄，造成 ${enemyMinion.attack} 伤害（我方HP ${heroHP.value}）`)
            checkVictory()
          }
        }, 120) // 攻击动作延迟（减少延迟，让音效更快响应）
      }, 350) // 移动到目标位置延迟
      
      // 6. 等待完整动画完成（900ms）后再触发结束事件并 resolve
      // 确保下一个攻击在前一个动画完全结束后才开始
      setTimeout(() => {
        const attackEndTime = Date.now()
        const actualDuration = attackEndTime - attackStartTime
        console.log(`[EnemyAttack] 攻击完成: ${enemyMinion.name} (ID: ${enemyMinion.id}) 在 ${attackEndTime}, 实际耗时: ${actualDuration}ms`)
        
        // 7. 触发攻击结束事件
        if (typeof window !== 'undefined') {
          window.dispatchEvent(new CustomEvent('attack-end', {
            detail: { attackerId: enemyMinion.id }
          }))
        }
        
        // 8. 完成攻击，resolve Promise
        // 确保在动画完全结束后才 resolve，让下一个攻击等待
        console.log(`[EnemyAttack] Promise 准备 resolve: ${enemyMinion.name} (ID: ${enemyMinion.id})`)
        resolve()
        console.log(`[EnemyAttack] Promise 已 resolve: ${enemyMinion.name} (ID: ${enemyMinion.id})`)
      }, 900) // 等待完整的 CSS 动画时长（900ms）
    })
  }

  // 初始化
  if (deck.value.length === 0) reset()

  // 从上阵区加载卡牌作为战斗牌库
  const loadEquippedCardsAsDeck = async () => {
    const campStore = useCampStore()

    // 获取camp store中已装备的卡牌（通过API获取最新状态）
    try {
      await campStore.fetchCampData()
      console.log('[GameStore] loadEquippedCardsAsDeck - camp data loaded')

      // 获取已部署的角色卡牌
      const deployedChars = campStore.userCardCharacters.filter((c: any) => c.isDeployed === true)
      console.log('[GameStore] loadEquippedCardsAsDeck - deployed characters:', deployedChars.length)

      // 获取已装备的普通卡牌（通过loadoutId=1标识）
      let equippedCards: any[] = []
      try {
        const deckResponse = await userCardApi.getDeckCards(1) // 使用默认卡组ID
        equippedCards = Array.isArray(deckResponse.data?.data) ? deckResponse.data.data : []
        console.log('[GameStore] loadEquippedCardsAsDeck - equipped cards:', equippedCards.length)
      } catch (e) {
        console.warn('[GameStore] loadEquippedCardsAsDeck - failed to load equipped cards:', e)
        equippedCards = []
      }

      // 转换角色卡牌格式
      const characterCards: Card[] = deployedChars.map((char: any) => {
        const cardType = 'character'
        const manaCost = Math.max(1, 3) // 角色卡默认3费
        const attack = typeof char.attack === 'number' ? char.attack : 1
        const health = typeof char.hp === 'number' || typeof char.health === 'number' ?
          (char.hp ?? char.health ?? 1) : 1
        const name = char.name || char.cardName || char.characterName || '未知角色'

        return {
          id: uid(),
          name,
          cost: manaCost,
          type: cardType,
          attack,
          health,
          cardCharacterId: char.id || char.cardCharacterId,
          raw: char
        }
      })

      // 转换普通卡牌格式
      const deckCards: Card[] = equippedCards.map((card: any) => {
        const cardType = normalizeCardTypeValue(card.cardType || card.type)
        const manaCost = Math.max(1, Number(card.manaCost ?? (cardType === 'spell' ? 2 : 3)))
        const attack = typeof card.attack === 'number' ? card.attack : undefined
        const health = typeof card.health === 'number' ? card.health : undefined
        const name = card.cardName || card.name || '未命名卡牌'
        const effect = inferEffectFromCard(card, cardType)

        return {
          id: uid(),
          name,
          cost: manaCost,
          type: cardType,
          attack,
          health,
          effect,
          cardCharacterId: card.cardCharacterId,
          raw: card
        }
      })

      const combined = [...characterCards, ...deckCards]

      deck.value = combined
      initialDeck.value = combined.slice()

      if (combined.length === 0) {
        log('提示：当前未配置上阵卡牌，请返回营地在卡组管理中选择卡牌后再开始战斗')
      } else {
        log(`已从上阵区加载 ${combined.length} 张卡牌（角色 ${characterCards.length}，技能 ${deckCards.length}）`)
      }

      shuffle(deck.value)
      hand.value = []
      board.value = []

      return combined
    } catch (e: any) {
      log('从上阵区加载卡牌失败：' + e?.message)
      return []
    }
  }

  return {
    // state
    heroHP, enemyHP, mana, manaMax, deck, initialDeck, hand, board,
    enemyDeck, enemyHand, enemyBoard, enemyMana, enemyManaMax, enemyDeckExhausted, hasEnemyPlayedCards,
    currentEnemyId, enemyPanel,
    turn, canPlay, logs, enemyDifficulty, battleOver, winner,
    // actions
    reset, draw, startPlayerTurn, playCard, endTurn, log, configureEncounter, loadUserDeckFromDB, loadEnemyDeck, equipCardToMinion,
    loadPlayerHealthFromCamp, // 导出加载血量函数，供外部调用
    deckExhausted, loadEquippedCardsAsDeck,

    // 营地衔接：将营地的角色与道具转换为战斗牌库
    setDeckFromCamp(campChars: Array<{ name: string; attack: number; health: number }>, campItems: Array<{ name?: string; effect: 'fireball3' | 'teamBuffAtk1'; cost?: number }>) {
      const charCards: Card[] = (campChars ?? []).map(ch => ({
        id: uid(),
        name: ch.name,
        cost: 2,
        type: 'character',
        attack: Math.max(0, Number(ch.attack ?? 1)),
        health: Math.max(1, Number(ch.health ?? 1))
      }))
      const itemCards: Card[] = (campItems ?? []).map(it => ({
        id: uid(),
        name: it.name ?? (it.effect === 'fireball3' ? '火球术' : '战旗'),
        cost: Math.max(1, Number(it.cost ?? (it.effect === 'fireball3' ? 2 : 3))),
        type: (it.effect === 'fireball3' ? 'spell' : 'equipment'),
        effect: it.effect
      }))
      deck.value = [...charCards, ...itemCards]
      initialDeck.value = deck.value.slice()
      shuffle(deck.value)
      hand.value = []
      board.value = []
      log('已从营地手牌生成战斗牌库')
      // 重新开局
      reset()
    }
  }
})