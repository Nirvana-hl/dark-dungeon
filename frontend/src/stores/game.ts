import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { gameApi } from '@/lib/api'
import { useAuthStore } from './auth'
import type { ApiResponse } from '@/lib/api'
import type { AxiosResponse } from 'axios'

export type CardType = 'character' | 'spell' | 'equipment'

export interface Card {
  id: string
  name: string
  cost: number
  type: CardType
  attack?: number
  health?: number
  effect?: 'fireball3' | 'teamBuffAtk1'
}

export interface Minion {
  id: string
  name: string
  attack: number
  health: number
  shield?: number
  stars?: number
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
  const board = ref<Minion[]>([])
  const enemyBoard = ref<Minion[]>([]) // 敌方“角色/随从”，需先击败其中角色后才能打敌方HP

  // 胜负状态
  const battleOver = ref<boolean>(false)
  const winner = ref<'player' | 'enemy' | null>(null)

  function checkVictory() {
    if (enemyHP.value <= 0 && !battleOver.value) {
      battleOver.value = true
      winner.value = 'player'
      log('胜利：敌方生命归零！')
    }
    if (heroHP.value <= 0 && !battleOver.value) {
      battleOver.value = true
      winner.value = 'enemy'
      log('失败：我方生命归零。')
    }
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
    name: string
    type: 'character' | 'spell' | 'equipment'
    quantity?: number
    attack?: number
    health?: number
    effect?: 'fireball3' | 'teamBuffAtk1'
  }

