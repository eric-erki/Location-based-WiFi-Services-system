<?php
require('snmp/snmp.php');
class adapter_cisco
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
        $data = $data_array[0];
        if(strlen($data)>8)
        {
            echo "<br><b>ERROR not a Codebook Type, please contact system administrator</b><br>";
            return -1;
        }
        //echo "setCodebookTypeData function called";
        //echo "Connect to database server";
        $connection = mysql_connect($_SESSION["sql_host"], $_SESSION["sql_user"], $_SESSION["sql_pass"]) or die("Connection to database server can not be established"); 
        mysql_select_db("lows-control-db") or die ("Could not select database table"); 
        //echo "Get corresponding connection data id for access point";
        $request = "SELECT * FROM access_point WHERE mac LIKE '$mac'"; 
        $result = mysql_query($request) or die (mysql_error());
        $row = mysql_fetch_object($result) or die (mysql_error());
        $ap_connection_data_id = $row->ap_connection_data_id;
        //echo "get the connection data";
        $request = "SELECT * FROM ap_connection_data WHERE id LIKE '$ap_connection_data_id'"; 
        $result = mysql_query($request) or die (mysql_error());
        $row = mysql_fetch_object($result) or die (mysql_error());

        $ip = $row->ip;
        $user = $row->user;
        $pass1 = $row->password1;
        $pass2 = $row->password2;
        //echo "start configuring snmp";
        ini_set('memory_limit', '256M');
        $snmp = new snmp();
        $snmp->version = SNMP_VERSION_3;
        $inTable=true;
        //The start OID of the AccessPoint Table on the Cisco WLC
        $oid_raw='.1.3.6.1.4.1.14179.2.2.1'; //Table start
        $ap_array = array();
        //echo "Requesting hostnames of all available APs via SNMPv3...\n";
        //Now starting to go trough the whole AP table on the WLC
        while($inTable == true)
        {
            $oid_array = ($snmp->get_next($ip, $oid_raw, array('v3_flags'=>SNMP_AUTH_PRIV, 'v3_user'=>$user,
                                                                    'v3_auth'=>$pass1, 'v3_priv'=>$pass2)));
            $oid=array_keys($oid_array);
            $oid_raw=$oid[0];
            $oid_content_raw=$oid_array[$oid[0]];
            
            //print_r($oid_array);
            //echo "<br>oid_raw".$oid_raw."<br>";
            //echo "<br>oid_content_raw".$oid_content_raw."<br>";
            //echo "*";
            
            //Find out if we already left the table
            if((strpos($oid_raw, '14179.2.2.1.'))==false)
            {
                //echo (strpos($oid_raw, '14179.2.2.1.'));
                //echo "String '14179.2.2.1' not found in $oid_raw \n";
                //echo "\n";
                $inTable = false;
            }
            //Find all OIDs where the AP hostname is configured
            if((strpos($oid_raw, '14179.2.2.1.1.3.'))!=false)
            {
                $ap_array[] = $oid_array;
                $ap_array_length = count($ap_array);
                //echo "!!! Content: $oid_content_raw \n";
                //echo "!!! OID: $oid_raw \n";
                //echo "Current length ap_array: $ap_array_length \n";
            }
            //Find all OIDs where the mac is configured
            if((strpos($oid_raw, '14179.2.2.1.1.33.'))!=false)
            {
                $mac_array[] = $oid_array;
                $mac_array_length = count($mac_array);
                //echo "!!! Content: $oid_content_raw \n";
                //echo "!!! OID: $oid_raw \n";
                //echo "Current length ap_array: $ap_array_length \n";
            }
            else
            {
                //echo "Content: $oid_content_raw \n";
                //echo "OID: $oid_raw \n";
            }
        }
        //echo "Found $ap_array_length APs\n";
        //print_r($ap_array);
        //print_r($mac_array);
        
        //Compare all found mac addresses with the mac we want
        for($i=0; $i<$mac_array_length; $i++)
        {
            $temp_mac_entry=$mac_array[$i];
            //print_r($temp_ap_entry);
            $temp_mac_entry_key=array_keys($temp_mac_entry);
            $temp_mac_entry_oid=$temp_mac_entry_key[0];
            $temp_mac_entry_mac=$temp_mac_entry[$temp_mac_entry_key[0]];
            //echo "mac: $temp_mac_entry_mac \nOID: $temp_mac_entry_oid \n\n";
            $temp_mac_entry_mac_with_colon = str_replace(" ", ":",$temp_mac_entry_mac);
            if(strcmp($mac, $temp_mac_entry_mac_with_colon)==0)
            {
                //Found the right OID!!!
                //Use the index to get the OID of the hostname
                $correct_ap_array_index=$i;
                $temp_ap_entry=$ap_array[$correct_ap_array_index];
                $temp_ap_entry_key=array_keys($temp_ap_entry);
                $temp_ap_entry_oid=$temp_ap_entry_key[0];
                $temp_ap_entry_hostname=$temp_ap_entry[$temp_ap_entry_key[0]];
                //echo "correct hostname: $temp_ap_entry_hostname \n correct OID: $temp_ap_entry_oid \n\n";
                $oid_of_hostname = $temp_ap_entry_oid;
                $old_hostname = $temp_ap_entry_hostname;
                break;
            }
        }
        //if the index was not set, we have not found the right accesspoint OID on the controller
        if(!isset($correct_ap_array_index)) 
        { 
            echo "Error, Access Point not found on the controller!"; 
            return -1;
        }    
        
        //Formating the data 
        echo "Setting Hostname: ";
        echo $old_hostname;
        echo " to new Hostname: ";
        //Overwrite available space with #
        $new_hostname = '###############';
        //Copy old hostname in new string
        $new_hostname = substr_replace($new_hostname, $old_hostname, 0, null);
        //Remove trailing 0x from data string
        $data_wo_zerox = substr($data,2); // hex hardcoded value without trailing 0x
        //extracting the BEPS ID
        $beps_id=substr($data_wo_zerox,0,2);
        //extracting the hardcoded value
        $hardcoded_value=substr($data_wo_zerox,2,2);
        //extracting the codebook value
        $codebook_value=substr($data_wo_zerox,4,2);
        //convert beps ID to ascii
        $beps_id_ascii=chr("0x".$beps_id);
        //convert hardcoded value to ascii
        $hardcoded_value_ascii=chr("0x".$hardcoded_value);
        //convert codebook value to ascii
        $codebook_value_ascii=chr("0x".$codebook_value);
        //Start writing the ascii values to the new hostname string
        $new_hostname = substr_replace($new_hostname, "^", 10);
        $new_hostname = substr_replace($new_hostname, $beps_id_ascii, 11);
        $new_hostname = substr_replace($new_hostname, $hardcoded_value_ascii, 12);
        $new_hostname = substr_replace($new_hostname, $codebook_value_ascii, 13);
        $new_hostname = substr_replace($new_hostname, "^", 14);
        echo $new_hostname;
        echo ' / OID: ';
        echo $oid_of_hostname;
        echo '<br>';
        //Send new hostname to controller
        $return_value = $snmp->set($ip, $oid_of_hostname, $new_hostname, 's', array('v3_flags'=>SNMP_AUTH_PRIV, 'v3_user'=>$user,'v3_auth'=>$pass1, 'v3_priv'=>$pass2));
        return 0;
    }
    
    
    public function setFlexibleData($mac, $data_array)
    {
        return -2;
    }
    
    public function setMixedData($mac, $data_array)
    {
        return -2;
    }
    
}
?>
