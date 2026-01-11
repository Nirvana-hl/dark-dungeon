<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

// uni-app 类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
  navigateBack: (options?: { delta?: number }) => void
  reLaunch: (options: { url: string }) => void
  showToast: (options: { title: string; icon?: string; duration?: number }) => void
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
}

const auth = useAuthStore()

/**
 * 设置页面
 * - 声音开关
 * - 特效开关
 * - 重置为默认
 * - 一键登出（清 token 并回登录页）
 */
const soundOn = ref(true)
const fxOn = ref(true)

// 页面加载时初始化
onLoad(() => {
  // 可以从存储中读取设置
  try {
    const savedSoundOn = uni.getStorageSync('soundOn')
    if (savedSoundOn !== null && savedSoundOn !== undefined) {
      soundOn.value = savedSoundOn === true || savedSoundOn === 'true'
    }
    
    const savedFxOn = uni.getStorageSync('fxOn')
    if (savedFxOn !== null && savedFxOn !== undefined) {
      fxOn.value = savedFxOn === true || savedFxOn === 'true'
    }
  } catch (error) {
    console.warn('[Settings] 读取设置失败:', error)
  }
})

function resetAll() {
  soundOn.value = true
  fxOn.value = true
  try {
    uni.setStorageSync('soundOn', true)
    uni.setStorageSync('fxOn', true)
  } catch (error) {
    console.warn('[Settings] 保存设置失败:', error)
  }
  uni.showToast({
    title: '已恢复默认设置',
    icon: 'success',
    duration: 2000
  })
}

function saveAndBack() {
  try {
    uni.setStorageSync('soundOn', soundOn.value)
    uni.setStorageSync('fxOn', fxOn.value)
  } catch (error) {
    console.warn('[Settings] 保存设置失败:', error)
  }
  uni.navigateBack()
}

async function handleLogout() {
  try {
    await auth.logout()
  } finally {
    uni.showToast({ title: '已退出登录', icon: 'success', duration: 1500 })
    // 直接回登录页，方便重新获取后端签发的 token
    uni.reLaunch({ url: '/pages/login/login' })
  }
}

function handleSoundChange(e: any) {
  soundOn.value = e.detail?.value ?? e
}

function handleFxChange(e: any) {
  fxOn.value = e.detail?.value ?? e
}

function goToHome() {
  uni.navigateTo({ url: '/pages/home/home' })
}
</script>

<template>
  <view class="settings-container">
    
    <view class="content-wrapper">
      <view class="settings-card">
        <text class="settings-title">设置</text>

        <view class="settings-list">
          <view class="setting-item">
            <text class="setting-label">声音</text>
            <switch :checked="soundOn" @change="handleSoundChange" />
          </view>
          <view class="setting-item">
            <text class="setting-label">特效</text>
            <switch :checked="fxOn" @change="handleFxChange" />
          </view>
        </view>

        <view class="button-group">
          <button class="action-button" @click="resetAll">恢复默认</button>
          <button class="action-button" @click="saveAndBack">保存并返回</button>
          <button class="action-button logout-button" @click="handleLogout">退出登录</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.settings-container {
  min-height: 100vh;
  background: linear-gradient(to bottom, #0f172a, #020617);
  color: white;
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 24px;
}

/* 返回首页按钮 */
.back-to-home {
  position: fixed;
  top: 20px;
  left: 20px;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 25px;
  color: white;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  font-weight: 500;
}

.back-to-home:active {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

.back-icon {
  font-size: 1rem;
}

.back-text {
  color: white;
}

.content-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 60px;
}

.settings-card {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 32px;
  width: 100%;
  max-width: 600px;
  border: 1px solid rgba(51, 65, 85, 0.5);
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
}

.settings-title {
  font-size: 2rem;
  font-weight: bold;
  display: block;
  margin-bottom: 24px;
  color: white;
}

.settings-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: rgba(30, 41, 59, 0.5);
  border-radius: 8px;
  border: 1px solid rgba(51, 65, 85, 0.5);
}

.setting-label {
  font-size: 1rem;
  color: white;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.action-button {
  flex: 1;
  padding: 12px 24px;
  border-radius: 8px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  transition: opacity 0.3s;
}

.action-button:active {
  opacity: 0.8;
}
</style>
