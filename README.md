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
├── backend/                      # Spring Boot 后端
│   └── src/main/java/com/dungeon/
│       ├── controller/          # REST 控制器
│       ├── service/             # 业务逻辑层
│       ├── mapper/              # MyBatis Plus 映射器
│       ├── entity/              # 实体类
│       └── config/              # 配置类
├── frontend/                     # Vue 3 前端
│   ├── src/
│   │   ├── components/          # Vue 组件
│   │   │   ├── StatusBar.vue    # 状态栏
│   │   │   ├── BattleField.vue  # 战斗区域
│   │   │   ├── ActionBar.vue    # 操作栏
│   │   │   ├── ItemModal.vue   # 道具弹窗
│   │   │   └── AIChat.vue       # AI 聊天组件
│   │   ├── views/               # 页面视图
│   │   │   ├── Game.vue         # 主游戏界面
│   │   │   ├── Camp.vue         # 营地界面
│   │   │   ├── Explore.vue      # 探索界面
│   │   │   └── ...
│   │   ├── stores/              # Pinia 状态管理
│   │   │   ├── auth.ts          # 认证状态
│   │   │   ├── characters.ts   # 角色管理
│   │   │   ├── game.ts          # 游戏核心逻辑
│   │   │   ├── battle.ts        # 战斗状态
│   │   │   ├── wallet.ts        # 钱包系统
│   │   │   └── run.ts           # 探索记录
│   │   └── lib/
│   │       └── api.ts           # API 客户端
│   ├── package.json
│   └── vite.config.ts
├── README.md                     # 项目说明
├── 需求文档.md                   # 产品需求文档
├── 架构迁移可行性分析.md         # 架构迁移分析文档
└── 项目结构分析.md               # 项目结构分析文档
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
- ✅ 成就系统（已实现后端接口）
- ⏳ AI 助手集成
- ⏳ 实时多人对战（如需要）

## 🚀 快速开始

### 完整启动流程

**详细启动说明请查看**: [项目启动说明.md](./项目启动说明.md)

#### 1. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

#### 2. 启动前端服务

```bash
# 进入前端目录
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:5180 启动

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

### 后端开发

**详细文档**: [backend/README.md](./backend/README.md)

后端已实现：
- ✅ Spring Boot 2.7.18
- ✅ MyBatis Plus 3.5.3
- ✅ Spring Security + JWT
- ✅ H2 数据库（开发环境，可切换 MySQL/PostgreSQL）

## 🏕️ 营地仪表盘 API

- **接口**：`GET /api/camp/dashboard`
- **认证**：请求头携带 `Authorization: Bearer {token}`
- **返回字段**：
  - `userPlayerCharacter`：玩家主角最新状态
  - `userCardCharacters`：拥有的角色卡实例
  - `userCards`：法术/装备卡列表
  - `inventory`：背包道具
  - `wallets`：多种货币余额
  - `shopOffers`：营地商城商品，按 `displayOrder` 排序

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userPlayerCharacter": { "...": "..." },
    "userCardCharacters": [{ "...": "..." }],
    "userCards": [{ "...": "..." }],
    "inventory": [{ "...": "..." }],
    "wallets": [{ "currencyType": "gold", "balance": 1200 }],
    "shopOffers": [{ "offerType": "card", "price": 300 }]
  }
}
```

## ⚔️ 地牢探索 API

- `GET /dungeons`：获取所有地牢模板（名称、主题、推荐卡组）
- `POST /dungeons/runs/start`：开启探索，创建 `runs` 记录并初始化关卡进度
- `GET /dungeons/runs/current`：查询当前进行中的探索
- `POST /dungeons/runs/{runId}/explore`：探索房间，可能触发随机事件或遭遇敌人
- `POST /dungeons/runs/{runId}/battle`：结算战斗，生成战报并更新角色状态
- `POST /dungeons/runs/{runId}/end`：结束探索（胜利/失败/放弃），自动更新 `user_stage_progress`

