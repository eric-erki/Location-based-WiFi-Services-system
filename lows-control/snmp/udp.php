<?php
 /**
  * UDP daemon
  *
  * This class handles listening on a UDP port and managing clients that connect.
  *
  * You need to write process_packet in your subclass!
  */
  class udp_daemon
  {
    var $bind_address;		// address for main listener
    var $bind_port;		// port for main listener
    var $max_read = 4096;	// max size of packets to read

    var $clients   = array();	// array of client sockets
    var $addresses = array();	// array of client addresses
    var $ports     = array();	// array of client ports
    var $expires   = array();	// array of timestamps to expire clients

    var $client_socket;		// current client socket
    var $client_address;	// current client address
    var $client_port;		// current client port
    var $client_buffer;		// current client buffer (received packet)
    var $client_index;		// index of current client in $clients

    var $nat = false;		// set to true to work better with NAT

    function udp_daemon($bind_address, $bind_port)
    {
      $this->bind_address = $bind_address;
      $this->bind_port = $bind_port;
    }

   /**
    * process a packet - this need to be created for your subclass!
    */
    function process_packet()
    {
      trigger_error('process_packet needs to be defined!', E_USER_ERROR);
    }

   /**
    * Destroy a client
    *
    * @param integer $index of client
    */
    function destroy_client($index=NULL)
    {
      if(is_null($index)) $index = $this->client_index;
      if(!$this->nat)
        socket_close($this->clients[$index]);
      unset($this->clients[$index], $this->addresses[$index], $this->ports[$index], $this->expires[$index]);
    }

   /**
    * listen for connections forever
    */
    function listen()
    {
      $w = $e = NULL;

      // create listener
      if(($listener = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP)) === false)
      {
        exit;
      }
      if(!socket_bind($listener, $this->bind_address, $this->bind_port))
      {
        socket_close($listener);
        exit;
      }

      while(true)
      {
        // wait for a connection from either the listener or the clients
        $active = $this->clients;
        $active[] = $listener;
        socket_select($active, $w, $e, NULL);

        foreach($active as $this->client_socket)
        {
          // read the packet
          socket_recvfrom($this->client_socket, $this->client_buffer, $this->max_read, 0,
                          $this->client_address, $this->client_port);

          if($this->client_socket == $listener)
          {
            // this is a new client
            if(!$this->nat)
            {
              if(($this->client_socket = socket_create(AF_INET, SOCK_DGRAM, SOL_UDP)) === false) continue;
              if(!socket_bind($this->client_socket, '0.0.0.0', 0)) continue;
            }
            $this->clients[] = $this->client_socket;
            $this->addresses[] = $this->client_address;
            $this->ports[] = $this->client_port;
            $this->expires[] = time() + 30;
          }

          // get the index of the current client
          $this->client_index = NULL;
          foreach($this->clients as $i=>$client)
          {
            if($client == $this->client_socket &&
               $this->ports[$i] == $this->client_port &&
               $this->addresses[$i] == $this->client_address)
            {
              $this->client_index = $i;
              break;
            }
          }
          // update expiration time
          $this->expires[$this->client_index] = time() + 30;

          // process the packet (this is a function defined in a subclass)
          $this->process_packet();
        }

        // clean up old clients
        foreach($this->expires as $i=>$time)
        {
          if($time < time())
            $this->destroy_client($i);
        }
      }
    }
  }
?>
