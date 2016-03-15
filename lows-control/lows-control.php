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
        echo '<b>->Service Type</b><br>';
        echo 'Access Point<br>';
        echo 'Service Data<br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Please choose the service type you want to use from the drop down list</h1>';
        echo '<p>                                                                                       ';
    }
    else if(strcmp($page,"ap_select")==0)
    {
        echo 'Service Type<br>';
        echo '<b>->Access Point</b><br>';
        echo 'Service Data<br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Please choose the access point(s) where you want to apply the service to:</h1>';
        echo '<p>                                                                                       ';
    }
    else if(strcmp($page,"show_current_sending")==0)
    {
        echo 'Service Type<br>';
        echo '<b>->Access Point</b><br>';
        echo 'Service Data<br>';
        echo 'Processing<br>';
        echo '</div>                                                                                    ';
        echo '                                                                                          ';
        echo '<div id="section">                                                                        ';
        echo '<h1>Your selected access points are currently sending:</h1>';
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
    $row = mysql_fetch_object($result);
    
    $admin = $row->role;
    $_SESSION["admin"]=$admin;
    
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
    */
    /*
    echo "<br> User permissions:";
    if($admin == 1)
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
    
    if($page==start)
    {
        /*This is called when the page is visited the first time*/
        echo "<br><br><br>";        
        echo "<form action=\"?page=ap_select\" method=\"post\">";
        echo "<p>";
        echo "<select name=\"chosen_type\" size=$types_count>";
        if($admin==1)
        {
            $request = "SELECT hexcode, name, st_type FROM service"; 
            $result = mysql_query($request); 
            //$service_types = mysql_fetch_object($result); 
            //echo "<br> Supported Service Types (Service Types in DB): <br>";
            while($row = mysql_fetch_object($result))
            {
                echo "<option>";
                echo $row->name;
                echo "</option>";
            }
        }
        else
        {
            for($i=0; $i<$types_count; $i++)
            {    
                echo "<option>";
                $hex_type = $types[$i];
                $result = mysql_query("SELECT name FROM service WHERE hexcode LIKE '$hex_type'") or die (mysql_error());
                $row = mysql_fetch_object($result);
                if(!isset($row->name))
                {
                    echo "Not in DB: ".$hex_type;
                }
                else
                {
                    echo $row->name;
                }
                echo "</option>";
            }   
        }
        echo "</select>";
        echo "</p>";
        echo "<input type=\"submit\">";
        echo "</form>";
        echo "<br>";
        
    }
    else if($page==ap_select)
    {

        $chosen_type = $_POST["chosen_type"];
        $result = mysql_query("SELECT st_type, hexcode FROM service WHERE name LIKE '$chosen_type'");
        $row = mysql_fetch_object($result);
        $service_type_num = $row->st_type;
        $service_type_hex = $row->hexcode;
        $_SESSION[chosen_type]=$chosen_type;
        $_SESSION[service_type_num]=$service_type_num;
        $_SESSION[service_type_hex]=$service_type_hex;
        echo "<br><br>Your chosen type: <b>".$chosen_type."</b>";
        //echo ", type_num: ".$service_type_num;
        echo "<br><br>";
        echo "Please select AP(s) to connect to:<br><hr>";
        
        echo "<form action=\"?page=show_current_sending\" method=\"post\">";
        if($_SESSION["admin"]==1)
        {
            $request = "SELECT mac, name, ap_type FROM access_point WHERE ap_connection_data_id IS NOT NULL"; 
            $result = mysql_query($request); 
            //$service_types = mysql_fetch_object($result); 
            //echo "<br> Supported Service Types (Service Types in DB): <br>";
            
            while($row = mysql_fetch_object($result))
            {
                $service_supported=true;
                if(strcmp($row->ap_type,"cisco")==0)
                {
                    //Cisco only supports codebook types
                    if(strcmp($service_type_num,"flexible")==0)
                    {
                        $service_supported=false;
                    }
                }
                else if(strcmp($row->ap_type,"openwrt")==0)
                {
                    //openwrt supports codebook and flexible type
                }
                else
                {
                    //unknown type
                    echo "<br>ERROR AP type not known<br>";
                    $service_supported=false;
                }
                if($service_supported==true)
                {
                    echo "<input type=\"checkbox\" name=\"";
                    echo $row->name; 
                    echo "\" value=\"";  
                    echo $row->mac;
                    echo "\">";
                    echo "<b>".$row->name.", ".$row->mac."</b><br>";
                }
            }
        }
        else
        {
            for($i=0; $i<$macs_count; $i++)
            {
                //echo "<br> Testing #".$macs[$i]."#<br>";
                $result = mysql_query("SELECT mac FROM access_point WHERE mac LIKE '$macs[$i]' AND ap_connection_data_id IS NOT NULL");
                if(mysql_num_rows($result) == 0)
                {
                    //echo "does not exist";
                }
                else
                {
                    //echo "does exist";
                    
                    $result = mysql_query("SELECT name, ap_type FROM access_point WHERE mac LIKE '$macs[$i]'");
                    $row = mysql_fetch_object($result);
                    $service_supported=true;
                    
                    //New Adapters must register here to check if codebook and/or flexible type(s) are supported 
                    if(strcmp($row->ap_type,"cisco")==0)
                    {
                        //Cisco only supports codebook types
                        if(strcmp($service_type_num,"flexible")==0)
                        {
                            $service_supported=false;
                        }
                    }
                    else if(strcmp($row->ap_type,"openwrt")==0)
                    {
                        //openwrt supports codebook and flexible type
                    }
                    else
                    {
                        //unknown type
                        echo "<br>ERROR AP type not known<br>";
                        $service_supported=false;
                    }
                    
                    
                    
                    if($service_supported==true)
                    {
                        echo "<input type=\"checkbox\" name=\"";
                        echo $row->name; 
                        echo "\" value=\"";  
                        echo $macs[$i];
                        echo "\">";
                        echo "<b>".$row->name.", ".$macs[$i]."</b><br>";
                    }
                }
            }
        }
        echo "<p><input type=\"submit\" /></p>";
        echo "</form>";
 
    }
    else if($page==show_current_sending)
    {
        $_SESSION["ap_keys"]=array_keys($_POST);
        $_SESSION["ap_array"]=$_POST;
        $_SESSION["ap_count"]=count($_POST);;
        $chosen_ap_names=$_SESSION["ap_keys"]; //ap names
        $chosen_ap_count=$_SESSION["ap_count"]; //number of chosen access points
        $chosen_ap_array=$_SESSION["ap_array"]; //access point macs array with ap names as key
        //echo "<br>chosen access_points: ";
        //print_r($chosen_ap_array);
        echo "<br>";
        echo "<form action=\"stf_unit.php\" method=\"post\">";
        for($i=0; $i< $chosen_ap_count; $i++)
        {
            $temp_ap_name = $chosen_ap_names[$i];
            $temp_mac = $chosen_ap_array[$chosen_ap_names[$i]];
            echo "<br>";
            echo $temp_ap_name."(".$temp_mac.") is currently sending:<br>";
            $result_se = mysql_query("SELECT * FROM running_services WHERE access_point_mac LIKE '$temp_mac'");
            if(mysql_num_rows($result_se)<1)
            {
                echo "-nothing<br>";
            }
            $j=1;
            while($row_se = mysql_fetch_object($result_se))
            {
                $result_ap = mysql_query("SELECT * FROM access_point WHERE mac LIKE '$temp_mac'");
                $row_ap = mysql_fetch_object($result_ap);
                $temp_ap_type=$row_ap->ap_type;
                $result_st = mysql_query("SELECT * FROM service WHERE hexcode LIKE '$row_se->service_hexcode'");
                $row_st = mysql_fetch_object($result_st);
                $temp_service_type_name=$row_st->name;
                echo $j.". ".$temp_service_type_name." (";
                echo $row_se->service_hexcode.", ";
                echo $row_se->data.")";
                if(strcmp($temp_ap_type, "cisco")==0)
                {
                    echo "<br>-> <b>will be replaced with your new request</b><br>";
                }
                if(strcmp($temp_ap_type, "openwrt")==0)
                {
                    echo "<input type=\"checkbox\" name=\"";
                    echo $row_se->id; 
                    echo "\" value=\"";  
                    echo $temp_mac;
                    echo "\">";
                    echo "<b>"."delete"."</b><br>";
                }
                $j++;
            }
    
        }
        echo "<p><input type=\"submit\" /></p>";
        echo "</form>";
    }
    else
    {
        echo "<br> <b>Page not found!</b>";
    }
    echo "<br><br><br><br><form><input type=\"button\" value=\"Restart\" onClick=\"window.location.href='lows-control.php'\"></form>";
    echo "<form><input type=\"button\" value=\"Logout\" onClick=\"window.location.href='login.html'\"></form>";
}
include("footer.php");
?> 



