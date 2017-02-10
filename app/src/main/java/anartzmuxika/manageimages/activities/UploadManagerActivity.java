package anartzmuxika.manageimages.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import anartzmuxika.manageimages.R;
import anartzmuxika.manageimages.utils.ConstantValues;
import anartzmuxika.manageimages.utils.DataPreferences;
import anartzmuxika.manageimages.utils.DateTime;
import anartzmuxika.manageimages.utils.Directory;

public class UploadManagerActivity extends AppCompatActivity {

    private ImageView show_loadImageView;
    private Button open_image_optionsButton, upload_imageButton;

    //Image manage values
    private File output;
    private String imagepath, from;
    private Uri imageUri;
    private Bitmap bitmap;
    private boolean upload_correct;
    private Button upload_photoButton;

    private Activity activity;


    private File upload_file;

    private FloatingActionMenu materialDesignFAM;
    private FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;


    private boolean start = true;
    private String url_to_upload;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        activity = UploadManagerActivity.this;

        DataPreferences.loadLocale(activity);

        initializeComponents();

        addActions();

        //Check file manage read storage in start
        checkIfPermissionToReadStorage();

        if( savedInstanceState == null ) {
            url_to_upload =
                    getIntent().getStringExtra("url_to_upload");
            System.out.println("LOCALHOST: " + url_to_upload);
        }

    }

    private boolean checkIfPermissionToReadStorage()
    {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT>=23) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ConstantValues.GRANTED_ACCESS_STORAGE);
            return false;
        }
        return true;
    }

    private void initializeComponents()
    {
        show_loadImageView = (ImageView) findViewById(R.id.show_loadImageView);
        open_image_optionsButton = (Button) findViewById(R.id.open_image_optionsButton);
        upload_imageButton = (Button) findViewById(R.id.upload_imageButton);
        upload_imageButton.setVisibility(View.GONE);

        materialDesignFAM = (FloatingActionMenu) findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = (FloatingActionButton) findViewById(R.id.material_design_floating_action_menu_item3);
    }

    private void addActions()
    {
        open_image_optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {
                        getResources().getString(R.string.camera),
                        getResources().getString(R.string.device_gallery),
                        getResources().getString(R.string.cancel_pick) };

                AlertDialog.Builder builder = new AlertDialog.Builder(UploadManagerActivity.this);
                builder.setTitle(getResources().getString(R.string.select_image));
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        //Open camera to capture image
                        if (options[item].equals(getResources().getString(R.string.camera))) {

                            if (checkIfPermissionsToManageCamera()) openCamera();

                        }
                        //Get image from gallery
                        else if (options[item].equals(getResources().getString(R.string.device_gallery)))
                        {
                            start = false;
                            if (checkIfPermissionToReadStorage()) openDeviceGallery();
                        }

                        //Exit dialog
                        else if (options[item].equals(getResources().getString(R.string.cancel_pick))) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        //Send photo to server
        upload_imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*String user_basic_data = DataPreferences.getPreference(activity, ConstantValues.USER_DEFAULT_DATA_TO_USE_IN_FILENAME);
                String name = "servirace_app";
                String datatime = DateTime.getCurrentDataTime(false);
                String filename = name+datatime+".jpeg";
                if (!user_basic_data.equals("")) filename = "%s_" + filename;
                //Context - Upload file - Request URL
                UploadPhoto upload_photo = new UploadPhoto(UploadManagerActivity.this, upload_file, url_to_upload);
                upload_photo.execute(imagepath, new Filename().getFilenameWithNameBasicData(user_basic_data, filename));*/

                //uploadFile(imagepath);

                new UploadTask().execute();
            }
        });

        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkIfPermissionsToManageCamera()) openCamera();
                materialDesignFAM.close(true);

            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start = false;
                if (checkIfPermissionToReadStorage()) openDeviceGallery();
                materialDesignFAM.close(true);
            }
        });
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendEmail("Contact with Anartz!");
                materialDesignFAM.close(true);
            }
        });
    }

    private void openCamera()
    {
        Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir=
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        output=new File(dir, "image" + System.currentTimeMillis() + ".jpeg");
        System.out.println("OUTPUT CAMERA: " + output);
        i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        startActivityForResult(i, ConstantValues.IMAGE_PICKER_CAMERA);
    }

    private void openDeviceGallery()
    {
        if (Build.VERSION.SDK_INT < 19) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), ConstantValues.IMAGE_PICKER_SELECT);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            File dir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            output = new File(dir, "image" + System.currentTimeMillis() + ".jpeg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
            startActivityForResult(intent, ConstantValues.IMAGE_PICKER_SELECT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        try
        {
            if (requestCode == ConstantValues.IMAGE_PICKER_SELECT && resultCode == ConstantValues.RESULT_CODE_OK_VALID) {
                imageUri = data.getData();
                if (Build.VERSION.SDK_INT < 19) {

                    String  selectedImagePath = Directory.getFilePath(UploadManagerActivity.this, imageUri);//Directory.convertDeviceURLToEmulateURL(data, activity);
                    bitmap = BitmapFactory.decodeFile(selectedImagePath);

                    bitmap = Directory.resizeImage(bitmap, ConstantValues.MAX_WIDTH_HEIGHT_IMG, ConstantValues.MAX_WIDTH_HEIGHT_IMG);

                    int height = bitmap.getHeight();
                    int width = bitmap.getWidth();

                    upload_file = Directory.savebitmap(bitmap);


                    System.out.println("**** HEIGHT: " + height + " / " + "WIDTH: " + width + " output: " + output);

                    if (Directory.isCorrectImageSize(width, height)) {

                        selectedImagePath = selectedImagePath.replace("file://", "");
                        selectedImagePath = selectedImagePath.replace("%20", " ");

                        imagepath = selectedImagePath;


                        upload_correct = true;

                        //sendImageToServer();
                    } else {
                        Toast.makeText(UploadManagerActivity.this, getResources().getString(R.string.image_no_take_correctly), Toast.LENGTH_LONG).show();
                        upload_correct = false;

                    }

                } else {
                    ParcelFileDescriptor parcelFileDescriptor;

                    parcelFileDescriptor = getContentResolver().openFileDescriptor(imageUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                    System.out.println("IMAGEURI: " + imageUri.toString());

                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                    bitmap = Directory.resizeImage(bitmap, ConstantValues.MAX_WIDTH_HEIGHT_IMG, ConstantValues.MAX_WIDTH_HEIGHT_IMG);

                    int height = bitmap.getHeight();
                    int width = bitmap.getWidth();

                    upload_file = Directory.savebitmap(bitmap);

                    System.out.println("**** HEIGHT: " + height + " / " + "WIDTH: " + width + " output: " + output.getAbsolutePath());

                    if (Directory.isCorrectImageSize(width, height)) {
                        String picturePath = "";
                        try
                        {
                            picturePath = Directory.convertDeviceURLToEmulateURL(data, UploadManagerActivity.this);
                            System.out.println("Picture path: " + picturePath);

                            picturePath = picturePath.replace("file://", "");
                            picturePath = picturePath.replace("%20", " ");
                        }
                        catch(Exception e)
                        {
                            picturePath = output.getAbsolutePath();
                        }

                        //Add bitmap in profile image
                        //show_loadImageView.setImageBitmap(bitmap);
                        System.out.println("URL: " + imageUri);
                        System.out.println("URL (After convert): " + picturePath);

                        if (imageUri.toString().contains(ConstantValues.CONTENT_MEDIA)) {

                            System.out.println("CONTENT MEDIA: " + Directory.getRealPathFromURI(imageUri, activity));

                            imagepath = Directory.getRealPathFromURI(imageUri, activity);

                            //imageUri = Uri.fromFile(output);
                        } else if (imageUri.toString().contains(ConstantValues.CONTENT_PROVIDER) &&
                                (!imageUri.toString().contains(ConstantValues.CONTENT_MEDIA_LOCAL) && (!imageUri.toString().contains(ConstantValues.CONTENT_MEDIA_LOCAL_2)))) {
                            imagepath = picturePath;
                            System.out.println("CONTENT PROVIDER: " + picturePath);
                        } else {
                            imagepath = Directory.getFilePath(activity, imageUri);
                        }

                        System.out.println("Absolute path: " + imagepath);

                        upload_correct = true;

                    } else {
                        Toast.makeText(activity, activity.getResources().getString(R.string.image_no_take_correctly), Toast.LENGTH_LONG).show();
                        upload_correct = false;
                    }

                }

            }
            else if (requestCode == ConstantValues.IMAGE_PICKER_CAMERA && resultCode == ConstantValues.RESULT_CODE_OK_VALID) {

                imageUri = Uri.fromFile(output);

                System.out.println(output + " ////// * " + output.getAbsolutePath());

                imagepath = imageUri.toString().substring(7);

                System.out.println("IMAGE FROM CAMERA!!: " + imagepath);

                bitmap= BitmapFactory.decodeFile(imagepath);
            /*bitmap = ImageManageUtils.compressImage(ConstantValues.MAX_WIDTH_HEIGHT_IMG,
                        ConstantValues.MAX_WIDTH_HEIGHT_IMG, bitmap);*/

                bitmap = Directory.resizeImage(bitmap, ConstantValues.MAX_WIDTH_HEIGHT_IMG, ConstantValues.MAX_WIDTH_HEIGHT_IMG);

                int height = bitmap.getHeight();
                int width = bitmap.getWidth();

                upload_file = Directory.savebitmap(bitmap);

                System.out.println("**** HEIGHT: " + height + " / " + "WIDTH: " + width + " output: " + output);

                if(Directory.isCorrectImageSize(width, height))
                {
                    //show_loadImageView.setImageBitmap(bitmap);
                    //sendImageToServer();
                    upload_correct = true;
                }
                else
                {
                    Toast.makeText(activity.getApplicationContext(), activity.getResources().getString(R.string.image_no_take_correctly), Toast.LENGTH_LONG).show();
                    upload_correct = false;
                }


            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        show_loadImageView.setImageBitmap(bitmap);

        if (bitmap != null) upload_imageButton.setVisibility(View.VISIBLE);
        else upload_imageButton.setVisibility(View.GONE);

    }

    @Override
    public void onRequestPermissionsResult(
            int rCode,
            @NonNull String [] permissions,
            @NonNull int[] res
    )
    {
        if(rCode == ConstantValues.GRANTED_CAMERA || rCode == ConstantValues.GRANTED_ACCESS_STORAGE) //Permission GRANTED to use camera / write external storage
        {
            try {
                if (res[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (rCode == ConstantValues.GRANTED_CAMERA) openCamera();
                    if (rCode == ConstantValues.GRANTED_ACCESS_STORAGE && !start) openDeviceGallery();
                    else System.out.println("Available!!!");
                }
            }
            catch (Exception e)
            {

            }

        }
    }

    private boolean checkIfPermissionsToManageCamera()
    {
        if (ActivityCompat.checkSelfPermission(UploadManagerActivity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
                && android.os.Build.VERSION.SDK_INT >= 23) {

            ActivityCompat.requestPermissions(UploadManagerActivity.this,
                    new String[]{Manifest.permission.CAMERA},
                    ConstantValues.GRANTED_CAMERA);
            return false;
        }
        return true;
    }

    private void sendEmail(String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ConstantValues.CONTACT_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) { startActivity(intent);}
    }



    class UploadTask extends AsyncTask<String, Void, Integer> {

        private Exception exception;

        protected Integer doInBackground(String... urls) {
            try {
                return uploadFile(imagepath);
            } catch (Exception e) {
                this.exception = e;

                return null;
            }
        }

        protected void onPostExecute(Integer int_) {
            // TODO: check this.exception
            // TODO: do something with the feed
            System.out.println("SERVER RETURN: " + int_);
        }

        public int uploadFile(final String selectedFilePath) {

            //Send filename (use in server with ['uploaded_file'] ['filename']
            String filename = DateTime.getCurrentData() + "-Anartz";
            int serverResponseCode = 0;

            HttpURLConnection connection;
            DataOutputStream dataOutputStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";


            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File selectedFile = new File(selectedFilePath);


            String[] parts = selectedFilePath.split("/");
            final String fileName = parts[parts.length - 1];

            if (!selectedFile.isFile()) {
                //dialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Source File Doesn't Exist: " + selectedFilePath);
                    }
                });
                return 0;
            } else {
                try {
                    FileInputStream fileInputStream = new FileInputStream(selectedFile);
                    URL url = new URL(url_to_upload);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);//Allow Inputs
                    connection.setDoOutput(true);//Allow Outputs
                    connection.setUseCaches(false);//Don't use a cached Copy
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Connection", "Keep-Alive");
                    connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                    connection.setRequestProperty(
                            "Content-Type", "multipart/form-data;boundary=" + boundary);
                    connection.setRequestProperty("uploaded_file",upload_file.getAbsolutePath());


                    //creating new dataoutputstream
                    dataOutputStream = new DataOutputStream(connection.getOutputStream());

                    //writing bytes to data outputstream
                    dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                    dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                            + filename + "\"" + lineEnd);

                    dataOutputStream.writeBytes(lineEnd);

                    //returns no. of bytes present in fileInputStream
                    bytesAvailable = fileInputStream.available();
                    //selecting the buffer size as minimum of available bytes or 1 MB
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    //setting the buffer as byte array of size of bufferSize
                    buffer = new byte[bufferSize];

                    //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    long totalSize = selectedFile.length();
                    long totalRead = 0;
                    //loop repeats till bytesRead = -1, i.e., no bytes are left to read

                    //TODO add fileuploadlistener
                    while (bytesRead > 0) {

                        try {

                            //write the bytes read from inputstream
                            dataOutputStream.write(buffer, 0, bufferSize);

                            totalRead += bytesRead;
                            int percentage = (int) ((totalRead / (float) totalSize) * 100);
                            //outputStream.write(buffer, 0, bytesRead);

                            long now = System.currentTimeMillis();

                            Log.e("", totalRead + " " + " " + percentage);
                            //this.listener.onUpdateProgress(percentage, totalRead);
                        } catch (OutOfMemoryError e) {
                            Toast.makeText(UploadManagerActivity.this, "Insufficient Memory!", Toast.LENGTH_SHORT).show();
                        }
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dataOutputStream.writeBytes(lineEnd);
                    dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    try{
                        serverResponseCode = connection.getResponseCode();
                    }catch (OutOfMemoryError e){
                        Toast.makeText(UploadManagerActivity.this, "Memory Insufficient!", Toast.LENGTH_SHORT).show();
                    }
                    String serverResponseMessage = connection.getResponseMessage();

                    Log.i("jjjjj", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                    //response code of 200 indicates the server status OK
                    if (serverResponseCode == 200) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/" + fileName);
                            }
                        });
                    }

                    //closing the input and output streams
                    fileInputStream.close();
                    dataOutputStream.flush();
                    dataOutputStream.close();



                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UploadManagerActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UploadManagerActivity.this, "URL Error!", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UploadManagerActivity.this, "Cannot Read/Write File", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                //dialog.dismiss();
                return serverResponseCode;
            }

        }
    }
}
