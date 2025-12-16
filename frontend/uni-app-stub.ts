// Minimal stub to satisfy H5/Vite build; in real mini-program, native lifecycle is provided.
// App 生命周期函数
export function onLaunch(cb: (options?: Record<string, any>) => void) {
  // No-op for H5; call once to avoid missing initialization
  typeof cb === 'function' && cb()
}

export function onShow(cb: () => void) {
  typeof cb === 'function' && cb()
}

export function onHide(cb: () => void) {
  // No-op
}

// 页面生命周期函数
export function onLoad(cb: (options?: Record<string, any>) => void) {
  // No-op for H5; call once to avoid missing initialization
  typeof cb === 'function' && cb()
}

export function onUnload(cb: () => void) {
  // No-op
}

export function onReady(cb: () => void) {
  typeof cb === 'function' && cb()
}



