# 暗黑地牢肉鸽 - API接口文档

> 最后更新：2025-01-XX  
> 基础URL：`http://localhost:8080/api`

---

## 📋 接口说明

### 统一响应格式
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 错误码说明
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权（需要登录）
- `403`: 无权限
- `404`: 资源不存在
- `500`: 服务器内部错误

### 认证方式
需要认证的接口，在请求头中添加：
```
Authorization: Bearer {token}
```

---

## 🔐 认证模块

### 用户注册
```
POST /auth/register
Content-Type: application/json
```

**请求体**：
```json
{
  "accountName": "testuser",
  "email": "test@example.com",
  "password": "123456"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "uuid-string",
    "accountName": "testuser",
    "email": "test@example.com"
  }
}
```

### 用户登录
```
POST /auth/login
Content-Type: application/json
```

**请求体**：
```json
{
  "email": "test@example.com",
  "password": "123456"
}
```

**响应**：同注册接口

---

## 👤 用户与角色模块

### 获取玩家角色模板列表
```
GET /player-characters
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "code": "warrior",
      "name": "战士",
      "baseHp": 100,
      "hpPerLevel": 10,
      "lore": "背景故事..."
    }
  ]
}
```

### 获取当前用户的角色实例
```
GET /user-player-characters
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": "uuid",
    "playerCharacterId": "uuid",
    "playerCharacterName": "战士",
    "maxHp": 110,
    "currentHp": 80,
    "maxActionPoints": 3,
    "currentActionPoints": 3,
    "currentStress": 20,
    "stressLevel": 1,
    "stressDebuffs": []
  }
}
```

### 创建角色实例
```
POST /user-player-characters
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "playerCharacterId": "uuid"
}
```

### 更新角色状态
```
PUT /user-player-characters/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "currentHp": 90,
  "currentActionPoints": 2,
  "currentStress": 25
}
```

---

## 🃏 卡牌模块

### 获取卡牌角色模板列表
```
GET /card-characters
```

**查询参数**：
- `class` (可选): 职业筛选
- `faction` (可选): 阵营筛选
- `rarity` (可选): 稀有度筛选

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "code": "priest_001",
      "name": "祭司",
      "class": "priest",
      "faction": "divine",
      "rarity": "rare",
      "baseHp": 60,
      "baseAttack": 20,
      "actionPointCost": 2,
      "baseStarLevel": 1,
      "maxStarLevel": 5,
      "shopPrice": 1000
    }
  ]
}
```

### 获取卡牌角色的特性列表
```
GET /card-characters/{id}/traits
```

**路径参数**：
- `id`: 卡牌角色ID

**响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "cardCharacterId": 1,
      "name": "星辉祝福",
      "type": "positive",
      "effectPayload": "{\"heal_allies\": 2}",
      "scalingPayload": "{\"2\": {\"heal_allies\": 3}, \"3\": {\"heal_allies\": 4}, \"4\": {\"heal_allies\": 5}}",
      "description": "提升全队治疗量"
    }
  ]
}
```

**说明**：
- `effectPayload`: 基础效果参数（JSON字符串），例如 `{"heal_allies": 2}` 表示基础治疗友方2点生命
- `scalingPayload`: 星级缩放配置（JSON字符串），例如 `{"2": {"heal_allies": 3}}` 表示2星时治疗量变为3
- 前端需要解析这两个JSON字段，并根据角色的当前星级计算实际效果值

### 获取用户持有的卡牌角色
```
GET /user-card-characters
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "cardCharacterId": "uuid",
      "cardCharacterName": "祭司",
      "currentHp": 60,
      "currentArmor": 0,
      "isDeployed": false,
      "currentStarLevel": 1
    }
  ]
}
```

### 获取卡牌模板列表（法术/装备）
```
GET /cards
```

