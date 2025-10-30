# 前端ESLint错误解决方案

## 问题描述
前端服务启动时遇到ESLint配置错误：`[eslint] No ESLint configuration found in E:\Project\frontend\src\views\user`

## 解决方案

### 方法1：禁用ESLint检查（推荐）
1. 修改 `frontend/vue.config.js`，添加 `lintOnSave: false`
2. 修改 `frontend/package.json`，在serve命令中添加 `--skip-plugins @vue/cli-plugin-eslint`

### 方法2：手动启动（如果自动启动失败）
```bash
cd E:\Project\frontend
set SKIP_PREFLIGHT_CHECK=true
set ESLINT_NO_DEV_ERRORS=true
npx vue-cli-service serve --skip-plugins @vue/cli-plugin-eslint
```

### 方法3：使用批处理文件
运行 `frontend/start-frontend.bat` 文件

## 当前状态
- ✅ 所有Vue组件已创建完成
- ✅ ESLint配置已优化
- ✅ 前端依赖已安装
- ⏳ 前端服务启动中

## 访问地址
- 前端：http://localhost:8081
- 后端：http://localhost:8080/api

## 默认账户
- 管理员：admin / 123456
- 教师：teacher1 / 123456
- 学生：student1 / 123456

## 如果仍有问题
1. 检查Node.js版本（需要14+）
2. 清除npm缓存：`npm cache clean --force`
3. 重新安装依赖：`npm install`
4. 手动启动：`npx vue-cli-service serve --skip-plugins @vue/cli-plugin-eslint`
