<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()

// 应用启动时初始化
onLaunch(() => {
  console.log('App Launch')
  // 初始化认证状态
  auth.init()
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
