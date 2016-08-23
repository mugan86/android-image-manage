package anartzmuxika.manageimages;

/**
 * Created by anartzmugika on 22/8/16.
 */

public interface FileUploadListener {
    void onUpdateProgress(int percentage, long kb);

    boolean isCanceled();

    void transferred(long num, long max);
}