import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 战斗核心状态（演示版）
 * - health: 生命值（0~100）
 * - mana: 法力上限 10，current 当前水晶数量
 * - turn: 当前是否为“你的回合”
 */
export const useBattleStore = defineStore('battle', () => {
  const health = ref(75)
  const healthMax = 100

  const manaMax = 10
  const manaCurrent = ref(4)

  const yourTurn = ref(true)

  const healthPct = computed(() => Math.max(0, Math.min(100, Math.round((health.value / healthMax) * 100))))

  function damage(val: number) {
    health.value = Math.max(0, health.value - val)
  }
  function heal(val: number) {
    health.value = Math.min(healthMax, health.value + val)
  }
  function spendMana(val: number) {
    manaCurrent.value = Math.max(0, manaCurrent.value - val)
  }
  function gainMana(val: number) {
    manaCurrent.value = Math.min(manaMax, manaCurrent.value + val)
  }
  function endTurn() {
    yourTurn.value = !yourTurn.value
  }

  return {
    // state
    health, healthMax, healthPct,
    manaCurrent, manaMax,
    yourTurn,
    // actions
    damage, heal, spendMana, gainMana, endTurn
  }
})