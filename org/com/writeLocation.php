<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];

	
	$sim = $_POST['sim'];
	$time = $_POST['time'];
	$straddleID = $_POST['straddleID'];
	$x = $_POST['x'];
	$y = $_POST['y'];
	$z = $_POST['z'];
	$contID = $_POST['contID'];
	$destination = $_POST['destination'];
	
	$roadID = $_POST['roadID'];
	$direction = $_POST['direction'];
	$rate = $_POST['rate'];
	
	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$req = 'INSERT INTO locations (sim,time,id,roadID,direction,rate,x,y,z,contID,destination) VALUES (\'' . $sim .'\',\'' . $time . '\',\'' . $straddleID . '\',\'' . $roadID . '\',\'' . $direction . '\',\'' . $rate . '\',\'' . $x .'\',\'' . $y . '\',\'' . $z . '\',\'' . $contID .'\',\'' . $destination . '\')';
	$bdd->exec($req);
}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>
