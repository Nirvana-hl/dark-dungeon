<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useGameStore } from '@/stores/game'

const game = useGameStore()
const { heroHP, enemyHP, board, enemyBoard, turn, logs, mana, manaMax } = storeToRefs(game)

// 简易图标映射（如未引入字体图标，将显示 emoji）
function iconFor(name: string, side: 'enemy' | 'ally') {
  const lower = name.toLowerCase()
  if (side === 'enemy') {
    if (lower.includes('守门者')) return { emoji: '🛡️', color: 'text-red-400' }
    if (lower.includes('龙')) return { emoji: '🐉', color: 'text-red-400' }
    if (lower.includes('猎手') || lower.includes('刺客')) return { emoji: '🗡️', color: 'text-purple-400' }
    return { emoji: '👹', color: 'text-rose-400' }
  } else {
    if (lower.includes('骑士')) return { emoji: '🛡️', color: 'text-yellow-400' }
    if (lower.includes('弓') || lower.includes('游侠')) return { emoji: '🏹', color: 'text-blue-400' }
    if (lower.includes('祭司') || lower.includes('法师')) return { emoji: '✨', color: 'text-indigo-400' }
    return { emoji: '⚔️', color: 'text-emerald-400' }
  }
}
</script>

<template>
  <main class="battle-area flex-1 py-6 px-4 grid grid-cols-1 lg:grid-cols-3 gap-6">
    <!-- 左侧（占两列）：上下对战布局 -->
    <div class="lg:col-span-2 space-y-6">
      <!-- 敌方（上） -->
      <div class="card rounded-2xl p-4 border border-slate-700">
        <div class="flex justify-between items-center mb-2">
          <div class="font-semibold">敌方</div>
          <div class="text-sm text-red-400">HP {{ enemyHP }}</div>
        </div>
        <div class="text-xs text-slate-400 mb-2">当前回合：{{ turn === 'player' ? '我方' : '敌方' }}</div>

        <div class="flex flex-wrap gap-3 justify-center">
          <div v-for="e in enemyBoard" :key="e.id" class="card w-28 h-36 rounded-xl p-2 border border-slate-700 flex flex-col items-center">
            <div class="font-semibold text-xs text-center line-clamp-1">{{ e.name }}</div>
            <div class="flex-1 flex items-center justify-center">
              <span :class="['text-2xl', iconFor(e.name, 'enemy').color]">{{ iconFor(e.name, 'enemy').emoji }}</span>
            </div>
            <div class="flex justify-between w-full text-[11px]">
              <div class="text-red-400">ATK {{ e.attack }}</div>
              <div class="text-emerald-400">HP {{ e.health }}</div>
            </div>
          </div>
          <div v-if="enemyBoard.length===0" class="text-xs text-slate-500">敌方角色已被击败，可直接对敌方造成伤害</div>
        </div>
      </div>

      <!-- 分隔线 -->
      <div class="flex justify-center">
        <div class="h-px bg-gray-600 w-3/4"></div>
      </div>

      <!-- 我方（下） -->
      <div class="card rounded-2xl p-4 border border-slate-700">
        <div class="flex justify-between items-center mb-2">
          <div class="font-semibold flex items-center gap-2">
            冒险者
            <!-- 冒险者生命值徽标：实时更新 -->
            <span class="inline-flex items-center gap-1 px-2 py-0.5 text-xs rounded bg-slate-800 border border-slate-600">
              <span class="text-emerald-400">HP</span>
              <span class="font-semibold text-emerald-300">{{ heroHP }}</span>
            </span>
          </div>
          <div class="text-xs text-slate-400">法力：{{ mana }}/{{ manaMax }}</div>
        </div>

        <div class="flex flex-wrap gap-3 justify-center">
          <div v-for="m in board" :key="m.id" class="card w-28 h-36 rounded-xl p-2 border border-slate-700 flex flex-col items-center">
            <div class="font-semibold text-xs text-center line-clamp-1">{{ m.name }}</div>
            <div class="flex-1 flex items-center justify-center">
              <span :class="['text-2xl', iconFor(m.name, 'ally').color]">{{ iconFor(m.name, 'ally').emoji }}</span>
            </div>
            <div class="flex justify-between w-full text-[11px]">
              <div class="text-blue-400">ATK {{ m.attack }}</div>
              <div class="text-emerald-400">HP {{ m.health }}</div>
            </div>
          </div>
          <div v-if="board.length===0" class="text-xs text-slate-500">暂无随从，打出手牌召唤或使用法术/装备</div>
        </div>
      </div>
    </div>

    <!-- 右侧：消息日志（固定高度，可滚动查看） -->
    <div class="card rounded-2xl p-4 border border-slate-700 flex flex-col">
      <div class="font-semibold mb-2">消息日志</div>
      <div class="flex-1 rounded-md bg-slate-900/60 border border-slate-700/60 p-2 text-xs space-y-1 overflow-y-auto max-h-[480px]">
        <div v-for="(l, i) in logs" :key="i" class="text-slate-300">{{ l }}</div>
        <div v-if="logs.length===0" class="text-slate-500">尚无消息</div>
      </div>
      <div class="text-[11px] text-slate-500 mt-2">提示：日志不会自动扩展高度，滚动查看历史记录。</div>
    </div>
  </main>
</template>