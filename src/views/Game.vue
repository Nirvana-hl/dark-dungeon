<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import StatusBar from '@/components/StatusBar.vue'
import BattleField from '@/components/BattleField.vue'
import CardItem from '@/components/CardItem.vue'
import { useGameStore } from '@/stores/game'
import { storeToRefs } from 'pinia'
import { supabase } from '@/lib/supabase'

const route = useRoute()
const router = useRouter()

const level = computed(() => Number(route.query.level ?? 0))
const chapter = computed(() => (level.value ? Math.floor((level.value - 1) / 5) + 1 : 0))

const game = useGameStore()
const { hand, canPlay, winner } = storeToRefs(game)

function onPlay(id: string) {
  game.playCard(id)
}

// 根据关卡参数配置敌方难度并开局（从数据库加载玩家/敌方手牌）
onMounted(async () => {
  const lv = level.value || 1
  const diff = lv <= 10 ? '普通' : lv <= 20 ? '困难' : '噩梦'
  game.configureEncounter(diff as any)

  const { data: userData } = await supabase.auth.getUser()
  const uid = userData.user?.id
  if (uid) {
    await game.loadUserDeckFromDB(uid)
  }
  await game.loadEnemyDeck(lv)
  game.reset()
})

// 若关卡参数变化，重新配置并重新加载敌方手牌
watch(level, async (lv) => {
  const diff = lv && lv <= 10 ? '普通' : lv && lv <= 20 ? '困难' : '噩梦'
  game.configureEncounter(diff as any)
  await game.loadEnemyDeck(lv || 1)
  game.reset()
})

// 监听胜负，均回到闯关并显示奖励（失败也按需求标记通关并待领取奖励）
watch(winner, (w) => {
  const lv = String(route.query.level ?? '1')
  if (w === 'player' || w === 'enemy') {
    router.push({ path: '/explore', query: { victory: '1', level: lv } })
  }
})
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <StatusBar />
    <!-- 关卡提示 -->
    <div v-if="level" class="p-3 text-xs text-slate-400">
      当前关卡：第 {{ level }} 关 · 第 {{ chapter }} 章
    </div>

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