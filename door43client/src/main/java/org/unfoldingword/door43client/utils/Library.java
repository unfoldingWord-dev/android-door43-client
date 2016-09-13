package org.unfoldingword.door43client.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.unfoldingword.door43client.objects.Category;
import org.unfoldingword.door43client.objects.Chunk;
import org.unfoldingword.door43client.objects.ChunkMarker;
import org.unfoldingword.door43client.objects.TargetLanguage;
import org.unfoldingword.door43client.objects.Project;
import org.unfoldingword.door43client.objects.Question;
import org.unfoldingword.door43client.objects.Resource;
import org.unfoldingword.door43client.objects.SourceLanguage;
import org.unfoldingword.door43client.objects.Versification;
import org.unfoldingword.door43client.objects.Catalog;
import org.unfoldingword.door43client.objects.Questionnaire;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Manages the indexed library content.
 */
public class Library {

    private final SQLiteHelper sqliteHelper;
    private final SQLiteDatabase db;

    /**
     * Intantiates a new library
     * @param sqliteHelper
     */
    public Library(SQLiteHelper sqliteHelper) {
        this.sqliteHelper = sqliteHelper;
        this.db = sqliteHelper.getWritableDatabase();
    }

    /**
     * Ensures a value is not null or empty
     * @param value
     * @throws Exception
     */
    private void validateNotEmpty(String value) throws Exception {
        if(value == null || value.isEmpty()) throw new Exception("Invalid parameter value");
    }

    /**
     * A utility to perform insert+update operations.
     * Insert failures are ignored.
     * Update failures are thrown.
     *
     * @param table
     * @param values
     * @param uniqueColumns an array of unique columns on this table. This should be a subset of the values.
     * @return the id of the inserted/updated row
     */
    private long insertOrUpdate(String table, ContentValues values, String[] uniqueColumns) throws Exception {
        // insert
        long id = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if(id == -1) {
            // prepare unique columns
            String uniqueWhere = TextUtils.join("=? and ", uniqueColumns) + "=?";
            String[] uniqueValues = new String[uniqueColumns.length];
            for(int i = 0; i < uniqueColumns.length; i ++) {
                uniqueValues[i] = String.valueOf(values.get(uniqueColumns[i]));
            }

            // clean values
            for(String key:uniqueColumns) {
                values.remove(key);
            }

            // update
            int numRows = db.updateWithOnConflict(table, values, uniqueWhere, uniqueValues, SQLiteDatabase.CONFLICT_ROLLBACK);
            if(numRows == 0) {
                throw new Exception("Failed to update the row in " + table);
            } else {
                // retrieve updated row id
                Cursor cursor = db.rawQuery("select id from " + table + " where " + uniqueWhere, uniqueValues);
                if(cursor.moveToFirst()) {
                    id = cursor.getLong(0);
                    cursor.close();
                } else {
                    cursor.close();
                    throw new Exception("Failed to find the row in " + table);
                }
            }
        }
        return id;
    }

    /**
     * Inserts or updates a source language in the library.
     *
     * @param language
     * @return the id of the source language row
     * @throws Exception
     */
    public long addSourceLanguage(SourceLanguage language) throws Exception {
        validateNotEmpty(language.slug);
        validateNotEmpty(language.name);
        validateNotEmpty(language.direction);

        ContentValues values = new ContentValues();
        values.put("slug", language.slug);
        values.put("name", language.name);
        values.put("direction", language.direction);

        return insertOrUpdate("source_language", values, new String[]{"slug"});
    }

    /**
     * Inserts or updates a target language in the library.
     *
     * Note: the result is boolean since you don't need the row id. See getTargetLanguages for more information
     *
     * @param language
     * @return
     * @throws Exception
     */
    public boolean addTargetLanguage(TargetLanguage language) throws Exception {
        validateNotEmpty(language.slug);
        validateNotEmpty(language.name);
        validateNotEmpty(language.direction);

        ContentValues values = new ContentValues();
        values.put("slug", language.slug);
        values.put("name", language.name);
        values.put("direction", language.direction);
        values.put("anglicized_name", language.anglicizedName);
        values.put("region", language.region);
        values.put("is_gateway_language", language.isGatewayLanguage ? 1: 0);

        long id = insertOrUpdate("target_language", values, new String[]{"slug"});
        return id > 0;
    }

