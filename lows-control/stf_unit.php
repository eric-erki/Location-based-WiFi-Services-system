<?php 
session_start(); 
?> 

<?php 
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
        echo '<b>->Service Data</b><br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Please choose the service data</h1>';
        echo '<p>                                                                                       ';
    }
    else if(strcmp($page,"select_codebook_value")==0)
    {
        echo 'Service Type<br>';
        echo 'Access Point<br>';
        echo '<b>->Service Data</b><br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Please select location specific value from codebook</h1>';
        echo '<p>                                                                                       ';
    }
    else if(strcmp($page,"ack_flexible_data")==0)
    {
        echo 'Service Type<br>';
        echo 'Access Point<br>';
        echo '<b>->Service Data</b><br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Please acknowledge your request</h1>';
        echo '<p>                                                                                       ';
    }
    else if(strcmp($page,"ack_custom_data")==0)
    {
        echo 'Service Type<br>';
        echo 'Access Point<br>';
        echo '<b>->Service Data</b><br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Please acknowledge your request</h1>';
        echo '<p>                                                                                       ';
    }
        
    //echo "PAGE: ".$page."<br><br>";
    echo "Logged in as: ";
    echo $_SESSION["username"];
    //echo ", userid:".$userid."<br>";
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
    $row = mysql_fetch_object($request);
    
    $admin = $row->role;
    
    
    /*Get available Service Types and list them*/
    
    $request = "SELECT hexcode, name, st_type FROM service"; 
    $result = mysql_query($request); 
    //$service_types = mysql_fetch_object($result); 
    /*
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
    */
    $chosen_type=$_SESSION[chosen_type];
    $chosen_service_type_num=$_SESSION[service_type_num];
    $chosen_service_type_hex=$_SESSION[service_type_hex];
    //echo "<br><br> Chosen parameters:<br>";
    //echo "chosen type: ".$chosen_type.", chosen service type number: ".$chosen_service_type_num.", chosen service type hex: ".$chosen_service_type_hex;
    
    
    if($page==start)
    {
        //print_r($_POST);
        $_SESSION[set_entries_delete]=array_keys($_POST);
        $_SESSION[set_entries_delete_count]=count($_POST);
        //$_SESSION["ap_array"]=$_POST;
        //$_SESSION["ap_count"]=count($_POST);;
        $chosen_ap_names=$_SESSION["ap_keys"]; //mac addresses
        $chosen_ap_count=$_SESSION["ap_count"]; //number of chosen access points
        $chosen_ap_array=$_SESSION["ap_array"]; //access point names array with mac addresses as key
        //echo "<br>chosen access_points: ";
        //print_r($chosen_ap_array);
        //echo "<br>";
        //if(!isset($_SESSION["codebook"])) 
        //{
        //    $_SESSION["codebook"]=1;
        //}
        //echo "<br>codebook round: ".$_SESSION["codebook"]."<br>";
        echo "<br><br>";
        //New service types must register here in advance to the mysql entry
        if(strcmp("0x21",$chosen_service_type_hex)==0) //BEPS
        {
            //Hardcoded Codebook part
            //$_SESSION["codebook"]++;
            echo "Please select HASC emergency reason:<br>";
            //echo "BEPS";
            echo '<br>';
            //echo '<br>';
            echo '<form action="?page=select_codebook_value" method="post">';
            echo '<br>';
            //List all before chosen APs with dropdown menue containing all hardcoded BEPS Strings
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                //Mac address of AP
                echo '<select name="';
                echo $chosen_ap_array[$chosen_ap_names[$i]];
                echo '">';
                //get all the hardcoded values and their data for BEPS and display them as dropdown
                $result = mysql_query("SELECT lic, data FROM lic_cb_value WHERE service_hexcode LIKE '$chosen_service_type_hex'");
                while($row = mysql_fetch_object($result))
                {
                    echo "<option value=\"".$row->lic."\">".$row->data."</option>";
                }
                echo '</select>';
                echo '<br>';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else if(strcmp("0x3f",$chosen_service_type_hex)==0)
        {
            //echo "PSA";
            //Hardcoded Codebook part
            echo "Please select Physical Service Announcement Type:<br>";
            echo '<br>';
            //echo '<br>';
            echo '<form action="?page=select_codebook_value" method="post">';
            echo '<br>';
            //List all before chosen APs with dropdown menue containing all hardcoded BEPS Strings
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                //Mac address of AP
                echo '<select name="';
                echo $chosen_ap_array[$chosen_ap_names[$i]];
                echo '">';
                //get all the hardcoded values and their data for PSA and display them as dropdown
                $result = mysql_query("SELECT lic, data FROM lic_cb_value WHERE service_hexcode LIKE '$chosen_service_type_hex'");
                while($row = mysql_fetch_object($result))
                {
                    echo "<option value=\"".$row->lic."\">".$row->data."</option>";
                }
                echo '</select>';
                echo '<br>';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else if(strcmp("0x20",$chosen_service_type_hex)==0)
        {
            //echo "ST";
            echo "Please Type in the string(s) you want to send (max 250 characters)<br>";
            echo '<br>';
            echo '<form action="?page=ack_flexible_data" method="post">';
            echo '<br>';
            //List all before chosen APs with textbox
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                //Mac address of AP
                echo "<INPUT TYPE = \"Text\" VALUE =\"String to send (max 250)\" NAME = \"".$temp_mac."\">";
                echo '<br>';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else if(strcmp("0x10",$chosen_service_type_hex)==0)
        {
            echo "Please Type in the current number and the name of the room/service the number belongs to:<br>";
            echo '<br>';
            echo '<form action="?page=ack_flexible_data" method="post">';
            echo '<br>';
            //List all before chosen APs with textbox
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                //Mac address of AP
                echo "<INPUT TYPE = \"Text\" VALUE =\"Room/Service:\" NAME = \"".$temp_mac."|R"."\">";
                echo "<INPUT TYPE = \"Text\" VALUE =\"New Number:\" NAME = \"".$temp_mac."|N"."\">";
                echo '<br>';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else if(strcmp("ccbt",$chosen_service_type_hex)==0)
        {
            //echo "ST";
            echo "Please Type in the hexstring you want to send (max 6 characters without 0x, on cisco only hex values that map to ascii chars are possible)<br>";
            echo '<br>';
            echo '<form action="?page=ack_custom_data" method="post">';
            echo '<br>';
            //List all before chosen APs with textbox
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                //Mac address of AP
                echo "<INPUT TYPE = \"Text\" VALUE =\"\" NAME = \"".$temp_mac."\" maxlength=\"6\">";
                echo '<br>';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else if(strcmp("cfbt",$chosen_service_type_hex)==0)
        {
            //echo "ST";
            echo "Please Type in the hexstring you want to send (max 252 characters)<br>";
            echo '<br>';
            echo '<form action="?page=ack_custom_data" method="post">';
            echo '<br>';
            //List all before chosen APs with textbox
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                //Mac address of AP
                echo "<INPUT TYPE = \"Text\" VALUE =\"\" NAME = \"".$temp_mac."\" maxlength=\"252\">";
                echo '<br>';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else
        {
            echo "ERROR unknown service type";
        }
        
    }
    
    
    else if($page==select_codebook_value)
    {
        $chosen_ap_names=$_SESSION["ap_keys"];
        $chosen_ap_count=$_SESSION["ap_count"];
        $chosen_ap_array=$_SESSION["ap_array"];
        $chosen_hardcoded_values=$_POST;
        $chosen_hardcoded_values_array_keys=array_keys($_POST);
        //echo "<br>chosen access_points: ";
        //print_r($chosen_ap_array);
        //echo "<br>chosen chosen_ap_names: ";
        //print_r($chosen_ap_names);
        //echo "<br>chosen hardcoded values: ";
        //print_r($chosen_hardcoded_values);
        echo "<br><br>";

        //Custom location specific Codebook part
        echo "Please select location dependent codebook (ldc) part:<br>";
        echo '<br>';
        //echo '<br>';
        //print_r($_POST);
        echo '<form action="sending_unit.php" method="post">';
        echo '<br>';
        for($i=0; $i<$chosen_ap_count; $i++)
        {
            $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
            $result = mysql_query("SELECT name, mac FROM access_point WHERE mac LIKE '$temp_mac'");
            $row = mysql_fetch_object($result);
            $temp_ap_name = $row->name;
            $temp_hardcoded_value = $chosen_hardcoded_values[$chosen_hardcoded_values_array_keys[$i]];
            
            //echo '<input type="hidden" name="'.$post_keys[$i].'/h" value="'; //hardcoded value
            //echo $temp_hardcoded_value;
            //echo '" />';
            
            echo "<br><b>".$temp_ap_name."</b> (".$temp_mac.")<br>";
            $result = mysql_query("SELECT data FROM lic_cb_value WHERE (lic LIKE '$temp_hardcoded_value' AND service_hexcode LIKE '$chosen_service_type_hex')")or die (mysql_error());;
            $row = mysql_fetch_object($result);
            echo " ".$row->data." ";
            echo '<select name="';
            echo $temp_mac; 
            echo '">';
            //$temp_mac = $chosen_hardcoded_values_array_keys[$i];
            $temp_hardcoded_value_without_zero_x = substr($temp_hardcoded_value,2); // hex hardcoded value without trailing 0x
            //echo "<option value=\"".$chosen_service_type_hex.$temp_hardcoded_value_without_zero_x."30"."\">"."No location specific custom codebook value (0x30)"."</option>";
            echo "<option value=\"".$temp_hardcoded_value."30"."\">"."No location specific custom codebook value (0x30)"."</option>";
            $result = mysql_query("SELECT data, ldc FROM ldc_codebook_entry WHERE access_point_mac LIKE '$temp_mac' AND service_hexcode LIKE '$chosen_service_type_hex' AND lic LIKE '$temp_hardcoded_value'")or die (mysql_error());;
            while($row = mysql_fetch_object($result))
            {
                $hex_cbv_without_zero_x = substr($row->ldc, 2); // hex codebook value without trailing 0x
                $temp_data = $temp_hardcoded_value_without_zero_x.$hex_cbv_without_zero_x;
                //echo "<option value=\"".$chosen_service_type_hex.$temp_hardcoded_value_without_zero_x.$hex_cbv_without_zero_x."\">".$row->data."</option>";
                echo "<option value=\"".$temp_hardcoded_value.$hex_cbv_without_zero_x."\">".$row->data."</option>";
            }
            echo '</select>';
            echo '<br>';
        }
        echo '<p><input type="submit" /></p>';
        echo '</form>';

    }
    
    else if($page==ack_custom_data)
    {
        $chosen_ap_names=$_SESSION["ap_keys"];
        $chosen_ap_count=$_SESSION["ap_count"];
        $chosen_ap_array=$_SESSION["ap_array"];
        $chosen_send_text_values=$_POST;
        $chosen_send_text_array_keys=array_keys($_POST);
        //echo "<br>chosen access_points: ";
        //print_r($chosen_ap_array);
        echo "<br>";
        //if(!isset($_SESSION["codebook"])) 
        //{
        //    $_SESSION["codebook"]=1;
        //}
        //echo "<br>codebook round: ".$_SESSION["codebook"]."<br>";
        if(strcmp("ccbt",$chosen_service_type_hex)==0 OR strcmp("cfbt",$chosen_service_type_hex)==0)
        {
            echo "<br><br>";
            echo "Please acknownledge your command<br>";
            echo '<br>';
            echo '<form action="sending_unit.php" method="post">';
            echo '<br>';
            //List all before chosen APs with the text set before
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                $temp_send_text_value = $chosen_send_text_values[$chosen_send_text_array_keys[$i]];
                
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ".$temp_send_text_value."<br>";
                //Mac address of AP
                echo '<br>';
                echo '<input type="hidden" name="'.$temp_mac.'"value="'; 
                //$chosen_service_type_hex_without_zero_x = substr($chosen_service_type_hex,2); // without trailing 0x
                //echo "0x80".$chosen_service_type_hex_without_zero_x.$temp_send_text_hex;
                echo "0x".$temp_send_text_value;
                echo '" />';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        
    }
    else if($page==ack_flexible_data)
    {
        $chosen_ap_names=$_SESSION["ap_keys"];
        $chosen_ap_count=$_SESSION["ap_count"];
        $chosen_ap_array=$_SESSION["ap_array"];
        $chosen_send_text_values=$_POST;
        $chosen_send_text_array_keys=array_keys($_POST);
        //echo "<br>chosen access_points: ";
        //print_r($chosen_ap_array);
        echo "<br>";
        //if(!isset($_SESSION["codebook"])) 
        //{
        //    $_SESSION["codebook"]=1;
        //}
        //echo "<br>codebook round: ".$_SESSION["codebook"]."<br>";
        if(strcmp("0x20",$chosen_service_type_hex)==0)
        {
            echo "<br><br>";
            echo "Please acknownledge your command<br>";
            echo '<br>';
            echo '<form action="sending_unit.php" method="post">';
            echo '<br>';
            //List all before chosen APs with the text set before
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                $temp_send_text_value = $chosen_send_text_values[$chosen_send_text_array_keys[$i]];
                $ascii = $temp_send_text_value;
                $hex = '';
                for ($j = 0; $j < strlen($ascii); $j++) {
                    $byte = strtoupper(dechex(ord($ascii{$j})));
                    $byte = str_repeat('0', 2 - strlen($byte)).$byte;
                    $hex.=$byte."";
                }
                $temp_send_text_hex = $hex;
                $was_cut=false;
                if(strlen($temp_send_text_value)>250)
                {
                    $temp_send_text_value=substr($temp_send_text_value,0,250);
                    $was_cut=true;
                }
                //Display AP Name
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ".$temp_send_text_value."<br>->0x".$temp_send_text_hex."<br>";
                if($was_cut==true)
                {
                    echo "shortened to fit 250 bytes<br>";
                }
                //Mac address of AP
                echo '<br>';
                echo '<input type="hidden" name="'.$temp_mac.'"value="'; 
                $chosen_service_type_hex_without_zero_x = substr($chosen_service_type_hex,2); // without trailing 0x
                //echo "0x80".$chosen_service_type_hex_without_zero_x.$temp_send_text_hex;
                echo "0x".$temp_send_text_hex;
                echo '" />';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
        else if(strcmp("0x10",$chosen_service_type_hex)==0)
        {
            echo "<br><br>";
            echo "Please acknownledge your command<br>";
            echo '<br>';
            echo '<form action="sending_unit.php" method="post">';
            echo '<br>';
            for($i=0; $i<$chosen_ap_count; $i++)
            {
                $temp_mac=$chosen_ap_array[$chosen_ap_names[$i]];
                $temp_room_name = $chosen_send_text_values[$chosen_send_text_array_keys[($i*2)]];
                $temp_room_number = $chosen_send_text_values[$chosen_send_text_array_keys[(($i*2)+1)]];
                if(is_numeric($temp_room_number)==false)
                {
                    echo "<b>ERROR Number belonging to:".$temp_room_name." is not numeric!!</b>";
                    break;
                }

                $intnum = intval($temp_room_number,10);
                if($intnum>65536)
                {
                    echo "<b>ERROR Number belonging to:".$temp_room_name." is too big, max 65536!</b>";
                    break;
                }
                $hexnum = dechex($intnum);
                $length_hexnum=strlen($hexnum);
                echo "<br><b>".$chosen_ap_names[$i]."</b><br> ";
                echo "NAME: ".$temp_room_name.", NUMBER: ".$temp_room_number;//."(".$hexnum.")";
                echo '<br>';
                $hexnum_to_send = (string)$hexnum;
                
                for($k=0; $k<(4-$length_hexnum);$k++)
                {
                    $hexnum_to_send = "0".$hexnum_to_send;
                }
                
                $ascii = $temp_room_name;
                $hex = '';
                for ($j = 0; $j < strlen($ascii); $j++) {
                    $byte = strtoupper(dechex(ord($ascii{$j})));
                    $byte = str_repeat('0', 2 - strlen($byte)).$byte;
                    $hex.=$byte."";
                }
                $temp_room_name_hex = $hex;
                                
                echo '<input type="hidden" name="'.$temp_mac.'"value="'; 
                $chosen_service_type_hex_without_zero_x = substr($chosen_service_type_hex,2); // without trailing 0x
                //echo "0x80".$chosen_service_type_hex_without_zero_x.$hexnum_to_send.$temp_room_name_hex;
                echo "0x".$hexnum_to_send.$temp_room_name_hex;
                echo '" />';
            }
            echo '<p><input type="submit" /></p>';
            echo '</form>';
        }
    }
    
    else
    {
        echo "<br> <b>Page not found!</b>";
        print_r($_POST);
    }
    echo "<br><br><br><br><form><input type=\"button\" value=\"Restart\" onClick=\"window.location.href='lows-control.php'\"></form>";
    echo "<form><input type=\"button\" value=\"Logout\" onClick=\"window.location.href='login.html'\"></form>";
}
include("footer.php");
?> 




