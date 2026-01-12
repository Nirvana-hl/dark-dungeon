# 暗黑地牢肉鸽游戏部署文档

## 📋 文档概述

本文档提供暗黑地牢肉鸽游戏项目的完整部署指南，包括开发环境和生产环境的部署流程。

## 🏗️ 系统架构

- **前端**: Vue 3 + TypeScript + Vite + UnoCSS
- **后端**: Spring Boot 2.7 + MyBatis Plus + JWT认证
- **数据库**: MySQL 8.0+ (生产环境) / H2 (开发环境)
- **通信**: RESTful API + WebSocket (计划中)

## 📋 环境要求

### 最低系统要求

| 组件 | 版本要求 | 推荐版本 |
|------|----------|----------|
| JDK | 17+ | OpenJDK 17 |
| Node.js | 16+ | Node.js 18 LTS |
| MySQL | 5.7+ | MySQL 8.0 |
| Maven | 3.6+ | Maven 3.8+ |
| 内存 | 4GB+ | 8GB+ |
| 磁盘空间 | 2GB+ | 5GB+ |

### 操作系统支持

- **Windows**: 10/11 (推荐)
- **macOS**: 10.15+ (推荐)
- **Linux**: Ubuntu 18.04+ / CentOS 7+

## 🔧 前置条件安装

### 1. Java 17 安装

#### Windows
```bash
# 下载并安装 OpenJDK 17
# https://adoptium.net/temurin/releases/

# 验证安装
java -version
javac -version
```

#### Linux (Ubuntu/Debian)
```bash
# 添加仓库
sudo apt update
sudo apt install openjdk-17-jdk

# 验证安装
java -version
javac -version
```

#### macOS
```bash
# 使用 Homebrew
brew install openjdk@17

# 设置环境变量
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/usr/local/opt/openjdk@17"' >> ~/.zshrc
source ~/.zshrc

# 验证安装
java -version
javac -version
```

### 2. Node.js 18 安装

#### Windows
```bash
# 下载并安装 Node.js 18 LTS
# https://nodejs.org/

# 验证安装
node --version
npm --version
```

#### 使用 nvm (推荐)
```bash
# Windows
# 下载 nvm-windows: https://github.com/coreybutler/nvm-windows

nvm install 18
nvm use 18

# Linux/macOS
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc

nvm install 18
nvm use 18
```

### 3. Maven 安装

#### Windows
```bash
# 下载并解压 Maven
# https://maven.apache.org/download.cgi

# 配置环境变量 MAVEN_HOME
# 将 %MAVEN_HOME%\bin 添加到 PATH

# 验证安装
mvn -version
```

#### Linux/macOS
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# macOS
brew install maven

# 验证安装
mvn -version
```

### 4. MySQL 8.0 安装

#### Windows
```bash
# 下载并安装 MySQL 8.0
# https://dev.mysql.com/downloads/mysql/

# 启动 MySQL 服务
net start mysql

# 安全配置
mysql_secure_installation
```

#### Linux (Ubuntu)
```bash
# 安装 MySQL
sudo apt update
sudo apt install mysql-server-8.0

# 启动服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

#### macOS
```bash
# 使用 Homebrew
brew install mysql
brew services start mysql

# 安全配置
mysql_secure_installation
```

### 5. Git 安装

```bash
# Windows: 下载安装包 https://git-scm.com/

# Linux
sudo apt install git  # Ubuntu/Debian
sudo yum install git  # CentOS/RHEL

# macOS
brew install git

# 验证安装
git --version
```

## 🗄️ 数据库设置

### 1. 创建数据库

```sql
-- 登录 MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE dark_dungeon CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'dungeon_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON dark_dungeon.* TO 'dungeon_user'@'localhost';
FLUSH PRIVILEGES;

-- 退出
EXIT;
```

### 2. 初始化数据库

```bash
# 进入项目根目录
cd dark-dungeon

# 导入数据库结构和初始数据
mysql -u root -p dark_dungeon < dark_dungeon.sql
```

### 3. 验证数据库

```sql
-- 连接数据库
mysql -u root -p dark_dungeon

-- 检查表是否创建成功
SHOW TABLES;

-- 检查数据
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM card_characters;
SELECT COUNT(*) FROM skills;
```

## 🚀 开发环境部署

### 快速启动脚本

