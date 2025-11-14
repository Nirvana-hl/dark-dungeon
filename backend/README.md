# 暗黑地牢肉鸽 - 后端服务

## 技术栈

- **Spring Boot 2.7.18** - Java 企业级应用框架
- **MyBatis Plus 3.5.3** - MyBatis 增强工具
- **Spring Security** - 安全框架
- **JWT** - JSON Web Token 认证
- **H2 Database** - 内存数据库（开发环境）

## 项目结构

```
backend/
├── src/main/java/com/dungeon/
│   ├── config/              # 配置类
│   │   ├── CorsConfig.java         # 跨域配置
│   │   ├── JwtConfig.java          # JWT 配置
│   │   └── SecurityConfig.java     # Spring Security 配置
│   ├── controller/          # REST 控制器
│   │   ├── AuthController.java     # 认证控制器
│   │   ├── CharacterController.java # 角色控制器
│   │   ├── WalletController.java   # 钱包控制器
│   │   └── GameController.java     # 游戏控制器
│   ├── service/             # 业务逻辑层
│   │   ├── AuthService.java
│   │   ├── CharacterService.java
│   │   ├── WalletService.java
│   │   └── GameService.java
│   ├── mapper/              # MyBatis Plus 映射器
│   │   ├── UserMapper.java
│   │   ├── CharacterMapper.java
│   │   └── ...
│   ├── entity/              # 实体类
│   │   ├── User.java
│   │   ├── Character.java
│   │   └── ...
│   ├── dto/                 # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── ...
│   ├── common/              # 通用类
│   │   └── Result.java      # 统一响应结果
│   └── util/                # 工具类
│       └── JwtUtil.java     # JWT 工具
└── src/main/resources/
    ├── application.yml      # 应用配置
    └── db/
        ├── schema.sql       # 数据库表结构
        └── data.sql         # 初始化数据
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+

### 2. 运行项目

```bash
# 进入后端目录
cd backend

# 使用 Maven 运行
mvn spring-boot:run

# 或者先编译再运行
mvn clean package
java -jar target/dark-dungeon-backend-1.0.0.jar
```

### 3. 访问地址

- **API 基础地址**: http://localhost:8080/api
- **H2 控制台**: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:file:./data/dungeon`
  - 用户名: `sa`
  - 密码: (空)

## API 接口文档

### 认证接口

#### 用户注册
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "123456"
}
```

#### 用户登录
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "123456"
}
```

响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### 角色接口

#### 获取所有角色
```
GET /api/characters
Authorization: Bearer {token}
```

#### 创建角色
```
POST /api/characters
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "新角色",
  "classType": "战士",
  "stars": 1,
  "hp": 60,
  "mp": 30
}
```

#### 删除角色
```
DELETE /api/characters/{id}
Authorization: Bearer {token}
```

#### 角色升星
```
POST /api/characters/{id}/star-up
Authorization: Bearer {token}
```

### 钱包接口

#### 获取金币
```
GET /api/wallet/gold
Authorization: Bearer {token}
```

#### 消费金币
```
POST /api/wallet/spend
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 100
}
```

#### 增加金币
```
POST /api/wallet/add
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 50
}
```

### 游戏接口

#### 获取用户卡牌
```
GET /api/game/cards
Authorization: Bearer {token}
```

#### 获取敌方卡牌
```
GET /api/game/enemy-cards?stage=1&difficulty=普通
Authorization: Bearer {token}
```

#### 获取角色特性
```
GET /api/game/character-traits
Authorization: Bearer {token}
```

## 数据库配置

### 开发环境（H2）

项目默认使用 H2 内存数据库，数据存储在 `./data/dungeon.mv.db` 文件中。

### 生产环境（MySQL/PostgreSQL）

修改 `application.yml`：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dungeon?useUnicode=true&characterEncoding=utf8
    username: root
    password: yourpassword
```

## 注意事项

1. **JWT Secret**: 生产环境请修改 `application.yml` 中的 `jwt.secret`
2. **跨域配置**: 前端地址在 `application.yml` 的 `cors.allowed-origins` 中配置
3. **数据库初始化**: 首次运行会自动创建数据库表结构（通过 `schema.sql`）

## 开发建议

1. 使用 IDE（如 IntelliJ IDEA）导入 Maven 项目
2. 确保 JDK 版本为 1.8 或更高
3. 运行前检查端口 8080 是否被占用
4. 前端开发时确保后端服务已启动

