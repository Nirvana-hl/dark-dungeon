<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { useRunStore } from '@/stores/run'
import AIChat from '@/components/AIChat.vue'

// uni-app 类型声明
declare const uni: {
  showToast: (options: { title: string; icon?: string; duration?: number }) => void
}

const run = useRunStore()

// 页面加载时初始化
onLoad(() => {
  // 可以在这里加载结算数据
})

function share() {
  uni.showToast({
    title: '已生成分享图并复制链接',
    icon: 'success',
    duration: 2000
  })
}
</script>

<template>
  <view class="summary-container">
    <text class="summary-title">战斗结算与通关报告</text>

    <view class="summary-grid">
      <view class="main-content">
        <view class="summary-card">
          <text class="card-title">奖励详情</text>
          <view class="card-content">
            <text class="info-text">金币：{{ run.reward.gold }}，经验：{{ run.reward.exp }}</text>
            <text class="info-text">战利品：</text>
            <text v-for="(it, i) in run.reward.items" :key="i" class="item-tag">{{ it }}</text>
          </view>
        </view>

        <view class="summary-card">
          <text class="card-title">数据统计</text>
          <view class="card-content">
            <text class="info-text">击败怪物数：{{ run.monstersDefeated }}</text>
            <text class="info-text">探索时长：{{ run.durationMin }} 分钟</text>
            <text class="info-text">造成伤害：{{ run.damageDone }}</text>
            <text class="info-text">承受伤害：{{ run.damageTaken }}</text>
          </view>
        </view>
      </view>

      <view class="sidebar-content">
        <view class="summary-card">
          <text class="card-title">AI 分析报告</text>
          <view class="card-content">
            <text class="analysis-text">
            - 关键决策：在低血时选择绕行并利用地形规避精英，减少了不必要的消耗。
            </text>
            <text class="analysis-text">
              - 潜在优化：Boss 阶段缺少控制，建议下次携带定身或击退类技能。
            </text>
            <text class="analysis-text">
              - 成长建议：提升生存曲线，优先升级防具与治疗药水配比。
            </text>
          </view>
        </view>

        <view class="summary-card">
          <text class="card-title">分享</text>
          <view class="card-content">
            <button class="action-button" @click="share">一键分享高光</button>
          </view>
        </view>
      </view>
    </view>

    <AIChat />
  </view>
</template>

<style scoped>
.summary-container {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(to bottom, #0f172a, #020617);
  color: white;
}

.summary-title {
  font-size: 2rem;
  font-weight: bold;
  display: block;
  margin-bottom: 24px;
  color: white;
}

.summary-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.main-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.sidebar-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.summary-card {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 16px;
  border: 1px solid rgba(51, 65, 85, 0.5);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
}

.card-title {
  font-weight: 600;
  font-size: 1.1rem;
  display: block;
  margin-bottom: 12px;
  color: white;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-text {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.8);
  display: block;
}

.item-tag {
  display: inline-block;
  margin-right: 8px;
  padding: 4px 8px;
  background: rgba(59, 130, 246, 0.2);
  border-radius: 4px;
  font-size: 0.875rem;
  color: #60a5fa;
}

.analysis-text {
  font-size: 0.875rem;
  color: rgba(255, 255, 255, 0.7);
  display: block;
  margin-bottom: 8px;
  line-height: 1.5;
}

.action-button {
  padding: 8px 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: opacity 0.3s;
}

.action-button:active {
  opacity: 0.8;
}

/* 响应式设计 */
@media (min-width: 1024px) {
  .summary-grid {
    grid-template-columns: 2fr 1fr;
  }
}
</style>
