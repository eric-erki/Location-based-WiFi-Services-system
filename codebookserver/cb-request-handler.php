<?php
mysql_connect("localhost","lows-ctrl-user","QQLDqennfCYev6Kx");
mysql_select_db("lows-control-db");

$version_request = mysql_query("SELECT * FROM codebook");
$version_row = mysql_fetch_object($version_request) or die (mysql_error());
$cb_version=$version_row->version;
 
$q=mysql_query("SELECT * FROM ldc_codebook_entry");
while($e=mysql_fetch_assoc($q))
{
    $e["version"]=$cb_version;
    $output[]=$e;

} 

print(json_encode($output));
 
mysql_close();
?>
