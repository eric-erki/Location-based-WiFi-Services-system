<?php

if(!isset($_POST['pass']) or !isset($_POST['user']) or !isset($_POST['ip']))
{
    echo "error wrong format!";
}
else
{   
    $db_user = $_POST['user'];
    $db_pass = $_POST['pass'];
    $cbs_ip = $_POST['ip'];
    $post_keys = array_keys($_POST);
    $post_number = count($post_keys);
    $macs_to_add=null;
    $macs_to_delete=null;
    //print_r($post_keys);
    for($i=3; $i<$post_number; $i++)
    {
        if(strcmp($_POST[$post_keys[$i]], "new")==0)
        {
            $macs_to_add[]=$post_keys[$i];
        }
        else if(strcmp($_POST[$post_keys[$i]], "deleted")==0)
        {
            $macs_to_delete[]=$post_keys[$i];
        }
    }  
    
    mysql_connect("localhost", $db_user, $db_pass);
    mysql_select_db("gcbas");
    if(isset($macs_to_add))
    {
        for($i=0; $i<count($macs_to_add); $i++)
        {
            $temp_mac=$macs_to_add[$i];
            $request_add = "INSERT INTO user_gcbae (mac, ip) VALUES('$temp_mac', '$cbs_ip')"; 
            $result_add = mysql_query($request_add) or die (mysql_error());
        }
    }
    if(isset($macs_to_delete))
    {
        for($i=0; $i<count($macs_to_delete); $i++)
        {
            $temp_mac=$macs_to_delete[$i];
            $request = "DELETE FROM user_gcbae WHERE mac LIKE '$temp_mac'"; 
            $result = mysql_query($request) or die (mysql_error());
        }
    }
    mysql_close();
    
    echo "OK";
}

?>
