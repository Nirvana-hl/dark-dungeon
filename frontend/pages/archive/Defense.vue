<script setup lang="ts">
import { useBattleStore } from '@/stores/battle'
const battle = useBattleStore()
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <div class="p-4"><RouterLink to="/" class="text-blue-400">&larr; 返回战斗</RouterLink></div>
    <div class="flex-1 flex items-center justify-center">
      <div class="card p-8 rounded-2xl space-y-4">
        <div class="text-2xl font-bold mb-2">防御</div>
        <div class="text-gray-300">演示：点击恢复生命或获取法力，顶部状态栏将实时变化。</div>

        <div class="flex gap-3">
          <button class="action-button rounded-button px-6 py-3" @click="battle.heal(5)">
            恢复 5 点生命
          </button>
          <button class="action-button rounded-button px-6 py-3" @click="battle.gainMana(1)">
            获得 1 水晶
          </button>
        </div>
      </div>
    </div>
  </div>
</template>