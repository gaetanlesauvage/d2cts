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
	$bdd = new PDO($"mysql:host=$host;dbname=$dbName"$, $user, $password, $pdo_options);

	$reponse = $bdd->query($request);
?>
	<table>
<?php
	 while ($donnees = $reponse->fetch())
    {
?>

    	<tr>
<?php
    	$i=0;
    	while($i < count($donnees)-1)
    	{
 ?>
         	 <td><?php echo $donnees[$i]; $i=$i+1; ?></td>
 <?php
 		}
 ?>
 		</tr>
 <?php
    }
?>
    </table>
<?php
    $reponse->closeCursor();

}
catch(Exception $e)
{
    die('Erreur : '.$e->getMessage());
}
?>
