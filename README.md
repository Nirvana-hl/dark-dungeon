<<<<<<< HEAD
# dark-dungeon
=======
# 暗黑地牢肉鸽 - Vue 脚手架

基于“页面 3.html”拆分而成，保留外观与交互，并新增路由点击跳转。

## 技术栈
- Vue 3 + Vite + TypeScript
- 路由：vue-router
- 样式：Tailwind CDN + Font Awesome CDN + Google 字体（快速跑通，后续可切换本地 Tailwind/PostCSS）

## 功能
- 主界面（/）：状态栏、战斗区域、底部操作区、道具弹窗
- 点击跳转：攻击/防御/技能按钮分别跳转到 /attack、/defense、/skills
- 道具弹窗：点击“道具”打开，支持关闭

## 目录
- src/components：StatusBar、BattleField、ActionBar、ItemModal
- src/views：Game（主页面）、Attack/Defense/Skills（占位页）

## 本地运行
1) 安装依赖
npm config set registry https://registry.npmmirror.com
npm i

2) 启动
npm run dev
- 打开浏览器访问 http://localhost:5180

## 后续可选
- 改用本地 Tailwind（postcss + tailwind.config.js）
- 将卡牌/状态等抽象为响应式数据与 props
- 新增全局 store（Pinia）管理生命/法力与回合状态
- 增加动画、音效与移动端适配
>>>>>>> 4b283b6 (初始化)
