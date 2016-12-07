<?php

	//echo "Image upload datetime: " . date("Y-m-d h:i:s") . '<br/>';

	$file = $_FILES['uploaded_file'];
	$name = $file['name'];
	$tmp_name = $file['tmp_name'];

	$extension = explode('.', $name);
	$extension = strtolower((end($extension)));

	//echo "EXTENSION: " . $extension . '<br/>';

	if ($extension != "png" && $extension != "jpg" && $extension != "jpeg")
	{
		//echo "Not exist extension and add png extension";
		$extension = "png";
	}

	//$title = $_POST['title'] ? $_POST['title'] : "Servirace";

	//New file name and new path to upload to server
	$tmp_file_name = "servirace_app{$name}.{$extension}";
	$tmp_file_path = "uploaded_files/" . $tmp_file_name;


	//echo $tmp_file_path . '<br/>';


	//Name of file with datetime
	$filename = $name . "_" . date("Y_m_d_h_i_s");

	//Not use, default target path (NOT DELETE to TEST!!)
	$target_path = "uploaded_files/" . basename( $_FILES['uploaded_file']['name']);


	if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $tmp_file_path)) {
	    //echo "File ". $target_path . " upload succesfully. Thank you " . $name . " !!! Filename: " . $filename;
	    $row_array['message'] = "File ". $tmp_file_path . " upload succesfully. Thank you " . $name . " !!! Filename: " . $filename;
		$row_array['success'] = true;
		echo json_encode($row_array);
	} else{
	    //echo "Error in file upload";
	    $row_array['message'] = "Error in file upload. Try again please.";
		$row_array['success'] = false;
		echo json_encode($row_array);
	}
?>