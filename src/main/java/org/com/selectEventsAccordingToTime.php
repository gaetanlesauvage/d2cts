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
	
	$pdo_options[PDO::ATTR_ERRMODE] = PDO::ERRMODE_EXCEPTION;
	$bdd = new PDO("mysql:host=$host;dbname=$dbName", $user, $password, $pdo_options);

	$req = 'SELECT * FROM events WHERE sim=\'' . $sim .'\' AND time>=\'' . $timeFrom . '\' AND time<\'' . $timeTo . '\'';
	$reponse = $bdd->query($req);
?>
	<table>
		<tr>
			<th> SIM </th> <th> TIME </th> <th> TYPE</th> <th> VALUE </th>
		</tr>
<?php
	 while ($donnees = $reponse->fetch())
    {
?>
         <tr>
			<td><?php echo $donnees['sim']; ?></td>
			<td><?php echo $donnees['time']; ?></td>
			<td><?php echo $donnees['type']; ?></td>
			<td><?php echo $donnees['value']; ?></td>
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
