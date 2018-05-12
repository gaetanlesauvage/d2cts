<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];
	$request = $_POST['request'];
	$request = stripslashes($request);
	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);
	$bdd->exec($request);
}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>