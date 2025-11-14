<script setup lang="ts">
import { ref, computed } from 'vue'

type Skill = {
  id: string
  name: string
  tier: number
  unlocked: boolean
  requires?: string[] // 依赖的前置技能
}

const skills = ref<Skill[]>([
  { id: 's1', name: '基础斩击', tier: 1, unlocked: true },
  { id: 's2', name: '格挡训练', tier: 1, unlocked: false },
  { id: 's3', name: '迅捷步伐', tier: 1, unlocked: false },

  { id: 's4', name: '猛击', tier: 2, unlocked: false, requires: ['s1'] },
  { id: 's5', name: '反击', tier: 2, unlocked: false, requires: ['s2'] },
  { id: 's6', name: '急速', tier: 2, unlocked: false, requires: ['s3'] },

  { id: 's7', name: '剑术大师', tier: 3, unlocked: false, requires: ['s4'] },
  { id: 's8', name: '钢铁意志', tier: 3, unlocked: false, requires: ['s5'] },
  { id: 's9', name: '风驰电掣', tier: 3, unlocked: false, requires: ['s6'] }
])

const tiers = computed(() => {
  const group: Record<number, Skill[]> = {}
  for (const s of skills.value) {
    (group[s.tier] ||= []).push(s)
  }
  return Object.entries(group)
    .sort((a, b) => Number(a[0]) - Number(b[0]))
    .map(([t, list]) => ({ tier: Number(t), list }))
})

function canUnlock(s: Skill) {
  if (!s.requires || s.requires.length === 0) return true
  const byId = new Map(skills.value.map(x => [x.id, x]))
  return s.requires.every(id => byId.get(id)?.unlocked)
}

function toggleSkill(s: Skill) {
  if (!s.unlocked && !canUnlock(s)) return
  s.unlocked = !s.unlocked
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <div class="status-bar py-3 px-6 flex items-center justify-between">
      <div class="font-semibold">技能树</div>
      <RouterLink to="/" class="action-button px-3 py-2 rounded-button">&larr; 返回战斗</RouterLink>
    </div>

    <div class="flex-1 p-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div v-for="t in tiers" :key="t.tier" class="card rounded-2xl p-4">
          <div class="font-semibold mb-3">第 {{ t.tier }} 层</div>
          <div class="space-y-3">
            <div v-for="s in t.list" :key="s.id"
                 class="p-3 rounded-lg border"
                 :class="s.unlocked ? 'border-green-500/40 bg-green-900/20' : canUnlock(s) ? 'border-yellow-500/40 bg-yellow-900/10' : 'border-slate-700 bg-slate-900/40'">
              <div class="flex items-center justify-between">
                <div>
                  <div class="font-medium">{{ s.name }}</div>
                  <div class="text-xs text-gray-400" v-if="s.requires?.length">前置：{{ s.requires.join('、') }}</div>
                </div>
                <button class="action-button rounded-button px-3 py-1"
                        :disabled="!s.unlocked && !canUnlock(s)"
                        @click="toggleSkill(s)">
                  {{ s.unlocked ? '重置' : (canUnlock(s) ? '解锁' : '锁定') }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>