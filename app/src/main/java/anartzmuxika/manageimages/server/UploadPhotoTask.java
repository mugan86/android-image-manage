package anartzmuxika.manageimages.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.File;

import anartzmuxika.manageimages.FileUploadListener;
import anartzmuxika.manageimages.R;

/***************************************************************************************************
 * Created by anartzmugika on 22/8/16 / Updated 10/02/2017
 * Class to upload photo get to activity path. Using with progress bar (in %) and return 200 or other
 * response code
 **************************************************************************************************/

public class UploadPhotoTask extends AsyncTask<String, Integer, Integer> {

    private Exception exception;
    private ProgressDialog progressDialog;
    private Context context;
    private File file;
    private String url_to_upload;
    private String selectedImagePath;

    public UploadPhotoTask(Context context, File file, String url_to_upload, String selectedImagePath)
    {
        this.context = context;
        this.file = file;
        this.url_to_upload = url_to_upload;
        this.selectedImagePath = selectedImagePath;
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

    protected Integer doInBackground(String... urls) {

        try {
            MultipartUtility upload = new MultipartUtility(new FileUploadListener() {
                /**
                 * @param percentage: Progress number to 100%
                 * @param kb:         Total of size to file
                 */
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
            }, context);
            return upload.uploadFile(selectedImagePath, url_to_upload, file);
        } catch (Exception e) {
            this.exception = e;

            return null;
        }
    }

    @Override
    public void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        // UPDATE THE PROGRESS DIALOG
        System.out.println("PROGRESS----------->: " + values[0]);
        if ( values[0] < 100) progressDialog.setMessage(String.valueOf(" (" + values[0] + " %)"));
        else progressDialog.setMessage(context.getResources().getString(R.string.image_upload_successfully));
    }

    protected void onPostExecute(Integer int_) {
        // TODO: check this.exception
        // TODO: do something with the feed

        if (int_ == 200)
        {
            System.out.println("SERVER RETURN: " + int_);
            try
            {
                Thread.sleep(1000);
                Toast.makeText(context, context.getResources().getString(R.string.image_upload_successfully), Toast.LENGTH_LONG).show();

            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(context, context.getString(R.string.image_upload_no_correct), Toast.LENGTH_LONG).show();
        }
        this.progressDialog.dismiss();

    }


}
