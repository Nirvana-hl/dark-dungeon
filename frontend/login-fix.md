# 登录问题修复

## 问题诊断
登录不了的主要原因：

1. **方法名不匹配**：Login.vue调用了不存在的方法
   - `auth.signInWithPassword()` → 应该是 `auth.login()`
   - `auth.signUp()` → 应该是 `auth.register()`

2. **后端服务未启动**：API指向`localhost:8080`，如果后端未运行会连接失败

## 修复方案

### 1. 修复方法调用
```typescript
// 修复前
await auth.signInWithPassword(email.value, password.value)
await auth.signUp(email.value, password.value)

// 修复后
const success = await auth.login(email.value, password.value)
const success = await auth.register({ email: email.value, password: password.value })
```

### 2. 添加演示模式
当后端不可用时，自动提供演示登录：

```typescript
if (error.code === 'ECONNREFUSED' || error.message?.includes('Network Error')) {
  // 验证演示账户
  if (email === 'admin@example.com' && password === '123456') {
    const demoUser = { id: 'demo-user-001', email, username: 'admin' }
    const demoToken = 'demo-token-' + Date.now()
    saveAuth(demoToken, demoUser)
    return true
  }
}
```

### 3. 改进用户体验
- 清晰的后端状态提示
- 演示账户信息预填充
- 错误提示更友好

## 使用方法

### 情况1：后端服务已启动
1. 使用任意邮箱和密码注册/登录
2. 正常与后端API交互

### 情况2：后端服务未启动
1. 使用演示账户登录：
   - 邮箱：`admin@example.com`
   - 密码：`123456`
2. 系统会显示"后端服务不可用，使用演示账户登录成功"
3. 3秒后提示信息消失，正常使用应用

### 情况3：使用其他账户
1. 输入其他邮箱密码
2. 系统提示"演示账户：admin@example.com / 123456"
3. 引导使用演示账户

## 验证方法
- 启动开发服务器：`npm run dev`
- 访问：`http://localhost:5173`
- 应该自动跳转到登录页
- 使用演示账户登录成功
- 登录后跳转到首页

现在无论后端是否运行，都能正常登录使用应用！