<?php 
session_start(); 
?> 

<?php 

require('adapter_cisco.php');
include("header.php");
if(!isset($_SESSION["username"])) 
{ 
   echo "Please log in first: <a href=\"login.html\">login</a>"; 
   exit; 
} 
else
{
    $username = $_SESSION["username"];
    $userid = $_SESSION["userid"];
    $page = $_GET["page"];
    if(!isset($page))
    {
        $page="start";
    }
    
    echo '<div id="nav">                                                                            ';
    if(strcmp($page,"start")==0)
    {
        echo 'Service Type<br>';
        echo 'Access Point<br>';
        echo 'Service Data<br>';
        echo '<b>->Processing</b><br>';
        echo "<br><br><form><input type=\"button\" value=\"Restart\" onClick=\"window.location.href='lows-control.php'\"></form>";
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Request processing report:</h1>';
        echo '<p>                                                                                       ';
    }
    //echo "PAGE: ".$page."<br><br>";
    echo "Logged in as: ";
    echo $_SESSION["username"];
    echo ", userid:".$userid."<br>";
    $connection = mysql_connect($_SESSION["sql_host"], $_SESSION["sql_user"], $_SESSION["sql_pass"]) or die("Connection to database server can not be established"); 
    mysql_select_db("lows-control-db") or die ("Could not select database table"); 
    
    
    /*Get the user permissions*/
    /*access point permissions:*/
    $request = "SELECT * FROM user_ap_permission WHERE user_id LIKE '$userid'"; 
    $result = mysql_query($request) or die (mysql_error());
    while($row = mysql_fetch_object($result))
    {
        $macs[] = $row->access_point_mac;
    }
    /*service type permissions:*/
    $request = "SELECT * FROM user_service_permission WHERE user_id LIKE '$userid'"; 
    $result = mysql_query($request) or die (mysql_error());
    while($row = mysql_fetch_object($result))
    {
        $types[] = $row->service_hexcode;
    }
    
    /*counting the permissions*/
    $types_count = count($types);
    $macs_count = count($macs);
    
    /*Find out if the user is an admin*/
    $request = "SELECT role FROM user WHERE id LIKE '$userid'"; 
    $result = mysql_query($request);
    $row = mysql_fetch_object($result);
    
    $admin = $row->role;
    
    
    /*Get available Service Types and list them*/
    
    $request = "SELECT hexcode, name, st_type FROM service"; 
    $result = mysql_query($request); 
    //$service_types = mysql_fetch_object($result); 
    echo "<br> Supported Service Types (Service Types in DB): <br>";
    while($row = mysql_fetch_object($result))
    {
        echo "$row->hexcode, $row->name, $row->st_type <br>";

    }
    
    echo "<br> User permissions:";
    if($admin == true)
    {
        echo "<br>Admin rights, all access points and all service types are allowed";
    }
    else
    {
        echo "<br> Allowed service types (hex): ";
        print_r($types);
        echo "<br> Accessible access points (MAC adresses): ";
        print_r($macs);
    }
    $chosen_type=$_SESSION[chosen_type];
    $chosen_service_type_num=$_SESSION[service_type_num];
    $chosen_service_type_hex=$_SESSION[service_type_hex];
    echo "<br><br> Chosen parameters:<br>";
    echo "chosen type: ".$chosen_type.", chosen service type number: ".$chosen_service_type_num.", chosen service type hex: ".$chosen_service_type_hex;
    echo "<br><br> Data to send:<br>";
    //print_r($_POST);
    $_SESSION["send_keys"]=array_keys($_POST);
    $_SESSION["send_array"]=$_POST;
    $_SESSION["send_count"]=count($_POST);
    $chosen_send_keys=$_SESSION["send_keys"];
    $chosen_send_array=$_SESSION["send_array"];
    $chosen_send_count=$_SESSION["send_count"];
    for($i=0; $i<$chosen_send_count; $i++)
    {
        echo "To ".$chosen_send_keys[$i]." send data: ".$chosen_send_array[$chosen_send_keys[$i]];
        $temp_mac=$chosen_send_keys[$i];
        $request = "SELECT ap_type FROM access_point WHERE mac LIKE '$temp_mac'"; 
        $result = mysql_query($request) or die (mysql_error());
        //print_r($result);
        $row = mysql_fetch_object($result) or die (mysql_error());

        echo ", use adapter: ".$row->ap_type."<br>";
    }
    
    if($page==start)
    {
        $set_entries_delete=$_SESSION[set_entries_delete];
        print_r($set_entries_delete);
        $set_entries_delete_count=$_SESSION[set_entries_delete_count];
        if($set_entries_delete_count>0)
        {
            echo "Deleting currently broadcasted service types selected before...<br>";
            for($p=0; $p<$set_entries_delete_count; $p++)
            {
                $temp_delete_id=$set_entries_delete[$p];
                $request = "DELETE FROM running_services WHERE id = '$temp_delete_id'"; 
                $result = mysql_query($request) or die (mysql_error());
                echo "Deleting id: ".$temp_delete_id."<br>";
            }
        }
        echo "<br>Now processing ".$chosen_send_count." requests<br>";
        for($i=0; $i<$chosen_send_count; $i++)
        {
            
            echo "<br> - Starting request #".$i." using ";
            $temp_send_data=$chosen_send_array[$chosen_send_keys[$i]];
            $temp_send_mac=$chosen_send_keys[$i];
            
            $request = "SELECT ap_type FROM access_point WHERE mac LIKE '$temp_send_mac'"; 
            $result = mysql_query($request) or die (mysql_error());
            $row = mysql_fetch_object($result) or die (mysql_error());
            $temp_send_ap_type=$row->ap_type;
            
            if(strcmp($temp_send_ap_type, "cisco")==0)
            {
                //Remove all currently saved entries for this ap
                $request = "DELETE FROM running_services WHERE access_point_mac LIKE '$temp_send_mac'"; 
                $result = mysql_query($request) or die (mysql_error());
            }
            
            echo "<br> Adding new entry into running_services table";
            $request_add = "INSERT INTO running_services (access_point_mac, service_hexcode, data) VALUES('$temp_send_mac', '$chosen_service_type_hex', '$temp_send_data')"; 
            //INSERT INTO tbl_name (col1,col2) VALUES(15,col1*2);
            $result_add = mysql_query($request_add) or die (mysql_error());
            
            unset($array_send_data);
            $flexible_count=0;
            $codebook_count=0;
            $mixed=false;
            $pure_codebook=false;
            $pure_flexible=false;
            echo "<br> Get the entries from the set_entries table";
            $request_data = "SELECT * FROM running_services WHERE access_point_mac LIKE '$temp_send_mac'"; 
            $result_data = mysql_query($request_data) or die (mysql_error());
            while($row_data = mysql_fetch_object($result_data))
            {
                $temp_service_type_hex = $row_data->service_hexcode;
                $temp_service_type_hex_wo0x=substr($temp_service_type_hex,2);
                $temp_data = substr($row_data->data,2);
                echo "<br>Get the service type type form service table";
                $request_st = "SELECT st_type FROM service WHERE hexcode LIKE '$temp_service_type_hex'"; 

                $result_st = mysql_query($request_st) or die (mysql_error());

                $row_st = mysql_fetch_object($result_st) or die (mysql_error());
                
                
                
                if(strcmp($row_st->st_type, "flexible")==0)
                {
                    if(strcmp($temp_service_type_hex,"cfbt")==0)
                    {
                        $array_send_data[] = "0x".$temp_data;
                    }
                    else
                    {
                        $array_send_data[] = "0x80".$temp_service_type_hex_wo0x.$temp_data;
                    }
                    $flexible_count++;
                }
                else if(strcmp($row_st->st_type, "codebook")==0)
                {
                    if(strcmp($temp_service_type_hex,"ccbt")==0)
                    {
                        $array_send_data[] = "0x".$temp_data;
                    }
                    else
                    {
                        $array_send_data[] = $temp_service_type_hex.$temp_data;
                    }
                    $codebook_count++;
                }
                else
                {
                    echo "Unsupported service type found while processing sending requests from set_entries table<br>";
                }
                echo "<br>3";
            }
            
            print_r($array_send_data);
            
            
            if($codebook_count>0 and $flexible_count ==0)
            {
                $pure_codebook=true;
            }
            else if($codebook_count==0 and $flexible_count>0)
            {
                $pure_flexible=true;
            }
            else if($codebook_count>0 and $flexible_count >0)
            {
                $mixed=true;
            }
            $returnValue=-3;
            if(strcmp($temp_send_ap_type,"cisco")==0)
            {   
                echo "adapter cisco<br>";
                $adapter_cisco = new adapter_cisco();
                
                if($pure_codebook==true and $codebook_count==1)
                {
                    $returnValue = $adapter_cisco->setCodebookData($temp_send_mac, $array_send_data);
                }
                else if($pure_codebook==true and $codebook_count>1)
                {
                    echo "<b>ERROR Cisco APs currently only support one Codebook Type per AP/b>";
                }
                else if($pure_flexible==true or $mixed==true)
                {
                    echo "<b>ERROR Cisco APs currently only support one Codebook Type per AP, no flexible types and no mixed requests/b>";
                }
                else
                {
                    echo "<b>ERROR service type not supported, please contact system administrator</b>";
                }
            }
            else if(strcmp($temp_send_ap_type,"openwrt")==0)
            {
                echo "adapter openwrt<br>";
                require('adapter_openwrt.php');
                $adapter_openwrt = new adapter_openwrt();
                if($pure_flexible==true)
                {
                    $returnValue = $adapter_openwrt->setFlexibleData($temp_send_mac, $array_send_data);
                }
                else if($pure_codebook==true)
                {
                    $returnValue = $adapter_openwrt->setCodebookData($temp_send_mac, $array_send_data);
                }
                else if($mixed==true)
                {
                    $returnValue = $adapter_openwrt->setMixedData($temp_send_mac, $array_send_data);
                }
                else
                {
                    echo "<b>ERROR service type not supported, please contact system administrator</b>";
                }
            }
            else
            {
                echo "<br> <b>Adapter ".$temp_send_ap_type." is not supported!</b><br>";
            }
            if(returnValue==0)
            {   
                echo "<br><b>Adapter returns: SUCCESS!</b>";
            }
            else if(returnValue==-1)
            {
                echo "<br><b>Adapter returns: Error!</b>";
            }
            else if(returnValue==-2)
            {
                echo "<br><b>Adapter returns: Not supported!</b>";
            }
            else if(returnValue==-3)
            {
                echo "<br><b>Adapter not available or not found</b>";
            }
        
        }
        
    }

    else
    {
        echo "<br> <b>Page not found!</b>";
        print_r($_POST);
    }
    echo "<br><br>";
    echo "<br><br><form><input type=\"button\" value=\"Process more Service Types\" onClick=\"window.location.href='lows-control.php'\"></form>";
    echo "<form><input type=\"button\" value=\"Logout\" onClick=\"window.location.href='login.html'\"></form>";
}
include("footer.php");
?> 
