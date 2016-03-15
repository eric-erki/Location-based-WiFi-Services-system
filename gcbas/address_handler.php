<?php
mysql_connect("localhost","root","toor");
mysql_select_db("gcbas");

$q=mysql_query("SELECT * FROM gcb_addr_entry WHERE mac LIKE '".$_REQUEST['mac']."'");
while($e=mysql_fetch_assoc($q))
        $output[]=$e;
 
print(json_encode($output));
 
mysql_close();
?>