    /**
     * Inserts or updates a temporary target language in the library.
     *
     * Note: the result is boolean since you don't need the row id. See getTargetLanguages for more information
     *
     * @param language
     * @return
     * @throws Exception
     */
    public boolean addTempTargetLanguage(TargetLanguage language) throws Exception {
        validateNotEmpty(language.slug);
        validateNotEmpty(language.name);
        validateNotEmpty(language.direction);

        ContentValues values = new ContentValues();
        values.put("slug", language.slug);
        values.put("name", language.name);
        values.put("direction", language.direction);
        values.put("anglicized_name", language.anglicizedName);
        values.put("region", language.region);
        values.put("is_gateway_language", language.isGatewayLanguage ? 1 : 0);

        long id = insertOrUpdate("temp_target_language", values, new String[]{"slug"});
        return id > 0;
    }

    /**
     * Updates the target language assigned to a temporary target language
     * @param tempTargetLanguageSlug the temporary target language that is being assigned a target language
     * @param targetLanguageSlug the assigned target language
     * @return indicates if the approved language was successfully set
     */
    public boolean setApprovedTargetLanguage(String tempTargetLanguageSlug, String targetLanguageSlug) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("approved_target_language_slug", targetLanguageSlug);

        int rowsAffected = db.updateWithOnConflict("temp_target_language", contentValues,
                "slug=?", new String[]{tempTargetLanguageSlug}, SQLiteDatabase.CONFLICT_IGNORE );

