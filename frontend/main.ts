import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './styles/index.css'
// Toggle verbose SafeClose logging. Set to true only when actively debugging origin.
const SAFE_CLOSE_VERBOSE = false;

// Safety: 全局封装 WebSocket.close，防止传入非法的 close code（如 1006）导致运行时异常。
// 说明：1006 是浏览器内部表示“abnormal closure”的代码，不能作为 close() 的参数传入。
if (typeof WebSocket !== 'undefined' && WebSocket.prototype && !(WebSocket.prototype as any).__safeClosePatched) {
  const _origClose = WebSocket.prototype.close
  WebSocket.prototype.close = function (code?: number, reason?: string) {
    let validCode: number | undefined
    if (typeof code === 'number' && (code === 1000 || (code >= 3000 && code <= 4999))) {
      validCode = code
    }
    if (typeof code === 'number' && validCode === undefined && SAFE_CLOSE_VERBOSE) {
      try {
        // Log illegal code usage with stack to help trace origin in runtime (dev only)
        // eslint-disable-next-line no-console
        console.debug('[SafeClose] intercepted invalid WebSocket.close code', { code, stack: new Error().stack })
      } catch (e) {}
    }
    try {
      if (validCode !== undefined) {
        return _origClose.call(this, validCode, reason)
      }
      return _origClose.call(this)
    } catch (err) {
      try {
        return _origClose.call(this)
      } catch (err2) {
        // 最后兜底：不抛异常影响页面
        // eslint-disable-next-line no-console
        console.warn('WebSocket.close failed', err2)
      }
    }
  }
  ;(WebSocket.prototype as any).__safeClosePatched = true
}

// Safety: 包装小程序/平台的 closeSocket（如 wx.closeSocket / uni.closeSocket）
// 目标：防止将非法的 close code（如 1006）传入底层 WebSocket.close
function patchPlatformCloseSocket(platformObj: any) {
  if (!platformObj) return
  const orig = platformObj.closeSocket
  if (typeof orig !== 'function' || (orig as any).__safeClosePatched) return
  platformObj.closeSocket = function (arg?: any) {
    try {
      // 如果传入的是对象参数并包含 code 字段，且 code 非法，则删除该字段
      if (arg && typeof arg === 'object') {
        if ('code' in arg) {
          const c = arg.code
          if (!(c === 1000 || (typeof c === 'number' && c >= 3000 && c <= 4999))) {
            // 删除非法 code，调用时不带 code 参数
            try {
              // log stack for tracing (dev only)
              // eslint-disable-next-line no-console
              SAFE_CLOSE_VERBOSE && console.debug('[SafeClose] intercepted invalid platform.closeSocket code', { code: c, stack: new Error().stack })
            } catch (e) {}
            try { delete arg.code } catch (e) { /* ignore */ }
          }
        }
        return orig.call(this, arg)
      }
      // 如果直接传入数字（某些实现可能这样做），仅当合法时传入
      if (typeof arg === 'number') {
        const c = arg
        if (c === 1000 || (c >= 3000 && c <= 4999)) {
          return orig.call(this, arg)
        }
        try {
          // eslint-disable-next-line no-console
          SAFE_CLOSE_VERBOSE && console.debug('[SafeClose] intercepted invalid platform.closeSocket numeric code', { code: c, stack: new Error().stack })
        } catch (e) {}
        return orig.call(this)
      }
      // 其余情况直接调用原始方法
      return orig.call(this, arg)
    } catch (err) {
      try { return orig.call(this) } catch (err2) { /* swallow */ }
    }
  }
  ;(platformObj.closeSocket as any).__safeClosePatched = true
}

// Try patching common platform globals
try { patchPlatformCloseSocket((globalThis as any).wx) } catch (e) {}
try { patchPlatformCloseSocket((globalThis as any).uni) } catch (e) {}

// Wrap connectSocket to patch returned SocketTask.close if present
function patchConnectSocket(platformObj: any) {
  if (!platformObj) return
  const origConnect = platformObj.connectSocket
  if (typeof origConnect !== 'function' || (origConnect as any).__safeConnectPatched) return
  platformObj.connectSocket = function (...args: any[]) {
    const result = origConnect.apply(this, args)
    try {
      const socketTask = result
      if (socketTask && typeof socketTask === 'object' && typeof socketTask.close === 'function') {
        const origTaskClose = socketTask.close
        socketTask.close = function (arg?: any) {
          try {
            if (arg && typeof arg === 'object' && 'code' in arg) {
              const c = arg.code
              if (!(c === 1000 || (typeof c === 'number' && c >= 3000 && c <= 4999))) {
                try {
                  // log stack for tracing (dev only)
                  // eslint-disable-next-line no-console
                  SAFE_CLOSE_VERBOSE && console.debug('[SafeClose] intercepted invalid socketTask.close code', { code: c, stack: new Error().stack })
                } catch (e) {}
                try { delete arg.code } catch (e) { /* ignore */ }
              }
            }
            if (typeof arg === 'number') {
              const c = arg
              if (!(c === 1000 || (c >= 3000 && c <= 4999))) {
                try {
                  // eslint-disable-next-line no-console
                  SAFE_CLOSE_VERBOSE && console.debug('[SafeClose] intercepted invalid socketTask.close numeric code', { code: c, stack: new Error().stack })
                } catch (e) {}
                return origTaskClose.call(this)
              }
            }
            return origTaskClose.call(this, arg)
          } catch (err) {
            try { return origTaskClose.call(this) } catch (err2) { /* swallow */ }
          }
        }
        ;(socketTask.close as any).__safeClosePatched = true
      }
    } catch (e) { /* ignore */ }
    return result
  }
  ;(platformObj.connectSocket as any).__safeConnectPatched = true
}

try { patchConnectSocket((globalThis as any).wx) } catch (e) {}
try { patchConnectSocket((globalThis as any).uni) } catch (e) {}

// Suppress noisy platform error reporting for invalid WebSocket close code when possible.
const INVALID_WS_CLOSE_MSG_PART = "Failed to execute 'close' on 'WebSocket'"
try {
  const wxObj = (globalThis as any).wx
  if (wxObj && typeof wxObj.onError === 'function') {
    wxObj.onError((errMsg: string) => {
      try {
        if (typeof errMsg === 'string' && errMsg.indexOf(INVALID_WS_CLOSE_MSG_PART) !== -1) {
          // swallow this specific runtime error to avoid crashing/telemetry noise
          return
        }
      } catch (e) {}
      // forward to console for other errors
      try { console.error(errMsg) } catch (e) {}
    })
  }
} catch (e) {}
try {
  if (typeof (globalThis as any).onerror === 'function') {
    const origOnError = (globalThis as any).onerror
    (globalThis as any).onerror = function (msg: any, src?: any, line?: any, col?: any, err?: any): any {
      try {
        if (typeof msg === 'string' && msg.indexOf(INVALID_WS_CLOSE_MSG_PART) !== -1) {
          return true
        }
      } catch (e) {}
      return origOnError.apply(this, arguments as any)
    }
  } else {
    (globalThis as any).onerror = function (msg: any): any {
      try {
        if (typeof msg === 'string' && msg.indexOf(INVALID_WS_CLOSE_MSG_PART) !== -1) {
          return true
        }
      } catch (e) {}
      return false
    }
  }
} catch (e) {}

// uni-app 入口：返回 app 实例（不手动 mount）
export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  return { app, pinia }
}
