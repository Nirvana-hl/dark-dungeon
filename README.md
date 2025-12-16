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

- [完整需求文档.md](./完整需求文档.md) - **完整的产品需求文档（PRD）** ⭐⭐⭐
- [需求文档.md](./需求文档.md) - 产品需求和功能规划（v1.1）
- [开发分工方案.md](./开发分工方案.md) - **两人团队开发分工指南** ⭐
- [API接口文档.md](./API接口文档.md) - RESTful API接口规范
- [战斗系统实现方案.md](./战斗系统实现方案.md) - **战斗系统详细实现方案** ⭐
- [Apifox测试指南-地牢探索与战斗系统.md](./Apifox测试指南-地牢探索与战斗系统.md) - **Apifox接口测试指南** ⭐
- [Apifox测试指南-治疗效果功能.md](./Apifox测试指南-治疗效果功能.md) - **治疗效果功能Apifox测试指南** ⭐
- [治疗效果系统可行性分析报告.md](./治疗效果系统可行性分析报告.md) - **治疗效果系统可行性分析** ⭐
- [地牢地图系统分析与实现方案.md](./地牢地图系统分析与实现方案.md) - **地牢地图系统分析与实现方案** ⭐
- [小程序迁移报告.md](./小程序迁移报告.md) - **小程序迁移前后端改动详细报告** ⭐⭐⭐
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

## ♻️ 今日任务复盘（2025-11-25）
- **产出**：
  1. 前端 `campApi.getCampData()` 对接真实 `GET /camp/dashboard`，确保所有营地视图读取的是后端聚合接口而非 Mock。
  2. 调整 `Pinia useCampStore` 数据结构，落地 `wallets`、`shopOffers` 等字段的规范化赋值，并在 `CampModern.vue` 中消费新的字段，营地主界面首次展示真实账号的主角/角色卡。
  3. 同步 README 记录营地功能的最新联调状态，方便前后端查看。
  4. 将技能系统路由（`/skills`）接入后端 `SkillController`，`SkillsModern.vue` 直接解析后端返回的技能树、解锁条件与状态，并支持一键解锁调用 `POST /user-skills/unlock`。
  5. 登录/注册页改为直接调用 Spring Boot `AuthController`，移除演示账号逻辑，统一使用后端签发的 JWT 与用户信息管理 Pinia 会话。
- **问题**：后端 DTO 暂未携带卡牌/商店的名称、描述等展示字段，前端只能基于 ID 输出调试信息，影响营地 UI 体验。
- **改进**：补充 `ShopOfferDTO`、`UserCardDTO` 的模板关联字段，或在聚合接口层做二次查询返回展示所需信息，减少前端二次请求与硬编码。

## ♻️ 前后端连接情况检查（2025-01-XX）
- **产出**：
  1. 完成前后端连接情况全面检查，生成详细报告 `前后端连接情况报告.md`。
  2. 识别出 8 个已连接模块、5 个部分连接模块、6 个未连接模块。
  3. 发现 11 处路径不匹配问题，影响核心功能使用。
- **已连接成功**：
  - ✅ 认证模块（缺少 logout 接口）
  - ✅ 营地仪表盘
  - ✅ 技能系统
  - ✅ 商城模块（路径不匹配：`/camp/shop-offers` vs `/shop/offers`）
  - ✅ 背包模块（路径不匹配：`/camp/inventory` vs `/api/inventory`）
  - ✅ 成就系统（路径不匹配：`/achievement/*` vs `/achievements/*`）
  - ✅ 地牢探索（路径不匹配，前端未实际使用）
  - ✅ 卡牌系统（路径不匹配：`/card/user-cards` vs `/user-cards`）
- **部分连接**：
  - ⚠️ 角色管理（路径不匹配）
  - ⚠️ 游戏战斗（缺少 `/character/traits` 和 `/card/enemy-cards` 接口）
  - ⚠️ 钱包模块（完全未实现）
  - ⚠️ 压力系统（完全未实现）
  - ⚠️ 营地其他功能（部署卡牌、任务事件、AI建议等未实现）
