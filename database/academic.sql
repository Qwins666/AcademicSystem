-- 学术活动与科研成果管理系统数据库加入数据之后的脚本
-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: academic_management_system
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `achievement`
--

DROP TABLE IF EXISTS `achievement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `achievement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '成果ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成果标题',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '成果类型：PAPER-论文，PATENT-专利，CERTIFICATE-证书，PROJECT-项目',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '成果描述',
  `file_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '成果文件URL',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签（JSON格式）',
  `level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '级别：NATIONAL-国家级，PROVINCIAL-省级，SCHOOL-校级',
  `course_id` bigint DEFAULT NULL COMMENT '关联课程ID',
  `submitter_id` bigint NOT NULL COMMENT '提交者ID',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已通过，REJECTED-已驳回',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_submitter` (`submitter_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `achievement`
--

LOCK TABLES `achievement` WRITE;
/*!40000 ALTER TABLE `achievement` DISABLE KEYS */;
INSERT INTO `achievement` VALUES (1,'航空飞行专利','PATENT','关于现代化飞行器缩减燃油使用率',NULL,NULL,NULL,'航空，飞行，专利','NATIONAL',NULL,3,'APPROVED','2025-10-18 20:18:19','2025-10-18 20:41:33'),(5,'2312313','CERTIFICATE','31231232132',NULL,NULL,NULL,'213112312','SCHOOL',NULL,4,'APPROVED','2025-10-18 20:56:06','2025-10-18 21:13:31'),(6,'123123','PAPER','3123123312',NULL,NULL,NULL,'2313','PROVINCIAL',NULL,3,'REJECTED','2025-10-18 21:06:09','2025-10-18 21:31:51'),(7,'城市视觉规划','PROJECT','关于城市视觉规划项目书的制定','/uploads/achievements/20251024194031_0237c129c5f342cc820b952134c30017.docx','Linux实践作业(第5周).docx',16314,'视觉，项目','NATIONAL',NULL,3,'PENDING','2025-10-23 14:50:16','2025-10-24 19:40:31'),(8,'减轻灯泡发热','PATENT','关于研究灯泡发热的专利报告','/uploads/achievements/20251024193216_3a471b0a963b484aa28d248d5f8403ce.pdf','热电转换与微流道散热的低热耗照明装置及其方法.pdf',154955,'国家级，专利','NATIONAL',NULL,3,'PENDING_FINAL','2025-09-23 20:34:22','2025-10-24 19:42:35');
/*!40000 ALTER TABLE `achievement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `achievement_approval`
--

DROP TABLE IF EXISTS `achievement_approval`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `achievement_approval` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `achievement_id` bigint NOT NULL COMMENT '成果ID',
  `approver_id` bigint NOT NULL COMMENT '审核者ID',
  `approval_level` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核级别：FIRST-初审，FINAL-终审',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '审核状态：APPROVED-通过，REJECTED-驳回',
  `comment` text COLLATE utf8mb4_unicode_ci COMMENT '审核意见',
  `approval_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_achievement` (`achievement_id`),
  KEY `idx_approver` (`approver_id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审核记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `achievement_approval`
--

LOCK TABLES `achievement_approval` WRITE;
/*!40000 ALTER TABLE `achievement_approval` DISABLE KEYS */;
INSERT INTO `achievement_approval` VALUES (3,1,2,'FIRST','APPROVED','教师审核通过','2025-10-18 20:40:34'),(4,1,1,'FINAL','APPROVED','管理员通过','2025-10-18 20:41:33'),(7,5,2,'FIRST','APPROVED','pass','2025-10-18 20:59:04'),(8,6,2,'FIRST','APPROVED','通过','2025-10-18 21:13:06'),(9,5,1,'FINAL','APPROVED','通过','2025-10-18 21:13:31'),(10,6,1,'FINAL','REJECTED','退回重修改','2025-10-18 21:31:51'),(11,8,2,'FIRST','APPROVED','审批通过','2025-10-24 19:42:35');
/*!40000 ALTER TABLE `achievement_approval` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity`
--

DROP TABLE IF EXISTS `activity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '活动描述',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动类型：LECTURE-讲座，COMPETITION-赛事，TRAINING-培训',
  `organizer` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '主办方',
  `location` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '活动地点',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `registration_deadline` datetime NOT NULL COMMENT '报名截止时间',
  `max_participants` int DEFAULT NULL COMMENT '最大参与人数',
  `current_participants` int DEFAULT '0' COMMENT '当前参与人数',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PUBLISHED-已发布，CANCELLED-已取消，COMPLETED-已完成',
  `creator_id` bigint NOT NULL COMMENT '创建者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学术活动表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity`
--

LOCK TABLES `activity` WRITE;
/*!40000 ALTER TABLE `activity` DISABLE KEYS */;
INSERT INTO `activity` VALUES (3,'蓝桥杯赛前培训','参赛人员的赛前培训活动','TRAINING','计算机学院','学术报告厅','2025-10-16 00:00:00','2025-10-20 00:00:00','2025-10-17 00:00:00',200,0,'COMPLETED',1,'2025-10-16 11:24:18','2025-10-16 11:24:18'),(12,'宏志计划','关于提高就业人群面试的成功','LECTURE','湖北工程学院','新火报告厅','2025-10-18 00:00:00','2025-10-19 00:00:00','2025-10-17 00:00:00',100,0,'COMPLETED',1,'2025-10-16 20:14:06','2025-10-16 20:14:06'),(13,'校园招聘会','湖北工程学院秋季招聘会第二场','COMPETITION','湖北工程学院','体育馆','2025-10-20 10:23:25','2025-10-17 08:00:00','2025-10-17 00:00:00',200,2,'COMPLETED',1,'2025-10-17 10:24:02','2025-10-17 13:20:46'),(16,'111','1111111111','COMPETITION','1111','1111','2025-10-19 19:27:12','2025-10-22 00:00:00','2025-10-20 00:00:00',100,1,'COMPLETED',2,'2025-10-19 19:27:21','2025-10-19 19:28:32'),(17,'学术交流会','关于学术交流会的相关业务','LECTURE','计算机学院','学术报告厅','2025-10-30 00:00:00','2025-10-31 00:00:00','2025-10-29 00:00:00',100,1,'PUBLISHED',1,'2025-10-26 09:35:06','2025-10-26 09:35:06');
/*!40000 ALTER TABLE `activity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `activity_participant`
--

DROP TABLE IF EXISTS `activity_participant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_participant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `registration_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'REGISTERED' COMMENT '状态：REGISTERED-已报名，ATTENDED-已参与，ABSENT-缺席',
  `certificate_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '参与证明文件URL',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_user` (`activity_id`,`user_id`),
  KEY `idx_activity` (`activity_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_participant`
--

LOCK TABLES `activity_participant` WRITE;
/*!40000 ALTER TABLE `activity_participant` DISABLE KEYS */;
INSERT INTO `activity_participant` VALUES (8,13,4,'2025-10-17 12:10:44','REGISTERED',''),(11,13,3,'2025-10-17 13:15:54','REGISTERED','/uploads/certificates/activities/20251026094155_0c056c272c0d492bb9dfaed16815cfe8.pdf'),(12,16,3,'2025-10-19 19:28:47','REGISTERED',NULL),(13,17,3,'2025-10-26 09:35:27','REGISTERED',NULL);
/*!40000 ALTER TABLE `activity_participant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '项目描述',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '项目类型：RESEARCH-研究项目，INNOVATION-创新项目，PRACTICE-实践项目',
  `level` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '项目级别：NATIONAL-国家级，PROVINCIAL-省级，SCHOOL-校级',
  `funding` decimal(10,2) DEFAULT NULL COMMENT '项目经费',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PLANNING' COMMENT '状态：PLANNING-规划中，ONGOING-进行中，COMPLETED-已完成，CANCELLED-已取消',
  `leader_id` bigint NOT NULL COMMENT '项目负责人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_leader` (`leader_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='科研项目表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project`
--

LOCK TABLES `project` WRITE;
/*!40000 ALTER TABLE `project` DISABLE KEYS */;
INSERT INTO `project` VALUES (4,'城乡规划','关于开展城乡规划项目','PRACTICE','PROVINCIAL',2000.00,'2025-10-20','2025-10-27','ONGOING',2,'2025-10-20 22:03:39','2025-10-20 22:03:39');
/*!40000 ALTER TABLE `project` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_file`
--

DROP TABLE IF EXISTS `project_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_file` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `milestone_id` bigint DEFAULT NULL COMMENT '阶段ID',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件名',
  `file_url` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `file_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件类型',
  `uploader_id` bigint NOT NULL COMMENT '上传者ID',
  `upload_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_milestone` (`milestone_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目文件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_file`
--

LOCK TABLES `project_file` WRITE;
/*!40000 ALTER TABLE `project_file` DISABLE KEYS */;
INSERT INTO `project_file` VALUES (1,4,5,'024321712123秦松-劳动.docx','/uploads/projects/4/20251023104728_8ae25ea72e21496fa7985eab0cfbb799.docx',313402,'docx',2,'2025-10-23 10:47:29'),(2,4,7,'第一学期云计算基础知识点.pdf','/uploads/projects/4/20251023105128_c142c36423b34501a0d855b985a7d931.pdf',600383,'pdf',2,'2025-10-23 10:51:28'),(3,4,0,'新建 文本文档.txt','/uploads/projects/4/20251023105212_ac6d4b6cc3c445e09f75f52124e8b56a.txt',1952,'txt',2,'2025-10-23 10:52:13');
/*!40000 ALTER TABLE `project_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_member`
--

DROP TABLE IF EXISTS `project_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：LEADER-负责人，MEMBER-成员',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE-活跃，INACTIVE-非活跃',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_member`
--

LOCK TABLES `project_member` WRITE;
/*!40000 ALTER TABLE `project_member` DISABLE KEYS */;
INSERT INTO `project_member` VALUES (3,4,2,'LEADER','2025-10-20 22:03:39','ACTIVE'),(4,4,4,'MEMBER','2025-10-20 22:03:59','ACTIVE');
/*!40000 ALTER TABLE `project_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_milestone`
--

DROP TABLE IF EXISTS `project_milestone`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_milestone` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '阶段名称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '阶段描述',
  `due_date` date NOT NULL COMMENT '截止日期',
  `status` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING' COMMENT '状态：PENDING-待完成，COMPLETED-已完成，OVERDUE-已逾期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目阶段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_milestone`
--

LOCK TABLES `project_milestone` WRITE;
/*!40000 ALTER TABLE `project_milestone` DISABLE KEYS */;
INSERT INTO `project_milestone` VALUES (1,4,'项目立项','项目启动阶段','2025-10-21','COMPLETED','2025-10-20 16:28:35','2025-10-23 10:50:09'),(3,4,'文献调研','文献的调研工作开始','2025-10-24','PENDING','2025-10-20 18:28:16','2025-10-20 22:04:42'),(4,4,'方案设计与开发','方案设计与开发进行中','2025-10-26','PENDING','2025-10-20 18:28:38','2025-10-20 22:04:42'),(5,4,'中期检查','中期需要上传相应的文件','2025-10-28','PENDING','2025-10-20 19:24:35','2025-10-23 10:51:55'),(6,4,'成果整理','将成功进行整理','2025-10-29','PENDING','2025-10-23 10:50:30','2025-10-23 10:50:39'),(7,4,'结题验收','结题验收阶段','2025-10-30','PENDING','2025-10-23 10:50:53','2025-10-23 10:50:53');
/*!40000 ALTER TABLE `project_milestone` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `student_id` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学号/工号',
  `department` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '院系',
  `major` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业',
  `grade` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '年级',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$YeAOqcl7zKNGlqR45OWtaOXQIVE47VsHF4pfDQmKAKui5MMKgcHPC','系统管理员','admin@example.com','13800138000','ADMIN001','信息工程学院',NULL,NULL,1,NULL,1,'2025-10-10 01:04:49','2025-10-10 02:03:36'),(2,'teacher1','$2a$10$YeAOqcl7zKNGlqR45OWtaOXQIVE47VsHF4pfDQmKAKui5MMKgcHPC','张教授','teacher1@example.com','13800138001','T001','信息工程学院',NULL,NULL,1,NULL,1,'2025-10-10 01:04:49','2025-10-10 02:04:01'),(3,'student1','$2a$10$ZwGSzSPLrobnRoAufkLj7uuOljZpZZnE17glCCVyKHqN7Iuph/pve','李同学','student1@example.com','13800138002','S001','信息工程学院','软件工程','2021级',1,'',1,'2025-10-10 01:04:50','2025-10-16 22:02:59'),(4,'student2','$2a$10$ywJ4/qKBTXRc5/8yGHGQFe8j04kHWeyLjvZtHH11wTrLSZXcaPWNO','张三','12344@163.com','15674898736','024321712101','计算机系','计算机科学与技术','2024级',1,NULL,1,'2025-10-10 02:00:48','2025-10-15 20:33:12');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色：STUDENT-学生，TEACHER-教师，ADMIN-管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES (1,1,'ADMIN','2025-10-10 01:04:49'),(2,2,'TEACHER','2025-10-10 01:04:49'),(3,3,'STUDENT','2025-10-10 01:04:50'),(8,4,'STUDENT','2025-10-15 20:33:11');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-10-26  9:52:45