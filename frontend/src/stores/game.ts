import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { gameApi, userCardApi, userCardCharacterApi } from '@/lib/api'
import { useAuthStore } from './auth'
import { useCampStore } from './camp'
import type { ApiResponse } from '@/lib/api'
import type { AxiosResponse } from 'axios'
import apiClient from '@/lib/api'

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
}

function uid() {
  return Math.random().toString(36).slice(2, 10)
}

function baseDeck(): Card[] {
  // 禁用静态默认卡组，牌库仅来自数据库或营地同步
  return []
}

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

  async function loadCharacterTraits() {
    try {
      const response: AxiosResponse<ApiResponse<Record<string, CharacterTrait>>> = await gameApi.getCharacterTraits()
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

  function applyTraitsAtTurnStart() {
    // 我方每回合开始时，根据在场角色的特性结算
    if (board.value.length === 0) return
    board.value.forEach(m => {
      const t = charTraits.value[m.name]
      if (!t) return
      const power = getTraitPower(m.name, m.stars ?? 1)
      if (power <= 0) return
      switch (t.trait_key) {
        case 'priest_bless':
          // 祭司：为友方全体恢复生命
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
          name: char.characterName || '营地角色',
          cost: 2,
          type: 'character' as CardType,
          attack,
          health
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
      const [deckResponse, deployedResponse] = await Promise.all([
        userCardApi.getDeckCards(loadoutId),
        userCardCharacterApi.getDeployedCardCharacters()
      ])

      const deckCardsRaw: UserCardResponse[] = Array.isArray(deckResponse.data?.data) ? deckResponse.data.data : []
      const deployedCharsRaw: UserCardCharacterResponse[] = Array.isArray(deployedResponse.data?.data)
        ? deployedResponse.data.data
        : []

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
          effect
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
    name: string
    type: 'character' | 'spell' | 'equipment'
    attack?: number
    health?: number
    effect?: string
    unique_play?: boolean
  }

  // 从数据库加载敌方手牌（按关卡与难度）
  /**
   * 从后端加载敌人卡牌列表，初始化敌人牌库和手牌
   */
  async function loadEnemyDeck(stageNum: number) {
    const diff = enemyDifficulty.value
    try {
      const response: AxiosResponse<ApiResponse<EnemyCardResponse[]>> = await gameApi.getEnemyCards(stageNum, diff)
      if (response.data.code === 200 && response.data.data) {
        // 将后端返回的卡牌转换为前端 Card 格式，作为敌人牌库
        const cards: Card[] = response.data.data.map((r: EnemyCardResponse) => {
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
    enemyHP.value = 100
    manaMax.value = 1
    mana.value = 1
    turn.value = 'player'
    battleOver.value = false
    winner.value = null
    // 不再使用静态默认卡组；保留现有 deck（来自DB或营地）
    hand.value = []
    board.value = []
    
    // 重置敌人卡牌系统
    // 注意：敌人牌库和手牌应该在 loadEnemyDeck 中初始化
    // 这里只重置战场和状态
    enemyBoard.value = []
    enemyMana.value = enemyManaMax.value
    enemyDeckExhausted.value = false
    hasEnemyPlayedCards.value = false // 重置敌人出牌标记
    
    logs.value = []
    deckExhausted.value = false
    
    // 重置濒死机制标记
    hasNearDeathTriggered.value = false
    
    // 确保血量不超过最大血量（防止数据不一致）
    const playerChar = campStore.playerCharacter
    const maxHp = playerChar?.maxHp || 100
    if (heroHP.value > maxHp) {
      log(`警告：当前血量 ${heroHP.value} 超过最大血量 ${maxHp}，已限制为 ${maxHp}`)
      heroHP.value = maxHp
    }
    
    if (deck.value.length > 0) {
      shuffle(deck.value)
      // 战斗开始抽两张
      draw(2)
    } else {
      log('提示：当前牌库为空，已跳过起始抽牌。请返回营地同步手牌后再开战。')
    }
    log(`战斗开始：玩家生命 ${heroHP.value}/${maxHp}，敌方初始生命 100。必须先击败对方角色后才能对其造成伤害。`)
  }

  function draw(n = 2) {
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

  function playCard(id: string) {
    if (turn.value !== 'player') return
    if (battleOver.value) return
    const idx = hand.value.findIndex(c => c.id === id)
    if (idx < 0) return
    const card = hand.value[idx]
    if (card.cost > mana.value) return
    // 扣蓝
    mana.value -= card.cost
    // 处理类型
    if (card.type === 'character') {
      if (board.value.length >= 7) return // 简化随从上限7
      // 如果已有同名随从，则执行升星（不重复上场）
      const existIdx = board.value.findIndex(x => x.name === card.name)
      if (existIdx >= 0) {
        const t = board.value[existIdx]
        t.stars = (t.stars ?? 1) + 1
        t.attack += 1
        t.health += 1
        log(`升星：${t.name} 升至 ${t.stars} 星（ATK ${t.attack} / HP ${t.health}）`)
      } else {
        const m: Minion = {
          id: card.id,
          name: card.name,
          attack: card.attack ?? 0,
          health: card.health ?? 1,
          shield: 0,
          stars: 1,
          canAttack: false // 召唤疲劳：刚打出的随从本回合不能攻击
        }
        board.value.push(m)
        log(`召唤：${card.name}（ATK ${card.attack ?? 0} / HP ${card.health ?? 1}），下回合才能攻击`)
        // 即时触发一次特性（按星级计算强度），随后每回合开始也会自动结算
        const t = charTraits.value[card.name]
        if (t) {
          const power = getTraitPower(card.name, 1)
          if (power > 0) {
            if (t.trait_key === 'priest_bless') {
              board.value = board.value.map(x => ({ ...x, health: x.health + power }))
              log(`特性：${card.name} 祭司祝福，友方全体恢复 ${power} 点生命`)
            } else if (t.trait_key === 'shield_guard') {
              let idx = 0
              for (let i = 1; i < board.value.length; i++) {
                if (board.value[i].health < board.value[idx].health) idx = i
              }
              const target = board.value[idx]
              target.shield = (target.shield ?? 0) + power
              log(`特性：${card.name} 盾卫守护，为 ${target.name} 提供 ${power} 点护盾`)
            }
          }
        }
      }
      hand.value.splice(idx, 1)
    } else if (card.type === 'spell') {
      if (card.effect === 'fireball3') {
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
    if (idx < 0) return
    const card = hand.value[idx]
    if (card.type !== 'equipment') return

    const mIdx = board.value.findIndex(m => m.id === minionId)
    if (mIdx < 0) return

    const target = board.value[mIdx]
    // 初始化装备列表
    if (!target.equipmentNames) {
      target.equipmentNames = []
    }
    target.equipmentNames.push(card.name)

    // 根据装备的 stat_modifiers 给单个随从加成：
    // - attack  → 角色攻击力
    // - hp      → 角色生命值
    // - defense → 角色护盾（防御力）
    const atkBonus = Number(card.bonusAttack ?? 0) || 0
    const hpBonus = Number(card.bonusHp ?? 0) || 0
    const shieldBonus = Number(card.bonusDefense ?? 0) || 0

    if (atkBonus) {
      target.attack += atkBonus
    }
    if (hpBonus) {
      target.health += hpBonus
    }
    if (shieldBonus) {
      target.shield = (target.shield ?? 0) + shieldBonus
    }

    if (atkBonus || hpBonus || shieldBonus) {
      log(`装备：${card.name} 装备到 ${target.name}，面板变化 ATK +${atkBonus} / HP +${hpBonus} / 盾 +${shieldBonus}`)
    } else {
      log(`装备：${card.name} 装备到 ${target.name}`)
    }

    // 从手牌中移除该装备
    hand.value.splice(idx, 1)
  }

  function endTurn() {
    if (turn.value !== 'player') return
    if (battleOver.value) return
    // 我方随从攻击阶段（只允许可以攻击的随从攻击）
    const enemyRole = enemyBoard.value[0]
    let total = 0
    board.value.forEach(m => {
      // 检查是否可以攻击（召唤疲劳机制）
      if (m.canAttack === false) {
        return // 跳过不能攻击的随从
      }
      
      if (enemyRole) {
        // 先消耗护盾
        const absorb = Math.min(enemyRole.shield ?? 0, m.attack)
        enemyRole.shield = Math.max(0, (enemyRole.shield ?? 0) - absorb)
        const dmg = m.attack - absorb
        enemyRole.health = Math.max(0, enemyRole.health - dmg)
        log(`我方随从 ${m.name} 攻击敌方 ${enemyRole.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${enemyRole.health}，盾 ${enemyRole.shield}）`)
        if (enemyRole.health <= 0) {
          enemyBoard.value.shift()
          log(`敌方角色 ${enemyRole.name} 被击败！现在可对敌方造成直接伤害。`)
        }
      } else {
        enemyHP.value = Math.max(0, enemyHP.value - m.attack)
        total += m.attack
      }
    })
    if (!enemyRole && total > 0) {
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
        // 角色卡：上场到战场
        // 检查是否已有同名角色（升星）
        const existingIndex = enemyBoard.value.findIndex(m => m.name === card.name)
        if (existingIndex >= 0) {
          // 升星
          const existing = enemyBoard.value[existingIndex]
          existing.stars = (existing.stars ?? 1) + 1
          existing.attack += 1
          existing.health += 2
          log(`敌人升星：${existing.name} 升至 ${existing.stars}★（ATK ${existing.attack} / HP ${existing.health}）`)
        } else if (enemyBoard.value.length < 7) {
          // 新角色上场
          const minion: Minion = {
            id: uid2(),
            name: card.name,
            attack: card.attack ?? 2,
            health: card.health ?? 20,
            shield: 0,
            stars: 1,
            canAttack: false // 召唤疲劳：刚打出的随从本回合不能攻击
          }
          enemyBoard.value.push(minion)
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
    enemyAttackPhase()
    
    // 回到玩家回合
    startPlayerTurn()
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

  /**
   * 敌人攻击阶段
   */
  function enemyAttackPhase() {
    if (battleOver.value) return
    
    // 敌人战场上的角色攻击（只允许可以攻击的随从攻击）
    enemyBoard.value.forEach((enemyMinion, index) => {
      if (battleOver.value) return
      
      // 检查是否可以攻击（召唤疲劳机制）
      if (enemyMinion.canAttack === false) {
        return // 跳过不能攻击的随从
      }
      
      // 优先攻击我方随从
      if (board.value.length > 0) {
        const targetIndex = Math.floor(Math.random() * board.value.length)
        const target = board.value[targetIndex]
        
        // 先消耗护盾
        const absorb = Math.min(target.shield ?? 0, enemyMinion.attack)
        target.shield = Math.max(0, (target.shield ?? 0) - absorb)
        const dmg = enemyMinion.attack - absorb
        
        target.health = Math.max(0, target.health - dmg)
        log(`敌人 ${enemyMinion.name} 攻击我方 ${target.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${target.health}）`)
        
        if (target.health <= 0) {
          board.value.splice(targetIndex, 1)
          log(`我方随从 ${target.name} 倒下。`)
        }
      } else {
        // 没有随从，直接攻击英雄
        heroHP.value = Math.max(0, heroHP.value - enemyMinion.attack)
        log(`敌人 ${enemyMinion.name} 攻击我方英雄，造成 ${enemyMinion.attack} 伤害（我方HP ${heroHP.value}）`)
        checkVictory()
      }
    })
  }

  // 初始化
  if (deck.value.length === 0) reset()

  return {
    // state
    heroHP, enemyHP, mana, manaMax, deck, hand, board, 
    enemyDeck, enemyHand, enemyBoard, enemyMana, enemyManaMax, enemyDeckExhausted, hasEnemyPlayedCards,
    turn, canPlay, logs, enemyDifficulty, battleOver, winner,
    // actions
    reset, draw, startPlayerTurn, playCard, endTurn, log, configureEncounter, loadUserDeckFromDB, loadEnemyDeck, equipCardToMinion,
    loadPlayerHealthFromCamp, // 导出加载血量函数，供外部调用
    deckExhausted,

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