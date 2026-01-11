<template>
  <view class="home-container">
    <!-- 背景图片 -->
    <image class="background-image" src="/static/tabbar/background.jpg" mode="aspectFill"></image>
    
    <!-- 右上角退出登录按钮 -->
    <view class="logout-container">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </view>

    <!-- 中间下方"开始冒险"按钮 -->
    <view class="main-action">
      <button class="start-btn" @click="handleStartAdventure">
        <text class="start-title">开始冒险</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'
import { useCampStore } from '@/stores/camp'
import apiClient, { API_ENDPOINTS, type ApiResponse } from '@/api/request'

declare const uni: {
  navigateTo: (options: { url: string }) => void
  switchTab: (options: { url: string }) => void
  reLaunch: (options: { url: string }) => void
}

const auth = useAuthStore()
const campStore = useCampStore()

// 检查用户是否选择了角色
async function checkPlayerCharacter(): Promise<boolean> {
  try {
    // 先尝试从store获取
    if (campStore.playerCharacter) {
      return true
    }
    
    // 如果store中没有，尝试获取营地数据
    await campStore.fetchCampData()
    
    if (campStore.playerCharacter) {
      return true
    }
    
    // 如果还是没有，尝试直接调用API
    const response = await apiClient.get<ApiResponse<any>>(
      API_ENDPOINTS.CHARACTER.PLAYER_INSTANCE
    )
    
    if (response.data && response.data.code === 200 && response.data.data) {
      return true
    }
    
    return false
  } catch (error: any) {
    const statusCode = error.statusCode || error.response?.status
    
    // 404表示没有角色
    if (statusCode === 404) {
      return false
    }
    
    // 401/403需要重新登录
    if (statusCode === 401 || statusCode === 403) {
      console.warn('检查角色时认证失败，需要重新登录')
      return false
    }
    
    console.warn('检查角色失败:', error?.message || error)
    return false
  }
}

// 开始冒险：检查是否选择了角色
async function handleStartAdventure() {
  try {
    console.log('[Home] 开始检查角色状态...')
    const hasCharacter = await checkPlayerCharacter()
    
    if (!hasCharacter) {
      // 未选择角色，跳转到选择界面
      console.log('[Home] 用户未选择角色，跳转到角色选择界面')
      uni.navigateTo({ url: '/pages/class-selection/class-selection' })
    } else {
      // 已选择角色，直接进入营地界面
      console.log('[Home] 用户已选择角色，跳转到营地界面')
      uni.navigateTo({ url: '/pages/camp/camp' })
    }
  } catch (error) {
    console.error('[Home] 检查角色状态失败:', error)
    // 出错时也尝试跳转到角色选择界面
    uni.navigateTo({ url: '/pages/class-selection/class-selection' })
  }
}

// 退出登录
async function handleLogout() {
  try {
    await auth.logout()
  } finally {
    uni.reLaunch({ url: '/pages/login/login' })
  }
}

// 页面加载时初始化
onLoad(() => {
  console.log('[Home] 页面加载 - 简化版本')
})

// 页面显示时
onShow(() => {
  console.log('[Home] 页面显示')
})

// 页面加载时初始化营地数据（如果有的话）
onMounted(async () => {
  if (auth.isAuthenticated) {
    try {
      // 预加载营地数据，但不阻塞界面
      campStore.fetchCampData().catch(err => {
        console.warn('[Home] 预加载营地数据失败:', err)
      })
    } catch (error) {
      console.warn('[Home] 初始化营地数据失败:', error)
    }
  }
})
</script>

<style scoped>
.home-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.logout-container {
  position: absolute;
  top: 40rpx;
  right: 40rpx;
  z-index: 10;
}

.logout-btn {
  padding: 12rpx 24rpx;
  font-size: 28rpx;
  border-radius: 8rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.3);
  background: rgba(15, 23, 42, 0.8);
  color: #e5e7eb;
  backdrop-filter: blur(10rpx);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.3);
}

.logout-btn:active {
  background: rgba(15, 23, 42, 0.9);
  transform: scale(0.98);
}

.main-action {
  position: absolute;
  bottom: 120rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 600rpx;
  z-index: 10;
}

.start-btn {
  width: 100%;
  padding: 32rpx 0;
  border-radius: 24rpx;
  border: none;
  background: linear-gradient(135deg, #f97316, #ea580c);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 12rpx 32rpx rgba(248, 113, 22, 0.6);
  font-weight: 700;
}

.start-btn:active {
  transform: scale(0.98);
  box-shadow: 0 8rpx 24rpx rgba(248, 113, 22, 0.5);
}

.start-title {
  font-size: 40rpx;
  font-weight: 700;
  letter-spacing: 2rpx;
}
</style>