**查询参数**：
- `cardType` (可选): spell | equipment
- `rarity` (可选): common | rare | epic | legendary

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "code": "fireball",
      "name": "火球术",
      "cardType": "spell",
      "rarity": "common",
      "actionPointCost": 2,
      "statModifiers": {},
      "effectPayload": {
        "damage": 30,
        "target": "enemy"
      }
    }
  ]
}
```

### 获取用户手牌
```
GET /user-cards
Authorization: Bearer {token}
```

**查询参数**：
- `cardType` (可选): 卡牌类型筛选
- `equipped` (可选): true | false - 是否已装备

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "cardId": "uuid",
      "cardName": "火球术",
      "cardType": "spell",
      "quantity": 1,
      "level": 1,
      "equippedToUserCardCharacterId": null,
      "acquiredAt": "2025-01-15T10:30:00Z"
    }
  ]
}
```

### 装备卡牌
```
POST /user-cards/{id}/equip
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "userCardCharacterId": "uuid"  // 装备到哪个卡牌角色上（仅装备类卡牌需要）
}
```

### 卸下卡牌
```
POST /user-cards/{id}/unequip
Authorization: Bearer {token}
```

---

## 🏕️ 营地模块

### 获取营地仪表盘数据（聚合接口）
```
GET /camp/dashboard
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userPlayerCharacter": {
      "id": "uuid",
      "playerCharacterName": "战士",
      "maxHp": 110,
      "currentHp": 80,
      "currentActionPoints": 3,
      "currentStress": 20,
      "stressLevel": 1
    },
    "userCardCharacters": [
      {
        "id": "uuid",
        "name": "祭司",
        "currentStarLevel": 1,
        "isDeployed": false
      }
    ],
    "userCards": [
      {
        "id": "uuid",
        "name": "火球术",
        "cardType": "spell",
        "equipped": false
      }
    ],
    "inventory": [
      {
        "id": "uuid",
        "itemName": "治疗药水",
        "quantity": 5
      }
    ],
    "wallet": {
      "gold": 1000,
      "soulstone": 50
    },
    "shopOffers": [
      {
        "id": "uuid",
        "offerType": "card",
        "targetName": "祭司",
        "price": 1000
      }
    ]
  }
}
```

---

## 💰 钱包模块

### 获取钱包余额
```
GET /wallet
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "gold": 1000,
    "soulstone": 50
  }
}
```

### 消费货币
```
POST /wallet/spend
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "currencyType": "gold",
  "amount": 100
}
```

### 增加货币
```
POST /wallet/add
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "currencyType": "gold",
  "amount": 50
}
```

---

## 🛒 商城模块

### 获取商城商品列表
```
GET /shop/offers
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "offerType": "card",
      "targetId": "uuid",
      "targetName": "祭司",
      "price": 1000,
      "currencyType": "gold"
    }
  ]
}
```

### 购买商品
```
POST /shop/purchase
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "offerId": "uuid"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "购买成功",
  "data": {
    "purchasedItem": {
      "type": "card",
      "id": "uuid",
      "name": "祭司"
    },
    "remainingBalance": 0
  }
}
```

---

## 🎒 背包模块

### 获取用户背包
```
GET /inventory
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "itemId": "uuid",
      "itemName": "治疗药水",
      "itemType": "consumable",
      "quantity": 5,
      "bindStatus": "unbound"
    }
  ]
}
```

### 使用道具
```
POST /inventory/{id}/use
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "targetId": "uuid"  // 可选，使用目标（如角色ID）
}
```

---

## ⚔️ 技能模块

### 获取职业技能树
```
GET /skills/{playerCharacterCode}
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "code": "battle_focus",
      "name": "战斗专注",
      "description": "提升攻击力",
      "requiredLevel": 5,
      "positionInTree": {
        "row": 1,
        "column": 1
      },
      "unlockPath": [],
      "unlocked": false
    }
  ]
}
```

### 获取用户已解锁技能
```
GET /user-skills
Authorization: Bearer {token}
```

