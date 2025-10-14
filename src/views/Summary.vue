<script setup lang="ts">
import { useRunStore } from '@/stores/run'
import AIChat from '@/components/AIChat.vue'
const run = useRunStore()

function share() {
  alert('已生成分享图并复制链接（示例）')
}
</script>

<template>
  <div class="min-h-screen p-6">
    <div class="text-2xl font-bold mb-4">战斗结算与通关报告</div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <div class="lg:col-span-2 space-y-4">
        <div class="card rounded-2xl p-4">
          <div class="font-semibold mb-2">奖励详情</div>
          <div class="text-sm">金币：{{ run.reward.gold }}，经验：{{ run.reward.exp }}</div>
          <div class="text-sm">战利品：<span v-for="(it,i) in run.reward.items" :key="i" class="mr-2">{{ it }}</span></div>
        </div>

        <div class="card rounded-2xl p-4">
          <div class="font-semibold mb-2">数据统计</div>
          <div class="text-sm">击败怪物数：{{ run.monstersDefeated }}</div>
          <div class="text-sm">探索时长：{{ run.durationMin }} 分钟</div>
          <div class="text-sm">造成伤害：{{ run.damageDone }}</div>
          <div class="text-sm">承受伤害：{{ run.damageTaken }}</div>
        </div>
      </div>

      <div class="space-y-4">
        <div class="card rounded-2xl p-4">
          <div class="font-semibold mb-2">AI 分析报告</div>
          <p class="text-sm text-gray-300">
            - 关键决策：在低血时选择绕行并利用地形规避精英，减少了不必要的消耗。
            <br />- 潜在优化：Boss 阶段缺少控制，建议下次携带定身或击退类技能。
            <br />- 成长建议：提升生存曲线，优先升级防具与治疗药水配比。
          </p>
        </div>

        <div class="card rounded-2xl p-4">
          <div class="font-semibold mb-2">分享</div>
          <button class="action-button rounded-button px-4 py-2" @click="share">一键分享高光</button>
        </div>
      </div>
    </div>

    <AIChat />
  </div>
</template>