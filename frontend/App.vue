<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './stores/auth'
import { useCampStore } from './stores/camp'
import apiClient, { API_ENDPOINTS, type ApiResponse } from './api/request'

// uni-app 全局对象类型（这里只声明用到的 API）
declare const uni: {
  reLaunch: (options: { url: string }) => void
}

const auth = useAuthStore()
const campStore = useCampStore()

// 检查用户是否有角色
async function checkPlayerCharacter(): Promise<boolean> {
  try {
    // 尝试从store获取角色数据
    if (campStore.playerCharacter) {
      return true
    }
    
    // 如果store中没有，尝试从API获取 - 使用正确的接口
    const response = await apiClient.get<ApiResponse<any>>(
      API_ENDPOINTS.CHARACTER.PLAYER_INSTANCE
    )
    
    if (response.data && response.data.code === 200 && response.data.data) {
      // 角色数据会通过campStore.fetchCampData()自动更新，这里不需要手动赋值
      return true
    }
    
    return false
  } catch (error: any) {
    // 注意：错误对象的结构可能是 error.statusCode 或 error.response?.status
    const statusCode = error.statusCode || error.response?.status
    
    // 如果接口返回404或没有数据，说明没有角色
    if (statusCode === 404) {
      return false
    }
    // 403或401错误需要向上抛出，让调用者处理（可能需要重新登录）
    if (statusCode === 403 || statusCode === 401) {
      throw error
    }
    // 其他错误也认为没有角色，避免阻塞用户
    console.warn('检查角色失败:', {
      statusCode,
      error: error.userMessage || error.errMsg || error.message
    })
    return false
  }
}

// 应用启动时：决定先进入「登录页」、「角色选择」还是「首页」
onLaunch(async () => {
  console.log('App Launch')
  
  try {
    // 初始化认证状态（从本地存储恢复 token 和用户信息）
    await auth.init()

    // 如果已经登录过（本地有 token 和 user）
    if (auth.isAuthenticated && auth.token) {
      console.log('用户已登录，检查角色状态...')
      
      // 检查是否有角色（如果403错误，说明token可能无效，应该重新登录）
      try {
        const hasCharacter = await checkPlayerCharacter()
        
        if (!hasCharacter) {
          // 没有角色，跳转到角色选择页面
          console.log('用户未选择角色，跳转到角色选择页面')
          uni.reLaunch({ url: '/pages/class-selection/class-selection' })
        } else {
          // 有角色，先进入主菜单界面
          console.log('用户已有角色，跳转到主菜单（首页）')
          uni.reLaunch({ url: '/pages/home/home' })
        }
      } catch (error: any) {
        // 如果检查角色时出现403或401错误，说明token无效，清除认证并跳转登录
        // 注意：错误对象的结构是 error.statusCode，不是 error.response.status
        const statusCode = error.statusCode || error.response?.status
        if (statusCode === 403 || statusCode === 401) {
          console.warn('Token无效或已过期，清除认证信息并跳转登录页', {
            statusCode,
            error: error.userMessage || error.errMsg || error.message
          })
          // 清除认证信息（通过logout方法，它会调用clearAuth）
          await auth.logout()
          uni.reLaunch({ url: '/pages/login/login' })
        } else {
          // 其他错误（如404），说明没有角色，跳转角色选择页
          console.log('未找到角色，跳转到角色选择页面', {
            statusCode,
            error: error.userMessage || error.errMsg || error.message
          })
          uni.reLaunch({ url: '/pages/class-selection/class-selection' })
        }
      }
    } else {
      // 未登录，先进入登录页面
      console.log('用户未登录，跳转到登录页面')
      uni.reLaunch({ url: '/pages/login/login' })
    }
  } catch (error) {
    console.error('App启动时发生错误:', error)
    // 发生错误时，默认跳转到登录页
    uni.reLaunch({ url: '/pages/login/login' })
  }
})

// 应用显示时
onShow(() => {
  console.log('App Show')
})

// 应用隐藏时
onHide(() => {
  console.log('App Hide')
})
</script>

<template>
  <view class="app-container">
    <!-- uni-app 会自动渲染当前页面，不需要 RouterView -->
  </view>
</template>

<style>
/* 全局样式 */
.app-container {
  width: 100%;
  height: 100%;
}
</style>