### 解锁技能
```
POST /user-skills/unlock
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "skillId": "uuid"
}
```

---

## 🗺️ 地牢探索模块

### 获取地牢列表
```
GET /dungeons
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": "uuid",
      "name": "黑暗森林",
      "difficulty": "normal",
      "recommendedCards": ["uuid1", "uuid2"]
    }
  ]
}
```

### 开始探索
```
POST /runs/start
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "dungeonId": "uuid",
  "selectedCardCharacterIds": ["uuid1", "uuid2"],
  "selectedCardIds": ["uuid3", "uuid4"]
}
```

**响应**：
```json
{
  "code": 200,
  "message": "探索开始",
  "data": {
    "runId": "uuid",
    "dungeonName": "黑暗森林",
    "currentStage": 1
  }
}
```

### 结束探索
```
POST /runs/{runId}/end
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "result": "victory",  // victory | defeat | retreat
  "finalStage": 3,
  "rewards": {
    "gold": 500,
    "items": ["uuid1"]
  }
}
```

---

## 📊 成就与统计模块

### 获取成就列表
```
GET /achievements
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成就列表成功",
  "data": [
    {
      "id": 1,
      "name": "初出茅庐",
      "category": "progression",
      "description": "完成第1关",
      "requirements": "{\"stage_number\": 1, \"is_passed\": true}",
      "isCompleted": false,
      "progress": 0
    }
  ]
}
```

### 根据分类获取成就列表
```
GET /achievements/category/{category}
```

**路径参数**：
- `category`: 分类（progression-进度, mastery-精通, collection-收集, social-社交）

**响应**：同获取成就列表

### 根据ID获取成就详情
```
GET /achievements/{id}
```

**路径参数**：
- `id`: 成就ID

**响应**：
```json
{
  "code": 200,
  "message": "获取成就详情成功",
  "data": {
    "id": 1,
    "name": "初出茅庐",
    "category": "progression",
    "description": "完成第1关",
    "requirements": "{\"stage_number\": 1, \"is_passed\": true}",
    "isCompleted": false,
    "progress": 0
  }
}
```

### 搜索成就
```
GET /achievements/search?name={name}
```

**查询参数**：
- `name`: 成就名称（支持模糊查询）

**响应**：同获取成就列表

### 获取用户成就列表（带完成状态）
```
GET /achievements/user
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取用户成就列表成功",
  "data": [
    {
      "id": 1,
      "name": "初出茅庐",
      "category": "progression",
      "description": "完成第1关",
      "requirements": "{\"stage_number\": 1, \"is_passed\": true}",
      "isCompleted": true,
      "progress": 100
    }
  ]
}
```

### 根据分类获取用户成就列表
```
GET /achievements/user/category/{category}
Authorization: Bearer {token}
```

**路径参数**：
- `category`: 分类（progression-进度, mastery-精通, collection-收集, social-社交）

**响应**：同获取用户成就列表

### 获取用户成就统计
```
GET /achievements/user/stats
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "获取成就统计成功",
  "data": {
    "totalCount": 50,
    "completedCount": 12,
    "completionRate": 24.0
  }
}
```

---

## ⚔️ 地牢探索模块

### 获取地牢列表
```
GET /dungeons
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "name": "暗影洞穴",
      "difficulty": "normal",
      "theme": "cave",
      "recommendedCards": "[1,2,3]",
      "description": "充满陷阱和黑暗魔物的洞穴"
    }
  ]
}
```

### 开启探索
```
POST /dungeons/runs/start
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "stageNumber": 3,
  "userPlayerCharacterId": 12,
  "cardCharacterIds": [101, 102],
  "cardIds": [201, 202, 203],
  "consumableItemIds": [301],
  "notes": "携带治疗药水"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "探索已开启",
  "data": {
    "id": 88,
    "stageNumber": 3,
    "stageName": "迷雾密林",
    "difficulty": "normal",
    "progress": {
      "status": "exploring",
      "currentRoom": "Entrance",
      "exploredRooms": 0,
      "defeatedEnemies": 0
    }
  }
}
```

