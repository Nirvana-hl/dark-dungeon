declare const uni: {
  navigateTo: (options: { url: string }) => void
  redirectTo: (options: { url: string }) => void
  navigateBack: (options?: { delta?: number }) => void
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  clearStorageSync: () => void
  showToast: (options: { title: string; icon?: 'success' | 'error' | 'loading' | 'none'; duration?: number }) => void
}

declare module '@dcloudio/uni-app' {
  // 页面生命周期
  export function onLoad(cb: (options?: Record<string, any>) => void): void
  export function onShow(cb: (options?: Record<string, any>) => void): void
  export function onUnload(cb: () => void): void
  export function onReady(cb: () => void): void
  export function onHide(cb: () => void): void
  
  // 应用生命周期（App.vue 中使用）
  export function onLaunch(cb: (options?: Record<string, any>) => void): void
}

