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


    public UploadPhoto(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... params) {

        if (uploadFile(params[0]) == 200) return true;
        return false;
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

    public int uploadFile(final String selectedFilePath){

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        listener = new FileUploadListener() {
            @Override
            public void onUpdateProgress(final int percentage, final long kb) {

                ((Activity) context).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //  your progress code
                        System.out.println("% " + percentage + " / " + kb + "(size)");
                        //progressDialog.setMessage(context.getResources().getString(R.string.download_image_data_progress) + " (" + percentage + " %)");
                        progressDialog.setProgress(percentage);

                    }
                });
            }

            @Override
            public boolean isCanceled() {
                // Canceled or not
                return false;
            }

            @Override
            public void transferred(long num, long max) {

            }
        };

        int bytesRead,bytesAvailable,bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length-1];

        if (!selectedFile.isFile()){


            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Source File Doesn't Exist: " + selectedFilePath);
                }
            });
            return 0;
        }else{
            try{
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(Urls.URL_LOCALHOST);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("uploaded_file",selectedFilePath);

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + selectedFilePath + "\"" + lineEnd);

                dataOutputStream.writeBytes(lineEnd);

                //returns no. of bytes present in fileInputStream
                bytesAvailable = fileInputStream.available();
                //selecting the buffer size as minimum of available bytes or 1 MB
                bufferSize = Math.min(bytesAvailable,maxBufferSize);
                //setting the buffer as byte array of size of bufferSize
                buffer = new byte[bufferSize];

                //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
                bytesRead = fileInputStream.read(buffer,0,bufferSize);
                long totalSize = selectedFile.length();
                long totalRead = 0;

                //loop repeats till bytesRead = -1, i.e., no bytes are left to read
                while (bytesRead > 0){

                    System.out.println("BYTES READ: " + bytesRead);

                    totalRead += bytesRead;
                    int percentage = (int) ((totalRead / (float) totalSize) * 100);
                    //this.listener.onUpdateProgress(percentage, totalRead);
                    //outputStream.write(buffer, 0, bytesRead);

                    long now = System.currentTimeMillis();

                    Log.e("", totalRead + " " + " " + percentage);
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer,0,bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable,maxBufferSize);
                    bytesRead = fileInputStream.read(buffer,0,bufferSize);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i("", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if(serverResponseCode == 200){
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("File Upload completed.\n\n You can see the uploaded file here: \n\n" + "http://coderefer.com/extras/uploads/"+ fileName);
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"File Not Found",Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(context, "URL error!", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(context, "Cannot Read/Write File!", Toast.LENGTH_SHORT).show();
            }
            return serverResponseCode;
        }

    }
}