import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

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
}

function uid() {
  return Math.random().toString(36).slice(2, 10)
}

function baseDeck(): Card[] {
  return [
    { id: uid(), name: '新兵', cost: 1, type: 'character', attack: 1, health: 1 },
    { id: uid(), name: '盾卫', cost: 2, type: 'character', attack: 2, health: 3 },
    { id: uid(), name: '弓手', cost: 2, type: 'character', attack: 3, health: 1 },
    { id: uid(), name: '火球术', cost: 2, type: 'spell', effect: 'fireball3' },
    { id: uid(), name: '火球术', cost: 2, type: 'spell', effect: 'fireball3' },
    { id: uid(), name: '战旗', cost: 3, type: 'equipment', effect: 'teamBuffAtk1' },
    { id: uid(), name: '骑士', cost: 3, type: 'character', attack: 3, health: 3 },
    { id: uid(), name: '狂战士', cost: 4, type: 'character', attack: 4, health: 2 },
    { id: uid(), name: '祭司', cost: 3, type: 'character', attack: 2, health: 4 },
    { id: uid(), name: '魔像', cost: 5, type: 'character', attack: 5, health: 5 }
  ]
}

export const useGameStore = defineStore('game', () => {
  // 英雄生命与法力
  const heroHP = ref(20)
  const enemyHP = ref(20)
  const manaMax = ref(1)
  const mana = ref(1)

  // 手牌/牌库/战场
  const deck = ref<Card[]>([])
  const hand = ref<Card[]>([])
  const board = ref<Minion[]>([])

  // 回合：'player' | 'opponent'
  const turn = ref<'player' | 'opponent'>('player')
  const canPlay = computed(() => turn.value === 'player')

  function shuffle<T>(arr: T[]) {
    for (let i = arr.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[arr[i], arr[j]] = [arr[j], arr[i]]
    }
  }

  function reset() {
    heroHP.value = 20
    enemyHP.value = 20
    manaMax.value = 1
    mana.value = 1
    turn.value = 'player'
    deck.value = baseDeck()
    hand.value = []
    board.value = []
    shuffle(deck.value)
    // 起手抽3
    draw(3)
  }

  function draw(n = 1) {
    for (let i = 0; i < n; i++) {
      const c = deck.value.shift()
      if (c) {
        if (hand.value.length < 10) hand.value.push(c)
        else {
          // 手牌满，烧牌（丢弃）
        }
      } else {
        // 疲劳伤害（简化版）：抽空牌库则自伤1
        heroHP.value = Math.max(0, heroHP.value - 1)
      }
    }
  }

  function startPlayerTurn() {
    turn.value = 'player'
    manaMax.value = Math.min(10, manaMax.value + 1) // 简化：每回合+1上限
    mana.value = manaMax.value
    draw(1)
  }

  function playCard(id: string) {
    if (turn.value !== 'player') return
    const idx = hand.value.findIndex(c => c.id === id)
    if (idx < 0) return
    const card = hand.value[idx]
    if (card.cost > mana.value) return
    // 扣蓝
    mana.value -= card.cost
    // 处理类型
    if (card.type === 'character') {
      if (board.value.length >= 7) return // 简化随从上限7
      board.value.push({
        id: card.id,
        name: card.name,
        attack: card.attack ?? 0,
        health: card.health ?? 1
      })
      hand.value.splice(idx, 1)
    } else if (card.type === 'spell') {
      if (card.effect === 'fireball3') {
        enemyHP.value = Math.max(0, enemyHP.value - 3)
      }
      hand.value.splice(idx, 1)
    } else if (card.type === 'equipment') {
      if (card.effect === 'teamBuffAtk1') {
        board.value = ref(board.value.map(m => ({ ...m, attack: m.attack + 1 }))).value
      }
      hand.value.splice(idx, 1)
    }
  }

  function endTurn() {
    if (turn.value !== 'player') return
    turn.value = 'opponent'
    // 非常简化的AI：造成1点伤害
    setTimeout(() => {
      heroHP.value = Math.max(0, heroHP.value - 1)
      // 回到玩家回合
      startPlayerTurn()
    }, 400)
  }

  // 初始化
  if (deck.value.length === 0) reset()

  return {
    // state
    heroHP, enemyHP, mana, manaMax, deck, hand, board, turn, canPlay,
    // actions
    reset, draw, startPlayerTurn, playCard, endTurn
  }
})