import { App } from 'vue'
import { createPerformanceMonitor, createMemoryMonitor, createErrorBoundary, debounce } from '@/utils/performance'

export default {
  install(app: App) {
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
    
    // 开发环境下的性能警告
    if (process.env.NODE_ENV === 'development') {
      // 内存使用监控
      const checkMemoryUsage = debounce(() => {
        const memory = memoryMonitor.check()
        if (memory && memory.usage > 70) {
          console.warn(`High memory usage detected: ${memory.usage.toFixed(2)}%`)
        }
      }, 5000)
      
      // 定期检查内存使用
      setInterval(checkMemoryUsage, 30000)
      
      // 性能指标监控
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
    
    // 生产环境下的性能上报
    if (process.env.NODE_ENV === 'production') {
      // 定期上报性能数据
      setInterval(() => {
        const metrics = performanceMonitor.getMetrics()
        const memory = memoryMonitor.check()
        
        // 这里可以发送到分析服务
        if (metrics.loadTime > 5000 || (memory && memory.usage > 90)) {
          console.warn('Performance issue detected, should report to analytics')
        }
      }, 60000) // 每分钟检查一次
    }
  }
}