<?php
try
{
	$host = $_POST['host'];
	$dbName = $_POST['dbName'];
	$user = $_POST['user'];
	$password = $_POST['password'];

	
	$sim = $_POST['sim'];
	
	$timeFrom = $_POST['timeFrom'];
	$timeTo = $_POST['timeTo'];
	$id = $_POST['id'];
	
	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$req = 'SELECT roadID,direction,rate,contID FROM locations WHERE sim=\'' . $sim .'\' AND id=\'' . $id . '\' AND time>=\'' . $timeFrom . '\' AND time<\'' . $timeTo . '\'';
	$reponse = $bdd->query($req);
?>
	<table>
		<tr>
			<th> ROAD ID </th> <th> DIRECTION </th> <th> RATE </th>
		</tr>
<?php
	 while ($donnees = $reponse->fetch())
    {
?>
         <tr>
			<td><?php echo $donnees['roadID']; ?></td>
			<td><?php echo $donnees['direction']; ?></td>
			<td><?php echo $donnees['rate']; ?></td>
			<td><?php echo $donnees['contID']; ?></td>
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
