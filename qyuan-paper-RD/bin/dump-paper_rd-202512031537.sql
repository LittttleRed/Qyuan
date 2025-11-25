/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.1.2-MariaDB, for Linux (x86_64)
--
-- Host: 120.46.205.113    Database: paper_rd
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `author_paper`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `author_paper` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `paper_id` bigint NOT NULL COMMENT '论文ID',
  `author_last_name` varchar(100) NOT NULL COMMENT '作者姓',
  `author_first_name` varchar(100) NOT NULL COMMENT '作者名',
  `author_rank` int NOT NULL COMMENT '作者次序(一作,二作)',
  `author_orcid` varchar(50) DEFAULT NULL COMMENT '作者ORCID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_author_rank` (`paper_id`,`author_rank`),
  KEY `idx_paper_id` (`paper_id`),
  KEY `idx_author_name` (`author_last_name`,`author_first_name`),
  KEY `idx_orcid` (`author_orcid`),
  KEY `idx_author_rank` (`author_rank`)
) ENGINE=InnoDB AUTO_INCREMENT=1354247 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='作者-论文关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `claim`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `claim` (
  `claim_id` bigint NOT NULL AUTO_INCREMENT COMMENT '认领ID（主键）',
  `user_id` bigint NOT NULL COMMENT '认领用户ID',
  `paper_id` bigint NOT NULL COMMENT '论文ID',
  `claim_picture` varchar(200) DEFAULT NULL COMMENT '认领信息图片url',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0-未认领 1-待审核 2-驳回 3-通过',
  PRIMARY KEY (`claim_id`),
  UNIQUE KEY `uk_user_paper` (`user_id`,`paper_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_paper_id` (`paper_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='认领信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `journal`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `journal` (
  `journal_id` bigint NOT NULL AUTO_INCREMENT COMMENT '期刊ID（主键）',
  `journal_name` varchar(200) NOT NULL COMMENT '期刊名称',
  `journal_url` varchar(255) NOT NULL COMMENT '期刊链接',
  `doaj_url` varchar(255) NOT NULL COMMENT 'doaj链接',
  `issn` varchar(20) DEFAULT NULL COMMENT 'ISSN号',
  `publish_time` int NOT NULL COMMENT '发表年份',
  `keywords` varchar(500) DEFAULT NULL COMMENT '关键词',
  `article_number` int NOT NULL COMMENT '包含文章数目',
  `languages` varchar(200) NOT NULL COMMENT '语言',
  `publisher` varchar(255) DEFAULT NULL COMMENT '发表者',
  PRIMARY KEY (`journal_id`),
  UNIQUE KEY `issn` (`issn`),
  KEY `idx_issn` (`issn`)
) ENGINE=InnoDB AUTO_INCREMENT=21967 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='期刊表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper` (
  `paper_id` bigint NOT NULL AUTO_INCREMENT COMMENT '论文ID（主键）',
  `title` varchar(255) NOT NULL COMMENT '论文标题',
  `submitter` varchar(50) NOT NULL COMMENT '提交人',
  `abstract` text COMMENT '摘要',
  `doi` varchar(100) DEFAULT NULL COMMENT 'DOI（唯一）',
  `journal_source` varchar(100) DEFAULT NULL COMMENT '期刊来源',
  `pdf_file_url` varchar(100) DEFAULT NULL COMMENT 'PDF文件URL',
  `url` varchar(100) NOT NULL COMMENT '原链接',
  `category_id` bigint DEFAULT NULL COMMENT '领域信息',
  `updated` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`paper_id`),
  KEY `idx_doi` (`doi`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=287834 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论文表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_category`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类中文名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='论文种类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `paper_ref`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `paper_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '引用主键',
  `paper_id` bigint NOT NULL COMMENT '论文id',
  `ref_title` varchar(255) DEFAULT NULL COMMENT '引用论文标题',
  `abstract` varchar(255) DEFAULT NULL COMMENT '引用论文摘要(前200字)',
  `auther_name` varchar(100) DEFAULT NULL COMMENT '一作姓名',
  `updated` year DEFAULT NULL COMMENT '发表年份',
  `be_refed_count` int DEFAULT NULL COMMENT '被引用次数',
  `url` varchar(100) DEFAULT NULL COMMENT '源url',
  PRIMARY KEY (`id`),
  KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB AUTO_INCREMENT=477259 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='论文引用表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `patent`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `patent` (
  `patent_number` text,
  `patent_name` text,
  `abstract` text,
  `inventor` text,
  `assignee` text,
  `application_date` datetime DEFAULT NULL,
  `authorization_date` datetime DEFAULT NULL,
  `citation_count` int DEFAULT NULL,
  `country` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_paper`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_paper` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `paper_id` bigint NOT NULL COMMENT '论文ID',
  `user_rank` tinyint DEFAULT NULL COMMENT '作者次序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_paper` (`user_id`,`paper_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户论文关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'paper_rd'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-12-03 15:37:48
