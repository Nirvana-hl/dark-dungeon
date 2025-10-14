<script setup lang="ts">
import StatusBar from '@/components/StatusBar.vue'
import BattleField from '@/components/BattleField.vue'
import CardItem from '@/components/CardItem.vue'
import { useGameStore } from '@/stores/game'
import { storeToRefs } from 'pinia'

const game = useGameStore()
const { hand, canPlay } = storeToRefs(game)

function onPlay(id: string) {
  game.playCard(id)
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <StatusBar />
    <BattleField />

    <!-- 底部：手牌区域 + 仅保留“结束回合”按钮 -->
    <div class="px-6 py-4 border-t border-slate-700 bg-slate-900/60">
      <div class="flex items-center justify-between mb-2">
        <div class="font-semibold">手牌（点击打出）</div>
        <button class="action-button rounded-button px-3 py-2" :disabled="!canPlay" @click="game.endTurn()">结束回合</button>
      </div>
      <div class="flex gap-3 overflow-x-auto pb-2">
        <CardItem v-for="c in hand" :key="c.id" :card="c" @play="onPlay" />
      </div>
    </div>
  </div>
</template>