  // 从数据库加载玩家手牌
  async function loadUserDeckFromDB() {
    const authStore = useAuthStore()
    if (!authStore.isAuthenticated) {
      log('未登录，无法加载玩家手牌')
      return
    }
    try {
      const response: AxiosResponse<ApiResponse<UserCardResponse[]>> = await gameApi.getUserCards()
      if (response.data.code === 200 && response.data.data) {
        const cards: Card[] = response.data.data.flatMap((r: UserCardResponse) => {
          const qty = Math.max(1, Number(r.quantity ?? 1))
          return Array.from({ length: qty }, () => ({
            id: uid2(),
            name: r.name,
            cost: r.type === 'spell' ? 2 : r.type === 'equipment' ? 3 : 2,
            type: r.type as CardType,
            attack: r.attack ?? undefined,
            health: r.health ?? undefined,
            effect: r.effect as 'fireball3' | 'teamBuffAtk1' | undefined
          }))
        })
        deck.value = cards
        initialDeck.value = cards.slice()
        if (cards.length === 0) {
          log('提示：数据库未找到玩家手牌（user_cards），请先在营地同步手牌后再开始战斗')
        }
        shuffle(deck.value)
        hand.value = []
        board.value = []
        log('已从数据库加载玩家手牌')
      }
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
  async function loadEnemyDeck(stageNum: number) {
    const diff = enemyDifficulty.value
    try {
      const response: AxiosResponse<ApiResponse<EnemyCardResponse[]>> = await gameApi.getEnemyCards(stageNum, diff)
      if (response.data.code === 200 && response.data.data) {
        enemyHand.value = response.data.data.map((r: EnemyCardResponse) => ({
          id: uid2(),
          name: r.name,
          cost: r.type === 'spell' ? 2 : r.type === 'equipment' ? 3 : 2,
          type: r.type as CardType,
          attack: r.attack ?? undefined,
          health: r.health ?? undefined,
          effect: r.effect ?? undefined,
          unique_play: !!r.unique_play
        }))
        log(`已加载敌方手牌（${diff}，关卡 ${stageNum}）`)
      }
    } catch (e: any) {
      log('加载敌方手牌失败：' + e?.message)
    }
  }

  const enemyHand = ref<Array<{ id: string; name: string; type: CardType; cost: number; attack?: number; health?: number; effect?: string; unique_play?: boolean }>>([])
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

  function reset() {
    heroHP.value = 100
    enemyHP.value = 100
    manaMax.value = 1
    mana.value = 1
    turn.value = 'player'
    battleOver.value = false
    winner.value = null
    // 不再使用静态默认卡组；保留现有 deck（来自DB或营地）
    hand.value = []
    board.value = []
    // 根据难度设置初始敌人
    if (enemyDifficulty.value === '普通') {
      enemyBoard.value = [{ id: uid(), name: '暗影猎手', attack: 3, health: 60, shield: 0 }]
    } else if (enemyDifficulty.value === '困难') {
      enemyBoard.value = [{ id: uid(), name: '腐化巨龙', attack: 6, health: 80, shield: 0 }]
    } else {
      enemyBoard.value = [{ id: uid(), name: '上古恶龙', attack: 8, health: 100, shield: 0 }]
    }
    logs.value = []
    if (deck.value.length > 0) {
      shuffle(deck.value)
      // 战斗开始抽两张
      draw(2)
    } else {
      log('提示：当前牌库为空，已跳过起始抽牌。请返回营地同步手牌后再开战。')
    }
    log('战斗开始：双方初始生命 100。必须先击败对方角色后才能对其造成伤害。')
  }

  function draw(n = 2) {
    for (let i = 0; i < n; i++) {
      const c = deck.value.shift()
      if (c) {
        if (hand.value.length < 10) hand.value.push(c)
        else {
          // 手牌满，丢弃
        }
      } else {
        // 牌库可重复抽取：当牌库耗尽时，从初始牌库快照随机补抽（有放回，不再疲劳）
        if (initialDeck.value.length > 0) {
          const idx = Math.floor(Math.random() * initialDeck.value.length)
          const base = initialDeck.value[idx]
          const clone: Card = { ...base, id: uid() }
          if (hand.value.length < 10) hand.value.push(clone)
        } else {
          // 无初始快照时，保持空抽
        }
      }
    }
  }

  function startPlayerTurn() {
    turn.value = 'player'
    manaMax.value = Math.min(10, manaMax.value + 1) // 简化：每回合+1上限
    mana.value = manaMax.value
    draw(2)
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
          stars: 1
        }
        board.value.push(m)
        log(`召唤：${card.name}（ATK ${card.attack ?? 0} / HP ${card.health ?? 1}）`)
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
      }
      hand.value.splice(idx, 1)
    }
  }

  function endTurn() {
    if (turn.value !== 'player') return
    if (battleOver.value) return
    // 我方随从攻击阶段（简化：全部攻击）
    const enemyRole = enemyBoard.value[0]
    let total = 0
    board.value.forEach(m => {
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
      // 敌方先尝试打出手牌（根据难度生成敌人上场）
      // 敌方出牌：优先从敌方手牌抽一张
      if (enemyHand.value.length > 0 && enemyBoard.value.length < 3) {
        const card = enemyHand.value.shift()!
        if (card.type === 'character') {
          // 不重复上场（同名唯一）
          if (enemyBoard.value.findIndex(x => x.name === card.name) < 0) {
            const m: Minion = { id: uid2(), name: card.name, attack: card.attack ?? 2, health: card.health ?? 20, shield: 0, stars: 1 }
            enemyBoard.value.push(m)
            log(`敌方打出：${m.name}（ATK ${m.attack} / HP ${m.health}）`)
            // 敌方特性
            if (m.name.includes('暗影猎手')) {
              m.attack += 1
              log('敌方特性：暗影猎手 攻击 +1')
            } else if (m.name.includes('巨龙')) {
              heroHP.value = Math.max(0, heroHP.value - 2)
              log('敌方特性：巨龙吐息，对我方英雄造成 2 伤害（我方HP ' + heroHP.value + '）')
              checkVictory()
              if (battleOver.value) return
            }
          } else {
            // 同名则升星
            const i = enemyBoard.value.findIndex(x => x.name === card.name)
            const t = enemyBoard.value[i]
            t.stars = (t.stars ?? 1) + 1
            t.attack += 1
            t.health += 1
            log(`敌方升星：${t.name} 升至 ${t.stars} 星（ATK ${t.attack} / HP ${t.health}）`)
          }
        } else if (card.type === 'spell') {
          if (card.effect === 'lightning3') {
            // 雷电之球：优先轰击我方随从，否则对英雄
            if (board.value.length > 0) {
              const i = Math.floor(Math.random() * board.value.length)
              const m = board.value[i]
              const absorb = Math.min(m.shield ?? 0, 3)
              m.shield = Math.max(0, (m.shield ?? 0) - absorb)
              const dmg = 3 - absorb
              m.health = Math.max(0, m.health - dmg)
              log(`敌方法术：雷电之球击中 ${m.name}，造成 ${dmg} 伤害，护盾吸收 ${absorb}（剩余HP ${m.health}，盾 ${m.shield}）`)
              if (m.health <= 0) {
                board.value.splice(i, 1)
                log(`我方随从 ${m.name} 倒下。`)
              }
            } else {
              heroHP.value = Math.max(0, heroHP.value - 3)
              log(`敌方法术：雷电之球对我方英雄造成 3 伤害（我方HP ${heroHP.value}）`)
              checkVictory()
              if (battleOver.value) return
            }
          }
        }
      }

      // 敌方攻击阶段（简化AI）
      if (board.value.length > 0) {
        // 攻击我方随机随从
        const i = Math.floor(Math.random() * board.value.length)
        const m = board.value[i]
        const dmg = enemyDifficulty.value === '普通' ? 2 : enemyDifficulty.value === '困难' ? 3 : 4
        m.health = Math.max(0, m.health - dmg)
        log(`敌方攻击我方随从 ${m.name}，造成 ${dmg} 伤害（剩余HP ${m.health}）`)
        if (m.health <= 0) {
          board.value.splice(i, 1)
          log(`我方随从 ${m.name} 倒下。`)
        }
      } else {
        // 无随从则对我方英雄造成伤害
        const dmg = enemyDifficulty.value === '普通' ? 3 : enemyDifficulty.value === '困难' ? 4 : 5
        heroHP.value = Math.max(0, heroHP.value - dmg)
        log(`敌方攻击我方英雄，造成 ${dmg} 伤害（我方HP ${heroHP.value}）`)
        checkVictory()
        if (battleOver.value) return
      }
      // 回到玩家回合
      startPlayerTurn()
    }, 500)
  }

  // 初始化
  if (deck.value.length === 0) reset()

  return {
    // state
    heroHP, enemyHP, mana, manaMax, deck, hand, board, enemyBoard, turn, canPlay, logs, enemyDifficulty, battleOver, winner,
    // actions
    reset, draw, startPlayerTurn, playCard, endTurn, log, configureEncounter, loadUserDeckFromDB, loadEnemyDeck,

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