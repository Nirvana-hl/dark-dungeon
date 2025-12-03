// 性能优化工具函数

// 防抖函数
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number,
  immediate?: boolean
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null
  
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

// 图片懒加载
export function createLazyLoader() {
  const imageObserver = new IntersectionObserver((entries, observer) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target as HTMLImageElement
        const src = img.dataset.src
        if (src) {
          img.src = src
          img.removeAttribute('data-src')
          observer.unobserve(img)
        }
      }
    })
  }, {
    rootMargin: '50px 0px',
    threshold: 0.01
  })
  
  return {
    observe: (img: HTMLImageElement) => {
      if (img.dataset.src) {
        imageObserver.observe(img)
      }
    },
    disconnect: () => {
      imageObserver.disconnect()
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

// 检测网络状态
export function createNetworkMonitor() {
  const listeners = new Set<((online: boolean) => void)>()
  
  const updateStatus = () => {
    listeners.forEach(listener => listener(navigator.onLine))
  }
  
  window.addEventListener('online', updateStatus)
  window.addEventListener('offline', updateStatus)
  
  return {
    isOnline: () => navigator.onLine,
    subscribe: (listener: (online: boolean) => void) => {
      listeners.add(listener)
      return () => listeners.delete(listener)
    },
    destroy: () => {
      window.removeEventListener('online', updateStatus)
      window.removeEventListener('offline', updateStatus)
      listeners.clear()
    }
  }
}

// 本地存储管理
export const storage = {
  set: (key: string, value: any, ttl?: number) => {
    try {
      const item = {
        value,
        timestamp: Date.now(),
        ttl: ttl ? Date.now() + ttl * 1000 : null
      }
      localStorage.setItem(key, JSON.stringify(item))
    } catch (error) {
      console.warn('Failed to save to localStorage:', error)
    }
  },
  
  get: (key: string): any => {
    try {
      const item = localStorage.getItem(key)
      if (!item) return null
      
      const parsed = JSON.parse(item)
      
      // 检查是否过期
      if (parsed.ttl && Date.now() > parsed.ttl) {
        localStorage.removeItem(key)
        return null
      }
      
      return parsed.value
    } catch (error) {
      console.warn('Failed to read from localStorage:', error)
      return null
    }
  },
  
  remove: (key: string) => {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.warn('Failed to remove from localStorage:', error)
    }
  },
  
  clear: (pattern?: string) => {
    try {
      if (pattern) {
        Object.keys(localStorage).forEach(key => {
          if (key.includes(pattern)) {
            localStorage.removeItem(key)
          }
        })
      } else {
        localStorage.clear()
      }
    } catch (error) {
      console.warn('Failed to clear localStorage:', error)
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

// 性能监控
export function createPerformanceMonitor() {
  const metrics = {
    navigationStart: 0,
    loadTime: 0,
    domContentLoaded: 0,
    firstContentfulPaint: 0,
    largestContentfulPaint: 0
  }
  
  // 监听页面加载性能
  const observePageLoad = () => {
    if ('performance' in window && 'getEntriesByType' in performance) {
      const navigationEntries = performance.getEntriesByType('navigation')
      if (navigationEntries.length > 0) {
        const nav = navigationEntries[0] as PerformanceNavigationTiming
        metrics.navigationStart = nav.fetchStart || nav.startTime
        metrics.loadTime = nav.loadEventEnd - nav.fetchStart
        metrics.domContentLoaded = nav.domContentLoadedEventEnd - nav.fetchStart
      }
      
      // 监听绘制性能
      const paintEntries = performance.getEntriesByType('paint')
      paintEntries.forEach(entry => {
        if (entry.name === 'first-contentful-paint') {
          metrics.firstContentfulPaint = entry.startTime
        }
      })
    }
  }
  
  // 监听 LCP (Largest Contentful Paint)
  const observeLCP = () => {
    if ('PerformanceObserver' in window) {
      const observer = new PerformanceObserver((list) => {
        const entries = list.getEntries()
        const lastEntry = entries[entries.length - 1]
        if (lastEntry) {
          metrics.largestContentfulPaint = lastEntry.startTime
        }
      })
      
      try {
        observer.observe({ entryTypes: ['largest-contentful-paint'] })
      } catch (e) {
        console.warn('LCP observation not supported')
      }
    }
  }
  
  // 计算FPS
  const calculateFPS = () => {
    let fps = 0
    let lastTime = performance.now()
    let frames = 0
    
    const measureFPS = (currentTime: number) => {
      frames++
      
      if (currentTime >= lastTime + 1000) {
        fps = Math.round((frames * 1000) / (currentTime - lastTime))
        frames = 0
        lastTime = currentTime
      }
      
      requestAnimationFrame(measureFPS)
    }
    
    requestAnimationFrame(measureFPS)
    return () => fps
  }
  
  return {
    init: () => {
      observePageLoad()
      observeLCP()
    },
    getMetrics: () => ({ ...metrics }),
    calculateFPS
  }
}

// 资源预加载
export function preloadResources(resources: string[]) {
  const promises = resources.map(url => {
    return new Promise((resolve, reject) => {
      const link = document.createElement('link')
      link.rel = 'preload'
      link.href = url
      
      // 根据文件类型设置 as 属性
      if (url.endsWith('.js')) {
        link.as = 'script'
      } else if (url.endsWith('.css')) {
        link.as = 'style'
      } else if (url.match(/\.(png|jpg|jpeg|gif|webp)$/)) {
        link.as = 'image'
      }
      
      link.onload = resolve
      link.onerror = reject
      document.head.appendChild(link)
    })
  })
  
  return Promise.allSettled(promises)
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
        
        // 使用 requestIdleCallback 在浏览器空闲时处理
        if ('requestIdleCallback' in window) {
          requestIdleCallback(processBatch)
        } else {
          setTimeout(processBatch, 0)
        }
      } catch (error) {
        reject(error)
      }
    }
    
    processBatch()
  })
}

// 内存使用监控
export function createMemoryMonitor() {
  const checkMemory = () => {
    if ('memory' in performance) {
      const memory = (performance as any).memory
      return {
        used: memory.usedJSHeapSize,
        total: memory.totalJSHeapSize,
        limit: memory.jsHeapSizeLimit,
        usage: (memory.usedJSHeapSize / memory.jsHeapSizeLimit) * 100
      }
    }
    return null
  }
  
  return {
    check: checkMemory,
    isHighUsage: () => {
      const memory = checkMemory()
      return memory ? memory.usage > 80 : false
    }
  }
}