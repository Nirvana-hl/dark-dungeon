import { createApp } from 'vue'
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'
import App from './App.vue'
import Home from './views/Home.vue'
import Game from './views/Game.vue'
import Skills from './views/Skills.vue'
import SkillsSimple from './views/SkillsSimple.vue'
import SkillsModern from './views/SkillsModern.vue'
import Settings from './views/Settings.vue'
import Camp from './views/Camp.vue'
import CampSimple from './views/CampSimple.vue'
import CampModern from './views/CampModern.vue'
import CampOfficial from './views/CampOfficial.vue'
import Explore from './views/Explore.vue'
import Summary from './views/Summary.vue'
import ShopSimple from './views/ShopSimple.vue'
import ShopModern from './views/ShopModern.vue'
import Login from './views/Login.vue'
import Achievements from './views/Achievements.vue'


// 导入全局样式
import './styles/index.css'

const routes: RouteRecordRaw[] = [
  { 
    path: '/', 
    component: Home, 
    meta: { title: '暗黑地牢肉鸽 - 首页' } 
  },
  { 
    path: '/battle', 
    component: Game, 
    meta: { title: '暗黑地牢肉鸽 - 战斗' } 
  },
  { 
    path: '/camp', 
    component: CampOfficial, 
    meta: { title: '营地' } 
  },
  { 
    path: '/camp-simple', 
    component: CampSimple, 
    meta: { title: '简化营地' } 
  },
  { 
    path: '/camp-original', 
    component: Camp, 
    meta: { title: '原始营地' } 
  },
  { 
    path: '/camp-modern', 
    component: CampModern, 
    meta: { title: '现代化营地' } 
  },
  { 
    path: '/explore', 
    component: Explore, 
    meta: { title: '闯关' } 
  },
  { 
    path: '/summary', 
    component: Summary, 
    meta: { title: '结算' } 
  },
  { 
    path: '/skills', 
    component: SkillsModern, 
    meta: { title: '技能树' } 
  },
  { 
    path: '/skills-original', 
    component: Skills, 
    meta: { title: '原始技能树' } 
  },
  { 
    path: '/skills-modern', 
    component: SkillsModern, 
    meta: { title: '现代化技能树' } 
  },
  { 
    path: '/shop', 
    component: ShopSimple, 
    meta: { title: '商城' } 
  },
  { 
    path: '/shop-modern', 
    component: ShopModern, 
    meta: { title: '现代化商城' } 
  },
  { 
    path: '/settings', 
    component: Settings, 
    meta: { title: '设置' } 
  },
  { 
    path: '/login', 
    component: Login, 
    meta: { title: '登录' } 
  },
  { 
    path: '/achievements', 
    component: Achievements, 
    meta: { title: '成就系统' } 
  },

]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态
router.beforeEach(async (to, from, next) => {
  // 不需要登录的页面
  const publicPages = ['/login']
  
  // 检查是否是公共页面
  const isPublicPage = publicPages.includes(to.path)
  
  // 初始化认证状态
  const authStore = useAuthStore()
  await authStore.init()
  
  if (!authStore.isAuthenticated && !isPublicPage) {
    // 未登录且访问非公共页面，跳转到登录页
    next('/login')
  } else if (authStore.isAuthenticated && isPublicPage) {
    // 已登录但访问登录页，跳转到首页
    next('/')
  } else {
    // 正常访问
    next()
  }
})

router.afterEach((to: any) => {
  if (to.meta?.title) {
    document.title = String(to.meta.title)
  }
})

// 全局错误处理
router.onError((error) => {
  console.error('Router error:', error)
})

// 初始化应用
const app = createApp(App)

// 注册全局组件
app.component('BaseButton', () => import('./components/base/BaseButton.vue'))
app.component('BaseCard', () => import('./components/base/BaseCard.vue'))
app.component('BaseModal', () => import('./components/base/BaseModal.vue'))

// 注册功能组件
app.component('CharacterCardSimple', () => import('./components/CharacterCardSimple.vue'))
app.component('StressIndicator', () => import('./components/StressIndicator.vue'))
app.component('ShopOfferItem', () => import('./components/ShopOfferItem.vue'))
app.component('ShopOfferModal', () => import('./components/ShopOfferModal.vue'))
app.component('PurchaseConfirmModal', () => import('./components/PurchaseConfirmModal.vue'))
app.component('ToastNotification', () => import('./components/ToastNotification.vue'))
app.component('AIAssistant', () => import('./components/AIAssistant.vue'))
app.component('LoadingSpinner', () => import('./components/LoadingSpinner.vue'))

app.use(createPinia())
   .use(router)
   .mount('#app')