### 查询当前探索
```
GET /dungeons/runs/current
Authorization: Bearer {token}
```

若无探索，`data` 为 `null`，`message` 为“暂无进行中的探索”。

### 探索房间/触发事件
```
POST /dungeons/runs/{runId}/explore
Authorization: Bearer {token}
Content-Type: application/json (可选)
```

**请求体验例**：
```json
{
  "action": "explore",
  "choice": "left_path"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "触发事件：流浪商人",
  "data": {
    "run": { "...": "..." },
    "eventSummary": "流浪商人：可低价购买药剂 (效果: {\"reward\":{\"gold\":-50}})",
    "battlePending": false
  }
}
```

当遭遇敌人时，`battlePending=true`，前端需要调用战斗接口。

### 结算战斗
```
POST /dungeons/runs/{runId}/battle
Authorization: Bearer {token}
Content-Type: application/json (可选)
```

**请求体**：
```json
{
  "strategy": "aggressive"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "战斗已结算，结果：victory",
  "data": {
    "run": { "...": "..." },
    "battleResult": {
      "outcome": "victory",
      "enemyName": "亡灵骑士",
      "heroRemainingHp": 35,
      "enemyRemainingHp": 0,
      "battleLog": [
        "第1回合：玩家造成 18 点伤害。敌人剩余 62 HP。",
        "第1回合：敌人造成 12 点伤害。玩家剩余 48 HP。",
        "..."
      ]
    },
    "battlePending": false
  }
}
```

若玩家失败，接口会自动标记本次探索为 `defeat` 并更新 `user_stage_progress`。

### 结束探索
```
POST /dungeons/runs/{runId}/end
Authorization: Bearer {token}
Content-Type: application/json
```

**请求体**：
```json
{
  "result": "victory",
  "notes": "击败首领后直接退出",
  "rewardChoice": {
    "gold": 350,
    "experience": 120
  }
}
```

若 result=“victory”，后端会调用 `user_stage_progress.passStage` 并写入奖励快照；其他结果则记为尝试/失败。


## 🔄 接口更新日志

### 2025-12-04
- ✅ 新增卡牌角色特性查询接口
  - GET /card-characters/{id}/traits - 获取指定卡牌角色的特性列表
  - 返回特性信息，包括effectPayload（基础效果）和scalingPayload（星级缩放配置）
  - 用于前端动态解析和执行特性效果（如治疗、护盾等）

### 2025-01-XX
- ✅ 新增成就模块接口（AchievementController）
  - GET /achievements - 获取所有成就列表
  - GET /achievements/category/{category} - 根据分类获取成就
  - GET /achievements/{id} - 获取成就详情
  - GET /achievements/search - 搜索成就
  - GET /achievements/user - 获取当前登录用户的成就列表（带完成状态）
  - GET /achievements/user/category/{category} - 根据分类获取当前用户成就
  - GET /achievements/user/stats - 获取当前用户成就统计
- ✅ 新增用户成就关联表（user_achievements）
  - 支持记录用户成就完成状态、进度、完成时间、奖励领取状态
  - 遵循项目"模板/实例分离"设计原则
- ❌ 移除统计模块接口（GameMetricsController）
  - 后端不再提供 /game-metrics 相关API
- ✅ 新增地牢探索接口（DungeonController）
  - GET /dungeons - 地牢列表
  - POST /dungeons/runs/start - 开启探索
  - GET /dungeons/runs/current - 当前探索状态
  - POST /dungeons/runs/{runId}/explore - 探索/触发事件
  - POST /dungeons/runs/{runId}/battle - 结算战斗
  - POST /dungeons/runs/{runId}/end - 结束探索并结算奖励

### 2025-01-XX
- 初始版本，定义基础接口规范

---

**最后更新**: 2025-01-XX | **维护者**: 后端开发团队

