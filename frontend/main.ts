import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import './styles/index.css'

// uni-app 入口：返回 app 实例（不手动 mount）
export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()
  app.use(pinia)
  return { app, pinia }
}
