<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];

	
	$sim = $_POST['sim'];
	$time = $_POST['time'];
	$type = $_POST['type'];
	$value = $_POST['value'];
	
	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$req = 'INSERT INTO events (sim,time,type,value) VALUES (\'' . $sim .'\',\'' . $time . '\',\'' . $type . '\',\'' . $value .'\')';
	$bdd->exec($req);
}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>
