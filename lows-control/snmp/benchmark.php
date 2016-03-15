<?php
  $get_oid = '.1.3.6.1.2.1.1.1.0';
  $walk_oid = '.1.3.6.1.2.1.1';
  $count = 100;

  require('snmp.php');

  $functions = array('native_get'=>$get_oid,   'binary_get'=>$get_oid,   'phpsnmp_get'=>$get_oid,
                     'native_walk'=>$walk_oid, 'binary_walk'=>$walk_oid, 'phpsnmp_walk'=>$walk_oid);
  foreach($functions as $function=>$oid)
  {
    $start = microtime(1);
    for($i = 0; $i < $count; $i++)
    {
      print_r($function('127.0.0.1', $oid));
      echo "\n";
    }
    $results[$function] = microtime(1) - $start;
  }
  echo "\nResults:\n";
  print_r($results);

  function binary_get($host, $oid)
  {
    return `snmpget -On -v2c -c public $host $oid`;
  }

  function phpsnmp_get($host, $oid)
  {
    $snmp = new snmp();
    $snmp->version = SNMP_VERSION_2C;
    return $snmp->get($host, $oid);
  }

  function native_get($host, $oid)
  {
    return snmpget($host, 'public', $oid);
  }

  function binary_walk($host, $oid)
  {
    return `snmpwalk -On -v2c -c public $host $oid`;
  }

  function phpsnmp_walk($host, $oid)
  {
    $snmp = new snmp();
    $snmp->version = SNMP_VERSION_2C;
    return $snmp->walk($host, $oid);
  }

  function native_walk($host, $oid)
  {
    return snmpwalk($host, 'public', $oid);
  }
?>
  