- **未连接**：
  - ❌ 统计模块（前端定义但未使用）
  - ❌ 用户信息模块（前端定义但未使用）
  - ❌ 卡牌装备模块（前端定义但未使用）
- **问题**：
  1. **高优先级**：商城、背包、卡牌接口路径不匹配，导致功能无法使用。
  2. **高优先级**：游戏战斗缺少角色特性和敌方卡牌接口。
  3. **中优先级**：钱包、压力系统完全未实现。
  4. **低优先级**：统计、用户信息、卡牌装备等模块前端定义但未使用。
- **改进**：
  1. 统一前后端路径规范，建议修改后端路径以匹配前端调用。
  2. 补充缺失的游戏战斗接口（角色特性、敌方卡牌）。
  3. 实现钱包和压力系统后端接口。
  4. 清理前端未使用的 API 定义，或实现对应的后端接口。

## ♻️ 今日任务复盘（2025-11-27）
- **产出**：
  1. 在 Windows 11 + Temurin JDK 25 环境执行 `mvn clean package`，复现 Lombok 在 `TypeTag::UNKNOWN` 被移除后触发的编译崩溃，并明确需切换到 LTS JDK（17/21）或为 Maven 配置 Toolchain 使用旧版本 JDK。
  2. 梳理 `backend/src/main/resources/application.yml`，确认当前 Spring Boot 默认直连本地 MySQL（`jdbc:mysql://localhost/dark_dungeon`，账号 `root/123456`），提醒必须先建库并导入 `dark_dungeon.sql`/`database/*.sql` 否则服务启动即抛出连接异常。
  3. 在 Node.js 22.16.0 环境完成 `npm install && npm run build`，确认前端依赖完整且 Vite 构建可过，仅遗留动态 import 与静态 import 混用的 chunk 警告。
- **问题**：
  1. 服务器 JDK 版本与 `java.version=1.8` 的编译配置不兼容，导致后端无法编译更别说启动。
  2. 后端未提供 H2 默认配置，启动依赖本地 MySQL，如果数据库/账号缺失将直接报错。
  3. 若继续保留重复的懒加载声明，Vite 将无法分包这些组件，后续需要统一 import 方式以减小首包。
- **改进**：
  1. 在 `backend/pom.xml` 中新增 `maven-toolchain-plugin`，显式声明 JDK 17 以屏蔽本地 JDK 波动；或在团队开发说明里列出必须安装的 JDK 版本。
  2. 增加 `.env` 或 `application-example.yml`，提供可选 H2/HSQL 配置，方便一键启动与 CI。
  3. 整理 `frontend/src/main.ts` 的动态 import，仅在组件未被其它视图静态引用时再做分包，确保构建告警清晰。

## ♻️ 前后端数据连接问题解决（2025-11-28）
- **问题**：前端显示没有后端数据，新注册用户看不到角色、卡牌、背包等信息
- **原因分析**：
  1. 新用户注册后没有自动初始化游戏数据（角色、钱包等）
  2. 前端需要用户先创建角色才能显示数据
  3. 数据库中有商城商品等模板数据，但用户实例数据为空
- **解决方案**：
  1. **创建角色**：新用户注册后需要调用 `POST /user-player-characters` 创建主角实例
     - 请求体：`{ "playerCharacterId": 1 }`（1=守望者, 2=秘术师, 3=游侠, 4=战士）
  2. **初始化钱包**：调用 `POST /wallet/add` 为新用户初始化金币
     - 请求体：`{ "currencyType": "gold", "amount": 1000 }`
  3. **获取角色模板**：调用 `GET /player-characters` 查看可用的角色模板
