# 学术活动与科研成果管理系统

## 联系方式

如有任何问题或建议，请联系我：QQ:2922891070

## 项目简介

学术活动与科研成果管理系统是一个基于Spring Boot和Vue.js的全功能Web应用平台，旨在为高校和研究机构提供完整的学术活动管理、科研成果申报与审批、项目管理及数据统计分析功能。系统支持多角色权限管理，实现了从活动发布、成果申报到审批、统计的全流程管理。

## 系统功能特点

### 角色划分与权限
- **学生用户**：
  - 参与学术活动报名与取消
  - 申报个人科研成果
  - 参与科研项目
  - 查看个人成果与活动记录

- **教师用户**：
  - 发布与管理学术活动
  - 初审学生申报的科研成果
  - 创建与管理科研项目
  - 邀请学生加入项目团队
  - 查看活动报名情况与项目进度

- **管理员用户**：
  - 用户管理与权限控制
  - 系统配置与维护
  - 成果终审
  - 数据统计与可视化分析
  - 系统日志管理

### 核心功能模块

1. **用户认证与权限管理**
   - 多角色用户注册与登录
   - JWT令牌认证机制
   - 细粒度权限控制
   - 用户注册申请与管理员审批

2. **学术活动管理**
   - 活动创建、编辑与发布
   - 活动分类与标签管理
   - 学生在线报名与取消
   - 活动报名截止时间控制
   - 报名名单导出功能

3. **成果申报与审批**
   - 多类型成果申报（论文、专利、证书、项目等）
   - 成果附件上传与管理（PDF预览功能）
   - 二级审批流程（教师初审、管理员终审）
   - 审批状态跟踪与通知
   - 成果详情查看与附件下载

4. **科研项目管理**
   - 项目创建与基本信息管理
   - 项目成员邀请与管理
   - 项目阶段划分与进度跟踪
   - 项目文件上传与共享
   - 项目成果关联

5. **数据统计与可视化**
   - 成果类型分布统计
   - 活动参与情况分析
   - 项目进度统计
   - 多维度数据图表展示
   - 数据导出功能

## 技术栈

### 后端技术
- **Spring Boot 2.7.x**：Java后端开发框架
- **MyBatis-Plus 3.5.x**：ORM框架，简化数据库操作
- **Spring Security + JWT**：身份认证与授权
- **MySQL 8.0**：关系型数据库
- **Redis 6.0+**：缓存服务（可选）
- **Maven**：项目管理与依赖控制
- **Apache Commons**：工具库

### 前端技术
- **Vue 3**：渐进式JavaScript框架
- **Element Plus**：UI组件库
- **Vue Router**：前端路由管理
- **Axios**：HTTP客户端
- **Vuex/Pinia**：状态管理
- **ECharts**：数据可视化图表库
- **Vite**：前端构建工具

## 项目结构

```
academic-management-system/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/       # Java源代码
│   │   └── com/academic/    # 业务代码包
│   ├── src/main/resources/  # 配置文件
│   ├── pom.xml              # Maven配置文件
│   └── target/              # 构建输出目录
├── frontend/                # 前端Vue项目
│   ├── src/                 # 前端源代码
│   │   ├── api/             # API请求封装
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 公共组件
│   │   ├── views/           # 页面视图
│   │   ├── router/          # 路由配置
│   │   ├── store/           # 状态管理
│   │   └── main.js          # 入口文件
│   ├── public/              # 公共静态资源
│   ├── package.json         # NPM依赖配置
│   └── dist/                # 构建输出目录
├── database/                # 数据库脚本
│   ├── academic.sql         # 数据库结构
│   └── init.sql             # 初始化数据脚本
├── file/                    # 文件存储目录
│   ├── achievements/        # 成果附件
│   ├── certificates/        # 证书文件
│   └── projects/            # 项目文件
├── install.bat              # Windows安装脚本
├── start.bat                # Windows启动脚本
├── start.sh                 # Linux/macOS启动脚本
└── stop.bat                 # Windows停止脚本
```

## 系统架构

系统采用前后端分离的架构设计，具有良好的可扩展性和可维护性：

