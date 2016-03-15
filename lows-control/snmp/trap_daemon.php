<?php
  require_once(dirname(__FILE__) . DIRECTORY_SEPARATOR . 'udp.php');
  require_once(dirname(__FILE__) . DIRECTORY_SEPARATOR . 'snmp.php');

  class trap_daemon extends udp_daemon
  {
    var $snmp;

    function trap_daemon($bind_address='0.0.0.0', $bind_port=162)
    {
      $this->max_read = 16384;
      parent::udp_daemon($bind_address, $bind_port);
      $this->snmp = new snmp();
    }

    function trap_handler($enterprise, $agent, $general, $specific, $timestamp, $oid, $value)
    {
      echo "trap:\n";
      print_r(array('enterprise'=>$enterprise, 'agent'=>$agent, 'general'=>$general, 'specific'=>$specific,
                    'timestamp'=>$timestamp, 'oid'=>$oid, 'value'=>$value->toString()));
    }

   /**
    * process an incoming packet
    *
    * called by udp::listen
    */
    function process_packet()
    {
      $msg = new rfc1157_Message();
      $msg = $msg->decode($this->client_buffer);
      $this->snmp->version = $msg->version();
      $pdu = $msg->pdu();
      if(is_a($pdu, 'rfc1157_TrapPDU'))
      {
        $enterprise = $pdu->enterprise();
        $agent = $pdu->agentAddr();
        $general = $pdu->genericTrap();
        $specific = $pdu->specificTrap();
        $timestamp = $pdu->timestamp();
        $vbl = $pdu->varBindList();
        foreach($vbl->value as $vb)
        {
          $oid = $vb->oid();
          $this->trap_handler($enterprise, $agent, $general, $specific, $timestamp, $oid->toString(), $vb->value());
        }
      }
    }
  }

  $z = new trap_daemon('0.0.0.0', 162);
  $z->listen();


?>