        return rowsAffected > 0;
    }

    /**
     * Inserts or updates a project in the library
     *
     * @param project
     * @param categories this is the category branch that the project will attach to
     * @param sourceLanguageId the parent source language row id
     * @return the id of the project row
     * @throws Exception
     */
    public long addProject(Project project, List<Category> categories, long sourceLanguageId) throws Exception {
        validateNotEmpty(project.slug);
        validateNotEmpty(project.name);

        // add categories
        long parentCategoryId = 0;
        if(categories != null) {
            // build categories
            for(Category category:categories) {
                ContentValues insertValues = new ContentValues();
                insertValues.put("slug", category.slug);
                insertValues.put("parent_id", parentCategoryId);

                long id = db.insertWithOnConflict("category", null, insertValues, SQLiteDatabase.CONFLICT_IGNORE);
                if(id > 0) {
                    parentCategoryId = id;
                } else {
                    Cursor cursor = db.rawQuery("select id from category where slug=? and parent_id=" + parentCategoryId, new String[]{category.slug});
                    if(cursor.moveToFirst()){
                        parentCategoryId = cursor.getLong(0);
                    } else {
                        throw new Exception("Invalid category");
                    }
                }

                ContentValues updateValues = new ContentValues();
                updateValues.put("source_language_id", sourceLanguageId);
                updateValues.put("category_id", parentCategoryId);
                updateValues.put("name", category.name);

                insertOrUpdate("category_name", updateValues, new String[]{"source_language_id", "category_id"});
            }
        }
        // add project
        ContentValues updateProject = new ContentValues();
        updateProject.put("slug", project.slug);
        updateProject.put("name", project.name);
        updateProject.put("desc", project.description);
        updateProject.put("icon", project.icon);
        updateProject.put("sort", project.sort);
        updateProject.put("chunks_url", project.chunksUrl);
        updateProject.put("source_language_id", sourceLanguageId);
        updateProject.put("category_id", parentCategoryId);

        return insertOrUpdate("project", updateProject, new String[]{"slug", "source_language_id"});
    }

    /**
     * Inserts or updates a versification in the library.
     *
     * @param versification
     * @param sourceLanguageId the parent source language row id
     * @return the id of the versification or -1
     * @throws Exception
     */
    public long addVersification(Versification versification, int sourceLanguageId) throws Exception{
        validateNotEmpty(versification.name);

        ContentValues values = new ContentValues();
        values.put("slug", versification.slug);

        long versificationId = db.insertWithOnConflict("versification", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if(versificationId > 0) {
            ContentValues cv = new ContentValues();
            cv.put("source_language_id", sourceLanguageId);
            cv.put("versification_id", versificationId);
            cv.put("name", versification.name);
            versificationId = insertOrUpdate("versification_name", cv, new String[]{"source_language_id", "versification_id"});
        }
        return versificationId;
    }

    /**
     * Inserts a chunk marker in the library.
     *
     * @param chunk
     * @param projectSlug the project that this marker exists in
     * @param versificationId the versification this chunk is a member of
     * @return the id of the chunk marker
     * @throws Exception
     */
    public long addChunkMarker(Chunk chunk, String projectSlug, long versificationId) throws Exception {
        validateNotEmpty(chunk.chapter);
        validateNotEmpty(chunk.verse);
        validateNotEmpty(projectSlug);

        ContentValues chunkValues = new ContentValues();
        chunkValues.put("chapter", chunk.chapter);
        chunkValues.put("verse", chunk.verse);
        chunkValues.put("project_slug", projectSlug);
        chunkValues.put("versification_id", versificationId);

        long id= db.insertWithOnConflict("chunk_marker", null, chunkValues, SQLiteDatabase.CONFLICT_IGNORE);
        if(id == -1) {
            Cursor cursor = db.rawQuery("select id from chunk_marker where project_slug=? and versification_id=" + versificationId, new String[]{projectSlug});
            if(cursor.moveToFirst()){
                id = cursor.getLong(0);
            } else {
                throw new Exception("Invalid Chunk Marker");
            }
        }
        return id;
    }

    /**
     * Inserts or updates a catalog in the library.
     *
     * @param catalog
     * @return the id of the catalog
     * @throws Exception
     */
    public long addCatalog(Catalog catalog) throws Exception{
        validateNotEmpty(catalog.slug);
        validateNotEmpty(catalog.url);

        ContentValues values = new ContentValues();
        values.put("slug", catalog.slug);
        values.put("url", catalog.url);
        values.put("modified_at", catalog.modifiedAt);

        return insertOrUpdate("catalog", values, new String[]{"slug"});
    }

    public long addResource(Resource resource, long projectId) throws Exception {
        validateNotEmpty(resource.slug);
        validateNotEmpty(resource.name);
        validateNotEmpty(resource.type);
        validateNotEmpty((String)resource.status.get("translate_mode"));
        validateNotEmpty((String)resource.status.get("checking_level"));
        validateNotEmpty((String)resource.status.get("version"));

        //TODO add more here
        return -1;
    }

    /**
     *Inserts or updates a questionnaire in the library.
     *
     * @param questionnaire the questionnaire to add
     * @return the id of the questionnaire row
     * @throws Exception
     */
    public long addQuestionaire(Questionnaire questionnaire) throws Exception {
        validateNotEmpty(questionnaire.languageSlug);
        validateNotEmpty(questionnaire.languageName);
        validateNotEmpty(questionnaire.languageDirection);

        ContentValues values = new ContentValues();
        values.put("language_slug", questionnaire.languageSlug);
        values.put("language_name", questionnaire.languageName);
        values.put("language_direction", questionnaire.languageDirection);
        values.put("td_id", questionnaire.tdId);

        return insertOrUpdate("questionnaire", values, new String[]{"td_id", "language_slug"});
    }

    /**
     * Inserts or updates a question in the library.
     *
     * @param question the questionnaire to add
     * @param questionnaireId the parent questionnaire row id
     * @return the id of the question row
     * @throws Exception
     */
    public long addQuestion(Question question, int questionnaireId) throws Exception {
        validateNotEmpty(question.text);
        validateNotEmpty(question.input_type);

        ContentValues values = new ContentValues();
        values.put("text", question.text);
        values.put("help", question.help);
        values.put("is_required", question.is_required);
        values.put("input_type", question.input_type);
        values.put("sort", question.sort);
        values.put("depends_on", question.depends_on);
        values.put("td_id", question.td_id);
        values.put("questionnaire_id", questionnaireId);

        return insertOrUpdate("question", values, new String[]{"td_id", "language_slug"});
    }

    /**
     * Returns a list of source languages and when they were last modified.
     * The value is taken from the max modified resource format date within the language
     *
     * @return {slug, modified_at}
     */
    public List<HashMap> listSourceLanguagesLastModified() {
        Cursor cursor = db.rawQuery("select sl.slug, max(rf.modified_at) as modified_at from resource_format as rf"
                + " left join resource  as r on r.id=rf.resource_id"
                + " left join project as p on p.id=r.project_id"
                + " left join source_language as sl on sl.id=p.source_language_id"
                + " where rf.mime_type like(\"application/ts+%\")"
                + " group by sl.slug", null);
        cursor.moveToFirst();
        List<HashMap> langsLastModifiedList = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(cursor.getColumnIndex("slug"));
            int modifiedAt = cursor.getInt(cursor.getColumnIndex("modified_at"));

            HashMap sourceLanguageMap = new HashMap();
            sourceLanguageMap.put(slug, modifiedAt);
            langsLastModifiedList.add(sourceLanguageMap);
        }
        cursor.close();
        return langsLastModifiedList;
    }

    /**
     * Returns a list of projects and when they were last modified
     * The value is taken from the max modified resource format date within the project
     *
     * @param languageSlug the source language who's projects will be selected. If left empty the results will include all projects in all languages.
     * @return
     */
    public List<HashMap> listProjectsLastModified(String languageSlug) {
        Cursor cursor = null;
        if(languageSlug != null || languageSlug != ""){
            cursor = db.rawQuery("select p.slug, max(rf.modified_at) as modified_at from resource_format as rf"
                + " left join resource  as r on r.id=rf.resource_id"
                + " left join project as p on p.id=r.project_id"
                + " left join source_language as sl on sl.id=p.source_language_id"
                + " where rf.mime_type like(\"application/ts+%\") and sl.slug=?"
                + " group by p.slug", new String[]{languageSlug});
        } else {
            cursor = db.rawQuery("select p.slug, max(rf.modified_at) as modified_at from resource_format as rf"
                + " left join resource  as r on r.id=rf.resource_id"
                + " left join project as p on p.id=r.project_id"
                + " left join source_language as sl on sl.id=p.source_language_id"
                + " where rf.mime_type like(\"application/ts+%\") and sl.slug=?"
                + " group by p.slug", null);
        }
        cursor.moveToFirst();
        List<HashMap> projectsLastModifiedList = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(cursor.getColumnIndex("slug"));
            int modifiedAt = cursor.getInt(cursor.getColumnIndex("modified_at"));

            HashMap projectMap = new HashMap();
            projectMap.put(slug, modifiedAt);
            projectsLastModifiedList.add(projectMap);
        }
        cursor.close();
        return projectsLastModifiedList;
    }

    /**
     * Returns a source language.
     *
     * @param slug
     * @return the language object or null if it does not exist
     */
    public SourceLanguage getSourceLanguage(String slug) {
        Cursor cursor = db.rawQuery("select * from source_language where slug=? limit 1", new String[]{slug});
        if(cursor.moveToFirst()){
            String name = cursor.getString(2);
            String direction = cursor.getString(3);
            SourceLanguage sourceLanguage = new SourceLanguage(slug, name, direction);
            cursor.close();
            return sourceLanguage;
        } else {
            cursor.close();
            return null;
        }
    }

    /**
     * Returns a list of every source language.
     *
     * @return an array of source languages
     */
    public List<SourceLanguage> getSourceLanguages() {
        Cursor cursor = db.rawQuery("select * from source_language order by slug desc", null);

        List<SourceLanguage> sourceLanguages = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(1);
            String name = cursor.getString(2);
            String direction = cursor.getString(3);

            SourceLanguage sourceLanguage = new SourceLanguage(slug, name, direction);

            sourceLanguages.add(sourceLanguage);
            cursor.moveToNext();
        }
        cursor.close();
        return sourceLanguages;
    }

    /**
     * Returns a target language.
     * The result may be a temp target language.
     *
     * Note: does not include the row id. You don't need it
     *
     * @param slug
     * @return the language object or null if it does not exist
     */
    public TargetLanguage getTargetLanguage(String slug) {
        Cursor cursor = db.rawQuery("select * from (" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from target_language" +
                "  union" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from temp_target_language" +
                "  where approved_target_language_slug is null" +
                ") where slug=? limit 1", new String[]{slug});

        if(cursor.moveToFirst()) {
            String name = cursor.getString(2);
            String anglicized = cursor.getString(3);
            String direction = cursor.getString(4);
            String region = cursor.getString(5);
            boolean isGateWay = cursor.getInt(6) == 1;

            TargetLanguage dummyTargetLanguage = new TargetLanguage(slug, name, anglicized, direction, region, isGateWay);
            cursor.close();
            return dummyTargetLanguage;
        } else {
            cursor.close();
            return null;
        }
    }

    /**
     * Returns a list of every target language.
     * The result may include temp target languages.
     *
     * Note: does not include the row id. You don't need it.
     * And we are pulling from two tables so it would be confusing.
     *
     * @return
     */
    public List<TargetLanguage> getTargetLanguages() {
        Cursor cursor = db.rawQuery("select * from (" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from target_language" +
                "  union" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from temp_target_language" +
                "  where approved_target_language_slug is null" +
                ") order by slug asc, name desc", new String[0]);

        List<TargetLanguage> languages = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(0);
            String name = cursor.getString(1);
            String angName = cursor.getString(2);
            String dir = cursor.getString(3);
            String region = cursor.getString(4);
            boolean isGate = cursor.getInt(5) == 1;

            TargetLanguage newLang = new TargetLanguage(slug, name, angName, dir, region, isGate);
            languages.add(newLang);
            cursor.moveToNext();
        }
        cursor.close();
        return languages;
    }

    /**
     * Returns the target language that has been assigned to a temporary target language.
     *
     * Note: does not include the row id. You don't need it
     *
     * @param slug the temporary target language with the assignment
     * @return the language object or null if it does not exist
     */
    public TargetLanguage getApprovedTargetLanguage(String slug) {
        TargetLanguage language = null;

        Cursor cursor = db.rawQuery("select tl.* from target_language as tl" +
                " left join temp_target_language as ttl on ttl.approved_target_language_slug=tl.slug" +
                " where ttl.slug=?", new String[]{slug});

        if(cursor.moveToFirst()) {
            String name = cursor.getString(2);
            String angName = cursor.getString(3);
            String dir = cursor.getString(4);
            String region = cursor.getString(5);
            boolean isGate = cursor.getInt(6) == 1;

            language = new TargetLanguage(slug, name, angName, dir, region, isGate);
            cursor.close();
        }
        return language;
    }

    /**
     * Returns a project
     *
     * @param languageSlug
     * @param projectSlug
     * @return the project object or null
     */
    public Project getProject(String languageSlug, String projectSlug) {
        Project project = null;
        Cursor cursor = db.rawQuery("select * from project" +
                " where slug=? and source_language_id in (" +
                " select id from source_language where slug=?)" +
                " limit 1", new String[]{projectSlug, languageSlug});

        if(cursor.moveToFirst()) {
            String slug = cursor.getString(1);
            String name = cursor.getString(2);
            String desc = cursor.getString(3);
            String icon = cursor.getString(4);
            int sort = cursor.getInt(5);
            String chunksUrl = cursor.getString(6);
            int sourceLanguageId = cursor.getInt(7);
            int categoryId = cursor.getInt(8);

            project = new Project(slug, name, desc, icon, sort, chunksUrl, sourceLanguageId, categoryId);
            //TODO: store the language slug for convenience
        }
        cursor.close();
        return project;
    }

    /**
     * Returns a list of projects available in the given language.
     *
     * @param languageSlug
     * @return an array of projects
     */
    public List<Project> getProjects(String languageSlug) {
        Cursor cursor = db.rawQuery("select * from project" +
                " where source_language_id in (select id from source_language where slug=?)" +
                " order by sort asc", new String[]{languageSlug});

        List<Project> projects = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(1);
            String name = cursor.getString(2);
            String desc = cursor.getString(3);
            String icon = cursor.getString(4);
            int sort = cursor.getInt(5);
            String chunksUrl = cursor.getString(6);
            int sourceLanguageId = cursor.getInt(7);
            int categoryId = cursor.getInt(8);

            Project project = new Project(slug, name, desc, icon, sort, chunksUrl, sourceLanguageId, categoryId);
            projects.add(project);
            cursor.moveToNext();
        }
        cursor.close();
        return projects;
    }

