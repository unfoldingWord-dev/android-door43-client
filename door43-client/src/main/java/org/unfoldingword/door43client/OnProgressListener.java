package org.unfoldingword.door43client;

/**
 * A utility to get progress updates durring long operations
 */
public interface OnProgressListener {
    /**
     *
     * @param tag used to identify what progress event is occurring
     * @param max the total number of items being processed
     * @param complete the number of items that have been successfully processed
     */
    void onProgress(String tag, long max, long complete);
}
