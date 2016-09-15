package org.unfoldingword.door43client;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.models.Catalog;
import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.ChunkMarker;
import org.unfoldingword.door43client.models.Project;
import org.unfoldingword.door43client.models.Question;
import org.unfoldingword.door43client.models.Questionnaire;
import org.unfoldingword.door43client.models.Resource;
import org.unfoldingword.door43client.models.TargetLanguage;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.Versification;

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
public class LibrarySettersUnitTest {
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

        // expect temp langauge no longer accessible
        assertNull(library.getTargetLanguage(tempLanguage.slug));

        // expect approved language
        TargetLanguage fetchedApprovedLanguage = library.getApprovedTargetLanguage(tempLanguage.slug);
        assertNotNull(fetchedApprovedLanguage);
        assertEquals(fetchedApprovedLanguage.slug, approvedLanguage.slug);
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

        // test update
        long updatedId = library.addProject(project, categories, languageId);
        assertEquals(updatedId, id);
    }

    @Test
    public void addVersification() throws Exception {
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        Versification v = new Versification("en", "English Versification");
        long languageId = library.addSourceLanguage(language);
        long id = library.addVersification(v, languageId);
        assertTrue(id > 0);

        // test update
        long updatedId = library.addVersification(v, languageId);
        assertEquals(updatedId, id);
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

        // test update
        long updatedId = library.addChunkMarker(marker, "gen", versificationId);
        assertEquals(updatedId, id);
    }

    @Test
    public void addCatalog() throws Exception {
        Catalog catalog = new Catalog("targetlanguages", "someurl", 0);
        long id = library.addCatalog(catalog);
        assertTrue(id > 0);

        // test update
        long updatedId = library.addCatalog(catalog);
        assertEquals(updatedId, id);
    }

    @Test
    public void addResource() throws Exception {
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        long languageId = library.addSourceLanguage(language);
        Project project = new Project("gen", "Genesis", "The Book of Genesis", null, 1, null);
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("bible-ot", "Old Testament"));
        long projectId = library.addProject(project, categories, languageId);

//        Resource resource = new Resource();
        // TODO: 9/14/16 finish building the resource
//        long resourceId = library.addResource(resource, projectId);
//        assertTrue(resourceId > 0);
    }

    @Test
    public void addQuestionnaire() throws Exception {
        Questionnaire questionnaire = new Questionnaire("en", "English", "ltr", 1);
        long id = library.addQuestionnaire(questionnaire);
        assertTrue(id > 0);

        // test update
        long updatedId = library.addQuestionnaire(questionnaire);
        assertEquals(updatedId, id);
    }

    @Test
    public void addQuestion() throws Exception {
        Questionnaire questionnaire = new Questionnaire("en", "English", "ltr", 1);
        long questionnaireId = library.addQuestionnaire(questionnaire);

        Question question = new Question("MY question", "answer me!", true, "text", 0, 0, 1);
        long id = library.addQuestion(question, questionnaireId);
        assertTrue(id > 0);

        // test update
        long updatedId = library.addQuestion(question, questionnaireId);
        assertEquals(updatedId, id);
    }
}