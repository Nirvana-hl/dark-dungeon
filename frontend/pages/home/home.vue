<template>
  <view class="home-container">
    <!-- 顶部玩家信息 -->
    <view class="player-header">
      <view class="player-left">
        <view class="avatar-wrapper">
          <image class="avatar" src="/static/tabbar/touxiang.jpg" mode="aspectFill" />
        </view>
        <view class="player-info">
          <view class="player-row">
            <text class="player-name">冒险者</text>
            <text class="player-level">Lv 1</text>
          </view>
          <view class="player-row">
            <text class="label">体力</text>
            <view class="bar-bg">
              <view class="bar-fill hp"></view>
            </view>
          </view>
        </view>
      </view>
      <view class="player-right">
        <view class="currency-box">
          <text class="currency-icon">💰</text>
          <text class="currency-value">1250</text>
        </view>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </view>
    </view>

    <!-- 中间“开始冒险”大按钮 -->
    <view class="main-action">
      <button class="start-btn" @click="handleStartAdventure">
        <text class="start-title">开始冒险</text>
        <text class="start-sub">前往营地，整装待发</text>
      </button>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-section">
      <view class="section-title-row">
        <text class="section-title">快捷入口</text>
      </view>
      <view class="quick-grid">
        <view class="quick-item" @click="goCamp">
          <view class="quick-icon">
            <i class="fas fa-campground"></i>
          </view>
          <text class="quick-label">营地</text>
        </view>
        <view class="quick-item" @click="goSkills">
          <view class="quick-icon">
            <i class="fas fa-sitemap"></i>
          </view>
          <text class="quick-label">技能树</text>
        </view>
        <view class="quick-item" @click="goExplore">
          <view class="quick-icon">
            <i class="fas fa-dungeon"></i>
          </view>
          <text class="quick-label">闯关</text>
        </view>
        <view class="quick-item" @click="goShop">
          <view class="quick-icon">
            <i class="fas fa-store"></i>
          </view>
          <text class="quick-label">商城</text>
        </view>
        <view class="quick-item" @click="goAchievements">
          <view class="quick-icon">
            <i class="fas fa-trophy"></i>
          </view>
          <text class="quick-label">成就</text>
        </view>
        <view class="quick-item" @click="goSettings">
          <view class="quick-icon">
            <i class="fas fa-cog"></i>
          </view>
          <text class="quick-label">设置</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

declare const uni: {
  navigateTo: (options: { url: string }) => void
  switchTab: (options: { url: string }) => void
  reLaunch: (options: { url: string }) => void
}

const auth = useAuthStore()

// 主按钮：开始冒险 → 进入营地
function handleStartAdventure() {
  uni.switchTab({ url: '/pages/camp/camp' })
}

// 快捷入口
function goCamp() {
  uni.switchTab({ url: '/pages/camp/camp' })
}

function goSkills() {
  uni.navigateTo({ url: '/pages/skills/skills' })
}

function goExplore() {
  uni.navigateTo({ url: '/pages/explore/explore' })
}

function goShop() {
  uni.navigateTo({ url: '/pages/shop/shop' })
}

function goAchievements() {
  uni.navigateTo({ url: '/pages/achievements/achievements' })
}

function goSettings() {
  uni.navigateTo({ url: '/pages/settings/settings' })
}

// 退出登录
async function handleLogout() {
  try {
    await auth.logout()
  } finally {
    uni.reLaunch({ url: '/pages/login/login' })
  }
}
</script>

<style scoped>
.home-container {
  width: 100%;
  height: 100vh;
  background: #020617;
  padding: 24rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.player-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 20rpx;
  border-radius: 20rpx;
  background: #0f172a;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.4);
}

.player-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.avatar-wrapper {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid #f97316;
}

.avatar {
  width: 100%;
  height: 100%;
}

.player-info {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.player-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.player-name {
  font-size: 28rpx;
  color: #e5e7eb;
  font-weight: 600;
}

.player-level {
  font-size: 22rpx;
  color: #facc15;
}

.label {
  font-size: 20rpx;
  color: #9ca3af;
}

.bar-bg {
  width: 200rpx;
  height: 10rpx;
  border-radius: 999rpx;
  background: #1f2937;
  overflow: hidden;
}

.bar-fill.hp {
  width: 60%;
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f97316);
}

.player-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8rpx;
}

.currency-box {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: 6rpx 10rpx;
  border-radius: 999rpx;
  background: #111827;
}

.currency-icon {
  font-size: 22rpx;
}

.currency-value {
  font-size: 22rpx;
  color: #facc15;
  font-weight: 600;
}

.logout-btn {
  padding: 4rpx 12rpx;
  font-size: 20rpx;
  border-radius: 999rpx;
  border: 1rpx solid #4b5563;
  background: transparent;
  color: #9ca3af;
}

.main-action {
  margin-top: 8rpx;
}

.start-btn {
  width: 100%;
  padding: 24rpx 0;
  border-radius: 24rpx;
  border: none;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(248, 113, 22, 0.5);
}

.start-title {
  font-size: 32rpx;
  font-weight: 700;
}

.start-sub {
  margin-top: 4rpx;
  font-size: 22rpx;
  opacity: 0.9;
}

.quick-section {
  flex: 1;
  padding-top: 8rpx;
}

.section-title-row {
  margin-bottom: 12rpx;
}

.section-title {
  font-size: 26rpx;
  color: #e5e7eb;
  font-weight: 600;
}

.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.quick-item {
  padding: 16rpx 8rpx;
  border-radius: 20rpx;
  background: #020617;
  border: 1rpx solid #1f2937;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}

.quick-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 20rpx;
  background: #0f172a;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #60a5fa;
  font-size: 32rpx;
}

.quick-label {
  font-size: 22rpx;
  color: #e5e7eb;
}
</style>
