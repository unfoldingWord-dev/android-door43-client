package org.unfoldingword.door43client;

import android.content.Context;

import org.unfoldingword.resourcecontainer.ResourceContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Provides a interface to the Door43 resource api
 */

public class Door43Client {

    private final API api;
    private static String schema = null;

    /**
     * Initializes a new Door43 client
     * @param context the application context
     * @param databasePath the name of the database where information will be indexed
     * @param resourceDir the directory where resource containers will be stored
     * @throws IOException
     */
    public Door43Client(Context context, File databasePath, File resourceDir) throws IOException {
        // load schema
        if(this.schema == null) {
            InputStream is = context.getAssets().open("schema.sqlite");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            this.schema = sb.toString();
        }

        this.api = new API(context, this.schema, databasePath, resourceDir);
    }

    /**
     * Attaches a listener to receive log events
     * @param listener
     */
    public void setLogger(OnLogListener listener) {
        api.setLogger(listener);
    }

    /**
     * Returns the read only index
     * @return
     */
    public Index index() {
        return api.index();
    }

    /**
     * Indexes the Door43 catalog.
     *
     * @param url the entry resource api catalog
     * @param listener an optional progress listener. This should receive progress id, total, completed
     */
    public void updateSources(String url, OnProgressListener listener) throws Exception {
        api.updateSources(url, listener);
    }

    /**
     * Indexes the supplementary catalogs
     * @param listener
     * @throws Exception
     */
    public void updateCatalogs(OnProgressListener listener) throws Exception {
        api.updateCatalogs(listener);
    }

    /**
     * Downloads a resource container from the api
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return
     * @throws Exception
     */
    public ResourceContainer download(String sourceLanguageSlug, String projectSlug, String resourceSlug) throws Exception {
        return api.downloadResourceContainer(sourceLanguageSlug, projectSlug, resourceSlug);
    }

    /**
     * Opens a resource container archive so it's contents can be read.
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return
     * @throws Exception
     */
    public ResourceContainer open(String sourceLanguageSlug, String projectSlug, String resourceSlug) throws Exception {
        return api.openResourceContainer(sourceLanguageSlug, projectSlug, resourceSlug);
    }

    /**
     * Opens a resource container archive so it's contents can be read.
     * @param containerSlug
     * @return
     */
    public ResourceContainer open(String containerSlug) throws Exception {
        return api.openResourceContainer(containerSlug);
    }

    /**
     * Closes a resource container directory
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @throws Exception
     */
    public void close(String sourceLanguageSlug, String projectSlug, String resourceSlug) throws Exception {
        api.closeResourceContainer(sourceLanguageSlug, projectSlug, resourceSlug);
    }

    /**
     * Closes the api
     */
    public void tearDown() {
        api.tearDown();
    }
}
