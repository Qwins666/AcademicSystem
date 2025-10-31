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
登录页面
<img width="1895" height="914" alt="image" src="https://github.com/user-attachments/assets/16654e7a-9d74-41ac-a856-2f57df948c93" />

注册页面
<img width="1905" height="948" alt="image" src="https://github.com/user-attachments/assets/693aa6ee-bd70-483c-a791-1611992fd01c" />

系统管理员首页
<img width="1893" height="945" alt="image" src="https://github.com/user-attachments/assets/9ce29c1e-d088-4334-a39e-0a6b12591495" />

系统管理员学术活动页面
<img width="1904" height="950" alt="image" src="https://github.com/user-attachments/assets/78bc5619-5373-4468-9f42-0a23f558717f" />

活动详情查看
<img width="1248" height="632" alt="image" src="https://github.com/user-attachments/assets/6e5ec85c-f9d2-44cb-a52a-299ceadf0411" />

编辑活动
<img width="892" height="753" alt="image" src="https://github.com/user-attachments/assets/bf9d7f5f-96df-4574-be4c-a88af7afd7e2" />

发布活动
<img width="997" height="767" alt="image" src="https://github.com/user-attachments/assets/90bd1c2f-fd44-4205-9007-fbdc9b14ebe3" />

成果列表
<img width="1286" height="626" alt="image" src="https://github.com/user-attachments/assets/5176238c-fc18-45fc-b495-405b43bdcf84" />

成果详情查看
<img width="1012" height="886" alt="image" src="https://github.com/user-attachments/assets/ced2cf84-634f-42a6-bb9e-c04daacff425" />

附件预览PDF
<img width="1026" height="891" alt="image" src="https://github.com/user-attachments/assets/510f5fd1-703b-451b-b965-c14c4c73f754" />

附件下载
<img width="453" height="199" alt="image" src="https://github.com/user-attachments/assets/8b990316-ace2-43f5-9aa4-413926920e9e" />

科研项目列表（教师端主导）
<img width="1501" height="585" alt="image" src="https://github.com/user-attachments/assets/f9c79d5f-199b-4e0c-a073-595abae5f68c" />


项目详情（教师端主导）
<img width="1442" height="738" alt="image" src="https://github.com/user-attachments/assets/27efcda4-d5a0-4756-9266-9da9478adb00" />

邀请成员（教师端主导）
<img width="672" height="288" alt="image" src="https://github.com/user-attachments/assets/aa76a34e-3f6a-4da1-861c-1efd76159aa4" />

项目阶段（教师端主导）
<img width="1257" height="680" alt="image" src="https://github.com/user-attachments/assets/1ee37b00-75e5-446b-ad71-aae1240f19ad" />

项目文件（教师端主导）
<img width="1188" height="549" alt="image" src="https://github.com/user-attachments/assets/4b8ae913-af44-47cd-ac6c-ebb9d4187ed9" />

编辑项目（教师端主导）
<img width="939" height="655" alt="image" src="https://github.com/user-attachments/assets/936530d3-f7c9-461d-a43a-8d51112f87fb" />

数据可视化统计模块
<img width="1360" height="675" alt="image" src="https://github.com/user-attachments/assets/f73b0329-679d-40c0-8c71-c6a53b3b6713" />
<img width="1224" height="860" alt="image" src="https://github.com/user-attachments/assets/91995c90-15e2-42e1-9b21-ed12c154d59f" />
<img width="1273" height="864" alt="image" src="https://github.com/user-attachments/assets/9a7c2e8f-b9f5-46e3-afbc-3177dcc597d5" />

用户管理模块，注册申请，管理员审批之后才可进行登录
<img width="1464" height="789" alt="image" src="https://github.com/user-attachments/assets/2371b2a4-d91f-41dc-9cb0-1211b6322e83" />

活动列表，学生端可在报名截止日期前进行报名，在截止日期前可取消报名
<img width="1473" height="693" alt="image" src="https://github.com/user-attachments/assets/af7b83ad-a5bc-49ed-9603-a2dc41c1beac" />
<img width="1277" height="630" alt="image" src="https://github.com/user-attachments/assets/c31b5c63-bec6-4aed-a901-3239071dfa0d" />

学生端可进行成果申报
<img width="1294" height="874" alt="image" src="https://github.com/user-attachments/assets/792a2b18-1b2f-48d4-93a7-126f08648c65" />

教师端可查看活动报名情况
<img width="1280" height="630" alt="image" src="https://github.com/user-attachments/assets/2e9fec14-381a-4eb3-9a63-38361fccfa81" />

对成果申报进行初审，终审由管理员进行审批
<img width="1467" height="703" alt="image" src="https://github.com/user-attachments/assets/27cca47e-fef4-48e7-97ee-9a66696a603e" />



























