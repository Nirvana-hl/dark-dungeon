import { createApp } from 'vue'
import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { createPinia } from 'pinia'
import App from './App.vue'
import Game from './views/Game.vue'
import Skills from './views/Skills.vue'
import Settings from './views/Settings.vue'
import Camp from './views/Camp.vue'
import Explore from './views/Explore.vue'
import Summary from './views/Summary.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', component: Game, meta: { title: '暗黑地牢肉鸽 - 战斗' } },
  { path: '/camp', component: Camp, meta: { title: '营地' } },
  { path: '/explore', component: Explore, meta: { title: '探索' } },
  { path: '/summary', component: Summary, meta: { title: '结算' } },
  { path: '/skills', component: Skills, meta: { title: '技能树' } },
  { path: '/settings', component: Settings, meta: { title: '设置' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.afterEach((to: any) => {
  if (to.meta?.title) document.title = String(to.meta.title)
})

createApp(App)
  .use(createPinia())
  .use(router)
  .mount('#app')