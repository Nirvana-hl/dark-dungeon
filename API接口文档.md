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

## 📊 统计模块

### 获取成就列表
```
GET /achievements
Authorization: Bearer {token}
```

### 获取游戏统计
```
GET /game-metrics
Authorization: Bearer {token}
```

---

## 🔄 接口更新日志

### 2025-01-XX
- 初始版本，定义基础接口规范

---

**最后更新**: 2025-01-XX | **维护者**: 后端开发团队

