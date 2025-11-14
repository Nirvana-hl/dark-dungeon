<script setup lang="ts">
import { useBattleStore } from '@/stores/battle'
const battle = useBattleStore()
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <div class="p-4"><RouterLink to="/" class="text-blue-400">&larr; 返回战斗</RouterLink></div>
    <div class="flex-1 flex items-center justify-center">
      <div class="card p-8 rounded-2xl space-y-4">
        <div class="text-2xl font-bold mb-2">攻击</div>
        <div class="text-gray-300">演示：点击造成伤害或结束回合，顶部状态栏生命/回合将实时变化。</div>

        <div class="flex gap-3">
          <button class="action-button rounded-button px-6 py-3" @click="battle.damage(5)">
            造成 5 点伤害
          </button>
          <button class="action-button rounded-button px-6 py-3" @click="battle.endTurn()">
            结束回合
          </button>
        </div>
      </div>
    </div>
  </div>
</template>