- **验证结果**：
  - ✅ 后端API正常工作，返回数据格式正确
  - ✅ 创建角色后，`GET /camp/dashboard` 可以返回主角数据
  - ✅ 初始化钱包后，可以显示金币余额
  - ✅ 商城商品数据正常显示（42个商品）
- **后续改进建议**：
  1. 在用户注册时自动创建默认角色和初始化钱包（在 `AuthController.register` 中实现）
  2. 前端在检测到用户没有角色时，自动引导用户创建角色
  3. 提供"新手引导"功能，帮助新用户快速上手

## ♻️ 今日任务复盘（2025-12-01）
- **产出**：
  1. 为 `ShopPanel.vue` 的卡牌商店位置新增稀有度感知样式，角色卡会根据 `common/rare/epic/legendary` 自动呈现不同背景与光效，便于中低端设备也能一眼区分稀有度。
  2. 补充前端构建验证（Vite build），确认新增样式不会破坏打包流程，仅保留已有的动态 import 警告。
  3. 修复卡牌角色购买后需手动刷新才能在卡组管理中看到的问题，`CampOfficial.vue` 现会在成功购买后同时刷新营地聚合数据。
- **问题**：
  1. 卡牌商店仍然使用占位图标，缺少实际角色立绘，后续需要配合后端返回的图片资源进一步美化。
  2. `ShopPanel` 目前只基于稀有度展示，没有结合职业/阵营等标签，用户在大量商品时仍需额外筛选。
- **改进**：
  1. 将 `cardCharacter` 的职业、阵营信息映射为统一的色条或角标，搭配稀有度背景提供复合信息。
  2. 继续梳理 `main.ts` 中动态 import + 静态 import 重复引用的问题，减少 Vite 告警并优化首包体积。

## ♻️ 今日任务复盘（2025-12-02）
- **产出**：
  1. **修复购买功能数据库保存问题**：
     - 在 `ShopService.purchaseItem()` 方法中添加了 `@Transactional(rollbackFor = Exception.class)` 注解，确保所有异常都会触发事务回滚
     - 添加了详细的日志记录，追踪购买过程中的每一步（商品查询、余额检查、扣款、物品发放）
     - 为所有数据库操作添加了结果验证（检查 `updateResult` 和 `insertResult`），确保操作成功
     - 在 `addItemToInventory()`、`addCardToUser()`、`addCardCharacterToUser()` 方法中添加了异常处理，确保失败时抛出异常触发回滚
  2. **优化前端购买后数据刷新**：
     - 在 `CampOfficial.vue` 的 `purchaseItem()` 函数中，购买成功后同时刷新钱包、背包、商城、营地数据和卡组数据
     - 添加了数据验证日志，确保购买后数据已正确更新
     - 增加了等待时间，确保异步数据加载完成后再进行验证
  3. **添加登出接口**：
     - 在 `AuthController` 中添加了 `/auth/logout` 接口，解决前端登出时的 404 错误
     - 优化了前端登出逻辑的错误处理，将错误改为警告，避免影响用户体验
  4. **营地沉浸式背景升级**：
     - 在 `CampOfficial.vue` 中将 `.camp-official` 容器背景替换为 `frontend/public/yingdi.png`，并连续降低遮罩透明度，当前梯度仅保留 35%~45% 的暗色遮罩，让篝火细节完全显现
     - 让用户在进入营地视图时立即看到篝火营地氛围图，有助于非战斗阶段的代入感
  5. **战斗牌库同步营地上阵卡**：
     - 新增 `userCardCharacterApi` 并在 `useGameStore.loadUserDeckFromDB()` 中并行请求 `/user-cards/deck` 与 `/user-card-characters/deployed`
     - 战斗界面会自动合并“已上阵角色 + 选定卡组”，彻底解决战斗日志提示“牌库为空”的问题
- **问题**：
  1. 购买功能之前存在事务未正确提交的问题，导致购买后数据没有保存到数据库
  2. 前端购买后没有刷新卡组数据，导致购买的卡牌在卡组管理中看不到
  3. 缺少详细的日志记录，难以追踪购买过程中的问题
