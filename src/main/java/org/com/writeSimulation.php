<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];
	
	$simID = $_POST['simID'];
	
	$stepSize = $_POST['stepSize'];
	$distribFile = $_POST['distribFile'];
 	


	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$req = 'SELECT * FROM simulations WHERE simID=\'' . $simID . '\'';
	$reponse = $bdd->query($req);

	if($reponse->fetch() == false){
	    $req = 'INSERT INTO simulations (simID,distribFile,stepSize) VALUES (\'' . $simID .'\',\'' . $distribFile .'\',\'' . $stepSize . '\')';
	    $bdd->exec($req);
	}
	$reponse->closeCursor(); // Termine le traitement de la requête
}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>
