<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];

	$request = 'SELECT * FROM simulations';
	
	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$reponse = $bdd->query($request);
	if ($donnees = $reponse->fetch())
    {
	  	echo "connection accepted";
	}
	 $reponse->closeCursor();
}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>
