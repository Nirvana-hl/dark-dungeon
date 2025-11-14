<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/game'
import CardItem from '@/components/CardItem.vue'

const game = useGameStore()
const { heroHP, enemyHP, mana, manaMax, hand, board, turn, canPlay } = storeToRefs(game)

function onPlay(id: string) {
  game.playCard(id)
}
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- 顶部：敌方信息 -->
    <div class="status-bar px-6 py-3 flex justify-between items-center">
      <div class="font-semibold">敌方英雄生命：<span class="text-red-400">{{ enemyHP }}</span></div>
      <div>当前回合：<span class="font-bold">{{ turn === 'player' ? '你的回合' : '对手回合' }}</span></div>
    </div>

    <!-- 战场 -->
    <div class="flex-1 p-6 grid grid-rows-2 gap-6">
      <!-- 敌方战场（占位） -->
      <div class="card rounded-2xl p-4 flex items-center justify-center text-gray-400">敌方战场（占位）</div>

      <!-- 我方战场 -->
      <div class="card rounded-2xl p-4">
        <div class="font-semibold mb-3">你的战场</div>
        <div class="flex flex-wrap gap-3">
          <div v-if="board.length === 0" class="text-sm text-gray-400">无随从</div>
          <div v-for="m in board" :key="m.id" class="rounded-xl px-4 py-3 border border-slate-700 bg-slate-800">
            <div class="font-semibold">{{ m.name }}</div>
            <div class="text-xs mt-1">ATK {{ m.attack }} · HP {{ m.health }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 英雄与法力 -->
    <div class="status-bar px-6 py-3 flex items-center justify-between">
      <div>英雄生命：<span class="text-red-400 font-semibold">{{ heroHP }}</span></div>
      <div class="flex items-center gap-2">
        <span>法力：</span>
        <div>
          <span v-for="i in manaMax" :key="i" class="mana-crystal" :class="{ empty: i > mana }"></span>
        </div>
      </div>
      <div class="flex gap-2">
        <button class="action-button rounded-button px-3 py-2" :disabled="!canPlay" @click="game.startPlayerTurn()">开始回合</button>
        <button class="action-button rounded-button px-3 py-2" :disabled="!canPlay" @click="game.draw(1)">抽1张</button>
        <button class="action-button rounded-button px-3 py-2" :disabled="!canPlay" @click="game.endTurn()">结束回合</button>
        <button class="action-button rounded-button px-3 py-2" @click="game.reset()">洗牌重置</button>
      </div>
    </div>

    <!-- 手牌区 -->
    <div class="px-6 py-4">
      <div class="font-semibold mb-2">手牌（点击打出）</div>
      <div class="flex gap-3 overflow-x-auto pb-2">
        <CardItem v-for="c in hand" :key="c.id" :card="c" @play="onPlay" />
      </div>
    </div>
  </div>
</template>