//    public getProjectCategories(int parentCategoryId, String languageSlug, String translateMode) {
//
//    }

    public Resource getResource(String languageSlug, String projectSlug, String resourceSlug) {
        Cursor cursor = db.rawQuery("select r.*, lri.translation_words_assignments_url from resource as r" +
                "  left join legacy_resource_info as lri on lri.resource_id=r.id" +
                "  where r.slug=? and r.project_id in (" +
                "  select id from project where slug=? and source_language_id in (" +
                "  select id from source_language where slug=?)" +
                " ) limit 1", new String[]{resourceSlug, projectSlug, languageSlug});

        if(cursor.moveToFirst()) {
            // load resource object

            // load formats and add to resource
        }

        cursor.close();
        return null;
    }

//    public List<Resource> getResources(String languageSlug, String projectSlug) {
//
//    }

    /**
     * Returns a catalog
     *
     * @param slug
     * @return the catalog object or null if it does not exist
     */
    public Catalog getCatalog(String slug) {
        Catalog catalog = null;
        Cursor cursor = db.rawQuery("select * from catalog where slug=?", new String[]{slug});
        if(cursor.moveToFirst()) {
            String url = cursor.getString(2);
            int modifiedAt = cursor.getInt(3);
            catalog = new Catalog(slug, url, modifiedAt);
        }
        cursor.close();
        return catalog;
    }

    /**
     * Returns a list of catalogs
     *
     * @return
     */
    public List<Catalog> getCatalogs() {
        Cursor cursor = db.rawQuery("select * from catalog", null);

        List<Catalog> catalogs = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(1);
            String url = cursor.getString(2);
            int modifiedAt = cursor.getInt(3);

            Catalog catalog = new Catalog(slug, url, modifiedAt);
            catalogs.add(catalog);
            cursor.moveToNext();
        }
        cursor.close();
        return catalogs;
    }

    /**
     * Returns a versification
     *
     * @param languageSlug
     * @param versificationSlug
     * @return versification or null
     */
    public Versification getVersification(String languageSlug, String versificationSlug) {
        Versification versification = null;
        Cursor cursor = db.rawQuery("select vn.name, v.slug, v.id from versification_name as vn" +
                " left join versification as v on v.id=vn.versification_id" +
                " left join source_language as sl on sl.id=vn.source_language_id" +
                " where sl.slug=? and v.slug=?", new String[]{languageSlug, versificationSlug});

        if(cursor.moveToFirst()) {
            String slug = cursor.getString(cursor.getColumnIndex("slug"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            versification = new Versification(slug, name);
        }
        cursor.close();
        return versification;
    }

    /**
     * Returns a list of versifications
     *
     * @param languageSlug
     * @return
     */
    public List<Versification> getVersifications(String languageSlug) {
        Cursor cursor = db.rawQuery("select vn.name, v.slug, v.id from versification_name as vn" +
                " left join versification as v on v.id=vn.versification_id" +
                " left join source_language as sl on sl.id=vn.source_language_id" +
                " where sl.slug=? and v.slug=?", new String[]{languageSlug});

        List<Versification> versifications = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(cursor.getColumnIndex("slug"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            Versification versification = new Versification(slug, name);
            versifications.add(versification);
            cursor.moveToNext();
        }
        cursor.close();
        return versifications;
    }

    /**
     * Returns a list of chunk markers for a project
     *
     * @param projectSlug
     * @param versificationSlug
     * @return
     */
    public List<ChunkMarker> getChunkMarkers(String projectSlug, String versificationSlug) {
        Cursor cursor = db.rawQuery("select cm.* from chunk_marker as cm" +
                " left join versification as v on v.id=cm.versification_id" +
                " where v.slug=? and cm.project_slug=?", new String[]{versificationSlug, projectSlug});

        List<ChunkMarker> chunkMarkers = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String chapter = cursor.getString(1);
            String verse = cursor.getString(2);
            String slug = cursor.getString(3);
            int versificationId = cursor.getInt(4);

            ChunkMarker chunkMarker = new ChunkMarker(chapter, verse, slug, versificationId);
            chunkMarkers.add(chunkMarker);
            cursor.moveToNext();
        }
        cursor.close();
        return chunkMarkers;
    }

    /**
     * Returns a list of questionnaires
     *
     * @return a list of questionnaires
     */
    public List<Questionnaire> getQuestionnaires() {
        Cursor cursor = db.rawQuery("select * from questionnaire", null);

        List<Questionnaire> questionnaires = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(1);
            String name = cursor.getString(2);
            String direction = cursor.getString(3);
            int td_id = cursor.getInt(4);

            Questionnaire questionnaire = new Questionnaire(slug, name, direction, td_id);
            questionnaires.add(questionnaire);
        }
        cursor.close();
        return questionnaires;
    }

    /**
     * Returns a list of questions.
     *
     * @param questionnaireId the parent questionnaire row id
     * @return a list of questions
     */
    public List<Question> getQuestions(int questionnaireId) {
        Cursor cursor = db.rawQuery("select question where questionnaire_id=?", new String[]{String.valueOf(questionnaireId)});

        List<Question> questions = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String text = cursor.getString(1);
            String help = cursor.getString(2);
            int is_required = cursor.getInt(3);
            String input_type = cursor.getString(4);
            int sort = cursor.getInt(5);
            int depends_on = cursor.getInt(6);
            int td_id = cursor.getInt(7);
            int questionnaire_id = cursor.getInt(8);

            Question question = new Question(text, help, is_required, input_type, sort, depends_on, td_id, questionnaire_id);
            questions.add(question);
            cursor.moveToNext();
        }
        cursor.close();
        return questions;
    }
}