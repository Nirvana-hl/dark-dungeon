<script setup lang="ts">
import type { Card } from '@/stores/game'

defineProps<{
  card: Card
}>()

defineEmits<{
  (e: 'play', id: string): void
}>()
</script>

<template>
  <div class="card rounded-xl p-3 w-36 border border-slate-700 hover:border-indigo-500 transition cursor-pointer"
       @click="$emit('play', card.id)">
    <div class="flex justify-between items-center mb-2">
      <span class="text-xs px-2 py-0.5 rounded bg-slate-700 capitalize">{{ card.type }}</span>
      <span class="text-sm font-bold text-indigo-400">{{ card.cost }}</span>
    </div>
    <div class="font-semibold">{{ card.name }}</div>
    <div v-if="card.type==='character'" class="mt-2 text-xs text-gray-300">
      ATK {{ card.attack }} · HP {{ card.health }}
    </div>
    <div v-else-if="card.type==='spell'" class="mt-2 text-xs text-gray-300">
      法术：{{ card.effect === 'fireball3' ? '造成3点伤害' : '效果' }}
    </div>
    <div v-else class="mt-2 text-xs text-gray-300">
      装备：{{ card.effect === 'teamBuffAtk1' ? '友方随从+1攻击' : '效果' }}
    </div>
  </div>
</template>