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
-- Table structure for table `sales`
--

DROP TABLE IF EXISTS `sales`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales` (
  `sale_id` int NOT NULL AUTO_INCREMENT,
  `item_id` int DEFAULT NULL,
  `customer_id` int DEFAULT NULL,
  `quantity` int NOT NULL,
  `profit` double DEFAULT NULL,
  `total_price` decimal(10,2) NOT NULL,
  `sale_date` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `item_name` varchar(100) DEFAULT NULL,
  `payment_type` enum('cash','account') DEFAULT NULL,
  `sale_price` double DEFAULT NULL,
  `reversed` tinyint(1) DEFAULT '0',
  `barcode` varchar(30) DEFAULT NULL,
  `customer_name` varchar(50) DEFAULT NULL,
  `type` enum('Whole Sale','Retail') NOT NULL DEFAULT 'Retail',
  PRIMARY KEY (`sale_id`),
  KEY `item_id` (`item_id`),
  KEY `customer_id` (`customer_id`),
  CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `inventory` (`item_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales`
--

LOCK TABLES `sales` WRITE;
/*!40000 ALTER TABLE `sales` DISABLE KEYS */;
INSERT INTO `sales` VALUES (1,1,1,10,-1700.0000000000005,300.00,'2025-03-18 20:02:00',NULL,'account',NULL,0,NULL,NULL,'Retail'),(2,1,NULL,4,-680.0000000000001,120.00,'2025-03-19 12:05:17','dollar 707','cash',NULL,1,NULL,NULL,'Retail'),(3,1,NULL,10,-1700.0000000000005,300.00,'2025-03-19 14:31:02','dollar 707','cash',30,1,NULL,NULL,'Retail'),(4,1,2,3,-510.0000000000001,90.00,'2025-03-19 15:46:56','dollar 707','account',30,1,NULL,NULL,'Retail'),(5,1,1,1,30,30.00,'2025-03-21 11:53:28','dollar 707','account',30,0,'12345678',NULL,'Retail'),(6,1,NULL,3,90,90.00,'2025-03-21 11:55:43','dollar 707','cash',30,0,'12345678',NULL,'Retail'),(7,1,NULL,3,90,90.00,'2025-03-21 11:57:42','dollar 707','cash',30,0,'12345678',NULL,'Retail'),(8,1,1,1,30,30.00,'2025-03-21 12:32:22','dollar 707','account',30,0,'12345678',NULL,'Retail'),(9,1,NULL,2,60,60.00,'2025-03-21 16:08:37','dollar 707','cash',30,0,'12345678',NULL,'Retail'),(10,2,NULL,1,20,20.00,'2025-03-21 16:10:33','dollar pointer','cash',20,0,'',NULL,'Retail'),(11,2,2,1,20,20.00,'2025-03-21 16:10:54','dollar pointer','account',20,0,'',NULL,'Retail'),(12,2,NULL,1,20,20.00,'2025-03-21 16:35:15','dollar pointer','cash',20,0,'',NULL,'Retail'),(13,1,3,1,30,30.00,'2025-03-21 16:51:18','dollar 707','account',30,0,'12345678',NULL,'Retail'),(14,1,1,2,60,60.00,'2025-03-22 20:31:14','dollar 707','cash',30,0,'12345678',NULL,'Retail'),(15,1,1,1,30,30.00,'2025-03-22 21:31:02','dollar 707','cash',30,0,'12345678',NULL,'Retail'),(19,1,0,2,60,60.00,'2025-03-23 14:53:29','dollar 707','cash',30,0,'12345678','Cash Customer','Retail'),(20,1,0,1,30,30.00,'2025-03-23 15:13:58','dollar 707','cash',30,0,'12345678','Abdullah','Retail'),(21,1,1,4,120,120.00,'2025-03-26 16:58:47','dollar 707','account',30,0,'12345678','abdullah','Retail'),(22,2,1,1,20,20.00,'2025-03-26 16:58:47','dollar pointer','account',20,1,'','abdullah','Retail'),(23,1,0,3,90,90.00,'2025-03-26 17:32:03','dollar 707','cash',30,0,'12345678','Cash Customer','Retail');
/*!40000 ALTER TABLE `sales` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `calculate_profit_on_insert` BEFORE INSERT ON `sales` FOR EACH ROW SET NEW.profit = (
    SELECT NEW.total_price * (i.profit_margin / 100)
    FROM inventory i
    WHERE i.item_id = NEW.item_id
) */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `after_sale_insert` AFTER INSERT ON `sales` FOR EACH ROW BEGIN
    DECLARE customerType VARCHAR(20);
    
    IF NEW.customer_id <> 0 THEN
        SELECT customer_type 
          INTO customerType 
          FROM customers 
         WHERE customer_id = NEW.customer_id;
        
        INSERT INTO deals (
            sale_id, 
            customer_id, 
            item_name, 
            payment_type, 
            type, 
            debit, 
            credit, 
            description
        )
        VALUES (
            NEW.sale_id,
            NEW.customer_id,
            NEW.customer_name,
            NEW.payment_type,
            CASE 
                WHEN customerType = 'permanent' THEN 'permanent customer deal'
                ELSE 'temporary customer deal'
            END,
            NEW.total_price,
            0,
            CONCAT('Sale recorded on ', NEW.payment_type)
        );
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `after_sale_balance_update` AFTER INSERT ON `sales` FOR EACH ROW BEGIN
    -- Update balance only if the sale is on account (customer_id ≠ 0)
    IF NEW.customer_id <> 0 THEN
        UPDATE customers 
        SET balance = balance + NEW.total_price
        WHERE customer_id = NEW.customer_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-09 19:51:33
