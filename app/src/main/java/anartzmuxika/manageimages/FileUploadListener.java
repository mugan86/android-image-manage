package anartzmuxika.manageimages;

/**************************************************************************************************
 * Created by anartzmugika on 7/12/2016.
 */

public interface FileUploadListener {
    /**
     *
     * @param percentage: Progress number to 100%
     * @param kb: Total of size to file
     */
    void onUpdateProgress(int percentage, long kb);

    /**
     *
     * @return cancelled or no
     */
    boolean isCanceled();

    void transferred(long num, long max);
}