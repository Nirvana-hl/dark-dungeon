import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface Character {
  id: string
  name: string
  cls: '战士' | '法师' | '游侠'
  stars: number // 星级 1~5
  hp: number
  mp: number
}

function uid() {
  return Math.random().toString(36).slice(2, 10)
}

export const useCharactersStore = defineStore('characters', () => {
  const list = ref<Character[]>([
    { id: uid(), name: '阿黎', cls: '战士', stars: 3, hp: 80, mp: 20 },
    { id: uid(), name: '青梧', cls: '法师', stars: 2, hp: 50, mp: 60 }
  ])
  const selectedId = ref<string | null>(list.value[0]?.id ?? null)

  const selected = computed(() => list.value.find(c => c.id === selectedId.value) || null)

  function select(id: string) {
    selectedId.value = id
  }

  function create(payload?: Partial<Character>) {
    const c: Character = {
      id: uid(),
      name: payload?.name ?? `新角色${list.value.length + 1}`,
      cls: payload?.cls ?? '战士',
      stars: payload?.stars ?? 1,
      hp: payload?.hp ?? 60,
      mp: payload?.mp ?? 30
    }
    list.value.push(c)
    selectedId.value = c.id
  }

  function remove(id: string) {
    const idx = list.value.findIndex(c => c.id === id)
    if (idx >= 0) {
      list.value.splice(idx, 1)
      if (selectedId.value === id) selectedId.value = list.value[0]?.id ?? null
    }
  }

  function starUp(id: string) {
    const c = list.value.find(x => x.id === id)
    if (!c) return
    if (c.stars >= 5) return
    c.stars += 1
    // 升星带来属性提升（示例：每星+10hp/+5mp）
    c.hp += 10
    c.mp += 5
  }

  return { list, selectedId, selected, select, create, remove, starUp }
})