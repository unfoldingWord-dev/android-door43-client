package org.unfoldingword.door43client;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.resourcecontainer.Project;
import org.unfoldingword.resourcecontainer.Resource;
import org.unfoldingword.resourcecontainer.ResourceContainer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by joel on 2/28/17.
 */
@RunWith(RobolectricTestRunner.class)
public class LibraryUpdateTest {
    private Library library;

    @Before
    public void initialize() throws Exception {
        Context context = RuntimeEnvironment.application;
        String schema = Util.loadResource(this.getClass().getClassLoader(), "schema.sqlite");

        // clean up old index
        context.deleteDatabase("index");

        // initialize library
        SQLiteHelper helper = new SQLiteHelper(context, schema, "index");
        this.library = new Library(helper);
    }

    @After
    public void tearDown() {
        library.closeDatabase();
    }

    @Test
    public void updateExistingResource() throws Exception {
        // set up initial resource
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        long languageId = library.addSourceLanguage(language);
        Project project = new Project("gen", "Genesis", 1);
        project.description = "The Book of Genesis";
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("bible-ot", "Old Testament"));
        long projectId = library.addProject(project, categories, languageId);

        Resource.Format format = new Resource.Format(ResourceContainer.version, ResourceContainer.baseMimeType + "+book", 10, "some url", false);
        Resource resource = new Resource("ulb", "Unlocked Literal Bible", "book", "all", "3", "4");
        resource.addFormat(format);
        long insertId = library.addResource(resource, projectId);
        assertTrue(insertId != -1);
        Resource initialResource = library.getResource(language.slug, project.slug, resource.slug);
        assertEquals(10, initialResource.formats.get(0).modifiedAt);


        // update resource
        Resource.Format updatedFormat = new Resource.Format(ResourceContainer.version, ResourceContainer.baseMimeType + "+book", 20, "some url", false);
        Resource updatedResource = new Resource("ulb", "Unlocked Literal Bible", "book", "all", "3", "4");
        updatedResource.addFormat(updatedFormat);
        long updatedId = library.addResource(updatedResource, projectId);

        // validate
        assertEquals(insertId, updatedId);
        Resource finalResource = library.getResource(language.slug, project.slug, resource.slug);
        assertEquals(20, finalResource.formats.get(0).modifiedAt);
    }
}
