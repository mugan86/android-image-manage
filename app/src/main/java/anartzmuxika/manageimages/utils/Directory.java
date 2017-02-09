package anartzmuxika.manageimages.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/****************************************************
 * Created by Anartz Muxika on 23/3/16.
 *
 * GET Android Device select image URL PATH
 */
public class Directory {

    //Path to Android Build < 19
    public static String getPath(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    //Path to Android Build 19
    public static String getRealPathFromURI(Uri contentURI, Context context) {
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    /**********************************************************************************************
     * Get url friendly to load in bitmap
     **********************************************************************************************/

    public static String convertDeviceURLToEmulateURL(Intent data, Context context) {
        Uri uri = data.getData();
        System.out.println("URI: " + uri.toString());
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        cursor.moveToFirst();

        Log.d("TEST", DatabaseUtils.dumpCursorToString(cursor));

        int columnIndex = cursor.getColumnIndex(projection[0]);
        String picturePath = cursor.getString(columnIndex); // returns null
        cursor.close();
        return picturePath;
    }


    /**********************************************************************************************
     * Check image size
     */

    public static boolean isCorrectImageSize(int width, int height) {
        if (height >= 4096 || width >= 4096) // Not support
        {
            System.out.println("Bitmap too large... (4096px)");

            String mezua;

            if (height >= 4096 && width >= 4096) {
                mezua = "Width and Height very big (4096px)";
            } else if (height >= 4096) {
                mezua = "Height very big (4096px)";
            } else {
                mezua = "Width very big (4096px)";
            }

            //Utils.errorMessage(MountainPhotoGalleryFragment.this, mezua);
            return false;
        }
        return true;
    }

    public static File savebitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        String image_url = (DateTime.getCurrentDataTime(true) + "_image.jpeg").replace(" ", "_").replace("-", "_").replace(":", "_");
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String pathname = Environment.getExternalStorageDirectory()
                + File.separator + image_url;
        File f = new File(pathname);
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }

    /*************************************
     * Reference: http://stackoverflow.com/a/37770562
     *
     * @param context
     * @param uri
     * @return
     */
    @SuppressLint("NewApi")
    public static String getFilePath(final Context context, final Uri uri) {

        // Google photo uri example
        // content://com.google.android.apps.photos.contentprovider/0/1/mediakey%3A%2FAF1QipMObgoK_wDY66gu0QkMAi/ORIGINAL/NONE/114919

        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String result = getDataColumn(context, uri, null, null); //
            if (TextUtils.isEmpty(result))
                if (uri.getAuthority().contains("com.google.android")) {
                    try {
                        File localFile = createImageFile(context, null);
                        FileInputStream remoteFile = getSourceStream(context, uri);
                        if (copyToFile(remoteFile, localFile))
                            result = localFile.getAbsolutePath();
                        remoteFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            return result;
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    static String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }

        return null;
    }


    /**
     * Copy data from a source stream to destFile.
     * Return true if succeed, return false if failed.
     */
    private static boolean copyToFile(InputStream inputStream, File destFile) {
        if (inputStream == null || destFile == null) return false;
        try {
            OutputStream out = new FileOutputStream(destFile);
            try {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) >= 0) {
                    out.write(buffer, 0, bytesRead);
                }
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String getTimestamp() {
        try {
            return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(new Date());
        } catch (RuntimeException e) {
            return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        }
    }

    public static File createImageFile(Context context, String imageFileName) throws IOException {
        if (TextUtils.isEmpty(imageFileName))
            imageFileName = getTimestamp(); // make random filename if you want.

        final File root;
        root = context.getExternalCacheDir();

        if (root != null && !root.exists())
            root.mkdirs();
        return new File(root, imageFileName);
    }


    public static FileInputStream getSourceStream(Context context, Uri u) throws FileNotFoundException {
        FileInputStream out = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(u, "r");
            FileDescriptor fileDescriptor = null;
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                out = new FileInputStream(fileDescriptor);
            }
        } else {
            out = (FileInputStream) context.getContentResolver().openInputStream(u);
        }
        return out;
    }

    //Resize image to low size
    public static Bitmap resizeImage(Bitmap image, int w, int h) {

        //Get image original width and height
        int width = image.getWidth();
        int height = image.getHeight();
/*
        //Android Note: Scale the bitmap and keep aspect ratio
        RectF defaultRect = new RectF(0, 0, width, height);
        RectF screenRect = new RectF(0, 0, w, h);

        System.out.println("Width: " + width + " / " + "Height: " + height);

        //Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        //Resize the bit map
        matrix.setRectToRect(defaultRect, screenRect, Matrix.ScaleToFit.CENTER);

        //Recreate the new Bitmap after config changes
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false);

        System.out.println("NEW---> Width" + resizedBitmap.getWidth() + " / " + "Height: " + resizedBitmap.getHeight());
        return resizedBitmap;*/


        BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inJustDecodeBounds = true;


        // Calculate the correct inSampleSize/scale value. This helps reduce memory use. It should be a power of 2
        // from: http://stackoverflow.com/questions/477572/android-strange-out-of-memory-issue/823966#823966
        int inSampleSize = 1;
        while(width / 2 > w){
            width /= 2;
            height /= 2;
            inSampleSize *= 2;
        }

        float desiredScale = (float) w / width;

        // Decode with inSampleSize
        options.inJustDecodeBounds = true;
        options.inDither = false;
        options.inSampleSize = inSampleSize;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;


        // Resize
        Matrix matrix = new Matrix();
        matrix.postScale(desiredScale, desiredScale);
        return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);



    }
}
