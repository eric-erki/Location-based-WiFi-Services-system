GRANT USAGE ON *.* TO 'cbs-tu-berlin'@'localhost' IDENTIFIED BY PASSWORD '*F7782B0650D49016C361D7820DD5C90510CEA198';

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON `gcbas`.`user_gcbae` TO 'cbs-tu-berlin'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON `gcbas`.`gcb_addr_entry` TO 'cbs-tu-berlin'@'localhost';

