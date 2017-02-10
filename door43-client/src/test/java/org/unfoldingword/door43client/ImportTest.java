package org.unfoldingword.door43client;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.CategoryEntry;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.Translation;
import org.unfoldingword.resourcecontainer.Project;
import org.unfoldingword.resourcecontainer.Resource;
import org.unfoldingword.resourcecontainer.ResourceContainer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class ImportTest {
    @Rule
    public TemporaryFolder resourceDir = new TemporaryFolder();

    private Context context;
    private API client;

    @Before
    public void setUp() throws Exception {
        this.context = RuntimeEnvironment.application;
        String schema = Util.loadResource(this.getClass().getClassLoader(), "schema.sqlite");

        // preload data
        DatabaseContext databaseContext = new DatabaseContext(context, resourceDir.getRoot(), "sqlite");
        SQLiteHelper helper = new SQLiteHelper(databaseContext, schema, "index");
        Library library = new Library(helper);

        // tit
        long langId = library.addSourceLanguage(new SourceLanguage("en", "English", "ltr"));
        Project p = new Project("tit", "Titus", 0);
        long projId = library.addProject(p, new ArrayList<Category>(), langId);
        Resource r = new Resource("udb", "Unlocked Dynamic Bible", "book", "gl", "3", "3.0");
        r.addFormat(new Resource.Format(ResourceContainer.version, "text/usfm", 0, "", false));
        library.addResource(r, projId);

        // 1th
        langId = library.addSourceLanguage(new SourceLanguage("en", "English", "ltr"));
        p = new Project("1th", "1 Thessalonians", 0);
        projId = library.addProject(p, new ArrayList<Category>(), langId);
        r = new Resource("udb", "Unlocked Dynamic Bible", "book", "gl", "3", "3.0");
        r.addFormat(new Resource.Format(ResourceContainer.version, "text/usfm", 0, "", false));
        library.addResource(r, projId);

        library.closeDatabase();

        // initialize client
        client = new API(context, schema, new File(resourceDir.getRoot(), "index.sqlite"), resourceDir.getRoot());
    }


    @After
    public void tearDown() {
        client.tearDown();
    }


    @Test
    public void ImportDuplicateResourceContainer() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("en_tit_ulb");
        File dir = new File(url.getPath());
        ResourceContainer rc = client.importResourceContainer(dir);

        // test checking level filter works.
        List<Translation> translations = client.index().findTranslations(null, "tit", null, "book", null, 3, -1);
        assertEquals(2, translations.size());
        translations = client.index().findTranslations(null, "tit", null, "book", null, 99, -1);
        assertEquals(1, translations.size());

        // test translate mode filter works
        translations = client.index().findTranslations(null, "tit", null, "book", "gl", 3, -1);
        assertEquals(2, translations.size());
        translations = client.index().findTranslations(null, "tit", null, "book", "all", 3, -1);
        assertEquals(1, translations.size());
    }

    @Test
    public void failImportingContainerWithNewProject() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("en_tit-new_ulb");
        File dir = new File(url.getPath());
        try {
            ResourceContainer rc = client.importResourceContainer(dir);
            assertTrue(false);
        } catch (Exception e) {
            assertEquals("Unsupported project", e.getMessage());
        }
    }

    @Test
    public void ImportResourceContainerWithCustomResource() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        URL url = classLoader.getResource("en_1th_ulb_ck");
        File dir = new File(url.getPath());
        ResourceContainer rc = client.importResourceContainer(dir);

        // TRICKY: manually imported containers are not bound by minimum and maximum checking levels
        List<Translation> translations = client.index().findTranslations(null, "1th", null, "book", null, 3, -1);
        assertEquals(2, translations.size());
        int numUDB = 0;
        int numCCK = 0;
        int numOther = 0;
        for(Translation t:translations) {
            if(t.resource.slug.equals("udb")) {
                numUDB ++;
            } else if(t.resource.slug.equals("cck")) {
                numCCK ++;
            } else {
                numOther ++;
            }
        }
        assertEquals(1, numUDB);
        assertEquals(1, numCCK);
        assertEquals(0, numOther);
    }

    // we currently do not support importing new projects. If we do this test will check that
//    @Test
//    public void ImportContainerWithNewProject() throws Exception {
//        ClassLoader classLoader = this.getClass().getClassLoader();
//        URL url = classLoader.getResource("en_tit-new_ulb");
//        File dir = new File(url.getPath());
//        ResourceContainer rc = client.importResourceContainer(dir);
//
//        // make sure the indexed project is accessible
//        Project p = client.index().getProject("en", "tit-new");
//        assertNotNull(p);
//
//        List<CategoryEntry> entries = client.index().getProjectCategories(0, "en", null);
//        assertEquals(1, entries.size());
//        assertEquals("bible-nt", entries.get(0).slug);
//        assertEquals(CategoryEntry.Type.CATEGORY, entries.get(0).entryType);
//        List<CategoryEntry> subEntries = client.index().getProjectCategories(entries.get(0).id, "en", null);
//        assertEquals(1, subEntries.size());
//        assertEquals("tit-new", subEntries.get(0).slug);
//        assertEquals(CategoryEntry.Type.PROJECT, subEntries.get(0).entryType);
//    }
}
