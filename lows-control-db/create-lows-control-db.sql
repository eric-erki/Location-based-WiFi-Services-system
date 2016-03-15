-- phpMyAdmin SQL Dump
-- version 3.4.11.1deb2+deb7u1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 01. Apr 2015 um 07:02
-- Server Version: 5.5.41
-- PHP-Version: 5.4.39-0+deb7u2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `lows-control-db`
--
CREATE DATABASE `lows-control-db` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `lows-control-db`;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `access_point`
--

CREATE TABLE IF NOT EXISTS `access_point` (
  `mac` varchar(17) NOT NULL,
  `name` varchar(150) NOT NULL,
  `ap_type` enum('cisco','openwrt') NOT NULL,
  `ap_connection_data_id` int(11) DEFAULT NULL,
  `send_mode` enum('integrated','separated') DEFAULT NULL,
  `rate` int(11) DEFAULT NULL,
  `beacon_interval` int(11) DEFAULT NULL,
  PRIMARY KEY (`mac`),
  KEY `ap_connection_data_id` (`ap_connection_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- RELATIONEN DER TABELLE `access_point`:
--   `ap_connection_data_id`
--       `ap_connection_data` -> `id`
--

--
-- Daten für Tabelle `access_point`
--

INSERT INTO `access_point` (`mac`, `name`, `ap_type`, `ap_connection_data_id`, `send_mode`, `rate`, `beacon_interval`) VALUES
('44:d3:ca:b1:d6:e0', 'HilfsAP', 'cisco', 3, NULL, NULL, NULL),
('44:d3:ca:fa:d8:65', 'AP3-EN-6FL', 'cisco', 3, NULL, NULL, NULL),
('64:66:b3:54:c9:ff', 'OpenWRT-BeaconSendingMachine', 'openwrt', 4, 'integrated', 5, 1000),
('a0:cf:5b:9f:93:c0', 'AP3-EN-6FL', 'cisco', NULL, NULL, NULL, NULL),
('a0:cf:5b:9f:93:c1', 'AP3-EN-6FL', 'cisco', NULL, NULL, NULL, NULL);

--
-- Trigger `access_point`
--
DROP TRIGGER IF EXISTS `access_point_changes_insert_trigger`;
DELIMITER //
CREATE TRIGGER `access_point_changes_insert_trigger` AFTER INSERT ON `access_point`
 FOR EACH ROW BEGIN
	INSERT INTO access_point_changes SET access_point_mac = NEW.mac, reason = 'new';
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `access_point_changes_delete_trigger`;
DELIMITER //
CREATE TRIGGER `access_point_changes_delete_trigger` AFTER DELETE ON `access_point`
 FOR EACH ROW BEGIN
	INSERT INTO access_point_changes SET access_point_mac = OLD.mac, reason = 'deleted';
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `access_point_changes`
--

CREATE TABLE IF NOT EXISTS `access_point_changes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `access_point_mac` varchar(17) DEFAULT NULL,
  `reason` enum('new','deleted') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `access_point_mac` (`access_point_mac`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ap_connection_data`
--

CREATE TABLE IF NOT EXISTS `ap_connection_data` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(150) NOT NULL,
  `user` varchar(150) NOT NULL,
  `password1` varchar(150) NOT NULL,
  `password2` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user` (`user`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Daten für Tabelle `ap_connection_data`
--

INSERT INTO `ap_connection_data` (`id`, `ip`, `user`, `password1`, `password2`) VALUES
(3, '192.168.68.222', 'test', 'test123456789', 'test123456789'),
(4, '192.168.68.4', 'root', 'toor', '');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `codebook`
--

CREATE TABLE IF NOT EXISTS `codebook` (
  `version` int(11) NOT NULL AUTO_INCREMENT,
  `reason` enum('updated','deleted','inserted') NOT NULL,
  PRIMARY KEY (`version`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=8 ;

--
-- Daten für Tabelle `codebook`
--

INSERT INTO `codebook` (`version`, `reason`) VALUES
(7, 'inserted');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `ldc_codebook_entry`
--

CREATE TABLE IF NOT EXISTS `ldc_codebook_entry` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `access_point_mac` varchar(17) NOT NULL,
  `service_hexcode` varchar(4) NOT NULL,
  `lic` varchar(4) NOT NULL,
  `ldc` varchar(4) NOT NULL,
  `data` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `access_point_mac` (`access_point_mac`),
  KEY `service_hexcode` (`service_hexcode`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=15 ;

--
-- RELATIONEN DER TABELLE `ldc_codebook_entry`:
--   `access_point_mac`
--       `access_point` -> `mac`
--   `service_hexcode`
--       `service` -> `hexcode`
--

--
-- Daten für Tabelle `ldc_codebook_entry`
--

INSERT INTO `ldc_codebook_entry` (`id`, `access_point_mac`, `service_hexcode`, `lic`, `ldc`, `data`) VALUES
(3, '44:d3:ca:fa:d8:65', '0x21', '0x52', '0x48', 'Use the helicopter landing point to evacuate.'),
(4, '44:d3:ca:fa:d8:65', '0x21', '0x50', '0x49', 'Rescue every child from the kindergarden in room A42'),
(5, '44:d3:ca:fa:d8:65', '0x3f', '0x54', '0x31', 'The location is next to the west entrance between the book store and the ice cream bar. First floor.'),
(6, 'a0:cf:5b:9f:93:c0', '0x21', '0x5a', '0x30', 'no problems at the moment, everything is fine. You are at TU Berlin.'),
(7, 'a0:cf:5b:9f:93:c1', '0x21', '0x5a', '0x30', 'no problems at the moment, everything is fine. You are at TU Berlin.'),
(8, 'a0:cf:5b:9f:93:c0', '0x3f', '0x54', '0x31', 'The location is next to the west entrance between the book store and the ice cream bar. Second floor.'),
(9, 'a0:cf:5b:9f:93:c1', '0x3f', '0x54', '0x31', 'The location is next to the west entrance between the book store and the ice cream bar. First floor.'),
(10, '44:d3:ca:b1:d6:e0', '0x3f', '0x25', '0x31', 'Hallo Test'),
(11, 'a0:cf:5b:9f:93:c0', '0x3f', '0x55', '0x30', 'Station U2, Ernst Reuter Platz'),
(12, 'a0:cf:5b:9f:93:c1', '0x3f', '0x55', '0x30', 'Station U2, Ernst Reuter Platz'),
(13, 'a0:cf:5b:9f:93:c0', '0x21', '0x52', '0x48', 'Use the helicopter landing point to evacuate.'),
(14, 'a0:cf:5b:9f:93:c1', '0x21', '0x52', '0x48', 'Use the helicopter landing point to evacuate.');

--
-- Trigger `ldc_codebook_entry`
--
DROP TRIGGER IF EXISTS `update_version_on_insert`;
DELIMITER //
CREATE TRIGGER `update_version_on_insert` BEFORE INSERT ON `ldc_codebook_entry`
 FOR EACH ROW BEGIN
	DELETE FROM codebook;
	INSERT INTO codebook SET reason = 'inserted';
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `update_version_on_update`;
DELIMITER //
CREATE TRIGGER `update_version_on_update` BEFORE UPDATE ON `ldc_codebook_entry`
 FOR EACH ROW BEGIN
	DELETE FROM codebook;
	INSERT INTO codebook SET reason = 'updated';
END
//
DELIMITER ;
DROP TRIGGER IF EXISTS `update_version_on_deleted`;
DELIMITER //
CREATE TRIGGER `update_version_on_deleted` BEFORE DELETE ON `ldc_codebook_entry`
 FOR EACH ROW BEGIN
	DELETE FROM codebook;
	INSERT INTO codebook SET reason = 'deleted';
END
//
DELIMITER ;

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `lic_cb_value`
--

CREATE TABLE IF NOT EXISTS `lic_cb_value` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `service_hexcode` varchar(4) NOT NULL,
  `lic` varchar(4) NOT NULL,
  `data` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `service_hexcode` (`service_hexcode`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=48 ;

--
-- RELATIONEN DER TABELLE `lic_cb_value`:
--   `service_hexcode`
--       `service` -> `hexcode`
--

--
-- Daten für Tabelle `lic_cb_value`
--

INSERT INTO `lic_cb_value` (`id`, `service_hexcode`, `lic`, `data`) VALUES
(1, '0x21', '0x5a', 'No emergency (Code ZERO)'),
(2, '0x21', '0x52', 'Fire Emergency (Code RED)'),
(3, '0x21', '0x62', 'Severe Weather Emergency (Code BROWN)'),
(4, '0x21', '0x47', 'Internal Disaster (Code GREEN)'),
(5, '0x21', '0x42', 'Bomb Threat (Code BLACK)'),
(6, '0x21', '0x50', 'Child Abduction (Code PINK)'),
(7, '0x21', '0x57', 'Building Evacuation (Code WHITE)'),
(8, '0x21', '0x4f', 'Aggressive Situation (Code ORANGE)'),
(9, '0x21', '0x59', 'Medical Emergency (Code YELLOW)'),
(10, '0x3f', '0x54', 'Public Toilet'),
(11, '0x3f', '0x43', 'Coffeeshop'),
(12, '0x3f', '0x63', 'Copyshop'),
(15, '0x3f', '0x40', 'Cyber Cafe'),
(17, '0x3f', '0x41', 'Airport'),
(19, '0x3f', '0x55', 'Underground (metro, subway)'),
(20, '0x3f', '0x47', 'Gate (Airport)'),
(21, '0x3f', '0x25', 'Cinema'),
(22, '0x3f', '0x2f', 'DIY Warehouse'),
(23, '0x3f', '0x3c', 'Beverage Store'),
(24, '0x3f', '0x3e', 'Bakery'),
(25, '0x3f', '0x42', 'Baby-Care-Room'),
(26, '0x3f', '0x44', 'Disabled Toilet'),
(27, '0x3f', '0x45', 'Elevator'),
(28, '0x3f', '0x46', 'Fast Food Restaurant'),
(29, '0x3f', '0x48', 'Hospital'),
(30, '0x3f', '0x49', 'Information Point'),
(31, '0x3f', '0x3b', 'Bank'),
(32, '0x3f', '0x4b', 'Kiosk'),
(33, '0x3f', '0x4d', 'Meat Market, Butchers Shop'),
(34, '0x3f', '0x50', 'Pizzeria'),
(35, '0x3f', '0x52', 'Restaurant'),
(36, '0x3f', '0x58', 'Clothing Store'),
(37, '0x3f', '0x61', 'apothecary, pharmacy'),
(38, '0x3f', '0x62', 'Bus Station'),
(39, '0x3f', '0x64', 'Drug Store'),
(40, '0x3f', '0x66', 'Free Wifi'),
(41, '0x3f', '0x67', 'Garage'),
(42, '0x3f', '0x6d', 'Doctor of Medicine'),
(43, '0x3f', '0x6f', 'Police Station'),
(44, '0x3f', '0x70', 'Parking Garage'),
(45, '0x3f', '0x73', 'Supermarket'),
(46, '0x3f', '0x74', 'Train Station'),
(47, '0x3f', '0x4e', 'Tourist Information');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `service`
--

CREATE TABLE IF NOT EXISTS `service` (
  `hexcode` varchar(4) NOT NULL,
  `name` varchar(150) NOT NULL,
  `st_type` enum('codebook','flexible') NOT NULL,
  `priority` int(11) NOT NULL,
  PRIMARY KEY (`hexcode`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Daten für Tabelle `service`
--

INSERT INTO `service` (`hexcode`, `name`, `st_type`, `priority`) VALUES
('0x10', 'Waiting Ticket Number (WTN)', 'flexible', 0),
('0x20', 'String Type (ST)', 'flexible', 0),
('0x21', 'Beacon Emergency Propagation System (BEPS)', 'codebook', 0),
('0x3f', 'Physical Service Announcement (PSA)', 'codebook', 0),
('ccbt', 'Custom Codebook Type (CCBT)', 'codebook', 0),
('cfbt', 'Custom Flexible Type (CFBT)', 'flexible', 0);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `running_services`
--

CREATE TABLE IF NOT EXISTS `running_services` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `access_point_mac` varchar(17) NOT NULL,
  `service_hexcode` varchar(4) NOT NULL,
  `data` text NOT NULL,
  PRIMARY KEY (`id`),
  KEY `access_point_mac` (`access_point_mac`),
  KEY `service_hexcode` (`service_hexcode`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=109 ;

--
-- RELATIONEN DER TABELLE `running_services`:
--   `access_point_mac`
--       `access_point` -> `mac`
--   `service_hexcode`
--       `service` -> `hexcode`
--

--
-- Daten für Tabelle `running_services`
--

INSERT INTO `running_services` (`id`, `access_point_mac`, `service_hexcode`, `data`) VALUES
(32, '44:d3:ca:b1:d6:e0', '0x3f', '0x5430'),
(103, '64:66:B3:54:C9:FF', '0x10', '0x01b2526F6F6D2F536572766963653A'),
(105, '64:66:B3:54:C9:FF', '0x20', '0x537472696E6720746F2073656E6420286D61782032353029'),
(108, '44:d3:ca:fa:d8:65', '0x21', '0x5a30');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user`
--

CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `password` varchar(150) NOT NULL,
  `role` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Daten für Tabelle `user`
--

INSERT INTO `user` (`id`, `name`, `password`, `role`) VALUES
(1, 'dca8', '8e1f4daf7551e7deb406ecbde62ff2b8', 0),
(2, 'root', '7b24afc8bc80e548d66c4e7ff72171c5', 1);

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user_ap_permission`
--

CREATE TABLE IF NOT EXISTS `user_ap_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `access_point_mac` varchar(17) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `access_point_mac` (`access_point_mac`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6 ;

--
-- RELATIONEN DER TABELLE `user_ap_permission`:
--   `user_id`
--       `user` -> `id`
--   `access_point_mac`
--       `access_point` -> `mac`
--

--
-- Daten für Tabelle `user_ap_permission`
--

INSERT INTO `user_ap_permission` (`id`, `user_id`, `access_point_mac`) VALUES
(3, 1, '44:d3:ca:fa:d8:65'),
(4, 1, '44:d3:ca:b1:d6:e0'),
(5, 1, '64:66:B3:54:C9:FF');

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `user_service_permission`
--

CREATE TABLE IF NOT EXISTS `user_service_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `service_hexcode` varchar(4) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `service_hexcode` (`service_hexcode`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=6 ;

--
-- RELATIONEN DER TABELLE `user_service_permission`:
--   `user_id`
--       `user` -> `id`
--   `service_hexcode`
--       `service` -> `hexcode`
--

--
-- Daten für Tabelle `user_service_permission`
--

INSERT INTO `user_service_permission` (`id`, `user_id`, `service_hexcode`) VALUES
(1, 1, '0x3f'),
(2, 1, '0x20'),
(3, 1, '0x21'),
(4, 1, '0x10'),
(5, 2, '0x21');

--
-- Constraints der exportierten Tabellen
--

--
-- Constraints der Tabelle `access_point`
--
ALTER TABLE `access_point`
  ADD CONSTRAINT `access_point_ibfk_1` FOREIGN KEY (`ap_connection_data_id`) REFERENCES `ap_connection_data` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `ldc_codebook_entry`
--
ALTER TABLE `ldc_codebook_entry`
  ADD CONSTRAINT `ldc_codebook_entry_ibfk_1` FOREIGN KEY (`access_point_mac`) REFERENCES `access_point` (`mac`) ON DELETE CASCADE ON UPDATE NO ACTION,
  ADD CONSTRAINT `ldc_codebook_entry_ibfk_2` FOREIGN KEY (`service_hexcode`) REFERENCES `service` (`hexcode`) ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `lic_cb_value`
--
ALTER TABLE `lic_cb_value`
  ADD CONSTRAINT `lic_cb_value_ibfk_1` FOREIGN KEY (`service_hexcode`) REFERENCES `service` (`hexcode`) ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `running_services`
--
ALTER TABLE `running_services`
  ADD CONSTRAINT `running_services_ibfk_1` FOREIGN KEY (`access_point_mac`) REFERENCES `access_point` (`mac`),
  ADD CONSTRAINT `running_services_ibfk_2` FOREIGN KEY (`service_hexcode`) REFERENCES `service` (`hexcode`);

--
-- Constraints der Tabelle `user_ap_permission`
--
ALTER TABLE `user_ap_permission`
  ADD CONSTRAINT `user_ap_permission_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_ap_permission_ibfk_2` FOREIGN KEY (`access_point_mac`) REFERENCES `access_point` (`mac`) ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constraints der Tabelle `user_service_permission`
--
ALTER TABLE `user_service_permission`
  ADD CONSTRAINT `user_service_permission_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_service_permission_ibfk_2` FOREIGN KEY (`service_hexcode`) REFERENCES `service` (`hexcode`) ON DELETE CASCADE ON UPDATE NO ACTION;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