- **改进**：
  1. 所有数据库操作现在都有结果验证，确保操作成功
  2. 购买流程现在有完整的日志追踪，便于调试和问题排查
  3. 前端购买后会刷新所有相关数据，确保界面显示最新状态
  4. 后续可以针对移动端或低性能设备提供“低特效背景”开关，必要时切换为纯色/渐变背景以兼顾性能

## ♻️ 今日任务复盘（2025-01-XX）
- **产出**：
  1. 修复压力系统 debuff 显示问题：在 `StressService.toDebuffDTO()` 方法中添加了 `stressLevel` 和 `triggerChance` 字段的映射，确保后端返回的数据包含压力层级信息。
  2. 压力详情弹窗功能完善：前端已实现从数据库获取压力等级对应的 debuff，并在弹窗中按等级显示。对于压力等级2级和3级，如果存在多个 debuff，会随机抽取一个显示。
  3. 压力系统数据流打通：从数据库 `stress_debuff_configs` 表 → 后端 `StressService.getAllDebuffConfigs()` → 前端 `openStressDetails()` → 弹窗显示，整个数据流已完整。
- **问题**：
  1. 后端 DTO 缺少 `id` 字段，但前端已通过 `${level}-${index}` 方式生成唯一标识，不影响功能使用。
- **改进**：
  1. 后续可以考虑在 `StressDebuffDTO` 中添加 `id` 字段，统一前后端数据结构。
  2. 可以考虑将随机抽取逻辑移到后端，减少前端计算负担。

## ♻️ 角色卡牌升星功能实现（2025-12-04）
- **产出**：
  1. **升星功能实现**：
     - ✅ 修改了 `ShopService.addCardCharacterToUser()` 方法，购买角色时只添加到1星角色的数量上
     - ✅ 在 `UserCardCharacterService` 中添加了 `upgradeStarLevel()` 方法
     - ✅ 实现了消耗3个当前星级角色，创建新的更高星级记录的逻辑
     - ✅ 实现了根据 `star_upgrade_payload` 计算属性增幅的逻辑
     - ✅ 添加了 `POST /user-card-characters/{id}/upgrade-star` 接口
  2. **数据隔离**：
     - ✅ 每个星级是独立的记录（1星和2星是不同的记录）
     - ✅ 购买1星角色不会添加到2星角色的数量上
     - ✅ 升星时创建新的记录，而不是修改现有记录
  3. **创建文件**：
     - `角色卡牌升星功能分析与实现方案.md`：详细的分析报告和实现说明
- **功能说明**：
  1. **购买逻辑**：购买角色时，只添加到1星角色的数量上（查询条件包含 `current_star_level = 1`）
  2. **升星条件**：当1星角色数量 >= 3 时，可以点击升星
  3. **升星效果**：消耗3个1星角色，创建新的2星角色记录，并根据 `star_upgrade_payload` 计算属性增幅
  4. **数据隔离**：每个星级是独立的记录，互不影响
- **使用方式**：
  - 购买角色：自动添加到1星角色数量
  - 升星操作：`POST /user-card-characters/{id}/upgrade-star`
  - 升星后：创建新的更高星级记录，属性根据配置自动增幅

## ♻️ 敌人卡牌查询接口优化（2025-12-04）
- **产出**：
  1. **接口功能增强**：
     - ✅ 修改了 `GET /game/enemy-cards` 接口，支持可选的 `enemyId` 参数
     - ✅ 添加了 `GET /game/stage-enemies` 接口，返回关卡中所有可能的敌人列表
     - ✅ 在 `EnemyService` 中添加了 `getStageEnemiesList()` 方法
  2. **问题解决**：
     - ✅ 解决了"一个关卡中不同敌人的卡牌无法区分"的问题
     - ✅ 现在可以通过 `enemyId` 直接查询指定敌人的卡牌
     - ✅ 可以通过 `stage-enemies` 接口获取关卡中所有敌人，然后逐个查询卡牌
  3. **创建文件**：
     - `敌人卡牌查询接口使用说明.md`：详细的接口使用说明和示例代码