#### Windows
```batch
@echo off
echo 启动暗黑地牢游戏开发环境...

cd backend
start cmd /k "mvn spring-boot:run"

cd ../frontend
start cmd /k "npm install && npm run dev"

echo 服务启动中...
echo 前端: http://localhost:5173
echo 后端: http://localhost:8080
echo H2控制台: http://localhost:8080/h2-console
```

#### Linux/macOS
```bash
#!/bin/bash
echo "启动暗黑地牢游戏开发环境..."

# 启动后端
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# 启动前端
cd ../frontend
npm install
npm run dev &
FRONTEND_PID=$!

echo "服务启动中..."
echo "前端: http://localhost:5173"
echo "后端: http://localhost:8080"
echo "H2控制台: http://localhost:8080/h2-console"

# 等待用户输入停止服务
read -p "按Enter键停止服务..."
kill $BACKEND_PID $FRONTEND_PID
```

### 手动部署步骤

#### 1. 克隆项目

```bash
git clone <repository-url>
cd dark-dungeon
```

#### 2. 后端部署

```bash
# 进入后端目录
cd backend

# 编译项目
mvn clean compile

# 运行应用
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

#### 3. 前端部署

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

#### 4. 验证部署

打开浏览器访问：
- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8080/api
- **H2控制台**: http://localhost:8080/h2-console (开发环境)

## 🏭 生产环境部署

### 1. 后端生产部署

#### 方式一：使用 Maven 打包

```bash
cd backend

# 清理并打包
mvn clean package -DskipTests

# 运行 JAR 包
java -jar target/dark-dungeon-backend-1.0.0.jar
```

#### 方式二：使用 Docker (推荐)

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/dark-dungeon-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# 构建镜像
docker build -t dark-dungeon-backend .

# 运行容器
docker run -d \
  --name dark-dungeon-backend \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -v /path/to/logs:/app/logs \
  dark-dungeon-backend
```

### 2. 前端生产部署

```bash
cd frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# 预览构建结果（可选）
npm run preview
```

#### Nginx 配置

```nginx
# /etc/nginx/sites-available/dark-dungeon

server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    root /path/to/frontend/dist;
    index index.html;

    # API 代理
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 代理（如果使用）
    location /ws/ {
        proxy_pass http://localhost:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # 处理 Vue Router 历史模式
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

```bash
# 启用站点
sudo ln -s /etc/nginx/sites-available/dark-dungeon /etc/nginx/sites-enabled/

# 测试配置
sudo nginx -t

# 重启 Nginx
sudo systemctl restart nginx
```

### 3. SSL 配置 (可选)

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取免费证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo crontab -e
# 添加：0 12 * * * /usr/bin/certbot renew --quiet
```

### 4. 生产环境配置

#### 后端生产配置

创建 `application-prod.yml`：

```yaml
server:
  port: 8080

spring:
  profiles:
    active: prod

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/dark_dungeon?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: dungeon_user
    password: your_secure_password

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl

jwt:
  secret: your-very-secure-jwt-secret-key-change-this-in-production-2024
  expiration: 86400000

cors:
  allowed-origins: https://your-domain.com

logging:
  level:
    com.dungeon: INFO
    org.springframework.security: DEBUG
  file:
    name: logs/dark-dungeon.log
  logback:
    rollingpolicy:
      max-file-size: 10MB
      max-history: 30
```

#### 环境变量

```bash
# 创建环境变量文件
cat > .env << EOF
DB_HOST=localhost
DB_PORT=3306
DB_NAME=dark_dungeon
DB_USER=dungeon_user
DB_PASS=your_secure_password
JWT_SECRET=your-very-secure-jwt-secret-key
EOF
```

### 5. 系统服务配置

#### 创建 Systemd 服务

```ini
# /etc/systemd/system/dark-dungeon-backend.service

[Unit]
Description=Dark Dungeon Backend Service
After=network.target mysql.service

[Service]
Type=simple
User=www-data
WorkingDirectory=/path/to/dark-dungeon/backend
ExecStart=/usr/bin/java -jar target/dark-dungeon-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

```bash
# 重新加载服务
sudo systemctl daemon-reload

# 启动服务
sudo systemctl start dark-dungeon-backend

# 设置开机自启
sudo systemctl enable dark-dungeon-backend

