<template>
  <view class="home-container">
    <!-- 背景图片 -->
    <image class="background-image" src="/static/background.png" mode="aspectFill"></image>
    
    <!-- 背景遮罩层 -->
    <view class="background-overlay"></view>
    
    <!-- 游戏标题 -->
    <view class="game-title">
      <text class="title-text">🎮 暗黑地牢肉鸽</text>
    </view>
    
    <!-- 底部中央开始挑战按钮 -->
    <view class="center-action">
      <button class="explore-button" @click="handleStartExplore">
        <view class="button-content">
          <view class="button-text-wrapper">
            <text class="button-text-line">开始</text>
            <text class="button-text-line">挑战</text>
          </view>
        </view>
      </button>
    </view>
  </view>
</template>

<style scoped>
.home-container {
  width: 100%;
  height: 100vh;
  position: relative;
  overflow: hidden;
}

/* 背景图片 */
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

/* 背景遮罩层 */
.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1;
}

/* 游戏标题 */
.game-title {
  position: absolute;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  width: 100%;
  text-align: center;
}

.title-text {
  display: block;
  font-size: 48px;
  font-weight: bold;
  color: #ffd700;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
}

/* 底部中央进入游戏按钮 */
.center-action {
  position: absolute;
  bottom: 100px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
}

.explore-button {
  background: none;
  border: none;
  padding: 0;
}

.button-content {
  background: linear-gradient(135deg, #4a1a1a, #2d1b1b);
  padding: 20px 50px;
  border-radius: 20px;
  border: 2px solid rgba(139, 69, 19, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}

.button-text-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  line-height: 1.2;
}

.button-text-line {
  color: #d4a574;
  font-size: 36px;
  font-weight: bold;
  display: block;
}
</style>

<script setup lang="ts">
import { onLoad, onShow } from '@dcloudio/uni-app'

// uni-app 类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
}

// 页面加载时初始化
onLoad(() => {
  console.log('[Home] 页面加载 - 简化版本')
})

// 页面显示时
onShow(() => {
  console.log('[Home] 页面显示')
})

// 处理开始挑战：从首页进入营地页面
function handleStartExplore() {
  console.log('[Home] 点击开始挑战按钮，跳转到营地')
  // 进入营地（营地里再通过底部导航进入闯关等功能）
  uni.navigateTo({ url: '/pages/camp/camp' })
}
</script>
