package org.unfoldingword.door43client;

import android.content.Context;
import android.util.Log;

import org.junit.After;
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
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.TargetLanguage;
import org.unfoldingword.door43client.models.Versification;
import org.unfoldingword.resourcecontainer.ResourceContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by joel on 9/14/16.
 */
@RunWith(RobolectricTestRunner.class)
public class LibraryGettersUnitTest {
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
            library.addTargetLanguage(l);
        }

        // approved target language
        library.setApprovedTargetLanguage("temp-en1", "en1");

        // source languages
        for(String lSlug:stringGenerator("en", GENERATOR_QTY)) {
            SourceLanguage l = new SourceLanguage(lSlug, "English", "ltr");
            long sourceLanguageId = library.addSourceLanguage(l);

            // versification
            long versificationId = 0;
            for(String slug:stringGenerator("versi", GENERATOR_QTY)) {
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
        for(String slug:stringGenerator("res", GENERATOR_QTY)) {
            Map<String, Object> status = new HashMap<>();
            status.put("translate_mode", "all");
            status.put("checking_level", "3");
            status.put("version", "4");
            Resource r = new Resource(slug, "Unlocked Literal Bible", "book", "some url", status);
            Resource.Format format = new Resource.Format(ResourceContainer.version, ResourceContainer.baseMimeType + "+book", 0, "some url");
            r.addFormat(format);
            library.addResource(r, projectId);
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
        context = RuntimeEnvironment.application;
        Log.d("Tests", "Initializing");

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

        library.beginTransaction();
        try {
            buildData();
            library.endTransaction(true);
        } catch(Exception e) {
            library.endTransaction(false);
            throw e;
        }
    }

    @After
    public void cleanup() {
        library.closeDatabase();
    }

    @Test
    public void listProjectsLastModified() throws Exception {
        Map<String, Integer> modified = library.listProjectsLastModified("en1");
        assertTrue(modified.size() > 0);
    }

    @Test
    public void getSourceLanguage() throws Exception {
        SourceLanguage found1 = library.getSourceLanguage("en1");
        assertNotNull(found1);
        assertEquals(found1.slug, "en1");

        SourceLanguage found2 = library.getSourceLanguage("en2");
        assertNotNull(found2);
        assertEquals(found2.slug, "en2");
    }

    @Test
    public void getSourceLanguages() throws Exception {
        List<SourceLanguage> languages = library.getSourceLanguages();
        assertTrue(languages.size() > 0);
    }

    @Test
    public void getTargetLanguage() throws Exception {
        TargetLanguage found1 = library.getTargetLanguage("en1");
        assertNotNull(found1);
        assertEquals(found1.slug, "en1");

        TargetLanguage found2 = library.getTargetLanguage("en2");
        assertNotNull(found2);
        assertEquals(found2.slug, "en2");

        // approved so this will not be accessible
        TargetLanguage found3 = library.getTargetLanguage("temp-en1");
        assertNull(found3);
    }

    @Test
    public void getTargetLanguages() throws Exception {
        List<TargetLanguage> languages = library.getTargetLanguages();
        assertTrue(languages.size() > 0);
    }

    @Test
    public void getApprovedTargetLanguage() throws Exception {
        TargetLanguage language = library.getApprovedTargetLanguage("temp-en1");
        assertNotNull(language);
        assertEquals(language.slug, "en1");
    }

    @Test
    public void getProject() throws Exception {
        Project p1 = library.getProject("en1", "proj-no-cat-1");
        Project p2 = library.getProject("en1", "proj-cat1-1");
        Project p3 = library.getProject("en1", "proj-cat2-1");

        assertNotNull(p1);
        assertNotNull(p2);
        assertNotNull(p3);
    }

    @Test
    public void getProjects() throws Exception {
        List<Project> projects = library.getProjects("en1");
        assertTrue(projects.size() > 0);
    }

    @Test
    public void getProjectCategories() throws Exception {
        // TODO: 9/16/16 write this after we finish the method
//        List<CategoryEntry> list = library.getProjectCategories(0, "en1", "all");
//        assertTrue(list.size() > 0);
    }

    @Test
    public void getResource() throws Exception {
        Resource r = library.getResource("en1", "proj-cat2-1", "res1");
        assertNotNull(r);

        // test missing resource
        Resource missingR = library.getResource("en1", "proj-cat2-1", "missing");
        assertNull(missingR);

        // test missing project
        Resource missingP = library.getResource("en1", "missing", "res1");
        assertNull(missingP);

        // test missing language
        Resource missingL = library.getResource("missing", "proj-cat2-1", "res1");
        assertNull(missingL);
    }

    @Test
    public void getResources() throws Exception {
        // test filtered by language
        List<Resource> filteredList = library.getResources("en1", "proj-cat2-1");
        assertTrue(filteredList.size() > 0);

        // test un-filtered
        List<Resource> list = library.getResources(null, "proj-cat2-1");
        assertTrue(list.size() > filteredList.size());
    }

    @Test
    public void getCatalog() throws Exception {
        Catalog c = library.getCatalog("cat1");
        assertNotNull(c);
    }

    @Test
    public void getCatalogs() throws Exception {
        List<Catalog> list = library.getCatalogs();
        assertTrue(list.size() > 0);
    }

    @Test
    public void getVersification() throws Exception {
        Versification v = library.getVersification("en1", "versi1");
        assertNotNull(v);
    }

    @Test
    public void getVersifications() throws Exception {
        List<Versification> list = library.getVersifications("en1");
        assertTrue(list.size() > 0);
    }

    @Test
    public void getChunkMarkers() throws Exception {
        List<ChunkMarker> list = library.getChunkMarkers("proj-cat1-1" , "versi1");
        assertTrue(list.size() > 0);
    }

    @Test
    public void getQuestionnairesAndQuestions() throws Exception {
        List<Questionnaire> list = library.getQuestionnaires();
        assertTrue(list.size() > 0);
        for(Questionnaire q:list) {
            List<Question> questions = library.getQuestions(q._dbInfo.rowId);
            assertTrue(questions.size() > 0);
        }
    }

}
