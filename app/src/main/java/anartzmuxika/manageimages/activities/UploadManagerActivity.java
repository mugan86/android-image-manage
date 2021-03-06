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

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.File;
import java.io.FileDescriptor;

import anartzmuxika.manageimages.R;
import anartzmuxika.manageimages.server.UploadPhotoTask;
import anartzmuxika.manageimages.utils.ConstantValues;
import anartzmuxika.manageimages.utils.DataPreferences;
import anartzmuxika.manageimages.utils.Directory;

public class UploadManagerActivity extends AppCompatActivity {

    private ImageView show_loadImageView;
    private Button open_image_optionsButton, upload_imageButton, rotateImageNineteenDegreeButton;

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

        rotateImageNineteenDegreeButton = (Button) findViewById(R.id.rotateImageNineteenDegreeButton);
        rotateImageNineteenDegreeButton.setVisibility(View.GONE);

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

                new UploadPhotoTask(UploadManagerActivity.this, upload_file, url_to_upload, imagepath).execute();
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

        rotateImageNineteenDegreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                show_loadImageView.setImageBitmap(Directory.setRotateBitmap(bitmap, 90));
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

        if (bitmap != null)
        {
            upload_imageButton.setVisibility(View.VISIBLE);
            rotateImageNineteenDegreeButton.setVisibility(View.VISIBLE);
        }
        else
        {
            rotateImageNineteenDegreeButton.setVisibility(View.GONE);
            upload_imageButton.setVisibility(View.GONE);
        }

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
}
