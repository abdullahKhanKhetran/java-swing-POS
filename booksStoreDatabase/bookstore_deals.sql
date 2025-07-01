-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bookstore
-- ------------------------------------------------------
-- Server version	9.2.0

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
-- Table structure for table `deals`
--

DROP TABLE IF EXISTS `deals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `deals` (
  `deal_id` int NOT NULL AUTO_INCREMENT,
  `sale_id` int DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `type` enum('permanent customer deal','permanent supplier deal','temporary supplier deal','temporary customer deal') DEFAULT NULL,
  `debit` decimal(10,2) DEFAULT NULL,
  `credit` decimal(10,2) DEFAULT NULL,
  `purchase_id` int DEFAULT NULL,
  `customer_id` int DEFAULT NULL,
  `supplier_id` int DEFAULT NULL,
  `item_name` varchar(100) DEFAULT NULL,
  `deal_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `quantity` int DEFAULT NULL,
  `payment_type` enum('cash','account') DEFAULT NULL,
  `reversed` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`deal_id`),
  KEY `sale_id` (`sale_id`),
  CONSTRAINT `deals_ibfk_1` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `deals`
--

LOCK TABLES `deals` WRITE;
/*!40000 ALTER TABLE `deals` DISABLE KEYS */;
INSERT INTO `deals` VALUES (1,NULL,'Purchased dollar 707','permanent supplier deal',0.00,20000.00,1,NULL,1,'dollar 707','2025-03-18 19:50:39',100,'account',1),(2,1,'Sale recorded on account','permanent customer deal',300.00,0.00,NULL,1,NULL,'dollar 707','2025-03-18 20:02:00',NULL,'account',0),(3,2,'Sale recorded on account','temporary customer deal',120.00,0.00,NULL,NULL,NULL,'dollar 707','2025-03-19 12:05:17',NULL,'cash',1),(4,3,'Sale recorded on account','temporary customer deal',300.00,0.00,NULL,NULL,NULL,'dollar 707','2025-03-19 14:31:02',NULL,'cash',1),(5,NULL,'Purchased dollar pointer','permanent supplier deal',0.00,1000.00,2,NULL,1,'dollar pointer','2025-03-19 14:50:37',100,'cash',0),(6,NULL,'Purchased dollar pointer','permanent supplier deal',0.00,1000.00,4,NULL,1,'dollar pointer','2025-03-19 15:23:58',100,'account',1),(7,NULL,'Purchased dollar pointer','permanent supplier deal',0.00,1000.00,5,NULL,1,'dollar pointer','2025-03-19 15:24:53',100,'cash',1),(8,4,'Sale recorded on account','temporary customer deal',90.00,0.00,NULL,2,NULL,'dollar 707','2025-03-19 15:46:56',NULL,'account',1),(9,NULL,'Purchased dollar pointer','permanent supplier deal',0.00,2000.00,6,NULL,1,'dollar pointer','2025-03-19 21:32:40',100,'cash',0),(10,5,'Sale recorded on account','permanent customer deal',30.00,0.00,NULL,1,NULL,'dollar 707','2025-03-21 11:53:28',NULL,'account',0),(11,6,'Sale recorded on account','temporary customer deal',90.00,0.00,NULL,NULL,NULL,'dollar 707','2025-03-21 11:55:43',NULL,'cash',0),(12,7,'Sale recorded on account','temporary customer deal',90.00,0.00,NULL,NULL,NULL,'dollar 707','2025-03-21 11:57:42',NULL,'cash',0),(13,8,'Sale recorded on account','permanent customer deal',30.00,0.00,NULL,1,NULL,'dollar 707','2025-03-21 12:32:22',NULL,'account',0),(14,9,'Sale recorded on account','temporary customer deal',60.00,0.00,NULL,NULL,NULL,'dollar 707','2025-03-21 16:08:37',NULL,'cash',0),(15,10,'Sale recorded on account','temporary customer deal',20.00,0.00,NULL,NULL,NULL,'dollar pointer','2025-03-21 16:10:33',NULL,'cash',0),(16,11,'Sale recorded on account','temporary customer deal',20.00,0.00,NULL,2,NULL,'dollar pointer','2025-03-21 16:10:54',NULL,'account',0),(17,12,'Sale recorded on account','temporary customer deal',20.00,0.00,NULL,NULL,NULL,'dollar pointer','2025-03-21 16:35:15',NULL,'cash',0),(18,13,'Sale recorded on account','temporary customer deal',30.00,0.00,NULL,3,NULL,'dollar 707','2025-03-21 16:51:18',NULL,'account',0),(19,14,'Sale recorded on account','permanent customer deal',60.00,0.00,NULL,1,NULL,'dollar 707','2025-03-22 20:31:14',NULL,'cash',0),(20,14,'Sale recorded','permanent customer deal',60.00,0.00,NULL,1,NULL,'dollar 707','2025-03-22 20:31:15',NULL,'cash',0),(21,15,'Sale recorded on account','permanent customer deal',30.00,0.00,NULL,1,NULL,'dollar 707','2025-03-22 21:31:02',NULL,'cash',0),(22,15,'Sale recorded','permanent customer deal',30.00,0.00,NULL,1,NULL,'dollar 707','2025-03-22 21:31:02',NULL,'cash',0),(23,NULL,'Purchased paper clips','permanent supplier deal',0.00,100.00,7,NULL,1,'paper clips','2025-03-23 16:56:50',100,'cash',0),(24,NULL,'Purchased dollar 707','permanent supplier deal',0.00,15.00,8,NULL,1,'dollar 707','2025-03-25 18:03:26',2,'cash',0),(25,21,'Sale recorded on account','permanent customer deal',120.00,0.00,NULL,1,NULL,'abdullah','2025-03-26 16:58:47',NULL,'account',0),(26,22,'Sale recorded on account','permanent customer deal',20.00,0.00,NULL,1,NULL,'abdullah','2025-03-26 16:58:47',NULL,'account',1),(27,NULL,'Purchased urdu 9th','permanent supplier deal',0.00,1000.00,9,NULL,1,'urdu 9th','2025-03-26 17:02:09',10,'cash',0);
/*!40000 ALTER TABLE `deals` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-09 19:51:31
