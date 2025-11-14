<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useBattleStore } from '@/stores/battle'

/**
 * 顶部状态栏
 * 使用 Pinia 读取生命、法力、回合信息
 */
const battle = useBattleStore()
const { healthPct, health, healthMax, manaCurrent, manaMax, yourTurn } = storeToRefs(battle)
</script>

<template>
  <header class="status-bar py-3 px-6 flex justify-between items-center">
    <div class="flex items-center space-x-4">
      <div class="w-12 h-12 rounded-full bg-gray-700 flex items-center justify-center">
        <i class="fas fa-crown text-yellow-400 text-xl"></i>
      </div>
      <div>
        <div class="font-bold">冒险者</div>
        <div class="text-sm text-gray-400">等级 5</div>
      </div>
    </div>

    <div class="flex-1 mx-8">
      <div class="flex items-center mb-1">
        <span class="text-sm w-16">生命值</span>
        <div class="flex-1 h-4 health-bar rounded-full overflow-hidden">
          <div class="health-fill" :style="{ width: healthPct + '%' }"></div>
        </div>
        <span class="text-sm ml-2">{{ health }}/{{ healthMax }}</span>
      </div>
      <div class="flex items-center mt-2">
        <span class="text-sm w-16">法力值</span>
        <div>
          <span v-for="i in manaMax" :key="i" class="mana-crystal" :class="{ empty: i > manaCurrent }"></span>
        </div>
      </div>
    </div>

    <div class="turn-indicator font-bold text-lg">
      <i class="fas fa-sync-alt mr-2"></i>{{ yourTurn ? '你的回合' : '对手回合' }}
    </div>
  </header>
</template>