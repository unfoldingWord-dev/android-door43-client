package org.unfoldingword.door43client;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by joel on 8/30/16.
 */
public class Door43Client {

    private final File resourceDir;
    private final Library library;

    /**
     * Initializes the new api client
     * @param context the application context
     * @param databaseName the name of the datbase where information will be indexed
     * @param resourceDir the directory where resource containers will be stored
     */
    public Door43Client(Context context, String databaseName, File resourceDir) throws IOException {
        this.resourceDir = resourceDir;

        // load schema
        InputStream in = context.getAssets().open("schema.sqlite");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        SQLiteHelper helper = new SQLiteHelper(context, sb.toString(), databaseName);
        this.library = new Library(helper);
    }

    /**
     * Returns the read only index
     * @return
     */
    public Index index() {
        return library;
    }

    /**
     * Indexes the Door43 catalog.
     *
     * @param url the entry resource api catalog
     * @param listener an optional progress listener. This should receive progress id, total, completed
     */
    public void updatePrimaryIndex(String url, OnProgressListener listener) {

    }

    /**
     * Downloads a global catalog and indexes it.
     *
     * @param catalogSlug the slug of the catalog to download. Or an object containing all the args.
     * @param listener an optional progress listener. This should receive progress id, total, completed
     */
    public void updateCatalogIndex(String catalogSlug, OnProgressListener listener) {

    }

    public void downloadResourceContainer() {

    }

    @Deprecated
    public void downloadFutureCompatibleResourceContainer() {

    }

    @Deprecated
    public void convertLegacyResource() {

    }

    public void openResourceContainer() {

    }

    public void closeResourceContainer() {

    }

    public void listResourceContainers() {

    }

    public void getProjectUpdates() {

    }

    public void getSourceLanguageUpdates() {

    }

    public interface OnProgressListener {
        /**
         *
         * @param tag used to identify what progress event is occurring
         * @param max the total number of items being processed
         * @param complete the number of items that have been successfully processed
         */
        void onProgress(String tag, long max, long complete);
    }
}
