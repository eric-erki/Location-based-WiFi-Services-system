<?php
include('Net/SSH2.php');
class adapter_openwrt
{
    
    public function removeData($mac)
    {
        return -2;
    }
    
    public function getData($mac)
    {
        return -2;
    }
    
    public function setCodebookData($mac, $data_array)
    {
        return $this->setMixedData($mac, $data_array);
    }

    
    public function setFlexibleData($mac, $data_array)
    {
        return $this->setMixedData($mac, $data_array);
    }
    
    
    public function setMixedData($mac, $data_array)
    {
        print_r($data_array);
        $number_of_services = count($data_array);
        //echo "Connect to database server";
        $connection = mysql_connect($_SESSION["sql_host"], $_SESSION["sql_user"], $_SESSION["sql_pass"]) or die("Connection to database server can not be established"); 
        mysql_select_db("lows-control-db") or die ("Could not select database table"); 
        //echo "Get corresponding connection data id for access point";
        $request = "SELECT * FROM access_point WHERE mac LIKE '$mac'"; 
        $result = mysql_query($request) or die (mysql_error());
        $row = mysql_fetch_object($result) or die (mysql_error());
        $send_mode=$row->send_mode;
        $rate=$row->rate;
        $beacon_interval=$row->beacon_interval;
        
        $ap_connection_data_id = $row->ap_connection_data_id;
        //echo "get the connection data";
        $request = "SELECT * FROM ap_connection_data WHERE id LIKE '$ap_connection_data_id'"; 
        $result = mysql_query($request) or die (mysql_error());
        $row = mysql_fetch_object($result) or die (mysql_error());

        $ip = $row->ip;
        $user = $row->user;
        $pass1 = $row->password1;
        
        $ssh = new Net_SSH2($ip);
        if (!$ssh->login($user, $pass1)) {
            echo 'Login Failed';
            return -1;
        }
        
        //get the used interface
        $mac_check_interface = $ssh->exec('hostapd_cli interface');
        $mac_check_interface_begin = strpos($mac_check_interface, "'");
        $mac_check_interface_begin++;
        $mac_check_interface_end = strpos($mac_check_interface, "'",$mac_check_interface_begin);
        $mac_check_interface=substr($mac_check_interface,$mac_check_interface_begin, ($mac_check_interface_end-$mac_check_interface_begin));
        
        echo "Using interface: ".$mac_check_interface.", ";
        
        //get the MAC address of the interface
        $send_string = "ifconfig -a ".$mac_check_interface;
        $mac_check_mac = $ssh->exec($send_string);
        $mac_check_mac_begin = strpos($mac_check_mac, "HWaddr");
        $mac_check_mac = substr($mac_check_mac, ($mac_check_mac_begin+7), 17);
        
        echo "Using mac: ".$mac_check_mac.", ";
        
        //Compare the mac of the ap with the mac the request was initially made for
        if(strcasecmp($mac, $mac_check_mac)!=0)
        {
            echo "<br><b>ERROR wrong MAC for chosen AP, please contact system administrator and check access point connection details</b><br>";
            return -1;
        }
        //send the data to the ap
        else
        {
            if($send_mode==null or !isset($send_mode) or (strcmp($send_mode, "separated")!=0))
            {
                //send services integrated in normal Beacons and Probe Respones from running AP
                //contact hostapd_cli
                
                //Stop Beaconspammer if it is running
                echo $ssh->exec("killall beaconspammer");
                //Build hostapd_cli command
                $send_string = "hostapd_cli add_ie ".$number_of_services;
                for($i=0; $i<$number_of_services; $i++)
                {
                    $data_wo_zerox = substr($data_array[$i],2);
                    $data_with_space = substr($data_wo_zerox,0,2)." ".substr($data_wo_zerox,2);
                    $send_string = $send_string." aaaaaa ".$data_with_space;
                }
                echo "<br> <b>".$send_string."</b>";
                echo $ssh->exec($send_string);
                return 0;
            }
            else if(strcmp($send_mode, "separated")==0)
            {
                //Inject Beacon Frames separately to normal AP
                //contact beaconspammer
                
                //Remove additional IEs from hostapd_cli
                echo $ssh->exec("hostapd_cli add_ie");
                echo $ssh->exec("killall beaconspammer");
                //Create monitor interface
                
                echo $ssh->exec("add_monitor.sh");
                
                //Build beaconspammer command
                
                $send_string = "beaconspammer mon0";
                if(isset($rate))
                {
                    $send_string = $send_string." -r".$rate;
                }
                if(isset($beacon_interval))
                {
                    $send_string = $send_string." -d".$beacon_interval;
                }
                $mac_wo_colons = str_replace(":", "", $mac);
                $send_string = $send_string." -b ".$mac_wo_colons." -i ";
                for($i=0; $i<$number_of_services; $i++)
                {
                    $data_wo_zerox = substr($data_array[$i],2);
                    $service_len = (strlen($data_wo_zerox)/2)+3; //String Länge /2 + 3 for OUI
                    $service_len = dechex($service_len);
                    if(strlen($service_len)==1)
                    {
                        $service_len = "0".$service_len;
                    }
                    $send_string = $send_string."dd".$service_len."aaaaaa".$data_wo_zerox;
                }
                $send_string = $send_string."&";
                echo "<br> <b>".$send_string."</b>";
                echo $ssh->exec($send_string);
                
                return 0;
                
            }
        }
    }
    
    
    /*
    public function setParameters($mac, $send_mode, $rate, $beacon_interval)
    {
        $connection = mysql_connect($_SESSION["sql_host"], $_SESSION["sql_user"], $_SESSION["sql_pass"]) or die("Connection to database server can not be established"); 
        mysql_select_db("lows-control-db") or die ("Could not select database table"); 
        if(isset($send_mode) && isset($mac))
        {
            $request = "INSERT INTO access_point (send_mode) VALUES('$send_mode') WHERE mac LIKE '$mac'"; 
            $result = mysql_query($request) or die (mysql_error());
        }
        if(isset($rate) && isset($mac))
        {
            $request = "INSERT INTO access_point (rate) VALUES('$rate') WHERE mac LIKE '$mac'"; 
            $result = mysql_query($request) or die (mysql_error());
        }
        if(isset($beacon_interval) && isset($mac))
        {
            $request = "INSERT INTO access_point (beacon_interval) VALUES('$beacon_interval') WHERE mac LIKE '$mac'"; 
            $result = mysql_query($request) or die (mysql_error());
        }
        return 0;
    }
    * */
}
?>

