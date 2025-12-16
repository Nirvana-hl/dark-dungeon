// 性能优化工具函数（uni-app 适配版）

// uni-app 类型声明
declare const uni: {
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
  getNetworkType: (options?: { success?: (res: { networkType: string }) => void }) => void
}

// 防抖函数
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  immediate?: boolean
): (...args: Parameters<T>) => void {
  let timeout: ReturnType<typeof setTimeout> | null = null
  
  return function executedFunction(...args: Parameters<T>) {
    const later = () => {
      timeout = null
      if (!immediate) func(...args)
    }
    
    const callNow = immediate && !timeout
    
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(later, wait)
    
    if (callNow) func(...args)
  }
}

// 节流函数
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  limit: number
): (...args: Parameters<T>) => void {
  let inThrottle: boolean = false
  
  return function executedFunction(...args: Parameters<T>) {
    if (!inThrottle) {
      func(...args)
      inThrottle = true
      setTimeout(() => inThrottle = false, limit)
    }
  }
}

// 图片懒加载（uni-app 中不需要，但保留接口兼容性）
export function createLazyLoader() {
  // uni-app 中图片会自动懒加载，此函数保留接口但不执行实际操作
  return {
    observe: () => {
      // 小程序中图片自动懒加载，无需手动处理
    },
    disconnect: () => {
      // 无需清理
    }
  }
}

// 虚拟滚动计算
export function calculateVirtualScroll(
  containerHeight: number,
  itemHeight: number,
  scrollTop: number,
  totalItems: number
) {
  const visibleCount = Math.ceil(containerHeight / itemHeight)
  const startIndex = Math.floor(scrollTop / itemHeight)
  const endIndex = Math.min(startIndex + visibleCount, totalItems - 1)
  
  const offsetY = startIndex * itemHeight
  
  return {
    startIndex,
    endIndex,
    offsetY,
    visibleCount
  }
}

// 格式化文件大小
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes'
  
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 检测网络状态（uni-app 版本）
export function createNetworkMonitor() {
  const listeners = new Set<((online: boolean) => void)>()
  
  let isOnline = true
  
  const updateStatus = (networkType: string) => {
    const online = networkType !== 'none'
    if (isOnline !== online) {
      isOnline = online
      listeners.forEach(listener => listener(online))
    }
  }
  
  // 初始检查网络状态
  uni.getNetworkType({
    success: (res) => {
      updateStatus(res.networkType)
    }
  })
  
  // 注意：小程序中网络状态变化需要通过页面生命周期监听
  // 这里提供一个基础实现，实际使用时需要在页面中调用 updateStatus
  
  return {
    isOnline: () => isOnline,
    subscribe: (listener: (online: boolean) => void) => {
      listeners.add(listener)
      return () => listeners.delete(listener)
    },
    updateStatus: (networkType: string) => {
      updateStatus(networkType)
    },
    destroy: () => {
      listeners.clear()
    }
  }
}

// 本地存储管理（uni-app 版本）
export const storage = {
  set: (key: string, value: any, ttl?: number) => {
    try {
      const item = {
        value,
        timestamp: Date.now(),
        ttl: ttl ? Date.now() + ttl * 1000 : null
      }
      uni.setStorageSync(key, JSON.stringify(item))
    } catch (error) {
      console.warn('Failed to save to storage:', error)
    }
  },
  
  get: (key: string): any => {
    try {
      const item = uni.getStorageSync(key)
      if (!item) return null
      
      // 如果是字符串，尝试解析
      const parsed = typeof item === 'string' ? JSON.parse(item) : item
      
      // 检查是否过期
      if (parsed.ttl && Date.now() > parsed.ttl) {
        uni.removeStorageSync(key)
        return null
      }
      
      return parsed.value
    } catch (error) {
      console.warn('Failed to read from storage:', error)
      return null
    }
  },
  
  remove: (key: string) => {
    try {
      uni.removeStorageSync(key)
    } catch (error) {
      console.warn('Failed to remove from storage:', error)
    }
  },
  
  clear: (pattern?: string) => {
    try {
      if (pattern) {
        // uni-app 中需要遍历所有 key，但无法直接获取所有 key
        // 这里提供一个简化实现，实际使用时需要维护 key 列表
        console.warn('[Storage] 按模式清除存储在小程序中需要维护 key 列表')
      } else {
        // uni-app 中清空所有存储
        uni.clearStorageSync()
      }
    } catch (error) {
      console.warn('Failed to clear storage:', error)
    }
  }
}

// 错误边界处理
export function createErrorBoundary() {
  const errors: Array<{ error: Error; timestamp: Date; context?: string }> = []
  
  const handleError = (error: Error, context?: string) => {
    errors.push({
      error,
      timestamp: new Date(),
      context
    })
    
    // 发送错误到监控服务
    if (process.env.NODE_ENV === 'production') {
      // 这里可以集成 Sentry 或其他错误监控服务
      console.error('Application error:', { error, context })
    }
  }
  
  return {
    handle: handleError,
    getErrors: () => [...errors],
    clearErrors: () => errors.length = 0
  }
}

// 性能监控（uni-app 简化版）
export function createPerformanceMonitor() {
  const metrics = {
    navigationStart: Date.now(),
    loadTime: 0,
    domContentLoaded: 0,
    firstContentfulPaint: 0,
    largestContentfulPaint: 0
  }
  
  // uni-app 中性能监控能力有限，提供基础实现
  const observePageLoad = () => {
    metrics.loadTime = Date.now() - metrics.navigationStart
    metrics.domContentLoaded = metrics.loadTime
    metrics.firstContentfulPaint = metrics.loadTime
  }
  
  return {
    init: () => {
      observePageLoad()
    },
    getMetrics: () => ({ ...metrics }),
    calculateFPS: () => {
      // 小程序中 FPS 计算需要特殊处理，这里返回一个简单的实现
      let fps = 60
      return () => fps
    }
  }
}

// 资源预加载（uni-app 中不需要，但保留接口兼容性）
export function preloadResources(resources: string[]): Promise<Array<{ status: string }>> {
  // uni-app 中资源会自动加载，这里返回成功状态
  return Promise.resolve(resources.map(() => ({ status: 'fulfilled' })))
}

// 代码分割加载
export async function loadComponent(componentPath: string) {
  try {
    const module = await import(/* @vite-ignore */ componentPath)
    return module.default
  } catch (error) {
    console.error(`Failed to load component: ${componentPath}`, error)
    throw error
  }
}

// 批量操作优化
export function batch<T>(items: T[], batchSize: number, processor: (batch: T[]) => Promise<void>) {
  return new Promise<void>((resolve, reject) => {
    let index = 0
    
    const processBatch = async () => {
      try {
        const batch = items.slice(index, index + batchSize)
        if (batch.length === 0) {
          resolve()
          return
        }
        
        await processor(batch)
        index += batchSize
        
        // 使用 setTimeout 在下一帧处理
        setTimeout(processBatch, 0)
      } catch (error) {
        reject(error)
      }
    }
    
    processBatch()
  })
}

// 内存使用监控（小程序中无法直接获取，提供占位实现）
export function createMemoryMonitor() {
  const checkMemory = () => {
    // 小程序中无法直接获取内存信息
    return null
  }
  
  return {
    check: checkMemory,
    isHighUsage: () => {
      // 小程序中无法判断内存使用情况
      return false
    }
  }
}
