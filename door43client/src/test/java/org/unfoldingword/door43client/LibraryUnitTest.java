package org.unfoldingword.door43client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.apache.tools.ant.taskdefs.Tar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.objects.Category;
import org.unfoldingword.door43client.objects.ChunkMarker;
import org.unfoldingword.door43client.objects.Project;
import org.unfoldingword.door43client.objects.TargetLanguage;
import org.unfoldingword.door43client.objects.SourceLanguage;
import org.unfoldingword.door43client.objects.Versification;
import org.unfoldingword.door43client.utils.Library;
import org.unfoldingword.door43client.utils.SQLiteHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
@RunWith(RobolectricTestRunner.class)
public class LibraryUnitTest {
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

        // clean up old index
        context.deleteDatabase("index");

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

    @Test
    public void addTempTargetLanguage() throws Exception {
        // test everything is good
        TargetLanguage language = new TargetLanguage("en", "English", "American English", "ltr", "United States", true);
        boolean success = library.addTempTargetLanguage(language);
        assertTrue(success);

        // test updated
        boolean updateSuccess = library.addTempTargetLanguage(language);
        assertTrue(updateSuccess);

        // test missing args
        TargetLanguage invalidLanguage = new TargetLanguage("en", null, "American English", "ltr", "United States", true);
        try {
            boolean invalidSuccess = library.addTempTargetLanguage(invalidLanguage);
            assertFalse(invalidSuccess);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getTargetLanguage() throws Exception {
        TargetLanguage l1 = new TargetLanguage("en1", "English", "American English", "ltr", "United States", true);
        TargetLanguage l2 = new TargetLanguage("en3", "English", "American English", "ltr", "United States", true);
        library.addTargetLanguage(l1);
        library.addTempTargetLanguage(l2);

        TargetLanguage found1 = library.getTargetLanguage(l1.slug);
        assertNotNull(found1);
        assertEquals(found1.slug, l1.slug);

        TargetLanguage found2 = library.getTargetLanguage(l2.slug);
        assertNotNull(found2);
        assertEquals(found2.slug, l2.slug);
    }

    public void getTargetLanguages() throws Exception {
        TargetLanguage l1 = new TargetLanguage("en1", "English", "American English", "ltr", "United States", true);
        TargetLanguage l2 = new TargetLanguage("en2", "English", "American English", "ltr", "United States", true);
        TargetLanguage l3 = new TargetLanguage("en3", "English", "American English", "ltr", "United States", true);
        TargetLanguage l4 = new TargetLanguage("en4", "English", "American English", "ltr", "United States", true);

        library.addTargetLanguage(l1);
        library.addTargetLanguage(l2);
        library.addTempTargetLanguage(l3);
        library.addTempTargetLanguage(l4);

        List<TargetLanguage> languages = library.getTargetLanguages();
        assertEquals(languages.size(), 4);
    }

    @Test
    public void setApprovedTargetLanguage() throws Exception {
        // temp language
        TargetLanguage tempLanguage = new TargetLanguage("temp-en", "Temporary English", "American English", "ltr", "United States", true);
        library.addTempTargetLanguage(tempLanguage);

        // target language as approved
        TargetLanguage approvedLanguage = new TargetLanguage("en", "English", "American English", "ltr", "United States", true);
        library.addTargetLanguage(approvedLanguage);

        // expect 2 languages
        assertEquals(library.getTargetLanguages().size(), 2);

        // assign approved language
        boolean success = library.setApprovedTargetLanguage(tempLanguage.slug, approvedLanguage.slug);
        assertTrue(success);

        // expect 1 language (they are now combined)
        assertEquals(library.getTargetLanguages().size(), 1);
        assertNull(library.getTargetLanguage(tempLanguage.slug));
    }

    @Test
    public void addProject() throws Exception {
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        long languageId = library.addSourceLanguage(language);
        Project project = new Project("gen", "Genesis", "The Book of Genesis", null, 1, null);
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("bible-ot", "Old Testament"));
        long id = library.addProject(project, categories, languageId);

        assertTrue(id > 0);
    }

    @Test
    public void addVersification() throws Exception {
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        Versification v = new Versification("en", "English Versification");
        long languageId = library.addSourceLanguage(language);
        long id = library.addVersification(v, languageId);
        assertTrue(id > 0);
    }

    @Test
    public void addChunkMarkers() throws Exception {
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        Versification v = new Versification("en", "English Versification");
        long languageId = library.addSourceLanguage(language);
        long versificationId = library.addVersification(v, languageId);

        ChunkMarker marker = new ChunkMarker("01", "01");
        long id = library.addChunkMarker(marker, "gen", versificationId);
        assertTrue(id > 0);
    }

    @Test
    public void addCatalog() throws Exception {
        // TODO: do this
    }
}