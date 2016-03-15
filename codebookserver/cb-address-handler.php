<?php

$cbs_ip="192.168.68.1";
$gcbas_ip="192.168.68.3";
$db_user="cbs-tu-berlin";
$db_pass="VLmGSpDbZ2jGbJLs";

mysql_connect("localhost","lows-ctrl-user","QQLDqennfCYev6Kx");
mysql_select_db("lows-control-db");

$q=mysql_query("SELECT * FROM access_point_changes");
while($e=mysql_fetch_object($q))
{
    
    if(strcmp($e->reason, "new")==0)
    {
        $new_macs[]=$e->access_point_mac;
    }
    else if(strcmp($e->reason, "deleted")==0)
    {
        $deleted_macs[]=$e->access_point_mac;
    }
}



# Create a connection
$url = "http://".$gcbas_ip."/gcbas/cba-request-handler.php";

$fields = array(
						'user' => urlencode($db_user),
						'pass' => urlencode($db_pass),
                        'ip' => urlencode($cbs_ip)
);


if(isset($new_macs))
{
    for($i=0; $i<count($new_macs); $i++)
    {
        $temp_mac=$new_macs[$i];
        $fields[$temp_mac]=urlencode('new');
    }
}
if(isset($deleted_macs))
{
    for($i=0; $i<count($deleted_macs); $i++)
    {
        $temp_mac=$deleted_macs[$i];
        $fields[$temp_mac]=urlencode('deleted');
    }
}

$fields_string=null;
//url-ify the data for the POST
foreach($fields as $key=>$value) { $fields_string .= $key.'='.$value.'&'; } 
rtrim($fields_string, '&');
//open connection
$ch = curl_init();

//set the url, number of POST vars, POST data
curl_setopt($ch,CURLOPT_URL, $url);
curl_setopt($ch,CURLOPT_POST, count($fields));
curl_setopt($ch,CURLOPT_POSTFIELDS, $fields_string);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true); 

//execute post

$result = curl_exec($ch);

//close connection
curl_close($ch);

//echo $result;




if(strcmp($result, "OK")==0)
{
    mysql_connect("localhost","lows-ctrl-user","QQLDqennfCYev6Kx");
    mysql_select_db("lows-control-db");

    if(isset($new_macs))
    {
        for($i=0; $i<count($new_macs); $i++)
        {
            $temp_mac=$new_macs[$i];
            $request = "DELETE FROM access_point_changes WHERE access_point_mac LIKE '$temp_mac' AND reason LIKE 'new'"; 
            $result = mysql_query($request) or die (mysql_error());
        }
        echo "Added ".count($new_macs)." AP(s)\n";
    }
    else
    {
        echo "No new APs added...\n";
    }
    if(isset($deleted_macs))
    {
        for($i=0; $i<count($deleted_macs); $i++)
        {
            $temp_mac=$deleted_macs[$i];
            $request = "DELETE FROM access_point_changes WHERE access_point_mac LIKE '$temp_mac' AND reason LIKE 'deleted'"; 
            $result = mysql_query($request) or die (mysql_error());
        }
        echo "Deleted ".count($deleted_macs)." AP(s)\n";
    }
    else
    {
        echo "No APs deleted...\n";
    }

    mysql_close();
}
else
{
    echo "Error happend: ".$result."\n";
}
?>
