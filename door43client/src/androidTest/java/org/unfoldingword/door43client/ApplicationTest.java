package org.unfoldingword.door43client;

import android.app.Application;
import android.test.ApplicationTestCase;

import org.junit.Test;
import org.unfoldingword.door43client.library.Library;
import org.unfoldingword.door43client.library.SQLiteHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    @Test
    public void getProjects() {
        StringBuilder sb = null;
        try {
            InputStream in = getContext().getAssets().open("schema.sqlite");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SQLiteHelper helper = new SQLiteHelper(getContext(), sb.toString(), "testDatabase");
        Library library = new Library(helper);
        library.listSourceLanguagesLastModified();
    }
}