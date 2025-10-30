# 快速启动指南

## 问题解决

您遇到的 `'vue-cli-service' 不是内部或外部命令` 错误已经解决！

## 当前状态

✅ **前端服务已启动** - http://localhost:8081
⏳ **后端服务启动中** - 需要配置数据库

## 下一步操作

### 1. 配置数据库
1. 启动MySQL服务
2. 创建数据库：
```sql
CREATE DATABASE academic_management_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 执行初始化脚本：
```bash
mysql -u root -p academic_management_system < database/init.sql
```

### 2. 修改数据库配置
编辑 `backend/src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/academic_management_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: your_username  # 修改为您的MySQL用户名
    password: your_password  # 修改为您的MySQL密码
```

### 3. 启动后端服务
```bash
cd backend
mvn spring-boot:run
```

## 访问系统

- **前端地址**: http://localhost:8081
- **后端API**: http://localhost:8080/api

## 默认账户

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |
| 学生 | student1 | 123456 |

## 常见问题

1. **前端启动失败**: 运行 `npm install` 安装依赖
2. **后端启动失败**: 检查数据库连接配置
3. **端口被占用**: 修改配置文件中的端口号

现在您可以访问 http://localhost:8081 查看前端界面了！
