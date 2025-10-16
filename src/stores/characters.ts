import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { supabase, getSession, tryAnonymousSignIn } from '@/lib/supabase'

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

type DbCharacter = {
  id: string
  user_id: string
  name: string
  class: string
  level: number
  hp: number
  mp: number
  stars: number
  stress: number
  attrs: Record<string, any> | null
  status: string
  created_at: string
}

function fromDb(row: DbCharacter): Character {
  return {
    id: row.id,
    name: row.name,
    cls: (row.class as Character['cls']) ?? '战士',
    stars: Number(row.stars ?? 1),
    hp: Number(row.hp ?? 60),
    mp: Number(row.mp ?? 30),
  }
}

function toDbPayload(c: Character) {
  return {
    name: c.name,
    class: c.cls,
    level: Math.max(1, Number(c.stars ?? 1)), // 简化：用星级映射等级
    hp: c.hp,
    mp: c.mp,
    stars: c.stars,
    stress: 0,
    attrs: { mp: c.mp, stars: c.stars }, // 保留冗余，兼容旧数据
    status: 'idle'
  }
}

export const useCharactersStore = defineStore('characters', () => {
  const list = ref<Character[]>([
    // 本地占位，若云端有数据会被覆盖
    { id: uid(), name: '阿黎', cls: '战士', stars: 3, hp: 80, mp: 20 },
    { id: uid(), name: '青梧', cls: '法师', stars: 2, hp: 50, mp: 60 }
  ])
  const selectedId = ref<string | null>(list.value[0]?.id ?? null)
  const hasSession = ref<boolean>(false)
  const loading = ref<boolean>(false)

  const selected = computed(() => list.value.find(c => c.id === selectedId.value) || null)

  async function ensureSession() {
    const session = await getSession()
    if (session) {
      hasSession.value = true
      return session
    }
    const anon = await tryAnonymousSignIn()
    hasSession.value = !!anon
    return anon
  }

  async function load() {
    loading.value = true
    try {
      const session = await ensureSession()
      if (!session) {
        // 无会话，保持本地模式
        return
      }
      const { data, error } = await supabase
        .from('characters')
        .select('*')
        .order('created_at', { ascending: true })
      if (error) {
        console.warn('Load characters error:', error.message)
        return
      }
      const rows = (data ?? []) as DbCharacter[]
      if (rows.length === 0) {
        // 云端无数据，创建一个默认角色
        const def: Character = {
          id: uid(),
          name: '新角色1',
          cls: '战士',
          stars: 1,
          hp: 60,
          mp: 30
        }
        const { error: insErr } = await supabase.from('characters').insert(toDbPayload(def)).select('id').single()
        if (insErr) {
          console.warn('Create default character failed:', insErr.message)
          return
        }
        // 重新拉取
        const { data: d2 } = await supabase.from('characters').select('*').order('created_at', { ascending: true })
        const mapped = (d2 ?? []).map(fromDb)
        list.value = mapped
        selectedId.value = mapped[0]?.id ?? null
        return
      }
      const mapped = rows.map(fromDb)
      list.value = mapped
      selectedId.value = mapped[0]?.id ?? null
    } finally {
      loading.value = false
    }
  }

  function select(id: string) {
    selectedId.value = id
  }

  async function create(payload?: Partial<Character>) {
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

    if (hasSession.value) {
      const { error } = await supabase.from('characters').insert(toDbPayload(c))
      if (error) console.warn('Create character sync failed:', error.message)
      else await load()
    }
  }

  async function remove(id: string) {
    const idx = list.value.findIndex(c => c.id === id)
    if (idx >= 0) {
      const removed = list.value.splice(idx, 1)[0]
      if (selectedId.value === id) selectedId.value = list.value[0]?.id ?? null

      if (hasSession.value) {
        // 通过 name+class+attrs.mp 简单定位（由于本地 id 与云端 id 不同）；更严谨做法是保存云端 id 映射
        const { error } = await supabase
          .from('characters')
          .delete()
          .match({ name: removed.name, class: removed.cls })
        if (error) console.warn('Delete character sync failed:', error.message)
        else await load()
      }
    }
  }

  async function starUp(id: string) {
    const c = list.value.find(x => x.id === id)
    if (!c) return
    if (c.stars >= 5) return
    c.stars += 1
    c.hp += 10
    c.mp += 5

    if (hasSession.value) {
      const { error } = await supabase
        .from('characters')
        .update(toDbPayload(c))
        .match({ name: c.name, class: c.cls })
      if (error) console.warn('Star up sync failed:', error.message)
      else await load()
    }
  }

  // 初始化时尝试加载
  load().catch(() => {})

  return { list, selectedId, selected, loading, hasSession, select, create, remove, starUp, load }
})