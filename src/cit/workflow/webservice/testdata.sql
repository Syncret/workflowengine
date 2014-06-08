
CREATE DATABASE `jworkflowtest2` ;

USE `jworkflowtest2`;

/*Table structure for table `activityinputont` */

DROP TABLE IF EXISTS `activityinputont`;

CREATE TABLE `activityinputont` (
  `WorkflowID` int(10) NOT NULL,
  `ActivityID` int(10) NOT NULL,
  `InputID` int(10) NOT NULL,
  `InputOntology` varchar(50) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