- **使用方式**：
  1. **直接查询**：`GET /game/enemy-cards?enemyId=1` - 查询指定敌人的卡牌
  2. **获取敌人列表**：`GET /game/stage-enemies?stage=1&difficulty=普通` - 获取关卡中所有可能的敌人
  3. **随机查询**：`GET /game/enemy-cards?stage=1&difficulty=普通` - 随机选择一个敌人并返回其卡牌

## ♻️ 敌人卡牌系统分析与实现（2025-12-04）
- **产出**：
  1. **敌人卡牌系统可行性分析**：
     - ✅ 数据库结构完全支持：`enemy_cards` 表关联 `cards` 表，`cards` 表的 `cardType` 字段可以区分装备（equipment）和法术（spell）
     - ✅ 后端代码已实现：`EnemyService.getEnemyCardsByEnemyId()` 可以获取敌人的所有卡牌（包括装备和法术）
     - ⚠️ **当前限制**：`DungeonService.simulateBattle()` 只是简单的回合制战斗，没有实际使用敌人的卡牌
     - ✅ **实现方案**：提供了详细的实现方案，包括改进 `simulateBattle()` 方法、实现敌人 AI 策略、卡牌效果执行等
  2. **敌人角色卡功能实现**：
     - ✅ 创建了 `enemy_card_characters` 表，用于关联敌人和角色卡（`card_characters`）
     - ✅ 创建了 `EnemyCardCharacter` 实体类和 `EnemyCardCharacterMapper` 接口
     - ✅ 在 `EnemyService` 中添加了 `getEnemyCardCharactersByEnemyId()` 方法
     - ✅ 修改了 `getEnemyCardsByEnemyId()` 方法，自动合并敌人的卡牌（法术/装备）和角色卡
     - ✅ 敌人现在可以从 `enemy_cards` 表获取法术/装备卡牌，从 `enemy_card_characters` 表获取角色卡
  3. **创建文件**：
     - `敌人卡牌系统分析与实现方案.md`：详细分析报告，包含数据库结构、代码实现、实现步骤和代码示例
     - `database/add_enemy_card_characters_table.sql`：数据库迁移脚本，创建敌人角色卡关联表
- **问题**：
  1. 当前战斗系统没有使用敌人的卡牌，敌人只是根据基础属性进行攻击
  2. 数据库中敌人卡牌数据较少（只有1条记录），需要为每个敌人添加更多卡牌
- **改进**：
  1. **优先级1（简单）**：完善数据库数据，为每个敌人添加装备和法术卡牌
  2. **优先级2（复杂）**：改进 `simulateBattle()` 方法，实现敌人抽牌、出牌和卡牌效果执行
  3. **优先级3（高级）**：实现更复杂的 AI 策略，让敌人根据战况智能选择卡牌
  4. **已完成**：✅ 敌人角色卡功能已实现，敌人可以从 `enemy_card_characters` 表抽取角色卡

## ♻️ 战斗界面效果优化（2025-01-XX）
- **产出**：
  1. **音效系统实现**：
     - ✅ 创建了 `soundManager.ts` 音效管理器，支持音效的加载、播放、音量控制
     - ✅ 实现了发牌音效（`draw` 函数触发）
     - ✅ 实现了出牌音效（`playCard` 函数触发）
     - ✅ 实现了攻击音效和受击音效（攻击逻辑中触发）
     - ✅ 实现了胜利/失败音效（战斗结束时触发）
     - ✅ 音效管理器支持音量控制和开关，设置保存在本地存储
  2. **受击反馈效果实现**：
     - ✅ 在 `BattleField.vue` 中添加了受击视觉反馈系统
     - ✅ 敌人受击时显示闪烁效果和伤害数字
     - ✅ 我方角色受击时显示闪烁效果和伤害数字
     - ✅ 玩家本体和敌人本体受击时显示震动效果和伤害数字
     - ✅ 使用事件系统（CustomEvent）实现组件间通信
  3. **创建文件**：
     - `frontend/src/utils/soundManager.ts`：音效管理器
     - `frontend/public/sounds/README.md`：音效文件说明
