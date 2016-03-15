<?php 
session_start(); 
?> 

<?php 
session_unset();

$_SESSION["sql_user"]="lows-ctrl-user";
$_SESSION["sql_pass"]="QQLDqennfCYev6Kx";
$_SESSION["sql_host"]="localhost";

$connection = mysql_connect($_SESSION["sql_host"], $_SESSION["sql_user"], $_SESSION["sql_pass"]) 
or die("Connection to database can not be established"); 
mysql_select_db("lows-control-db") or die ("Can not open database"); 

$username = $_POST["username"]; 
$password = md5($_POST["password"]); 

$request = "SELECT id, name, password FROM user WHERE name LIKE '$username' LIMIT 1"; 
$result = mysql_query($request); 
$row = mysql_fetch_object($result); 

if($row->password == $password) 
    { 
    $_SESSION["username"] = $username; 
    $_SESSION["userid"] = $row->id; 
    include("header.php");
    echo '<div id="nav">                                                                            ';
    echo '<b>Login</b><br>                                                                          ';
    echo '</div>                                                                                    ';
    echo '                                                                                          ';
    echo '<div id="section">                                                                        ';
    echo "Login successfull. Welcome ";
    echo $_SESSION["username"];
    echo "<br> <a href=\"lows-control.php\">Go to LoWS Control Center</a>"; 
    $request = "SELECT role FROM user WHERE name LIKE '$username' LIMIT 1"; 
    $result = mysql_query($request); 
    $row = mysql_fetch_object($result); 
    if($row->role == true) 
    {
        echo "<br> You are an adminstrator! your pass for Adminer: QQLDqennfCYev6Kx";
        echo "<br> <a href=\"adminer.php\">Go to Admin Page</a>"; 
    }
    } 
else 
    { 
    echo "User and/or password are not correct. <a href=\"login.html\">Login</a>"; 
    } 
include("footer.php");
?>
