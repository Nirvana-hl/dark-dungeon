# 暗黑地牢肉鸽 - 全栈游戏项目

一个基于 Vue 3 + Spring Boot 的暗黑风格地牢探索肉鸽游戏。

## 📋 项目概述

本项目是一个前后端分离的游戏应用，采用现代化的技术栈实现。当前正在从 Supabase 架构迁移到 Spring Boot + MyBatis Plus 架构。

## 🏗️ 当前架构

### 前端
- **框架**: Vue 3 + TypeScript + Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **样式**: Tailwind CSS (CDN) + Font Awesome
- **运行端口**: http://localhost:5180

### 后端（当前）
- **平台**: Supabase (PostgreSQL + 实时订阅 + 认证)
- **数据访问**: Supabase JavaScript SDK

### 后端（目标架构）
- **框架**: Spring Boot
- **ORM**: MyBatis Plus
- **数据库**: 本地数据库（MySQL/PostgreSQL/H2）
- **架构**: Controller-Service-Mapper 三层架构
- **认证**: Spring Security + JWT

## 📁 项目结构

```
ai-full-stack---dark-dungeon/
├── src/                          # 前端源码
│   ├── components/              # Vue 组件
│   │   ├── StatusBar.vue        # 状态栏
│   │   ├── BattleField.vue      # 战斗区域
│   │   ├── ActionBar.vue        # 操作栏
│   │   ├── ItemModal.vue        # 道具弹窗
│   │   └── AIChat.vue           # AI 聊天组件
│   ├── views/                    # 页面视图
│   │   ├── Game.vue             # 主游戏界面
│   │   ├── Camp.vue             # 营地界面
│   │   ├── Explore.vue         # 探索界面
│   │   └── ...
│   ├── stores/                   # Pinia 状态管理
│   │   ├── auth.ts             # 认证状态
│   │   ├── characters.ts       # 角色管理
│   │   ├── game.ts             # 游戏核心逻辑
│   │   ├── battle.ts           # 战斗状态
│   │   ├── wallet.ts           # 钱包系统
│   │   └── run.ts              # 探索记录
│   └── lib/
│       └── supabase.ts         # Supabase 客户端（待迁移）
├── backend/                      # 后端项目（待创建）
│   └── src/main/java/com/dungeon/
│       ├── controller/          # REST 控制器
│       ├── service/             # 业务逻辑层
│       ├── mapper/              # MyBatis Plus 映射器
│       ├── entity/              # 实体类
│       └── config/              # 配置类
├── README.md                     # 项目说明
├── 需求文档.md                   # 产品需求文档
└── 架构迁移可行性分析.md         # 架构迁移分析文档
```

## 🎮 核心功能

### 已实现功能
- ✅ 用户认证（Supabase Auth）
- ✅ 角色管理（创建、升级、删除）
- ✅ 战斗系统（卡牌战斗、回合制）
- ✅ 钱包系统（金币管理）
- ✅ 营地系统（角色选择、装备管理）
- ✅ 地牢探索（关卡系统）

### 待实现功能
- ⏳ 技能系统
- ⏳ 成就系统
- ⏳ AI 助手集成
- ⏳ 实时多人对战（如需要）

## 🚀 快速开始

### 前端开发

1. **安装依赖**
```bash
npm config set registry https://registry.npmmirror.com
npm install
```

2. **启动开发服务器**
```bash
npm run dev
```
访问 http://localhost:5180

3. **构建生产版本**
```bash
npm run build
```

### 后端开发（待实现）

后端项目结构将在后续创建，预计包含：
- Spring Boot 2.7+ 或 3.x
- MyBatis Plus 3.5+
- Spring Security + JWT
- MySQL/PostgreSQL 数据库

## 📊 数据库设计

### 核心数据表（基于需求文档）

1. **users** - 用户表
2. **characters** - 角色表（职业、等级、属性）
3. **inventory** - 背包表（物品、装备）
4. **skills** - 技能表
5. **dungeons** - 地牢关卡表
6. **runs** - 探索记录表
7. **enemies** - 怪物图鉴表
8. **items** - 物品图鉴表
9. **events** - 随机事件表
10. **player_actions** - 玩家操作记录表
11. **achievements** - 成就表
12. **user_cards** - 用户卡牌表
13. **enemy_cards** - 敌方卡牌表
14. **character_traits** - 角色特性表
15. **user_wallets** - 用户钱包表

详细表结构设计请参考 `需求文档.md`

## 🔄 架构迁移计划

### 当前状态
- ✅ 前端：Vue 3 已完成
- ⚠️ 后端：使用 Supabase（计划迁移到 Spring Boot）

### 迁移方案
详细分析请查看：**架构迁移可行性分析.md**

**关键决策点**：
1. **完全迁移**: 全部功能迁移到 Spring Boot（推荐用于学习）
2. **混合架构**: 保留 Supabase 实时功能，复杂逻辑用 Spring Boot
3. **渐进式迁移**: 逐步迁移功能模块

## 🛠️ 技术栈说明

### 前端技术
- **Vue 3**: 渐进式 JavaScript 框架
- **TypeScript**: 类型安全的 JavaScript
- **Pinia**: Vue 3 官方状态管理库
- **Vite**: 下一代前端构建工具
- **Tailwind CSS**: 实用优先的 CSS 框架

### 后端技术（目标）
- **Spring Boot**: Java 企业级应用框架
- **MyBatis Plus**: MyBatis 增强工具
- **Spring Security**: 安全框架
- **JWT**: JSON Web Token 认证

## 📝 开发规范

### 代码风格
- 使用 TypeScript 严格模式
- 遵循 Vue 3 Composition API 最佳实践
- 组件命名使用 PascalCase
- 文件命名使用 kebab-case

### Git 提交规范
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `refactor`: 代码重构
- `style`: 代码格式调整

## 🐛 已知问题

1. README.md 存在 Git 冲突标记（已解决）
2. 部分功能依赖 Supabase，迁移时需要重构
3. 实时功能（如需要）需要额外实现 WebSocket

## 📚 相关文档

- [需求文档.md](./需求文档.md) - 产品需求和功能规划
- [架构迁移可行性分析.md](./架构迁移可行性分析.md) - 架构迁移详细分析

## 🤝 贡献指南

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。

## 🔮 未来规划

### 短期目标（1-3个月）
- [ ] 完成 Spring Boot 后端搭建
- [ ] 实现 Controller-Service-Mapper 三层架构
- [ ] 迁移核心功能模块
- [ ] 实现 JWT 认证系统

### 中期目标（3-6个月）
- [ ] 完善 AI 助手功能
- [ ] 实现技能系统
- [ ] 添加成就系统
- [ ] 优化游戏平衡性

### 长期目标（6-12个月）
- [ ] 多人在线对战
- [ ] 移动端适配
- [ ] 游戏内容扩展
- [ ] 性能优化

---

**最后更新**: 2024年 | **维护者**: 项目团队
