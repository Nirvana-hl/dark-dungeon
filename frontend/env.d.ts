/// <reference path="./types/vite-client.d.ts" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// uni-app 类型声明
declare const uni: {
  navigateTo: (options: { url: string }) => void
  redirectTo: (options: { url: string }) => void
  reLaunch: (options: { url: string }) => void
  navigateBack: (options?: { delta?: number }) => void
  switchTab: (options: { url: string }) => void
  showToast: (options: { title: string; icon?: 'success' | 'error' | 'loading' | 'none'; duration?: number }) => void
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
  clearStorageSync: () => void
  [key: string]: any
};

// Vue 3 全局类型
declare module 'vue' {
  export * from '@vue/runtime-core'
}

