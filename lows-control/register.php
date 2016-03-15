<?php 
$connection = mysql_connect("localhost", "root" , "toor") 
or die("Can not access database"); 

mysql_select_db("lows-control-db") or die ("Can not choose database"); 

$username = $_POST["username"]; 
$password = $_POST["password"]; 
$password2 = $_POST["password2"]; 

include("header.php");
echo '<div id="nav">                                                                            ';
echo '<b>Register</b><br>                                                                          ';
echo '</div>                                                                                    ';
echo '                                                                                          ';
echo '<div id="section">                                                                        ';

if($password != $password2 OR $username == "" OR $password == "") 
    { 
    echo "Error, try again. <a href=\"register.html\">Back</a>"; 
    exit; 
    } 
$password = md5($password); 

$result = mysql_query("SELECT id FROM user WHERE username LIKE '$username'"); 
$amount = mysql_num_rows($result); 

if($amount == 0) 
    { 
    $entry = "INSERT INTO user (name, password) VALUES ('$username', '$password')"; 
    $inserted = mysql_query($entry); 

    if($inserted == true) 
        { 
        echo "user <b>$username</b> successfully registered. <a href=\"login.html\">Login</a>"; 
        } 
    else 
        { 
        echo "An error occured while trying to register the new user. <a href=\"register.html\">Back</a>"; 
        } 


    } 

else 
    { 
    echo "This name is already registered, please choose another. <a href=\"register.html\">back</a>"; 
    } 
    include("footer.php");
?>
