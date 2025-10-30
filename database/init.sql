-- 学术活动与科研成果管理系统数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS academic_management_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE academic_management_system;

-- 用户基本信息表
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `student_id` varchar(20) DEFAULT NULL COMMENT '学号/工号',
  `department` varchar(100) DEFAULT NULL COMMENT '院系',
  `major` varchar(100) DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) DEFAULT NULL COMMENT '年级',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';

-- 权限管理表
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) NOT NULL COMMENT '角色：STUDENT-学生，TEACHER-教师，ADMIN-管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限管理表';

-- 学术活动表
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `description` text COMMENT '活动描述',
  `type` varchar(20) NOT NULL COMMENT '活动类型：LECTURE-讲座，COMPETITION-赛事，TRAINING-培训',
  `organizer` varchar(100) NOT NULL COMMENT '主办方',
  `location` varchar(200) DEFAULT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `registration_deadline` datetime NOT NULL COMMENT '报名截止时间',
  `max_participants` int DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` int DEFAULT '0' COMMENT '当前参与人数',
  `status` varchar(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，CANCELLED-已取消，COMPLETED-已完成',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学术活动表';

-- 活动报名关系表
CREATE TABLE `activity_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `registration_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `status` varchar(20) DEFAULT 'REGISTERED' COMMENT '状态：REGISTERED-已报名，ATTENDED-已参与，ABSENT-缺席',
  `certificate_url` varchar(255) DEFAULT NULL COMMENT '参与证明文件URL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名关系表';

-- 成果表
CREATE TABLE `achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成果ID',
  `title` varchar(200) NOT NULL COMMENT '成果标题',
  `type` varchar(20) NOT NULL COMMENT '成果类型：PAPER-论文，PATENT-专利，CERTIFICATE-证书，PROJECT-项目',
  `description` text COMMENT '成果描述',
  `file_url` varchar(255) DEFAULT NULL COMMENT '成果文件URL',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签（JSON格式）',
  `level` varchar(20) DEFAULT NULL COMMENT '级别：NATIONAL-国家级，PROVINCIAL-省级，SCHOOL-校级',
  `course_id` bigint DEFAULT NULL COMMENT '关联课程ID',
  `submitter_id` bigint NOT NULL COMMENT '提交者ID',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已通过，REJECTED-已驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_submitter` (`submitter_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成果表';

-- 审核记录表
CREATE TABLE `achievement_approval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `achievement_id` bigint NOT NULL COMMENT '成果ID',
  `approver_id` bigint NOT NULL COMMENT '审核者ID',
  `approval_level` varchar(20) NOT NULL COMMENT '审核级别：FIRST-初审，FINAL-终审',
  `status` varchar(20) NOT NULL COMMENT '审核状态：APPROVED-通过，REJECTED-驳回',
  `comment` text COMMENT '审核意见',
  `approval_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_achievement` (`achievement_id`),
  KEY `idx_approver` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';

-- 科研项目表
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(200) NOT NULL COMMENT '项目名称',
  `description` text COMMENT '项目描述',
  `type` varchar(20) NOT NULL COMMENT '项目类型：RESEARCH-研究项目，INNOVATION-创新项目，PRACTICE-实践项目',
  `level` varchar(20) DEFAULT NULL COMMENT '项目级别：NATIONAL-国家级，PROVINCIAL-省级，SCHOOL-校级',
  `funding` decimal(10,2) DEFAULT NULL COMMENT '项目经费',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` varchar(20) DEFAULT 'PLANNING' COMMENT '状态：PLANNING-规划中，ONGOING-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `leader_id` bigint NOT NULL COMMENT '项目负责人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_leader` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研项目表';

-- 项目成员表
CREATE TABLE `project_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) NOT NULL COMMENT '角色：LEADER-负责人，MEMBER-成员',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`, `user_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';

-- 项目阶段表
CREATE TABLE `project_milestone` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `name` varchar(200) NOT NULL COMMENT '阶段名称',
  `description` text COMMENT '阶段描述',
  `due_date` date NOT NULL COMMENT '截止日期',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待完成，COMPLETED-已完成，OVERDUE-已逾期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目阶段表';

-- 项目文件表
CREATE TABLE `project_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `milestone_id` bigint DEFAULT NULL COMMENT '阶段ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_url` varchar(255) NOT NULL COMMENT '文件URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `file_type` varchar(50) DEFAULT NULL COMMENT '文件类型',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_milestone` (`milestone_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目文件表';

-- 插入初始数据
-- 插入管理员用户
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `student_id`, `department`, `gender`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '系统管理员', 'admin@example.com', '13800138000', 'ADMIN001', '信息工程学院', 1, 1);

-- 插入管理员角色
INSERT INTO `user_role` (`user_id`, `role`) VALUES (1, 'ADMIN');

-- 插入示例教师用户
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `student_id`, `department`, `gender`, `status`) VALUES
('teacher1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '张教授', 'teacher1@example.com', '13800138001', 'T001', '信息工程学院', 1, 1);

-- 插入教师角色
INSERT INTO `user_role` (`user_id`, `role`) VALUES (2, 'TEACHER');

-- 插入示例学生用户
INSERT INTO `user` (`username`, `password`, `real_name`, `email`, `phone`, `student_id`, `department`, `major`, `grade`, `gender`, `status`) VALUES
('student1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '李同学', 'student1@example.com', '13800138002', 'S001', '信息工程学院', '计算机科学与技术', '2021级', 1, 1);

-- 插入学生角色
INSERT INTO `user_role` (`user_id`, `role`) VALUES (3, 'STUDENT');

