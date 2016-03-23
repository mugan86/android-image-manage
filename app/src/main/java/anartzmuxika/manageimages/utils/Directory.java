package anartzmuxika.manageimages.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import anartzmuxika.manageimages.R;

/****************************************************
 * Created by Anartz Muxika on 23/3/16.
 *
 * GET Android Device select image URL PATH
 */
public class Directory {

    //Path to Android Build < 19
    public static String getPath(Uri uri, Context context) {
        if( uri == null ) {
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
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

    public static String convertDeviceURLToEmulateURL(Intent data, Context context)
    {
        Uri uri = data.getData();
        String[] projection = { MediaStore.Images.Media.DATA };

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

    public static boolean isCorrectImageSize(int width, int height)
    {
        if (height >= 4096 || width >= 4096) // Not support
        {
            System.out.println("Bitmap too large... (4096px)");

            String mezua;

            if (height>=4096 && width>=4096)
            {
                mezua = "Width and Height very big (4096px)";
            }
            else if (height>=4096)
            {
                mezua = "Height very big (4096px)";
            }
            else
            {
                mezua = "Width very big (4096px)";
            }

            //Utils.errorMessage(MainActivity.this, mezua);
            return false;
        }
        return true;
    }

}