# 查看状态
sudo systemctl status dark-dungeon-backend

# 查看日志
sudo journalctl -u dark-dungeon-backend -f
```

## 🔍 部署验证

### 1. 健康检查

```bash
# 检查后端服务
curl -f http://localhost:8080/api/health || echo "后端服务异常"

# 检查前端服务
curl -f http://localhost:5173 || echo "前端服务异常"
```

### 2. API 测试

```bash
# 测试用户注册接口
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"123456"}'

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"123456"}'
```

### 3. 数据库连接测试

```sql
-- 连接数据库
mysql -u dungeon_user -p dark_dungeon

-- 执行测试查询
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as card_count FROM card_characters;
SELECT COUNT(*) as skill_count FROM skills;
```

## 🐛 常见问题排查

### 1. 端口占用

```bash
# 检查端口占用
netstat -tulpn | grep :8080
netstat -tulpn | grep :5173

# 杀死进程
kill -9 <PID>
# 或 Windows: taskkill /PID <PID> /F
```

### 2. 数据库连接失败

```bash
# 检查 MySQL 服务状态
sudo systemctl status mysql

# 检查数据库是否存在
mysql -u root -p -e "SHOW DATABASES;"

# 检查用户权限
mysql -u root -p -e "SELECT User, Host FROM mysql.user WHERE User='dungeon_user';"
```

### 3. 前端构建失败

```bash
# 清理缓存
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force
npm install

# 检查 Node.js 版本
node --version
npm --version
```

### 4. 后端启动失败

```bash
# 检查 JDK 版本
java -version

# 检查 Maven 版本
mvn -version

# 清理 Maven 缓存
mvn clean
rm -rf ~/.m2/repository/com/dungeon

# 重新编译
mvn clean compile
```

### 5. 跨域问题

检查 `application.yml` 中的 CORS 配置：

```yaml
cors:
  allowed-origins: http://localhost:5173  # 开发环境
  # 或生产环境：https://your-domain.com
```

### 6. 内存不足

```bash
# 检查系统内存
free -h  # Linux
systeminfo | findstr Memory  # Windows

# 增加 JVM 内存
java -Xmx2g -Xms1g -jar target/dark-dungeon-backend-1.0.0.jar
```

### 7. 防火墙问题

```bash
# Linux 开放端口
sudo ufw allow 8080
sudo ufw allow 5173

# Windows 防火墙
# 控制面板 > 系统和安全 > Windows Defender 防火墙 > 高级设置
# 入站规则 > 新建规则 > 端口 > TCP 8080, 5173
```

## 📊 监控和维护

### 1. 日志管理

```bash
# 查看应用日志
tail -f backend/logs/dark-dungeon.log

# 日志轮转配置 (logback-spring.xml)
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/dark-dungeon.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/dark-dungeon.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>10MB</maxFileSize>
        <maxHistory>30</maxHistory>
        <totalSizeCap>1GB</totalSizeCap>
    </rollingPolicy>
</appender>
```

### 2. 性能监控

```yaml
# 启用 Spring Boot Actuator
dependencies:
  - spring-boot-starter-actuator

# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### 3. 备份策略

```bash
# 数据库备份脚本
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u dungeon_user -p dark_dungeon > backup_${DATE}.sql

# 自动备份 (crontab)
0 2 * * * /path/to/backup.sh
```

### 4. 更新部署

```bash
# 停止服务
sudo systemctl stop dark-dungeon-backend

# 备份当前版本
cp -r backend backend_backup_$(date +%Y%m%d)

# 更新代码
git pull origin main

# 重新构建
cd backend
mvn clean package -DskipTests

# 启动服务
sudo systemctl start dark-dungeon-backend

# 验证服务
curl -f http://localhost:8080/api/health
```

## 📞 技术支持

如果在部署过程中遇到问题，请：

1. 检查本文档的常见问题部分
2. 查看项目 GitHub Issues
3. 联系技术支持团队

## 📝 版本信息

- **文档版本**: 1.0.0
- **最后更新**: 2025-01-12
- **适用项目版本**: v1.0.0

---

**部署完成清单**:
- [ ] 环境依赖安装完成
- [ ] 数据库配置完成
- [ ] 后端服务启动成功
- [ ] 前端应用部署完成
- [ ] 功能验证通过
- [ ] 监控配置完成
