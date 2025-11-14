import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { characterApi, type CharacterDTO } from '@/lib/api'
import { useAuthStore } from './auth'

export interface Character {
  id: string
  name: string
  cls: '战士' | '法师' | '游侠'
  stars: number // 星级 1~5
  hp: number
  mp: number
}

export const useCharactersStore = defineStore('characters', () => {
  const list = ref<Character[]>([])
  const selectedId = ref<string | null>(null)
  const hasSession = ref<boolean>(false)
  const loading = ref<boolean>(false)

  const selected = computed(() => list.value.find(c => c.id === selectedId.value) || null)

  // 检查是否有登录会话
  function checkSession() {
    const authStore = useAuthStore()
    hasSession.value = !!authStore.session
    return hasSession.value
  }

  // 将后端DTO转换为前端Character
  function fromDTO(dto: CharacterDTO): Character {
    return {
      id: dto.id?.toString() || '',
      name: dto.name,
      cls: dto.classType as Character['cls'],
      stars: dto.stars,
      hp: dto.hp,
      mp: dto.mp
    }
  }

  // 将前端Character转换为后端DTO
  function toDTO(c: Partial<Character>): Partial<CharacterDTO> {
    return {
      name: c.name,
      classType: c.cls,
      stars: c.stars,
      hp: c.hp,
      mp: c.mp
    }
  }

  async function load() {
    loading.value = true
    try {
      if (!checkSession()) {
        // 无会话，保持本地模式
        return
      }
      const response = await characterApi.getCharacters()
      if (response.code === 200 && response.data) {
        const mapped = response.data.map(fromDTO)
        list.value = mapped
        if (mapped.length > 0 && !selectedId.value) {
          selectedId.value = mapped[0].id
        }
      } else if (response.data && response.data.length === 0) {
        // 无数据，创建一个默认角色
        const def: Partial<Character> = {
          name: '新角色1',
          cls: '战士',
          stars: 1,
          hp: 60,
          mp: 30
        }
        await create(def)
      }
    } catch (error: any) {
      console.warn('Load characters error:', error.message)
    } finally {
      loading.value = false
    }
  }

  function select(id: string) {
    selectedId.value = id
  }

  async function create(payload?: Partial<Character>) {
    const c: Partial<Character> = {
      name: payload?.name ?? `新角色${list.value.length + 1}`,
      cls: payload?.cls ?? '战士',
      stars: payload?.stars ?? 1,
      hp: payload?.hp ?? 60,
      mp: payload?.mp ?? 30
    }

    if (checkSession()) {
      try {
        const response = await characterApi.createCharacter(toDTO(c) as CharacterDTO)
        if (response.code === 200 && response.data) {
          const newChar = fromDTO(response.data)
          list.value.push(newChar)
          selectedId.value = newChar.id
        }
      } catch (error: any) {
        console.warn('Create character failed:', error.message)
      }
    } else {
      // 本地模式
      const newChar: Character = {
        id: Math.random().toString(36).slice(2, 10),
        name: c.name || `新角色${list.value.length + 1}`,
        cls: c.cls || '战士',
        stars: c.stars || 1,
        hp: c.hp || 60,
        mp: c.mp || 30
      }
      list.value.push(newChar)
      selectedId.value = newChar.id
    }
  }

  async function remove(id: string) {
    const idx = list.value.findIndex(c => c.id === id)
    if (idx >= 0) {
      const removed = list.value[idx]
      if (selectedId.value === id) {
        selectedId.value = list.value[0]?.id ?? null
      }

      if (checkSession() && removed.id) {
        try {
          const numId = Number(removed.id)
          if (!isNaN(numId)) {
            await characterApi.deleteCharacter(numId)
            list.value.splice(idx, 1)
          }
        } catch (error: any) {
          console.warn('Delete character failed:', error.message)
        }
      } else {
        list.value.splice(idx, 1)
      }
    }
  }

  async function starUp(id: string) {
    const c = list.value.find(x => x.id === id)
    if (!c) return
    if (c.stars >= 5) return

    if (checkSession() && c.id) {
      try {
        const numId = Number(c.id)
        if (!isNaN(numId)) {
          const response = await characterApi.starUp(numId)
          if (response.code === 200 && response.data) {
            const updated = fromDTO(response.data)
            const idx = list.value.findIndex(x => x.id === id)
            if (idx >= 0) {
              list.value[idx] = updated
            }
          }
        }
      } catch (error: any) {
        console.warn('Star up failed:', error.message)
      }
    } else {
      // 本地模式
      c.stars += 1
      c.hp += 10
      c.mp += 5
    }
  }

  // 初始化时尝试加载
  load().catch(() => {})

  return { list, selectedId, selected, loading, hasSession, select, create, remove, starUp, load }
})