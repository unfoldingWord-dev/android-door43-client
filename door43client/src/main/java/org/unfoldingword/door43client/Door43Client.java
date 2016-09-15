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

    // TODO: 9/2/16 write all the methods!
}
