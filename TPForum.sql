-- MySQL dump 10.13  Distrib 5.7.11, for osx10.9 (x86_64)
--
-- Host: localhost    Database: TPForum
-- ------------------------------------------------------
-- Server version	5.7.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `follows`
--

DROP TABLE IF EXISTS `follows`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `follows` (
  `follower` varchar(150) NOT NULL,
  `followed` varchar(150) NOT NULL,
  PRIMARY KEY (`follower`,`followed`),
  KEY `fk_follows_followed_idx` (`followed`),
  CONSTRAINT `fk_follows_followed` FOREIGN KEY (`followed`) REFERENCES `user` (`email`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_follows_follower` FOREIGN KEY (`follower`) REFERENCES `user` (`email`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `follows`
--

LOCK TABLES `follows` WRITE;
/*!40000 ALTER TABLE `follows` DISABLE KEYS */;
/*!40000 ALTER TABLE `follows` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum`
--

DROP TABLE IF EXISTS `forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forum` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `short_name` varchar(150) NOT NULL,
  `user` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `short_name_UNIQUE` (`short_name`),
  KEY `fk_forum_user_idx` (`user`),
  CONSTRAINT `fk_forum_user` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum`
--

LOCK TABLES `forum` WRITE;
/*!40000 ALTER TABLE `forum` DISABLE KEYS */;
INSERT INTO `forum` VALUES (1,'Forum With Sufficiently Large Name','forumwithsufficientlylargename','richard.nixon@example.com'),(2,'Forum I','forum1','richard.nixon@example.com'),(3,'Forum II','forum2','example2@mail.ru'),(4,'Форум Три','forum3','example@mail.ru');
/*!40000 ALTER TABLE `forum` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `post` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `message` text NOT NULL,
  `parent` int(11) DEFAULT NULL,
  `likes` int(11) NOT NULL DEFAULT '0',
  `dislikes` int(11) NOT NULL DEFAULT '0',
  `points` int(11) NOT NULL DEFAULT '0',
  `isApproved` tinyint(4) NOT NULL DEFAULT '0',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0',
  `isEdited` tinyint(4) NOT NULL DEFAULT '0',
  `isHighlighted` tinyint(4) NOT NULL DEFAULT '0',
  `isSpam` tinyint(4) NOT NULL DEFAULT '0',
  `forum` varchar(150) NOT NULL,
  `thread` int(11) NOT NULL,
  `user` varchar(150) NOT NULL,
  `first_path` int(11) DEFAULT NULL,
  `last_path` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_post_forum_idx` (`forum`),
  KEY `fk_post_thread_idx` (`thread`),
  KEY `fk_post_user_idx` (`user`),
  CONSTRAINT `fk_post_forum` FOREIGN KEY (`forum`) REFERENCES `forum` (`short_name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_post_thread` FOREIGN KEY (`thread`) REFERENCES `thread` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_post_user` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `post`
--

LOCK TABLES `post` WRITE;
/*!40000 ALTER TABLE `post` DISABLE KEYS */;
INSERT INTO `post` VALUES (1,'2014-05-28 13:36:23','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0',0,0,0,0,1,0,1,0,1,'forum1',1,'example3@mail.ru',1,NULL),(2,'2015-10-11 18:45:04','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1',1,0,0,0,0,0,0,0,0,'forum1',1,'example3@mail.ru',1,''),(3,'2016-03-03 15:29:22','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2',2,0,0,0,0,0,0,0,0,'forum1',1,'example4@mail.ru',1,'.'),(4,'2016-03-16 20:52:59','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3',1,0,0,0,0,0,0,0,1,'forum1',1,'example@mail.ru',1,''),(5,'2016-03-26 04:35:59','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4',0,0,0,0,1,0,0,0,1,'forum1',1,'example4@mail.ru',5,NULL),(6,'2016-04-01 05:47:52','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5',0,0,0,0,0,0,0,1,1,'forum1',1,'example@mail.ru',6,NULL),(7,'2016-04-06 14:39:54','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6',0,0,0,0,0,0,1,1,1,'forum1',1,'example2@mail.ru',7,NULL),(8,'2016-04-07 18:49:02','my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7',0,0,0,0,1,0,1,1,0,'forum1',1,'example@mail.ru',8,NULL),(9,'2016-04-07 21:37:40','my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8',5,0,0,0,1,0,1,1,1,'forum1',1,'richard.nixon@example.com',5,'	'),(10,'2015-09-27 22:20:59','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0',0,0,0,0,1,0,0,0,1,'forum2',2,'richard.nixon@example.com',10,NULL),(11,'2015-11-11 20:49:12','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1',10,0,0,0,0,0,0,1,1,'forum2',2,'example3@mail.ru',10,''),(12,'2016-02-05 05:58:30','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2',0,0,0,0,0,0,1,1,1,'forum2',2,'richard.nixon@example.com',12,NULL),(13,'2016-04-04 21:20:29','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3',12,0,0,0,0,0,1,0,0,'forum2',2,'example2@mail.ru',12,'\r'),(14,'2016-04-07 11:55:31','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4',0,0,0,0,0,0,0,0,0,'forum2',2,'example@mail.ru',14,NULL),(15,'2016-04-07 22:02:19','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5',13,0,0,0,0,0,1,0,1,'forum2',2,'example4@mail.ru',12,'\r.'),(16,'2016-04-07 22:49:33','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6',10,0,0,0,0,0,0,0,0,'forum2',2,'example4@mail.ru',10,''),(17,'2015-09-12 01:12:09','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0',0,0,0,0,1,0,1,0,1,'forum1',3,'richard.nixon@example.com',17,NULL),(18,'2015-12-21 12:03:11','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1',0,0,0,0,1,0,1,1,0,'forum1',3,'example4@mail.ru',18,NULL),(19,'2016-01-22 20:11:33','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2',0,0,0,0,1,0,1,0,0,'forum1',3,'richard.nixon@example.com',19,NULL),(20,'2016-03-21 05:25:38','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3',18,0,0,0,1,0,1,1,1,'forum1',3,'richard.nixon@example.com',18,''),(21,'2016-03-27 03:25:30','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4',0,0,0,0,0,0,1,1,1,'forum1',3,'richard.nixon@example.com',21,NULL),(22,'2016-03-29 04:57:55','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5',20,0,0,0,1,0,1,1,0,'forum1',3,'example3@mail.ru',18,'.'),(23,'2016-04-06 19:10:31','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6',0,0,1,-1,0,0,0,0,0,'forum1',3,'richard.nixon@example.com',23,NULL),(24,'2015-10-23 07:47:18','my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0my message 0',0,0,0,0,1,0,1,1,0,'forum1',4,'example2@mail.ru',24,NULL),(25,'2015-12-28 04:28:39','my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1my message 1',0,0,0,0,0,0,0,1,0,'forum1',4,'example4@mail.ru',25,NULL),(26,'2016-03-04 05:01:02','my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2my message 2',24,0,0,0,1,0,1,0,0,'forum1',4,'example2@mail.ru',24,'\Z'),(27,'2016-03-16 08:19:45','my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3my message 3',24,0,0,0,1,0,0,1,1,'forum1',4,'example4@mail.ru',24,''),(28,'2016-04-03 14:58:03','my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4my message 4',26,0,0,0,1,0,0,0,1,'forum1',4,'example4@mail.ru',24,'\Z.'),(29,'2016-04-05 03:28:01','my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5my message 5',0,0,0,0,1,0,1,1,1,'forum1',4,'example@mail.ru',29,NULL),(30,'2016-04-07 17:29:48','my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6my message 6',0,0,0,0,1,0,0,1,0,'forum1',4,'example2@mail.ru',30,NULL),(31,'2016-04-07 21:18:57','my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7my message 7',0,0,0,0,1,0,0,0,0,'forum1',4,'richard.nixon@example.com',31,NULL),(32,'2016-04-07 22:25:40','my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8my message 8',25,0,0,0,0,0,0,1,1,'forum1',4,'example@mail.ru',25,' ');
/*!40000 ALTER TABLE `post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subscribed`
--

DROP TABLE IF EXISTS `subscribed`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscribed` (
  `user` varchar(150) NOT NULL,
  `thread` int(11) NOT NULL,
  PRIMARY KEY (`user`,`thread`),
  KEY `fk_subscribed_thread_idx` (`thread`),
  CONSTRAINT `fk_subscribed_thread` FOREIGN KEY (`thread`) REFERENCES `thread` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_subscribed_user` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subscribed`
--

LOCK TABLES `subscribed` WRITE;
/*!40000 ALTER TABLE `subscribed` DISABLE KEYS */;
INSERT INTO `subscribed` VALUES ('example4@mail.ru',1),('richard.nixon@example.com',1),('example4@mail.ru',2),('example2@mail.ru',3),('example3@mail.ru',3);
/*!40000 ALTER TABLE `subscribed` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thread`
--

DROP TABLE IF EXISTS `thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thread` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `date` datetime NOT NULL,
  `slug` varchar(150) NOT NULL,
  `message` text NOT NULL,
  `likes` int(11) NOT NULL DEFAULT '0',
  `dislikes` int(11) NOT NULL DEFAULT '0',
  `points` int(11) NOT NULL DEFAULT '0',
  `posts` int(11) NOT NULL DEFAULT '0',
  `isClosed` tinyint(4) NOT NULL DEFAULT '0',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0',
  `forum` varchar(150) NOT NULL,
  `user` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_thread_forum_idx` (`forum`),
  KEY `fk_thread_user_idx` (`user`),
  CONSTRAINT `fk_thread_forum` FOREIGN KEY (`forum`) REFERENCES `forum` (`short_name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_thread_user` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thread`
--

LOCK TABLES `thread` WRITE;
/*!40000 ALTER TABLE `thread` DISABLE KEYS */;
INSERT INTO `thread` VALUES (1,'Thread With Sufficiently Large Title','2014-01-01 00:00:01','Threadwithsufficientlylargetitle','hey hey hey hey!',0,0,0,9,1,0,'forum1','example4@mail.ru'),(2,'Thread I','2013-12-31 00:01:01','thread1','hey!',0,1,-1,7,0,0,'forum2','example2@mail.ru'),(3,'Thread II','2013-12-30 00:01:01','newslug','hey hey!',0,0,0,7,0,0,'forum1','example2@mail.ru'),(4,'Тред Три','2013-12-29 00:01:01','thread3','hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! hey hey hey! ',0,0,0,9,0,0,'forum1','example2@mail.ru');
/*!40000 ALTER TABLE `thread` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(150) NOT NULL,
  `username` varchar(150) DEFAULT NULL,
  `name` varchar(150) DEFAULT NULL,
  `about` text,
  `isAnonymous` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'example@mail.ru','user1','John','hello im user1',0),(2,'richard.nixon@example.com',NULL,NULL,NULL,1),(3,'example2@mail.ru','user2','NewName','Wowowowow',0),(4,'example3@mail.ru','user3','NewName2','Wowowowow!!!',0),(5,'example4@mail.ru','user4','Jim','hello im user4',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-04-08  1:02:22
