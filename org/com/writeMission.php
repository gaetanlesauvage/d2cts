<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];
	
	$sim = $_POST['sim'];
	$id = $_POST['id'];
	$contID = $_POST['contID'];
	$tw_p = $_POST['tw_p'];
	$tw_d = $_POST['tw_d'];
	$destination = $_POST['destination'];
	$level = $_POST['level'];
	$alignment = $_POST['alignment'];
	$type = $_POST['type'];
	$knowing_time = $_POST['knowing_time'];

	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$req = 'INSERT INTO missions (sim,id,contID,tw_p,tw_d,destination,level,alignment,type,knowing_time) VALUES (\'' . $sim .'\',\'' . $id . '\',\'' . $contID . '\',\'' . $tw_p .'\',\'' . $tw_d . '\',\'' . $destination . '\',\'' . $level .'\',\'' . $alignment . '\',\'' . $type . '\',\'' . $knowing_time . '\')';
	$bdd->exec($req);
}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>
