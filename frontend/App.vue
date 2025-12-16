<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './stores/auth'

// uni-app 全局对象类型（这里只声明用到的 API）
declare const uni: {
  reLaunch: (options: { url: string }) => void
}

const auth = useAuthStore()

// 应用启动时：决定先进入「微信认证」还是「首页」
onLaunch(async () => {
  console.log('App Launch')
  // 初始化认证状态（从本地存储恢复 token 和用户信息）
  await auth.init()

  // 如果已经登录过（本地有 token 和 user），直接进首页
  if (auth.isAuthenticated) {
    uni.reLaunch({ url: '/pages/home/home' })
  } else {
    // 否则先进入登录/微信认证页面
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
