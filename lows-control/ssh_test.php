<?php
include('Net/SSH2.php');

$ssh = new Net_SSH2('192.168.1.231');
if (!$ssh->login('owb', 'qwertzsz')) {
    exit('Login Failed');
}

echo $ssh->exec('pwd');
echo $ssh->exec('ls -la');
?>