1. **前端层**：Vue 3单页应用，负责用户界面展示和交互
2. **API网关层**：处理请求路由、认证和跨域问题
3. **业务逻辑层**：Spring Boot应用，实现核心业务逻辑
4. **数据访问层**：MyBatis-Plus，封装数据库操作
5. **数据存储层**：MySQL存储结构化数据，文件系统存储附件

## 快速开始

### 环境要求

- **JDK 8** 或更高版本
- **Maven 3.6** 或更高版本
- **Node.js 14** 或更高版本
- **MySQL 8.0** 或更高版本
- **Redis 6.0+**（可选）
- **现代浏览器**（Chrome 90+、Firefox 88+、Edge 90+）

### 安装步骤

#### 1. 数据库准备

1. 启动MySQL服务
2. 创建数据库：
```sql
CREATE DATABASE academic_management_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
3. 执行初始化脚本：
```bash
mysql -u root -p academic_management_system < database/init.sql
```

#### 2. 配置修改

编辑 `backend/src/main/resources/application.yml` 文件，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/academic_management_system?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: your_username  # 修改为您的MySQL用户名
    password: your_password  # 修改为您的MySQL密码
```

#### 3. 安装与启动

##### Linux/macOS用户

```bash
# 安装后端依赖
cd backend
mvn clean install

# 安装前端依赖
cd ../frontend
npm install

#### 启动后端服务

```bash
cd backend
mvn spring-boot:run
```
也可以在IDEA中直接运行`AcademicManagementSystemApplication.java`类

#### 启动前端服务

```bash
cd frontend
npm run serve
```

## 访问系统

- **前端地址**: http://localhost:8081
- **后端API**: http://localhost:8080/api

### 默认账户信息

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |
| 学生 | student1 | 123456 |

## 系统使用说明

### 用户注册与登录
1. 新用户需先注册并等待管理员审批
2. 审批通过后可使用注册信息登录系统
3. 登录后系统根据角色自动跳转至对应首页

### 学术活动管理
1. **教师/管理员**：点击"学术活动"→"发布活动"创建新活动
2. **学生**：在活动列表查看并报名感兴趣的活动
3. 活动详情页可查看活动信息、报名状态和参与者名单

### 成果申报与审批
1. **学生**：点击"成果管理"→"申报成果"填写信息并上传附件
2. **教师**：在"待初审"列表中审核学生提交的成果
3. **管理员**：在"待终审"列表中进行最终审批
4. 成果详情页支持PDF预览和附件下载功能

### 科研项目管理
1. **教师**：点击"项目管理"→"创建项目"启动新项目
2. 通过"邀请成员"功能添加项目组成员
3. 设置项目阶段和进度跟踪
4. 上传和共享项目相关文件

### 数据统计分析
1. **管理员**：在"数据统计"模块查看各类数据图表
2. 支持按时间、类型、级别等多维度筛选
3. 数据可视化展示各类统计信息

## 常见问题与解决方案

### 1. 前端启动失败
- **问题**：'vue-cli-service' 不是内部或外部命令
- **解决方案**：运行 `npm install` 安装依赖后再启动

### 2. 后端启动失败
- **问题**：数据库连接错误
- **解决方案**：检查application.yml中的数据库配置是否正确

### 3. 端口被占用
- **问题**：启动时提示端口已被占用
- **解决方案**：修改配置文件中的端口号，或关闭占用端口的进程

### 4. 文件上传失败
- **问题**：附件上传提示失败
- **解决方案**：检查文件大小限制，确保有足够的磁盘空间，检查文件权限

### 5. 登录失败
- **问题**：输入正确用户名密码但无法登录
- **解决方案**：检查用户是否已通过管理员审批，或联系管理员重置密码

### 6.x 系统展示
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

### 代码结构规范

- **后端**：遵循MVC架构，采用RESTful API设计规范
- **前端**：组件化开发，遵循Vue官方最佳实践
- **数据库**：遵循范式设计，合理使用索引

### 安全注意事项

1. 定期更新系统依赖库版本
2. 实施数据库备份策略
3. 定期审查系统日志
4. 遵循最小权限原则配置用户权限
5. 敏感数据加密存储

### 性能优化建议

1. 使用Redis缓存热点数据
2. 优化数据库查询，添加必要索引
3. 实现分页查询避免大数据量加载
4. 前端使用懒加载和组件按需引入
5. 合理设置JWT过期时间



