- **功能说明**：
  1. **音效系统**：
     - 支持 7 种音效类型：发牌、出牌、攻击、受击、治疗、胜利、失败
     - 音效文件需要放在 `public/sounds/` 目录下
     - 如果音效文件不存在，系统会静默处理，不影响游戏运行
     - 音效设置（开关、音量）保存在本地存储
  2. **受击反馈**：
     - 角色受击时会有闪烁动画（亮度变化 + 缩放）
     - 显示伤害数字，向上浮动并逐渐消失
     - 玩家/敌人本体受击时会有震动效果
     - 伤害数字颜色：我方角色（红色）、敌方角色（黄色）
- **使用方式**：
  - 音效文件：将音效文件放在 `frontend/public/sounds/` 目录下，文件名需与 `soundManager.ts` 中配置的一致
  - 音效控制：可以通过 `soundManager.setEnabled()` 和 `soundManager.setMasterVolume()` 控制音效
  - 受击反馈：自动触发，无需手动调用
- **改进建议**：
  1. 后续可以添加音效设置界面，让玩家可以调整音效开关和音量
  2. 可以添加更多音效类型（如技能释放、装备穿戴等）
  3. 可以添加受击反馈的强度设置（轻度/中度/重度）

## ♻️ 治疗效果系统可行性分析（2025-01-XX）
- **产出**：
  1. **治疗效果系统可行性分析报告**：
     - ✅ 数据库结构完全支持：`cards` 表的 `effect_payload` 字段存储法术治疗效果，`card_character_traits` 表的 `effect_payload` 和 `scaling_payload` 字段存储角色特性治疗效果
     - ✅ 后端代码已实现：`CardEffectService` 完整实现了治疗效果执行逻辑，包括单体治疗、群体治疗、角色特性治疗等
     - ✅ 战斗系统已集成：`DungeonService` 已在战斗中使用治疗效果，法术卡牌和角色特性的治疗效果都能正常执行
     - ⚠️ **装备效果待完善**：装备卡的 `effect_payload` 和 `stat_modifiers` 在战斗中没有被应用，需要集成
  2. **治疗效果实现方式**：
     - ✅ **法术卡牌治疗**：玩家主动使用法术卡牌，系统读取 `cards.effect_payload` 执行治疗效果
     - ✅ **角色特性治疗**：每回合开始时自动触发，系统读取 `card_character_traits.effect_payload` 并根据星级缩放计算效果
     - ⚠️ **装备卡牌治疗**：装备的持续效果（如 `regen`）和属性加成（`stat_modifiers`）需要在战斗中使用
  3. **创建文件**：
     - `治疗效果系统可行性分析报告.md`：详细的分析报告，包含数据库结构、代码实现、实现方式、完整性评估和改进建议
- **结论**：
  - ✅ **完全可行**：数据库和项目架构完全支持治疗效果实现
  - ✅ **核心功能已完成**：法术和角色特性的治疗效果已实现并在战斗中使用
  - ⚠️ **需要完善**：装备效果需要在战斗系统中集成
- **改进建议**：
  1. **短期（1-2周）**：完善装备效果在战斗中的应用，添加装备效果的测试用例
  2. **中期（1个月）**：如果支持多角色，扩展治疗目标系统
  3. **长期（可选）**：添加治疗效果的可视化（前端动画），支持治疗效果的条件触发

---

**最后更新**: 2024年 | **维护者**: 项目团队
