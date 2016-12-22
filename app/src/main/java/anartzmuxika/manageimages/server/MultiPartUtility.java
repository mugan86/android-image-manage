package anartzmuxika.manageimages.server;

import android.app.Activity;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


import javax.net.ssl.HttpsURLConnection;

import anartzmuxika.manageimages.FileUploadListener;

/**************************************************
 * Created by anartzmugika on 2/12/16.
 */

public class MultiPartUtility {
    private String boundary;
    private static final String LINE_FEED = "\r\n";
    private String path;
    private FileUploadListener listener;
    private Context context;
    private File file;
    private HttpURLConnection httpConn;
    private HttpsURLConnection httpsConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private boolean with_https;
    public MultiPartUtility(File file, String path, Context context, FileUploadListener listener)
    {
        this.path = path;
        this.listener = listener;
        this.context = context;
        this.file = file;
    }

    public MultiPartUtility(String requestURL, FileUploadListener listener, boolean with_https) throws IOException {
        this.charset =  "UTF-8";

        this.listener = listener;
        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
        this.with_https = with_https;
        URL url = new URL(requestURL);
        if (this.with_https) //Https
        {
            httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setReadTimeout(15000);
            httpsConn.setConnectTimeout(15000);
            httpsConn.setRequestMethod("POST");
            httpsConn.setUseCaches(false);
            httpsConn.setDoOutput(true);	// indicates POST method
            httpsConn.setDoInput(true);
            httpsConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            outputStream = httpsConn.getOutputStream();
        }
        else
        {
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setReadTimeout(15000);
            httpConn.setConnectTimeout(15000);
            httpConn.setRequestMethod("POST");
            httpConn.setUseCaches(false);
            httpConn.setDoOutput(true);	// indicates POST method
            httpConn.setDoInput(true);
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            outputStream = httpConn.getOutputStream();
        }

        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    public int up() {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 2 * 1024 * 1024;
        File selectedFile = new File(path);


        String[] parts = path.split("/");
        final String fileName = parts[parts.length - 1];

        if (!selectedFile.isFile()) {


            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Source File Doesn't Exist: " + path);
                }
            });
            return 0;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(selectedFile);
                URL url = new URL(Urls.URL_LOCALHOST_LOCAL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);//Allow Inputs
                connection.setDoOutput(true);//Allow Outputs
                connection.setUseCaches(false);//Don't use a cached Copy
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("ENCTYPE", "multipart/form-data");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                //connection.setRequestProperty("uploaded_file", path);
                //connection.setRequestProperty("filename", path);

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                //creating new dataoutputstream
                dataOutputStream = new DataOutputStream(connection.getOutputStream());

                //writing bytes to data outputstream
                dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
                dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + path + "\"" + lineEnd);

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
                while (bytesRead > 0) {

                    System.out.println("BYTES READ: " + bytesRead);

                    totalRead += bytesRead;
                    int percentage = (int) ((totalRead / (float) totalSize) * 100);
                    //this.listener.onUpdateProgress(percentage, totalRead);
                    //outputStream.write(buffer, 0, bytesRead);

                    long now = System.currentTimeMillis();

                    Log.e("", totalRead + " " + " " + percentage);
                    //write the bytes read from inputstream
                    dataOutputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    if (this.listener != null)
                        this.listener.onUpdateProgress(percentage, totalRead);
                }

                dataOutputStream.writeBytes(lineEnd);
                dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();

                Log.i("", "Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

                //response code of 200 indicates the server status OK
                if (serverResponseCode == 200) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(String.format("File %s Upload completed :) :)", fileName));
                        }
                    });
                }

                //closing the input and output streams
                fileInputStream.close();
                dataOutputStream.flush();
                dataOutputStream.close();


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "File Not Found", Toast.LENGTH_SHORT).show();
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

    /**
     * Adds a form field to the request
     * @param name field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
                .append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=" + charset).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(String fieldName, File uploadFile)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append("--" + boundary).append(LINE_FEED);
        writer.append(
                "Content-Disposition: form-data; name=\"" + fieldName
                        + "\"; filename=\"" + fileName + "\"")
                .append(LINE_FEED);
        writer.append(
                "Content-Type: "
                        + URLConnection.guessContentTypeFromName(fileName))
                .append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        long totalSize = uploadFile.length();
        byte[] buffer = new byte[4096];
        long totalRead = 0;
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, bytesRead);
            System.out.println("BYTES READ: " + bytesRead);

            totalRead += bytesRead;
            int percentage = (int) ((totalRead / (float) totalSize) * 100);
            //outputStream.write(buffer, 0, bytesRead);

            long now = System.currentTimeMillis();

            Log.e("", totalRead + " " + " " + percentage);
            this.listener.onUpdateProgress(percentage, totalRead);
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    public boolean  finish() throws IOException {
        List<String> response = new ArrayList<>();
        StringBuilder json = new StringBuilder();
        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        if (this.with_https)
        {
            // checks server's status code first
            int status = httpsConn.getResponseCode();
            if (status == HttpsURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpsConn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                    json.append(line + "\n");
                }
                System.out.println(json);
                reader.close();
                httpsConn.disconnect();
            } else {

                throw new IOException("Server returned non-OK status: " + status);
            }
        }
        else
        {
            // checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        httpConn.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    response.add(line);
                    json.append(line + "\n");
                }
                System.out.println(json);
                reader.close();
                httpConn.disconnect();
            } else {

                throw new IOException("Server returned non-OK status: " + status);
            }
        }


        JSONObject object;

        try {
            object = new JSONObject(String.valueOf(json));

            boolean success = object.getBoolean("success");
            System.out.println("Success? :" + success + " / Message: " + object.getString("message"));
            return success;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        System.out.println("Return false!!");
        return false;
    }
}
