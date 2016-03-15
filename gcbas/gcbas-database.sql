-- phpMyAdmin SQL Dump
-- version 3.4.11.1deb2+deb7u1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 04. Feb 2015 um 23:32
-- Server Version: 5.5.41
-- PHP-Version: 5.4.4-14+deb7u5

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `gcbas`
--
CREATE DATABASE `gcbas` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `gcbas`;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `gcb_addr_entry`
--

CREATE TABLE IF NOT EXISTS `gcb_addr_entry` (
  `mac` varchar(17) NOT NULL,
  `ip` varchar(150) NOT NULL,
  `resp_user` varchar(150) NOT NULL,
  PRIMARY KEY (`mac`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Daten für Tabelle `gcb_addr_entry`
--

INSERT INTO `gcb_addr_entry` (`mac`, `ip`, `resp_user`) VALUES
('55:55:55:44:44:44', '192.168.68.1', 'cbs-tu-berlin'),
('a0:cf:5b:9f:93:c0', '192.168.68.1', 'test'),
('a0:cf:5b:9f:93:c1', '192.168.68.1', 'test');

--
-- Trigger `gcb_addr_entry`
--
DROP TRIGGER IF EXISTS `tr_gcb_addr_entry_before_insert`;
DELIMITER //
CREATE TRIGGER `tr_gcb_addr_entry_before_insert` BEFORE INSERT ON `gcb_addr_entry`
 FOR EACH ROW BEGIN
    SET NEW.resp_user = substring_index(user(), '@', 1);
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `tr_gcb_addr_entry_before_update`;
DELIMITER //
CREATE TRIGGER `tr_gcb_addr_entry_before_update` BEFORE UPDATE ON `gcb_addr_entry`
 FOR EACH ROW BEGIN
        SET NEW.resp_user = SUBSTRING_INDEX(user(), '@', 1);
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Stellvertreter-Struktur des Views `user_gcbae`
--
CREATE TABLE IF NOT EXISTS `user_gcbae` (
`mac` varchar(17)
,`ip` varchar(150)
);
-- --------------------------------------------------------

--
-- Struktur des Views `user_gcbae`
--
DROP TABLE IF EXISTS `user_gcbae`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `user_gcbae` AS select `gcb_addr_entry`.`mac` AS `mac`,`gcb_addr_entry`.`ip` AS `ip` from `gcb_addr_entry` where (`gcb_addr_entry`.`resp_user` = substring_index(user(),'@',1));

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
