package org.unfoldingword.door43client;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.objects.TargetLanguage;
import org.unfoldingword.door43client.objects.SourceLanguage;
import org.unfoldingword.door43client.utils.Library;
import org.unfoldingword.door43client.utils.SQLiteHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
public class ExampleUnitTest {
    private Context context;
    private Library library;


    @Before
    public void initialize() throws Exception {
        this.context = RuntimeEnvironment.application;

        // load schema
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL resource = classLoader.getResource("schema.sqlite");
        File sqliteFile = new File(resource.getPath());

        // read schema
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sqliteFile)));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        // TODO: make sure the database is brand new (deleted)

        // initialize library
        SQLiteHelper helper = new SQLiteHelper(context, sb.toString(), "index");
        this.library = new Library(helper);
    }

    @Test
    public void addSourceLanguage() throws Exception {
        // test everything is good
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        long id = library.addSourceLanguage(language);
        assertTrue(id > 0);

        // test updated
        long updateId = library.addSourceLanguage(language);
        assertEquals(updateId, id);

        // test missing args
        SourceLanguage invalidLanguage = new SourceLanguage("en", "English", null);
        try {
            long invalidId = library.addSourceLanguage(invalidLanguage);
            assertTrue(invalidId < 0);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void addTargetLanguage() throws Exception {
        // test everything is good
        TargetLanguage language = new TargetLanguage("en", "English", "American English", "ltr", "United States", true);
        boolean success = library.addTargetLanguage(language);
        assertTrue(success);

        // test updated
        boolean updateSuccess = library.addTargetLanguage(language);
        assertTrue(updateSuccess);

        // test missing args
        TargetLanguage invalidLanguage = new TargetLanguage("en", null, "American English", "ltr", "United States", true);
        try {
            boolean invalidSuccess = library.addTargetLanguage(invalidLanguage);
            assertFalse(invalidSuccess);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }
}