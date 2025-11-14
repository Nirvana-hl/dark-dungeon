import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface Reward {
  gold: number
  exp: number
  items: string[]
}

export const useRunStore = defineStore('run', () => {
  const monstersDefeated = ref(12)
  const durationMin = ref(18)
  const damageDone = ref(2350)
  const damageTaken = ref(980)
  const reward = ref<Reward>({ gold: 320, exp: 450, items: ['神秘碎片', '治疗药水'] })

  function reset() {
    monstersDefeated.value = 0
    durationMin.value = 0
    damageDone.value = 0
    damageTaken.value = 0
    reward.value = { gold: 0, exp: 0, items: [] }
  }

  return { monstersDefeated, durationMin, damageDone, damageTaken, reward, reset }
})