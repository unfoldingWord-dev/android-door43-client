package org.unfoldingword.door43client;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.models.Catalog;
import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.ChunkMarker;
import org.unfoldingword.door43client.models.Question;
import org.unfoldingword.door43client.models.Questionnaire;
import org.unfoldingword.door43client.models.TargetLanguage;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.Versification;
import org.unfoldingword.resourcecontainer.Project;
import org.unfoldingword.resourcecontainer.Resource;
import org.unfoldingword.resourcecontainer.ResourceContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void addSourceLanguage() throws Exception {
        // test everything is good
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        long id = library.addSourceLanguage(language);
        assertTrue(id > 0);

        // test updated
        SourceLanguage newLanguage = new SourceLanguage("en", "Updated English", "ltr");
        long updateId = library.addSourceLanguage(newLanguage);
        assertEquals(updateId, id);
        SourceLanguage updatedLanguage = library.getSourceLanguage("en");
        assertTrue(updatedLanguage.name.equals(newLanguage.name));

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
        TargetLanguage newLanguage = new TargetLanguage("en", "Updated English", "American English Updated", "ltr", "United States", true);
        boolean updateSuccess = library.addTargetLanguage(newLanguage);
        assertTrue(updateSuccess);
        TargetLanguage updatedLanguage = library.getTargetLanguage("en");
        assertTrue(updatedLanguage.name.equals(newLanguage.name));

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
        TargetLanguage language = new TargetLanguage("temp-en", "English", "American English", "ltr", "United States", true);
        boolean success = library.addTempTargetLanguage(language);
        assertTrue(success);

        // test updated
        TargetLanguage newLanguage = new TargetLanguage("temp-en", "Updated English", "American English", "ltr", "United States", true);
        boolean updateSuccess = library.addTempTargetLanguage(newLanguage);
        assertTrue(updateSuccess);
        TargetLanguage updatedLanguage = library.getTargetLanguage("temp-en");
        assertTrue(updatedLanguage.name.equals(newLanguage.name));

        // test missing args
        TargetLanguage invalidLanguage = new TargetLanguage("temp-en", null, "American English", "ltr", "United States", true);
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

        // expect temp language no longer accessible
        assertNull(library.getTargetLanguage(tempLanguage.slug));

        // expect approved language
        TargetLanguage fetchedApprovedLanguage = library.getApprovedTargetLanguage(tempLanguage.slug);
        assertNotNull(fetchedApprovedLanguage);
        assertEquals(fetchedApprovedLanguage.slug, approvedLanguage.slug);
        assertTrue(fetchedApprovedLanguage.name.equals(approvedLanguage.name));
    }

    @Test
    public void addProject() throws Exception {
        SourceLanguage language = new SourceLanguage("en", "English", "ltr");
        long languageId = library.addSourceLanguage(language);
        Project project = new Project("gen", "Genesis", 1);
        project.description = "The Book of Genesis";
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("bible-ot", "Old Testament"));
        long id = library.addProject(project, categories, languageId);
        assertTrue(id > 0);

        // test update
        Project newProject = new Project("gen", "Updated Genesis", 1);
        newProject.description = project.description;
        long updatedId = library.addProject(newProject, categories, languageId);
        assertEquals(updatedId, id);
        Project updatedProject = library.getProject(language.slug, project.slug);
        assertTrue(updatedProject.name.equals(newProject.name));
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
        Project project = new Project("gen", "Genesis", 1);
        project.description = "The Book of Genesis";
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("bible-ot", "Old Testament"));
        long projectId = library.addProject(project, categories, languageId);

        Resource.Format format = new Resource.Format(ResourceContainer.version, ResourceContainer.baseMimeType + "+book", 0, "some url", false);
        Resource resource = new Resource("ulb", "Unlocked Literal Bible", "book", "all", "3", "4");


        // test invalid
        try {
            long invalidId = library.addResource(resource, projectId);
            assertTrue(invalidId == -1);
        } catch (Exception e) {
            assertNotNull(e);
        }

        // test good
        resource.addFormat(format);
        long resourceId = library.addResource(resource, projectId);
        assertTrue(resourceId > 0);

        // test update
        Resource newResource = new Resource("ulb", "Updated Unlocked Literal Bible", "book", "all", "3", "4");
        newResource.addFormat(format);
        long updatedResourceid = library.addResource(newResource, projectId);
        assertEquals(updatedResourceid, resourceId);
    }

    @Test
    public void addQuestionnaire() throws Exception {
        Map<String, Long> dataFields = new HashMap<>();
        dataFields.put("ln", (long)1);
        dataFields.put("ld", (long)2);
        dataFields.put("lr", (long)3);
        Questionnaire questionnaire = new Questionnaire("en", "English", "ltr", 1, dataFields);
        long id = library.addQuestionnaire(questionnaire);
        assertTrue(id > 0);

        // test update
        long updatedId = library.addQuestionnaire(questionnaire);
        assertEquals(updatedId, id);
    }

    @Test
    public void addQuestion() throws Exception {
        Map<String, Long> dataFields = new HashMap<>();
        dataFields.put("ln", (long)1);
        dataFields.put("ld", (long)2);
        dataFields.put("lr", (long)3);
        Questionnaire questionnaire = new Questionnaire("en", "English", "ltr", 1, dataFields);
        long questionnaireId = library.addQuestionnaire(questionnaire);

        Question question = new Question("MY question", "answer me!", true, Question.InputType.String, 0, 0, 1);
        long id = library.addQuestion(question, questionnaireId);
        assertTrue(id > 0);

        // test update
        long updatedId = library.addQuestion(question, questionnaireId);
        assertEquals(updatedId, id);
    }

    @Test
    public void addQuestionWithNoDependency() throws Exception {
        Map<String, Long> dataFields = new HashMap<>();
        dataFields.put("ln", (long)1);
        dataFields.put("ld", (long)2);
        dataFields.put("lr", (long)3);
        Questionnaire questionnaire = new Questionnaire("en", "English", "ltr", 1, dataFields);
        long questionnaireId = library.addQuestionnaire(questionnaire);

        Question question = new Question("My other question", "answer me!", true, Question.InputType.String, 0, -1, 2);
        long id = library.addQuestion(question, questionnaireId);
        assertTrue(id > 0);

        List<Question> questions = library.getQuestions(questionnaireId);
        for(Question q:questions) {
            if(q.tdId == question.tdId) {
                assertEquals(-1, q.dependsOn);
                return;
            }
        }
        throw new Exception("missing question");
    }
}