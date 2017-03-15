<?php
require_once 'vendor/autoload.php';

//The router class is the main entry point for interaction.
$router = new HSPDev\HuaweiApi\Router;

//If specified without http or https, assumes http://
$router->setAddress('192.168.8.1');

//Username and password. 
//Username is always admin as far as I can tell.
$router->login('admin', 'admin');

ob_start();
var_dump($router->getSmsCount());

$myfile = fopen("newfile.txt", "w") or die("Unable to open file!");
$txt = ob_get_clean();
fwrite($myfile, $txt);
fclose($myfile);

echo 'start';
$cmd = shell_exec('sudo python /home/pi/huawei-api/Huawei-E5180-API/read_and_publish.py');
echo 'end';
echo $cmd;

