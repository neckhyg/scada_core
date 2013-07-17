-- phpMyAdmin SQL Dump
-- version 3.4.5
-- http://www.phpmyadmin.net
--
-- 主机: localhost
-- 生成日期: 2013 年 07 月 17 日 09:06
-- 服务器版本: 5.5.16
-- PHP 版本: 5.3.8

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- 数据库: `scada_db`
--

-- --------------------------------------------------------

--
-- 表的结构 `sms_tb_msg`
--

CREATE TABLE IF NOT EXISTS `sms_tb_msg` (
  `sms_msg_no` varchar(6) NOT NULL DEFAULT '',
  `from_mobile` varchar(11) DEFAULT NULL,
  `to_mobile` varchar(11) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `msg_status` varchar(1) DEFAULT NULL COMMENT 'R-新接收  L-已阅读  N-待发  Y-已发  F-无效',
  `finish_datetime` datetime DEFAULT NULL,
  `enter_datetime` datetime DEFAULT NULL,
  `ip` varchar(15) DEFAULT NULL,
  `regid` int(11) DEFAULT NULL,
  PRIMARY KEY (`sms_msg_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- 转存表中的数据 `sms_tb_msg`
--

INSERT INTO `sms_tb_msg` (`sms_msg_no`, `from_mobile`, `to_mobile`, `content`, `msg_status`, `finish_datetime`, `enter_datetime`, `ip`, `regid`) VALUES
('1', NULL, '13771373256', 'COD已经超过7.00mg/L', 'N', NULL, '2013-07-17 14:58:25', NULL, NULL);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
