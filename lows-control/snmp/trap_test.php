<?php
  require('snmp.php');

  $snmp = new snmp();

  // send a trap
  $ip = '127.0.0.1';
  $community = 'public';
  $varbind = $snmp->build_varbind('.1.3.6.1.3.83.1.1.4.1', 17, 'i');
  $enterprise = '.1.3.6.1.3.83.1.1.4.0.1.1.3.0';
  $agent = '127.0.0.1';
  $trap_type = TRAP_LINKUP;
  $specific_trap_type = 2;
  $uptime = 123;

  $snmp->trap($ip, $community, $varbind, $enterprise, $agent, $trap_type, $specific_trap_type, $uptime);

?>

