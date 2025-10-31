<<<<<<< HEAD
# 学术活动与科研成果管理系统

基于Spring Boot + MyBatis-Plus + Vue的学术活动与科研成果管理系统

## 系统功能

### 角色划分
- 学生：参与活动、申报成果、参与项目
- 教师：发布活动、审核成果、管理项目
- 管理员：系统管理、数据统计、用户管理

### 核心功能模块
1. 用户认证与权限管理
2. 学术活动管理
3. 成果申报与审批
4. 科研项目管理
5. 数据统计与可视化

## 技术栈

### 后端
- Spring Boot 2.7.x
- MyBatis-Plus 3.5.x
- Spring Security + JWT
- MySQL 8.0
- Redis (缓存)
- Maven

### 前端
- Vue 3
- Element Plus
- Vue Router
- Axios
- ECharts (数据可视化)

## 项目结构
```
academic-management-system/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/               # 前端Vue项目
│   ├── src/
│   ├── public/
│   └── package.json
└── database/              # 数据库脚本
    └── init.sql
```

## 快速开始

### 后端启动
```bash
cd backend
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run serve
```

## 数据库配置
请先创建数据库并执行 `database/init.sql` 脚本初始化表结构。
=======
# AcademicSystem
The system of academic activites and scientific research projects
>>>>>>> c6acdb3c1a9a4e9964d0079389429289ca713c74
