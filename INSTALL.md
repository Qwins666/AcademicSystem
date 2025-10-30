# 学术活动与科研成果管理系统安装指南

## 系统要求

### 开发环境
- **Java**: JDK 8 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 14 或更高版本
- **MySQL**: 8.0 或更高版本
- **Redis**: 6.0 或更高版本（可选，用于缓存）

### 操作系统
- Windows 10/11
- macOS 10.15+
- Ubuntu 18.04+

## 快速安装

### 1. 克隆项目
```bash
git clone <repository-url>
cd academic-management-system
```

### 2. 数据库配置
1. 启动MySQL服务
2. 创建数据库：
```sql
CREATE DATABASE academic_management_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 执行初始化脚本：
```bash
mysql -u root -p academic_management_system < database/init.sql
```

### 3. 配置修改
修改 `backend/src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/academic_management_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
```

### 4. 安装依赖

#### Windows用户
```bash
# 运行安装脚本
install.bat
```

#### Linux/macOS用户
```bash
# 安装后端依赖
cd backend
mvn clean install

# 安装前端依赖
cd ../frontend
npm install
```

### 5. 启动系统

#### Windows用户
```bash
# 启动系统
start.bat

# 停止系统
stop.bat
```

#### Linux/macOS用户
```bash
# 启动系统
./start.sh

# 停止系统
pkill -f "spring-boot:run"
pkill -f "npm run serve"
```

## 手动启动

### 启动后端服务
```bash
cd backend
mvn spring-boot:run
```

### 启动前端服务
```bash
cd frontend
npm run serve
```

## 访问系统

- **前端地址**: http://localhost:8081
- **后端API**: http://localhost:8080/api

## 默认账户

系统初始化后会创建以下默认账户：

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 系统管理员 |
| 教师 | teacher1 | 123456 | 示例教师账户 |
| 学生 | student1 | 123456 | 示例学生账户 |

## 常见问题

### 1. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库连接信息是否正确
- 检查防火墙设置

### 2. 端口被占用
- 后端默认端口：8080
- 前端默认端口：8081
- 可在配置文件中修改端口

### 3. 前端依赖安装失败
```bash
# 清除缓存重新安装
npm cache clean --force
npm install
```

### 4. 后端编译失败
```bash
# 清理重新编译
mvn clean
mvn compile
```

## 开发说明

### 项目结构
```
academic-management-system/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/
│   │   └── com/academic/
│   │       ├── controller/     # 控制器
│   │       ├── service/       # 服务层
│   │       ├── mapper/          # 数据访问层
│   │       ├── entity/          # 实体类
│   │       ├── common/          # 通用类
│   │       └── config/          # 配置类
│   └── src/main/resources/
├── frontend/               # 前端Vue项目
│   ├── src/
│   │   ├── views/          # 页面组件
│   │   ├── components/     # 通用组件
│   │   ├── api/            # API接口
│   │   ├── router/          # 路由配置
│   │   └── store/           # 状态管理
└── database/              # 数据库脚本
```

### 技术栈
- **后端**: Spring Boot + MyBatis-Plus + Spring Security + JWT
- **前端**: Vue 3 + Element Plus + Vue Router + Vuex
- **数据库**: MySQL 8.0
- **缓存**: Redis（可选）

## 联系支持

如有问题，请联系开发团队或提交Issue。
