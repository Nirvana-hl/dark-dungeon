import { App } from 'vue'
import { createPerformanceMonitor, createMemoryMonitor, createErrorBoundary, debounce } from '@/utils/performance'

// uni-app API 类型声明
declare const uni: {
  getStorageSync: (key: string) => any
  setStorageSync: (key: string, value: any) => void
  removeStorageSync: (key: string) => void
  clearStorageSync: () => void
  onHide: (callback: () => void) => void
}

export default {
  install(app: App) {
    // 清理本地存储数据，但保留认证相关的token
    try {
      if (typeof uni !== 'undefined') {
        console.log('[PerformancePlugin] 清理本地存储数据（保留认证信息）...')

        // 保留认证相关的数据
        let savedToken = null
        let savedUser = null
        try {
          savedToken = uni.getStorageSync('token')
          savedUser = uni.getStorageSync('user')
        } catch (e) {
          // 忽略获取错误
        }

        // 清理所有存储
        uni.clearStorageSync()

        // 恢复认证数据
        if (savedToken) {
          try {
            uni.setStorageSync('token', savedToken)
          } catch (e) {
            console.warn('[PerformancePlugin] 恢复token失败:', e)
          }
        }
        if (savedUser) {
          try {
            uni.setStorageSync('user', savedUser)
          } catch (e) {
            console.warn('[PerformancePlugin] 恢复user失败:', e)
          }
        }

        console.log('[PerformancePlugin] 本地存储数据清理完成，认证信息已保留')
      }
    } catch (e) {
      console.log('[PerformancePlugin] 本地存储清理跳过（非uni-app环境）')
    }

    // 创建性能监控器
    const performanceMonitor = createPerformanceMonitor()
    const memoryMonitor = createMemoryMonitor()
    const errorBoundary = createErrorBoundary()

    // 初始化性能监控
    performanceMonitor.init()
    
    // 全局错误处理
    app.config.errorHandler = (error, instance, info) => {
      console.error('Vue Error:', error, info)
      const errorObj = error instanceof Error ? error : new Error(String(error))
      errorBoundary.handle(errorObj, `Vue Error - ${info}`)
    }
    
    // 未捕获的Promise错误
    window.addEventListener('unhandledrejection', (event) => {
      console.error('Unhandled Promise Rejection:', event.reason)
      const error = event.reason instanceof Error ? event.reason : new Error(String(event.reason))
      errorBoundary.handle(error, 'Unhandled Promise Rejection')
    })
    
    // 全局错误
    window.addEventListener('error', (event) => {
      console.error('Global Error:', event.error)
      const error = event.error instanceof Error ? event.error : new Error(String(event.error || 'Unknown error'))
      errorBoundary.handle(error, 'Global Error')
    })
    
    // 提供性能监控工具
    app.provide('performance', performanceMonitor)
    app.provide('memory', memoryMonitor)
    app.provide('errors', errorBoundary)
    
    // 添加全局性能检查方法
    app.config.globalProperties.$performance = {
      getMetrics: () => performanceMonitor.getMetrics(),
      getFPS: () => performanceMonitor.calculateFPS(),
      getMemory: () => memoryMonitor.check(),
      isHighMemoryUsage: () => memoryMonitor.isHighUsage(),
      getErrors: () => errorBoundary.getErrors(),
      clearErrors: () => errorBoundary.clearErrors()
    }
    
    // 安全的 setInterval 封装：回调抛错时清除定时器，避免小程序内部 runtime 在无页面上下文时抛出不可控错误
    const activeIntervals: number[] = []
    function safeSetInterval(fn: () => void, ms: number) {
      const wrapper = () => {
        try {
          // 仅在可用环境下执行
          if (typeof fn === 'function') fn()
        } catch (err) {
          // 出现异常则清理当前定时器，避免小程序 runtime 继续在无效页面上下文调用内部 API
          try {
            const id = idRef.id
            if (typeof id === 'number') {
              clearInterval(id)
              const idx = activeIntervals.indexOf(id)
              if (idx !== -1) activeIntervals.splice(idx, 1)
            }
          } catch (e) {}
          console.warn('[performance] safeSetInterval callback threw, cleared interval', err)
        }
      }
      const idRef: { id: number | null } = { id: null }
      // 尝试创建定时器（如果环境不支持则降级为 setTimeout）
      try {
        const id = setInterval(wrapper, ms) as unknown as number
        idRef.id = id
        activeIntervals.push(id)
        return id
      } catch (e) {
        // 退化：使用 setTimeout 循环
        const loop = () => {
          wrapper()
          idRef.id = setTimeout(loop, ms) as unknown as number
        }
        idRef.id = setTimeout(loop, ms) as unknown as number
        activeIntervals.push(idRef.id)
        return idRef.id
      }
    }

    // 开发环境下的性能警告（使用 safeSetInterval）
    if (process.env.NODE_ENV === 'development') {
      // 内存使用监控
      const checkMemoryUsage = debounce(() => {
        const memory = memoryMonitor.check()
        if (memory && typeof memory.usage === 'number' && memory.usage > 70) {
          console.warn(`High memory usage detected: ${memory.usage.toFixed(2)}%`)
        }
      }, 5000)
      
      // 定期检查内存使用
      safeSetInterval(checkMemoryUsage, 30000)
      
      // 性能指标监控（一次性）
      setTimeout(() => {
        const metrics = performanceMonitor.getMetrics()
        console.log('Performance Metrics:', metrics)
        
        if (metrics.loadTime > 3000) {
          console.warn('Slow load time detected:', metrics.loadTime)
        }
        
        if (metrics.firstContentfulPaint > 2000) {
          console.warn('Slow FCP detected:', metrics.firstContentfulPaint)
        }
      }, 5000)
    }
    
    // 生产环境下的性能上报（使用 safeSetInterval）
    if (process.env.NODE_ENV === 'production') {
      // 定期上报性能数据
      safeSetInterval(() => {
        const metrics = performanceMonitor.getMetrics()
        const memory = memoryMonitor.check()

        // 这里可以发送到分析服务
        if (metrics.loadTime > 5000 || (memory && typeof memory.usage === 'number' && memory.usage > 90)) {
          console.warn('Performance issue detected, should report to analytics')
        }
      }, 60000) // 每分钟检查一次
    }

    // 在小程序切后台时清理 activeIntervals，避免 runtime 在无页面上下文继续回调
    try {
      if (typeof uni !== 'undefined' && uni && typeof uni.onHide === 'function') {
        uni.onHide(() => {
          for (const id of activeIntervals.slice()) {
            try {
              clearInterval(id)
            } catch (e) {
              try { clearTimeout(id) } catch (e2) {}
            }
          }
          activeIntervals.length = 0
        })
      }
    } catch (e) {
      // 忽略注册失败
    }
  }
}