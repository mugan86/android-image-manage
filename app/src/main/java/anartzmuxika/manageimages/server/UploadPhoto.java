package anartzmuxika.manageimages.server;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.io.File;

import anartzmuxika.manageimages.FileUploadListener;

/***************************************************************************************************
 * Created by anartzmugika on 22/8/16 / Updated 7/12/2016
 * Class to upload photo get to activity path. Using with progress bar (in %) and return 0 or 1
 **************************************************************************************************/

public class UploadPhoto extends AsyncTask<String, Integer, Boolean> {
    private Context context;
    private File file;
    private ProgressDialog progressDialog;
    private String request_url;
    private String filename;

    public UploadPhoto(Context context, File file, String request_url) { this.context = context; this.file = file; this.request_url = request_url;}

    @Override
    protected Boolean doInBackground(String... params) {

        filename = params[1];
        return uploadFile();
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

    private boolean uploadFile()
    {
        try
        {
            boolean with_https = false;
            if (request_url.contains("https"))
            {
                with_https = true;
            }

            System.out.println(with_https);
            MultiPartUtility upload = new MultiPartUtility(request_url, new FileUploadListener() {
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
            }, with_https);
            upload.addFilePart("uploaded_file", file);
            upload.addFormField("filename", filename.trim());
            //upload.addFormField("mountainid", params[1]);
            //multipart.addFormField("userid", id);

            return upload.finish();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
