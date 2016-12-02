package anartzmuxika.manageimages.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import anartzmuxika.manageimages.FileUploadListener;

/**
 * Created by anartzmugika on 22/8/16.
 */

public class UploadPhoto extends AsyncTask<String, Integer, Boolean> {
    private Context context;

    private ProgressDialog progressDialog;

    private FileUploadListener listener;

    private File file;

    public UploadPhoto(Context context, File file) {
        this.context = context;
        this.file = file;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        return uploadFile(params[0]) == 200;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.progressDialog =new ProgressDialog(context);
        this.progressDialog.setMessage("Upload image...");
        this.progressDialog.setCancelable(false);
        this.progressDialog.setMax(100);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        this.progressDialog.setProgress(0);
        this.progressDialog.show();

    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);


        if (result) {

            System.out.println("Upload correct!!");
        } else {
            System.out.println("***********Irudia gaizki igota***********");
        }

        this.progressDialog.dismiss();

    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        // UPDATE THE PROGRESS DIALOG

        System.out.println("PROGRESS----------->: " + values[0]);
        progressDialog.setMessage(String.valueOf(" (" + values[0] + " %)"));


    }

    private int uploadFile(final String selectedFilePath){

        UploadUtility upload = new UploadUtility(selectedFilePath, context, new FileUploadListener() {
            @Override
            public void onUpdateProgress(final int percentage, final long kb) {
                ((Activity)context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //  your progress code
                        System.out.println("% " + percentage + " / " + kb + "(size)");
                        //progressDialog.setMessage(context.getResources().getString(R.string.download_image_data_progress) + " (" + percentage + " %)");
                        progressDialog.setProgress(percentage);

                    }});
            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public void transferred(long num, long max) {

            }
        });
        return upload.up();
    }
}
