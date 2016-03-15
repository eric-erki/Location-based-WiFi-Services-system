--
--User lows-ctrl-user
--
CREATE USER 'lows-ctrl-user'@'localhost' IDENTIFIED BY 'QQLDqennfCYev6Kx';

GRANT USAGE ON *.* TO 'lows-ctrl-user'@'localhost' IDENTIFIED BY PASSWORD '*FBD60A9AEDEEAB7B08978702B5DA3D759DEED28C';

GRANT SELECT, INSERT, UPDATE, DELETE ON `lows-control-db`.* TO 'lows-ctrl-user'@'localhost';
