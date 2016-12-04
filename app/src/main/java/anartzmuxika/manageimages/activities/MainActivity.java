package anartzmuxika.manageimages.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;

import anartzmuxika.manageimages.R;
import anartzmuxika.manageimages.server.UploadPhoto;
import anartzmuxika.manageimages.utils.ConstantValues;
import anartzmuxika.manageimages.utils.DataPreferences;
import anartzmuxika.manageimages.utils.Directory;

public class MainActivity extends AppCompatActivity {

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = MainActivity.this;

        initializeComponents();

        addActions();

        //Only use in Android M
        if (Build.VERSION.SDK_INT >= 23) openPermissionToReadStorage();
    }

    private void openPermissionToReadStorage()
    {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT>=23) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ConstantValues.GRANTED_ACCESS_STORAGE);
        }
    }

    private void initializeComponents()
    {
        show_loadImageView = (ImageView) findViewById(R.id.show_loadImageView);
        open_image_optionsButton = (Button) findViewById(R.id.open_image_optionsButton);
        upload_imageButton = (Button) findViewById(R.id.upload_imageButton);
        upload_imageButton.setVisibility(View.GONE);
    }

    private void addActions()
    {
        open_image_optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {
                        "Camera",
                        "Device (Gallery)",
                        "Open Storage file reference",
                        "Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Select image from camera or device");
                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        //Open camera to capture image
                        if (options[item].equals("Camera")) {

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                                    != PackageManager.PERMISSION_GRANTED
                                    && android.os.Build.VERSION.SDK_INT >= 23) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.CAMERA},
                                        ConstantValues.GRANTED_CAMERA);
                            } else {
                                openCamera();
                            }

                        } else if (options[item].equals("Open Storage file reference")) {

                            if (!DataPreferences.getPreference(getApplicationContext(), "Local_Reference").equals("")) {
                                //Change reference after take one reference from image picker or camera
                                //Necessary add "android.permission.READ_EXTERNAL_STORAGE" permission in manifest and accept permission in version Android M
                                bitmap = BitmapFactory.decodeFile(DataPreferences.getPreference(getApplicationContext(), "Local_Reference"));

                                show_loadImageView.setImageBitmap(bitmap);
                            }
                            else
                            {
                                openDeviceGallery();
                            }
                        }

                        //Get image from gallery
                        else if (options[item].equals("Device (Gallery)"))
                        {
                            openDeviceGallery();
                        }

                        //Exit dialog
                        else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        upload_imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadPhoto upload_photo = new UploadPhoto(MainActivity.this, upload_file);
                upload_photo.execute(imagepath);
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

                    String  selectedImagePath = Directory.getFilePath(MainActivity.this, imageUri);//Directory.convertDeviceURLToEmulateURL(data, activity);
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
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.image_no_take_correctly), Toast.LENGTH_LONG).show();
                        upload_correct = false;

                    }


                } else {
                    ParcelFileDescriptor parcelFileDescriptor;

                    parcelFileDescriptor = getContentResolver().openFileDescriptor(imageUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();


                    bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);

                    bitmap = Directory.resizeImage(bitmap, ConstantValues.MAX_WIDTH_HEIGHT_IMG, ConstantValues.MAX_WIDTH_HEIGHT_IMG);

                    int height = bitmap.getHeight();
                    int width = bitmap.getWidth();

                    upload_file = Directory.savebitmap(bitmap);

                    System.out.println("**** HEIGHT: " + height + " / " + "WIDTH: " + width + " output: " + output);

                    if (Directory.isCorrectImageSize(width, height)) {
                        String picturePath = Directory.convertDeviceURLToEmulateURL(data, MainActivity.this);
                        picturePath = picturePath.replace("file://", "");
                        picturePath = picturePath.replace("%20", " ");

                        //Add bitmap in profile image
                        //show_loadImageView.setImageBitmap(bitmap);
                        System.out.println("URL: " + imageUri);
                        //Toast.makeText(getApplicationContext(), "URL: " + imageUri, Toast.LENGTH_LONG).show();
                        System.out.println("URL (After convert): " + picturePath);
                        //Toast.makeText(getApplicationContext(), "URL (After convert): " + picturePath, Toast.LENGTH_LONG).show();


                        if (imageUri.toString().contains(ConstantValues.CONTENT_MEDIA)) {

                            System.out.println(Directory.getRealPathFromURI(imageUri, activity));

                            imagepath = Directory.getRealPathFromURI(imageUri, activity);

                            //imageUri = Uri.fromFile(output);
                        } else if (imageUri.toString().contains(ConstantValues.CONTENT_PROVIDER)) {
                            imagepath = picturePath;
                        } else {
                            imagepath = imageUri.getPath();
                        }

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
            if (res[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (rCode == ConstantValues.GRANTED_CAMERA) openCamera();
                else System.out.println("Available!!!");
            }
            else
            {

            }
        }
    }
}
