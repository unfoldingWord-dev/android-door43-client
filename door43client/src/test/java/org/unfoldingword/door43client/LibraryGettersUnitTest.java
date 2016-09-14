package org.unfoldingword.door43client;

import android.content.Context;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.objects.Catalog;
import org.unfoldingword.door43client.objects.Category;
import org.unfoldingword.door43client.objects.ChunkMarker;
import org.unfoldingword.door43client.objects.Project;
import org.unfoldingword.door43client.objects.Question;
import org.unfoldingword.door43client.objects.Questionnaire;
import org.unfoldingword.door43client.objects.Resource;
import org.unfoldingword.door43client.objects.SourceLanguage;
import org.unfoldingword.door43client.objects.TargetLanguage;
import org.unfoldingword.door43client.objects.Versification;
import org.unfoldingword.door43client.utils.Library;
import org.unfoldingword.door43client.utils.SQLiteHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by joel on 9/14/16.
 */
@RunWith(RobolectricTestRunner.class)
public class LibraryGettersUnitTest {
    private boolean setupIsDone = false;
    private static final int GENERATOR_QTY = 5;
    private static Context context;
    private static Library library;

    /**
     * Generates a bunch of unique strings
     * @param prefix the string prefix
     * @param quantity
     * @return
     */
    private static List<String> stringGenerator(String prefix, int quantity) {
        List<String> results = new ArrayList<>();
        for(int i = 0; i < quantity; i ++) {
            results.add(prefix + (i + 1));
        }
        return results;
    }

    private static void buildData() throws Exception {
        // temp target languages
        for(String slug:stringGenerator("temp-en", GENERATOR_QTY)) {
            TargetLanguage l = new TargetLanguage(slug, "Temp English", "American English", "ltr", "United States", true);
            library.addTempTargetLanguage(l);
        }

        // target languages
        for(String slug:stringGenerator("en", GENERATOR_QTY)) {
            TargetLanguage l = new TargetLanguage(slug, "English", "American English", "ltr", "United States", true);
            library.addTempTargetLanguage(l);
        }

        // source languages
        for(String lSlug:stringGenerator("en", GENERATOR_QTY)) {
            SourceLanguage l = new SourceLanguage(lSlug, "English", "ltr");
            long sourceLanguageId = library.addSourceLanguage(l);

            // versification
            long versificationId = 0;
            for(String slug:stringGenerator("versification", GENERATOR_QTY)) {
                Versification v = new Versification(slug, "Versification");
                long id = library.addVersification(v, sourceLanguageId);
                if(versificationId == 0) versificationId = id; // keep the first one for projects
            }

            // projects - no category
            for(String pSlug:stringGenerator("proj-no-cat-", GENERATOR_QTY)) {
                Project p = new Project(pSlug, "Genesis", "The Book of Genesis", null, 1, null);
                long projectId = library.addProject(p, new ArrayList(){}, sourceLanguageId);
                buildResources(projectId);
                buildChunks(pSlug, versificationId);
            }

            // projects - level 1 category
            for(String pSlug:stringGenerator("proj-cat1-", GENERATOR_QTY)) {
                Project p = new Project(pSlug, "Genesis", "The Book of Genesis", null, 1, null);
                List<Category> categories = new ArrayList<>();
                categories.add(new Category("cat1", "First level category"));
                long projectId = library.addProject(p, categories, sourceLanguageId);
                buildResources(projectId);
                buildChunks(pSlug, versificationId);
            }

            // projects - level 2 category
            for(String pSlug:stringGenerator("proj-cat2-", GENERATOR_QTY)) {
                Project p = new Project(pSlug, "Genesis", "The Book of Genesis", null, 1, null);
                List<Category> categories = new ArrayList<>();
                categories.add(new Category("cat1", "First level category"));
                categories.add(new Category("cat2", "Second level category"));
                long projectId = library.addProject(p, categories, sourceLanguageId);
                buildResources(projectId);
                buildChunks(pSlug, versificationId);
            }
        }

        // catalogs
        for(String slug:stringGenerator("cat", GENERATOR_QTY)) {
            Catalog c = new Catalog(slug, "some url", 0);
            library.addCatalog(c);
        }

        for(int i = 0; i < GENERATOR_QTY; i ++) {
            Questionnaire questionnaire = new Questionnaire("lang" + i, "Language name", "ltr", (long)i);
            long questionnaireId = library.addQuestionnaire(questionnaire);
            for(int j = 0; j < GENERATOR_QTY; j ++) {
                Question q = new Question("question", "help", true, "text", 0, 0, j);
                library.addQuestion(q, questionnaireId);
            }
        }
    }

    private static void buildResources(long projectId) throws Exception {
        for(String slug:stringGenerator("res", 20)) {
            // TODO: 9/14/16 add resources
//            Resource r = new Resource();
//            library.addResource(r, projectId);
        }
    }

    private static void buildChunks(String projectSlug, long versificationId) throws Exception {
        for(String chapter:stringGenerator("", GENERATOR_QTY)) {
            for(String verse:stringGenerator("", GENERATOR_QTY)) {
                ChunkMarker c = new ChunkMarker(chapter, verse);
                library.addChunkMarker(c, projectSlug, versificationId);
            }
        }
    }

    @Before
    public void initialize() throws Exception {
        if(setupIsDone) return;
        setupIsDone = true;
        context = RuntimeEnvironment.application;

        // load schema
        ClassLoader classLoader = getClass().getClassLoader();
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
        library = new Library(helper);

        buildData();
    }

    @Test
    public void getTargetLanguage() throws Exception {
        TargetLanguage found1 = library.getTargetLanguage("en1");
        assertNotNull(found1);
        assertEquals(found1.slug, "en1");

        TargetLanguage found2 = library.getTargetLanguage("en2");
        assertNotNull(found2);
        assertEquals(found2.slug, "en2");
    }

    @Test
    public void getTargetLanguages() throws Exception {
        List<TargetLanguage> languages = library.getTargetLanguages();
        assertTrue(languages.size() > 0);
    }
}