所有接口都要求在请求头携带 `Authorization: Bearer {token}`，后端会根据 Token 判定用户身份并记录探索日志。

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

## 🔄 架构迁移状态

### ✅ 迁移完成

- ✅ **前端**: Vue 3 + TypeScript + Pinia（保持不变）
- ✅ **后端**: 已从 Supabase 迁移到 Spring Boot + MyBatis Plus
- ✅ **数据库**: 使用本地 H2 数据库（可切换 MySQL/PostgreSQL）
- ✅ **认证**: 从 Supabase Auth 迁移到 JWT
- ✅ **API**: 前后端通过 REST API 通信

### 已实现功能模块

1. ✅ **用户认证模块** - 注册、登录、JWT Token 管理
2. ✅ **角色管理模块** - 创建、删除、升星
3. ✅ **钱包系统模块** - 金币获取、消费、增加
4. ✅ **游戏战斗模块** - 卡牌加载、角色特性、敌方卡牌
5. ✅ **成就系统模块** - 成就查询、分类筛选、搜索功能
6. ✅ **地牢探索与战斗** - 地牢列表、探索流程、战斗模拟、结算奖励

### 详细文档

- [架构迁移可行性分析.md](./架构迁移可行性分析.md) - 迁移分析文档
- [项目启动说明.md](./项目启动说明.md) - 启动指南

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
- [开发分工方案.md](./开发分工方案.md) - **两人团队开发分工指南** ⭐
- [API接口文档.md](./API接口文档.md) - RESTful API接口规范
- [战斗系统实现方案.md](./战斗系统实现方案.md) - **战斗系统详细实现方案** ⭐
- [Apifox测试指南-地牢探索与战斗系统.md](./Apifox测试指南-地牢探索与战斗系统.md) - **Apifox接口测试指南** ⭐
- [地牢地图系统分析与实现方案.md](./地牢地图系统分析与实现方案.md) - **地牢地图系统分析与实现方案** ⭐
- [架构迁移可行性分析.md](./架构迁移可行性分析.md) - 架构迁移详细分析
- [项目启动说明.md](./项目启动说明.md) - 启动指南

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
- [x] 添加成就系统（后端接口已完成）
- [ ] 优化游戏平衡性

### 长期目标（6-12个月）
- [ ] 多人在线对战
- [ ] 移动端适配
- [ ] 游戏内容扩展
- [ ] 性能优化

## 🧠 本次分析回顾
- 已根据营地界面痛点重写 `需求文档.md` v1.1，统一角色/法术/装备卡牌模型，并梳理用户手牌、商城与道具的字段定义。
- 下一步需落地营地聚合 API 与后端实体映射，确保前端能够一次性获取营地所需数据。
- 营地设施、任务与商城在数据库层面已规划，后续需要在前端 `Camp.vue` 与 Pinia store 中补充状态管理与交互逻辑。
- 2025-11-17：评审 v1.1 数据库设计，识别模板/实例表重复、字段定义不统一等问题，并输出精简版表一览方案，后续调整数据建模与文档结构。

## ♻️ 今日任务复盘（2025-11-17）
- **产出**：
  1. 新增并持续维护 `database/schema_v1_1.sql`（PostgreSQL 版）与 `database/schema_v1_1_mysql.sql`（MySQL 8.0 版），均包含 20+ 张核心营地/战斗/经济相关表结构和示例数据，可在 `psql` 或 Navicat 中直接执行。
  2. 根据 v1.1.1 需求，重构技能系统：每个职业拥有独立技能树（`skills.player_character_code`），并以 `user_player_character_skills` 记录解锁进度；示例数据同步更新。
  3. 通过固定 UUID 的方式，简化示例数据的串联与排查。
