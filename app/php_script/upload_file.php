<?php
 
$target_path = "uploaded_files/" . basename( $_FILES['uploaded_file']['name']);
 
if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $target_path)) {
    echo "Archivo ". $target_path . "subido correctamente";
} else{
    echo "Error al subir el archivo";
}
 
?>