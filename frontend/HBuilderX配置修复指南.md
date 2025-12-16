# HBuilderX 配置修复指南

## 🚨 发现的关键问题

### 问题：项目被识别为 Vite 项目而非 uni-app 项目

**原因分析**：
1. ✅ 项目根目录存在 `vite.config.ts` - 这会让 HBuilderX 误判为 Vite 项目
2. ✅ 项目根目录存在 `index.html` - 这也是 Vite 项目的特征
3. ✅ `package.json` 中只有 Vite 相关脚本，没有 uni-app 相关脚本

**结果**：HBuilderX 使用 Vite 编译（生成 `index.html`），而不是 uni-app 编译（生成 `app.json`）

---

## 🔧 修复方案

### 方案 1：重命名/移动 Vite 配置文件（推荐）

**步骤**：

1. **重命名 `vite.config.ts`**：
   - 将 `frontend/vite.config.ts` 重命名为 `frontend/vite.config.ts.bak`
   - 或者移动到其他目录（如 `frontend/config/vite.config.ts`）

2. **重命名/删除 `index.html`**：
   - 将 `frontend/index.html` 重命名为 `frontend/index.html.bak`
   - 或者移动到其他目录
   - uni-app 会自动生成 `index.html`，不需要手动创建

3. **重新编译**：
   - 删除 `frontend/unpackage` 目录
   - 在 HBuilderX 中重新运行到微信小程序

### 方案 2：创建 HBuilderX 项目配置文件

如果不想删除文件，可以创建 `.hbuilderx` 配置文件来明确告诉 HBuilderX 这是 uni-app 项目。

---

## 📋 需要修改的配置项

根据你提供的截图，需要检查以下设置：

### 1. 运行设置（最重要！）

**位置**：`运行` → `运行设置`

**需要检查的项**：

- ❌ **"外部web服务器调用url"** - 必须为空
  - 如果填写了 URL，HBuilderX 会使用外部服务器而不是编译小程序
  
- ❌ **"保存自动刷新浏览器(仅内置服务器)"** - 可以取消勾选
  - 这是 H5 预览的选项，小程序不需要

- ✅ **"微信开发者工具路径"** - 已正确配置
  - `C:\Program Files (x86)\Tencent\微信web开发者工具\`

### 2. 项目类型识别

**检查方法**：
- 在 HBuilderX 左侧项目树中，项目名称旁边应该显示 "uni-app" 标识
- 如果没有，说明项目类型识别错误

---

## 🎯 立即执行的修复步骤

### 步骤 1：重命名 Vite 配置文件

```bash
# 在 frontend 目录下执行
mv vite.config.ts vite.config.ts.bak
mv index.html index.html.bak
```

### 步骤 2：清理编译输出

```bash
# 删除编译输出目录
rm -rf unpackage
```

### 步骤 3：检查运行设置

1. 打开 HBuilderX
2. 菜单：`运行` → `运行设置`
3. 确认：
   - ✅ "外部web服务器调用url" 为空
   - ✅ "外部web服务器url是否包括项目名称" 可以取消勾选
   - ✅ 其他设置保持默认

### 步骤 4：重新编译

1. 关闭所有微信开发者工具
2. 在 HBuilderX 中：`运行` → `运行到小程序模拟器` → `微信开发者工具`
3. 等待编译完成
4. 检查 `unpackage/dist/dev/mp-weixin/` 目录是否生成了 `app.json`

---

## 🔍 验证修复是否成功

编译成功后，`unpackage/dist/dev/mp-weixin/` 目录应该包含：

- ✅ `app.json` - 小程序配置文件（必须存在）
- ✅ `app.js` - 小程序入口文件
- ✅ `app.wxss` - 小程序全局样式
- ✅ `pages/` - 页面目录
- ✅ `static/` - 静态资源目录
- ❌ `index.html` - 不应该存在（这是 H5 产物）

---

## 📝 注意事项

1. **保留备份**：
   - 重命名文件而不是删除，方便以后恢复
   - 如果项目需要同时支持 H5 和小程序，可以考虑使用条件编译

2. **package.json**：
   - 当前 `package.json` 中的脚本都是 Vite 相关的
   - uni-app 项目不需要这些脚本（HBuilderX 会自动处理）
   - 但保留它们不影响小程序编译

3. **vite.config.ts**：
   - 如果项目需要同时支持 H5 预览，可以保留但重命名
   - 或者使用条件判断，只在 H5 模式下生效

---

## 🆘 如果修复后仍无 app.json

如果按照上述步骤修复后仍然没有生成 `app.json`，请检查：

1. **HBuilderX 版本**：
   - 建议使用最新版本（3.9+）
   - 旧版本可能不支持 Vue 3 的 uni-app

2. **uni-app 插件**：
   - 确认已安装 uni-app 相关插件
   - 菜单：`工具` → `插件安装`

3. **项目结构**：
   - 确认 `manifest.json` 存在且配置正确
   - 确认 `pages.json` 存在且配置正确
   - 确认 `App.vue` 存在

4. **编译日志**：
   - 查看 HBuilderX 控制台的完整输出
   - 查找是否有错误或警告信息