- **问题**：当前 Spring Boot 模块仍引用旧版 H2 schema，尚未与 v1.1 表结构对齐；后续需要补充 MyBatis Plus 实体与 Mapper，以及数据迁移脚本。
- **改进**：下一轮迭代计划将 SQL 拆分为 schema/data 两份脚本，并编写自动化校验（如 `psql -f` / `mysql -f` + smoke test）确保 README 中的建库流程一键可跑。

## ♻️ 今日任务复盘（2025-11-19）
- **产出**：
  1. 修复 `/cards` 接口 500 报错：移除未实现的 `CardMapper#selectByCardType|selectByRarity` 映射，统一改为在 `CardService` 内部使用 `LambdaQueryWrapper` 进行筛选，避免 MyBatis 找不到 bound statement。
  2. 同步修复 `/card-characters` 接口：去除 `CardCharacterMapper` 中缺失 SQL 的自定义方法，并在 `CardCharacterService` 引入 `LambdaQueryWrapper`，保持职业/阵营筛选功能可用。
  3. 解决 `card_characters.class` 字段命名不一致问题：通过在实体上添加 `@TableField("class")` 显式映射，避免 MySQL 抛出 “Unknown column class_type”。
  4. 优化卡牌与卡牌角色筛选的空参处理逻辑，参数为空时自动回退为全量查询，减少调用端判空成本。
  5. 更新卡牌角色模板支持“只改动一部分字段”的 PATCH 行为：在 Service 层引入忽略 null/空字符串 的属性复制，避免用户只传部分字段就触发数据库 NOT NULL 或 JSON 校验约束。
  6. 修复 `/user-cards` 接口 500：移除 `UserCardMapper` 中缺失 SQL 的自定义查询，改由 `UserCardService` 使用 `LambdaQueryWrapper` 构建查询，保证用户手牌/已装备查询与鉴权一致。
  7. 修复 `/user-card-characters` 接口 500：统一 `UserCardCharacterMapper` 与 Service 的查询实现，消除缺失 SQL 的调用点，并对“已上阵/按用户筛选”逻辑使用 LambdaQueryWrapper 实现。
  8. 新增 `CampController` + `CampService`，一次聚合主角、卡牌、背包、钱包与商城数据，提供前端营地仪表盘的唯一数据源。
- **问题**：项目缺少卡牌相关 Mapper XML，自定义 SQL 若需落地必须新增文件或使用注解；需要一份清单记录当前 Mapper 及其 SQL 完成度。
- **改进**：补充卡牌接口的集成测试样例，并在 `API接口文档.md` 中新增卡牌筛选说明，确保迁移到 MyBatis Plus 的模块有验收标准。

## ♻️ 今日任务复盘（2025-12-01）
- **产出**：
  1. 梳理 `/game/enemy-cards` 接口的调用链路（`Stage` → `Enemy` → `EnemyCard` → `Card`），明确它理应依据 `stages.enemy_pool` 中的敌人 ID 再去 `enemy_cards` 拉取卡组。
  2. 定位 `stages.enemy_pool` 现有示例采用 `{"enemies":[...]}` 结构，与 `EnemyService#selectEnemyForStage` 预期的数组/对象列表不一致，导致解析失败并回退到“按难度随机”策略。
  3. 重构 `DungeonService` 遭遇逻辑：沿用 `EnemyService` 的敌人池解析，遭遇敌人后立刻拉取其卡组并写入 `RunProgressState.pendingEnemyCards`，`RunActionResponse` 也同步返回 `pendingEnemyCards/pendingEnemyId`。
- **问题**：关卡并没有真正驱动敌人选择，接口表现为“按难度+关卡编号查询”，与产品预期（关卡指定敌人池）不符。
- **改进**：统一 `enemy_pool` 的 JSON 结构（如 `[1,2]` 或 `[{ "enemyId": 1, "weight": 3 }]`），或扩展解析逻辑兼容 `{"enemies":[...]}`，并补充校验脚本，确保 enemyId 能顺利映射到 `enemy_cards` 获取卡牌。

---

**最后更新**: 2024年 | **维护者**: 项目团队
