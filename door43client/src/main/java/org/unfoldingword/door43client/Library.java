package org.unfoldingword.door43client;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.CategoryEntry;
import org.unfoldingword.door43client.models.ChunkMarker;
import org.unfoldingword.door43client.models.TargetLanguage;
import org.unfoldingword.door43client.models.Project;
import org.unfoldingword.door43client.models.Question;
import org.unfoldingword.door43client.models.Resource;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.Versification;
import org.unfoldingword.door43client.models.Catalog;
import org.unfoldingword.door43client.models.Questionnaire;
import org.unfoldingword.resourcecontainer.ResourceContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Manages the indexed library content.
 */
class Library implements Index {

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
     * Used to open a transaction
     */
    public void beginTransaction() {
        db.beginTransactionNonExclusive();
    }

    /**
     * Used to close a transaction
     * @param success set to false if the transaction should fail and the changes rolled back.
     */
    public void endTransaction(boolean success) {
        if(success) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
    }

    /**
     * Closes the database
     */
    public void closeDatabase() {
        db.close();
    }

    /**
     * Ensures a value is not null or empty
     * @param value
     * @throws Exception
     */
    private void validateNotEmpty(String value) throws Exception {
        if(value == null || value.trim().isEmpty()) throw new Exception("Invalid parameter value");
    }

    /**
     * Converts null strings to empty strings
     * @param value
     * @return
     */
    private String deNull(String value) {
        return value == null ? "" : value;
    }

    /**
     * Attepts to insert a row.
     *
     * There is a bug in the SQLiteDatabase API that prevents us from using insertWithOnConflict + CONFLICT_IGNORE
     * https://code.google.com/p/android/issues/detail?id=13045
     * @param table
     * @param values
     * @param uniqueColumns
     * @return the id of the inserted row or the id of the existing row.
     */
    private long insertOrIgnore(String table, ContentValues values, String[] uniqueColumns) {
        // try to insert
        Exception error = null;
        try {
            return db.insertOrThrow(table, null, values);
        } catch (SQLException e) {
            error = e;
        }

        WhereClause where = WhereClause.prepare(values, uniqueColumns);

        Cursor cursor = db.rawQuery("select id from " + table + " where " + where.statement, where.arguments);
        if(cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            cursor.close();
            return id;
        } else {
            cursor.close();
        }
        if(error != null) error.printStackTrace();
        return -1;
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
        long id = insertOrIgnore(table, values, uniqueColumns);
        if(id == -1) {
            WhereClause where = WhereClause.prepare(values, uniqueColumns);

            // clean values
            for(String key:uniqueColumns) {
                values.remove(key);
            }

            // update
            int numRows = db.updateWithOnConflict(table, values, where.statement, where.arguments, SQLiteDatabase.CONFLICT_ROLLBACK);
            if(numRows == 0) {
                throw new Exception("Failed to update the row in " + table);
            } else {
                // retrieve updated row id
                Cursor cursor = db.rawQuery("select id from " + table + " where " + where.statement, where.arguments);
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
        values.put("anglicized_name", deNull(language.anglicizedName));
        values.put("region", deNull(language.region));
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
        values.put("anglicized_name", deNull(language.anglicizedName));
        values.put("region", deNull(language.region));
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
    public boolean setApprovedTargetLanguage(String tempTargetLanguageSlug, String targetLanguageSlug) throws Exception {
        validateNotEmpty(tempTargetLanguageSlug);
        validateNotEmpty(targetLanguageSlug);

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
                validateNotEmpty(category.slug);
                validateNotEmpty(category.name);

                ContentValues insertValues = new ContentValues();
                insertValues.put("slug", category.slug);
                insertValues.put("parent_id", parentCategoryId);

                long id = insertOrIgnore("category", insertValues, new String[]{"slug", "parent_id"});
                if(id > 0) {
                    parentCategoryId = id;
                } else {
                    throw new Exception("Invalid category");
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
        updateProject.put("desc", deNull(project.description));
        updateProject.put("icon", deNull(project.icon));
        updateProject.put("sort", project.sort);
        updateProject.put("chunks_url", deNull(project.chunksUrl));
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
    public long addVersification(Versification versification, long sourceLanguageId) throws Exception{
        validateNotEmpty(versification.slug);
        validateNotEmpty(versification.name);

        ContentValues values = new ContentValues();
        values.put("slug", versification.slug);

        long versificationId = insertOrIgnore("versification", values, new String[]{"slug"});
        if(versificationId > 0) {
            ContentValues cv = new ContentValues();
            cv.put("source_language_id", sourceLanguageId);
            cv.put("versification_id", versificationId);
            cv.put("name", versification.name);
            insertOrUpdate("versification_name", cv, new String[]{"source_language_id", "versification_id"});
        } else {
            throw new Exception("Invalid versification");
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
    public long addChunkMarker(ChunkMarker chunk, String projectSlug, long versificationId) throws Exception {
        validateNotEmpty(chunk.chapter);
        validateNotEmpty(chunk.verse);
        validateNotEmpty(projectSlug);

        ContentValues chunkValues = new ContentValues();
        chunkValues.put("chapter", chunk.chapter);
        chunkValues.put("verse", chunk.verse);
        chunkValues.put("project_slug", projectSlug);
        chunkValues.put("versification_id", versificationId);

        long id = insertOrIgnore("chunk_marker", chunkValues, new String[]{"project_slug", "versification_id", "chapter", "verse"});
        if(id == -1) {
            throw new Exception("Invalid Chunk Marker");
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

    /**
     * Inserts or updates a resource in the library.
     *
     * @param resource
     * @param projectId the parent project row id
     * @return the id of the resource row
     * @throws Exception
     */
    public long addResource(Resource resource, long projectId) throws Exception {
        validateNotEmpty(resource.slug);
        validateNotEmpty(resource.name);
        validateNotEmpty(resource.type);
        validateNotEmpty(resource.formats.size() > 0 ? "good" : null);
        validateNotEmpty((String)resource.status.get("translate_mode"));
        validateNotEmpty((String)resource.status.get("checking_level"));
        validateNotEmpty((String)resource.status.get("version"));

        ContentValues values = new ContentValues();
        values.put("slug", resource.slug);
        values.put("name", resource.name);
        values.put("type", resource.type);
        values.put("translate_mode", (String)resource.status.get("translate_mode"));
        values.put("checking_level", (String)resource.status.get("checking_level"));
        values.put("comments", deNull((String)resource.status.get("comments")));
        values.put("pub_date", deNull((String)resource.status.get("pub_date")));
        values.put("license", deNull((String)resource.status.get("license")));
        values.put("version", (String)resource.status.get("version"));
        values.put("project_id", projectId);

        long resourceId = insertOrUpdate("resource", values, new String[]{"slug", "project_id"});

        // add formats
        for(Resource.Format format : resource.formats) {
            validateNotEmpty(format.mimeType);
            ContentValues formatValues = new ContentValues();
            formatValues.put("package_version", format.packageVersion);
            formatValues.put("mime_type", format.mimeType);
            formatValues.put("modified_at", format.modifiedAt);
            formatValues.put("url", deNull(format.url));
            formatValues.put("resource_id", resourceId);

            insertOrUpdate("resource_format", formatValues, new String[]{"mime_type", "resource_id"});
        }

        //add legacy data
        if(resource.wordsAssignmentsUrl != null && !resource.wordsAssignmentsUrl.equals("")) {
            ContentValues legacyValues = new ContentValues();
            legacyValues.put("translation_words_assignments_url", resource.wordsAssignmentsUrl);
            legacyValues.put("resource_id", resourceId);
            insertOrUpdate("legacy_resource_info", legacyValues, new String[]{"resource_id"});
        }
        return resourceId;
    }

    /**
     *Inserts or updates a questionnaire in the library.
     *
     * @param questionnaire the questionnaire to add
     * @return the id of the questionnaire row
     * @throws Exception
     */
    public long addQuestionnaire(Questionnaire questionnaire) throws Exception {
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
    public long addQuestion(Question question, long questionnaireId) throws Exception {
        validateNotEmpty(question.text);
        validateNotEmpty(question.inputType);

        ContentValues values = new ContentValues();
        values.put("text", question.text);
        values.put("help", deNull(question.help));
        values.put("is_required", question.isRequired ? 1 : 0);
        values.put("input_type", question.inputType);
        values.put("sort", question.sort);
        values.put("depends_on", question.dependsOn);
        values.put("td_id", question.tdId);
        values.put("questionnaire_id", questionnaireId);

        return insertOrUpdate("question", values, new String[]{"td_id", "questionnaire_id"});
    }

    public List<HashMap> listSourceLanguagesLastModified() {
        Cursor cursor = db.rawQuery("select sl.slug, max(rf.modified_at) as modified_at from resource_format as rf"
                + " left join resource  as r on r.id=rf.resource_id"
                + " left join project as p on p.id=r.project_id"
                + " left join source_language as sl on sl.id=p.source_language_id"
                + " where rf.mime_type like(\"" + ResourceContainer.baseMimeType + "+%\")"
                + " group by sl.slug", null);
        cursor.moveToFirst();
        List<HashMap> langsLastModifiedList = new ArrayList<>();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            int modifiedAt = reader.getInt("modified_at");

            HashMap sourceLanguageMap = new HashMap();
            sourceLanguageMap.put(slug, modifiedAt);
            langsLastModifiedList.add(sourceLanguageMap);
        }
        cursor.close();
        return langsLastModifiedList;
    }

    public Map<String, Integer> listProjectsLastModified(String languageSlug) {
        Cursor cursor = null;
        if(languageSlug != null || languageSlug != ""){
            cursor = db.rawQuery("select p.slug, max(rf.modified_at) as modified_at from resource_format as rf"
                + " left join resource  as r on r.id=rf.resource_id"
                + " left join project as p on p.id=r.project_id"
                + " left join source_language as sl on sl.id=p.source_language_id"
                + " where rf.mime_type like(\"" + ResourceContainer.baseMimeType + "+%\") and sl.slug=?"
                + " group by p.slug", new String[]{languageSlug});
        } else {
            cursor = db.rawQuery("select p.slug, max(rf.modified_at) as modified_at from resource_format as rf"
                + " left join resource  as r on r.id=rf.resource_id"
                + " left join project as p on p.id=r.project_id"
                + " left join source_language as sl on sl.id=p.source_language_id"
                + " where rf.mime_type like(\"" + ResourceContainer.baseMimeType + "+%\") and sl.slug=?"
                + " group by p.slug", null);
        }
        Map<String, Integer> projectsLastModifiedList = new HashMap();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);
            projectsLastModifiedList.put(reader.getString("slug"), reader.getInt("modified_at"));
            cursor.moveToNext();
        }
        cursor.close();
        return projectsLastModifiedList;
    }

    public SourceLanguage getSourceLanguage(String sourceLanguageSlug) {
        Cursor cursor = db.rawQuery("select * from source_language where slug=? limit 1", new String[]{sourceLanguageSlug});
        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            String name = reader.getString("name");
            String direction = reader.getString("direction");

            SourceLanguage sourceLanguage = new SourceLanguage(sourceLanguageSlug, name, direction);
            sourceLanguage._dbInfo.rowId = reader.getLong("id");
            cursor.close();
            return sourceLanguage;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<SourceLanguage> getSourceLanguages() {
        Cursor cursor = db.rawQuery("select * from source_language order by slug desc", null);

        List<SourceLanguage> sourceLanguages = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("id");
            String name = reader.getString("name");
            String direction = reader.getString("direction");

            SourceLanguage sourceLanguage = new SourceLanguage(slug, name, direction);
            sourceLanguage._dbInfo.rowId = reader.getLong("id");
            sourceLanguages.add(sourceLanguage);
            cursor.moveToNext();
        }
        cursor.close();
        return sourceLanguages;
    }

    public TargetLanguage getTargetLanguage(String targetLangaugeSlug) {
        Cursor cursor = db.rawQuery("select * from (" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from target_language" +
                "  union" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from temp_target_language" +
                "  where approved_target_language_slug is null" +
                ") where slug=? limit 1", new String[]{targetLangaugeSlug});

        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            String name = reader.getString("name");
            String anglicized = reader.getString("anglicized_name");
            String direction = reader.getString("direction");
            String region = reader.getString("region");
            boolean isGateWay = reader.getBoolean("is_gateway_language");

            TargetLanguage dummyTargetLanguage = new TargetLanguage(targetLangaugeSlug, name, anglicized, direction, region, isGateWay);
            cursor.close();
            return dummyTargetLanguage;
        } else {
            cursor.close();
            return null;
        }
    }

    public List<TargetLanguage> getTargetLanguages() {
        Cursor cursor = db.rawQuery("select * from (" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from target_language" +
                "  union" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from temp_target_language" +
                "  where approved_target_language_slug is null" +
                ") order by slug asc, name desc", null);

        List<TargetLanguage> languages = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            String name = reader.getString("name");
            String angName = reader.getString("anglicized_name");
            String dir = reader.getString("direction");
            String region = reader.getString("region");
            boolean isGate = reader.getBoolean("is_gateway_language");

            TargetLanguage newLang = new TargetLanguage(slug, name, angName, dir, region, isGate);
            languages.add(newLang);
            cursor.moveToNext();
        }
        cursor.close();
        return languages;
    }

    public TargetLanguage getApprovedTargetLanguage(String tempTargetLanguageSlug) {
        TargetLanguage language = null;

        Cursor cursor = db.rawQuery("select tl.slug, tl.name, tl.anglicized_name, tl.direction, tl.region, tl.is_gateway_language" +
                " from target_language as tl" +
                " left join temp_target_language as ttl on ttl.approved_target_language_slug=tl.slug" +
                " where ttl.slug=?", new String[]{tempTargetLanguageSlug});

        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            String approvedSlug = reader.getString("slug");
            String name = reader.getString("name");
            String angName = reader.getString("anglicized_name");
            String dir = reader.getString("direction");
            String region = reader.getString("region");
            boolean isGate = reader.getBoolean("is_gateway_language");

            language = new TargetLanguage(approvedSlug, name, angName, dir, region, isGate);
            cursor.close();
        }
        return language;
    }

    public Project getProject(String sourceLanguageSlug, String projectSlug) {
        Project project = null;
        Cursor cursor = db.rawQuery("select * from project" +
                " where slug=? and source_language_id in (" +
                " select id from source_language where slug=?)" +
                " limit 1", new String[]{projectSlug, sourceLanguageSlug});

        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            String name = reader.getString("name");
            String desc = reader.getString("desc");
            String icon = reader.getString("icon");
            int sort = reader.getInt("sort");
            String chunksUrl = reader.getString("chunks_url");

            project = new Project(slug, name, desc, icon, sort, chunksUrl);
            project._dbInfo.rowId = reader.getLong("id");
            //TODO: store the language slug for convenience
        }
        cursor.close();
        return project;
    }

    public List<Project> getProjects(String sourceLanguageSlug) {
        Cursor cursor = db.rawQuery("select * from project" +
                " where source_language_id in (select id from source_language where slug=?)" +
                " order by sort asc", new String[]{sourceLanguageSlug});

        List<Project> projects = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            String name = reader.getString("name");
            String desc = reader.getString("desc");
            String icon = reader.getString("icon");
            int sort = reader.getInt("sort");
            String chunksUrl = reader.getString("chunks_url");

            Project project = new Project(slug, name, desc, icon, sort, chunksUrl);
            project._dbInfo.rowId = reader.getLong("id");
            projects.add(project);
            cursor.moveToNext();
        }
        cursor.close();
        return projects;
    }

    public List<CategoryEntry> getProjectCategories(long parentCategoryId, String languageSlug, String translateMode) {
        Cursor categoryCursor = null;
        if(!translateMode.isEmpty()) {
            categoryCursor = db.rawQuery("select \'category\' as type, c.slug as name, \'\' as source_language_slug," +
                    " c.id, c.slug, c.parent_id, count(p.id) as num from category as c" +
                    " left join (" +
                    "  select p.id, p.category_id, count(r.id) as num from project as p" +
                    "  left join resource as r on r.project_id=p.id and r.translate_mode like(?)" +
                    "  group by p.slug" +
                    " ) p on p.category_id=c.id and p.num > 0" +
                    " where parent_id=? and num > 0 group by c.slug", new String[]{translateMode, String.valueOf(parentCategoryId)});
        } else {
            categoryCursor = db.rawQuery("select \'category\' as type, category.slug as name, \'\' as source_language_slug, * from category where parent_id=" + parentCategoryId, null);
        }

        //find best name
        while(!categoryCursor.isAfterLast()) {
            int catId = categoryCursor.getInt(categoryCursor.getColumnIndex("id"));

//            Cursor cursor = db.rawQuery("select sl.slug as source_language_slug, cn.name as name" +
//                    " from category_name as cn" +
//                    " left join source_language as sl on sl.id=cn.source_language_id" +
//                    " where sl.slug like(?) and cn.category_id=?", new String[]{   ,catId});
//
//            if(cursor.moveToFirst()) {
//
//            }

            categoryCursor.moveToNext();
        }

        return null;
    }

    public Resource getResource(String sourceLanguageSlug, String projectSlug, String resourceSlug) {
        Resource resource = null;
        Cursor cursor = db.rawQuery("select r.id, r.name, r.translate_mode, r.type, r.checking_level," +
                " r.comments, r.pub_date, r.license, r.version," +
                " lri.translation_words_assignments_url from resource as r" +
                " left join legacy_resource_info as lri on lri.resource_id=r.id" +
                " where r.slug=? and r.project_id in (" +
                "  select id from project where slug=? and source_language_id in (" +
                "  select id from source_language where slug=?)" +
                " ) limit 1", new String[]{resourceSlug, projectSlug, sourceLanguageSlug});

        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            long resourceId = reader.getLong("id");
            String name = reader.getString("name");
            String translateMode = reader.getString("translate_mode");
            String type = reader.getString("type");
            String checkingLevel = reader.getString("checking_level");
            String comments = reader.getString("comments");
            String pubDate = reader.getString("pub_date");
            String license = reader.getString("license");
            String version = reader.getString("version");
            String wordsAssignmentsUrl = reader.getString("translation_words_assignments_url");

            HashMap status = new HashMap();
            status.put("translateMode", translateMode);
            status.put("checkingLevel", checkingLevel);
            status.put("comments", comments);
            status.put("pub_date", pubDate);
            status.put("license", license);
            status.put("version", version);

            resource = new Resource(resourceSlug, name, type, wordsAssignmentsUrl, status);

            // load formats and add to resource
            Cursor formatCursor = db.rawQuery("select * from resource_format where resource_id=" + resourceId, null);
            formatCursor.moveToFirst();
            while(!formatCursor.isAfterLast()) {
                CursorReader formatReader = new CursorReader(formatCursor);

                int packageVersion = formatReader.getInt("package_version");
                String mimeType = formatReader.getString("mime_type");
                int modifiedAt = formatReader.getInt("modified_at");
                String url = formatReader.getString("url");

                Resource.Format format = new Resource.Format(packageVersion, mimeType, modifiedAt, url);
                resource.addFormat(format);
                formatCursor.moveToNext();
            }
            formatCursor.close();
        }
        cursor.close();
        return resource;
    }

    /**
     * Returns a list of resources available in the given project
     *
     * @param languageSlug the language of the resource. If null then all resources of the project will be returned.
     * @param projectSlug the project who's resources will be returned
     * @return an array of resources
     */
    public List<Resource> getResources(String languageSlug, String projectSlug) {
        List<Resource> resources = new ArrayList<>();
        Cursor resourceCursor = null;
        if(languageSlug != null && !languageSlug.isEmpty()) {
            resourceCursor = db.rawQuery("select r.*, lri.translation_words_assignments_url from resource as r" +
                    " left join legacy_resource_info as lri on lri.resource_id=r.id" +
                    " where r.project_id in (" +
                    "  select id from project where slug=? and source_language_id in (" +
                    "   select id from source_language where slug=?)" +
                    " )" +
                    " order by r.slug desc", new String[]{projectSlug, languageSlug});
        } else {
            resourceCursor = db.rawQuery("select sl.slug as source_language_slug, r.*, lri.translation_words_assignments_url from resource as r" +
                    " left join legacy_resource_info as lri on lri.resource_id=r.id" +
                    " left join project as p on p.id=r.project_id" +
                    " left join (" +
                    "  select id, slug from source_language" +
                    " ) as sl on sl.id=p.source_language_id" +
                    " where p.slug=? order by r.slug asc", new String[]{projectSlug});
        }

        resourceCursor.moveToFirst();
        while(!resourceCursor.isAfterLast()) {
            CursorReader reader = new CursorReader(resourceCursor);

            long resourceId = reader.getLong("id");
            String slug = reader.getString("slug");
            String name = reader.getString("name");
            String translateMode = reader.getString("translate_mode");
            String type = reader.getString("type");
            String checkingLevel = reader.getString("checking_level");
            String comments = reader.getString("comments");
            int pubDate = reader.getInt("pub_date");
            String license = reader.getString("license");
            String version = reader.getString("version");
            String wordsAssignmentsUrl = reader.getString("translation_words_assignments_url");

            HashMap status = new HashMap();
            status.put("translateMode", translateMode);
            status.put("checkingLevel", checkingLevel);
            status.put("comments", comments);
            status.put("pub_date", pubDate);
            status.put("license", license);
            status.put("version", version);

            Resource resource = new Resource(slug, name, type, wordsAssignmentsUrl, status);

            // load formats and add to resource
            Cursor formatCursor = db.rawQuery("select * from resource_format where resource_id=" + resourceId, null);
            formatCursor.moveToFirst();
            while(!formatCursor.isAfterLast()) {
                CursorReader formatReader = new CursorReader(formatCursor);

                int packageVersion = formatReader.getInt("package_version");
                String mimeType = formatReader.getString("mime_type");
                int modifiedAt = formatReader.getInt("modified_at");
                String url = formatReader.getString("url");

                Resource.Format format = new Resource.Format(packageVersion, mimeType, modifiedAt, url);
                resource.addFormat(format);
                formatCursor.moveToNext();
            }
            formatCursor.close();

            resources.add(resource);
            resourceCursor.moveToNext();
        }
        resourceCursor.close();
        return resources;
    }

    public Catalog getCatalog(String catalogSlug) {
        Catalog catalog = null;
        Cursor cursor = db.rawQuery("select id, url, modified_at from catalog where slug=?", new String[]{catalogSlug});
        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            String url = reader.getString("url");
            int modifiedAt = reader.getInt("modified_at");

            catalog = new Catalog(catalogSlug, url, modifiedAt);
            catalog._dbInfo.rowId = reader.getLong("id");
        }
        cursor.close();
        return catalog;
    }

    public List<Catalog> getCatalogs() {
        Cursor cursor = db.rawQuery("select * from catalog", null);

        List<Catalog> catalogs = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            String url = reader.getString("url");
            int modifiedAt = reader.getInt("modified_at");

            Catalog catalog = new Catalog(slug, url, modifiedAt);
            catalog._dbInfo.rowId = reader.getLong("id");
            catalogs.add(catalog);
            cursor.moveToNext();
        }
        cursor.close();
        return catalogs;
    }

    public Versification getVersification(String sourceLanguageSlug, String versificationSlug) {
        Versification versification = null;
        Cursor cursor = db.rawQuery("select v.id, v.slug, vn.name from versification_name as vn" +
                " left join versification as v on v.id=vn.versification_id" +
                " left join source_language as sl on sl.id=vn.source_language_id" +
                " where sl.slug=? and v.slug=?", new String[]{sourceLanguageSlug, versificationSlug});

        if(cursor.moveToFirst()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            String name = reader.getString("name");

            versification = new Versification(slug, name);
            versification._dbInfo.rowId = reader.getLong("id");
        }
        cursor.close();
        return versification;
    }

    public List<Versification> getVersifications(String sourceLanguageSlug) {
        Cursor cursor = db.rawQuery("select vn.name, v.slug, v.id from versification_name as vn" +
                " left join versification as v on v.id=vn.versification_id" +
                " left join source_language as sl on sl.id=vn.source_language_id" +
                " where sl.slug=?", new String[]{sourceLanguageSlug});

        List<Versification> versifications = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("slug");
            String name = reader.getString("name");

            Versification versification = new Versification(slug, name);
            versification._dbInfo.rowId = reader.getLong("id");
            versifications.add(versification);
            cursor.moveToNext();
        }
        cursor.close();
        return versifications;
    }

    public List<ChunkMarker> getChunkMarkers(String projectSlug, String versificationSlug) {
        Cursor cursor = db.rawQuery("select cm.id, cm.chapter, cm.verse from chunk_marker as cm" +
                " left join versification as v on v.id=cm.versification_id" +
                " where v.slug=? and cm.project_slug=?", new String[]{versificationSlug, projectSlug});

        List<ChunkMarker> chunkMarkers = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String chapter = reader.getString("chapter");
            String verse = reader.getString("verse");

            ChunkMarker chunkMarker = new ChunkMarker(chapter, verse);
            chunkMarker._dbInfo.rowId = reader.getLong("id");
            chunkMarkers.add(chunkMarker);
            cursor.moveToNext();
        }
        cursor.close();
        return chunkMarkers;
    }

    public List<Questionnaire> getQuestionnaires() {
        Cursor cursor = db.rawQuery("select * from questionnaire", null);

        List<Questionnaire> questionnaires = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String slug = reader.getString("language_slug");
            String name = reader.getString("language_name");
            String direction = reader.getString("language_direction");
            long tdId = reader.getLong("td_id");

            Questionnaire questionnaire = new Questionnaire(slug, name, direction, tdId);
            questionnaire._dbInfo.rowId = reader.getLong("id");
            questionnaires.add(questionnaire);
            cursor.moveToNext();
        }
        cursor.close();
        return questionnaires;
    }

    public List<Question> getQuestions(long questionnaireTDId) {
        Cursor cursor = db.rawQuery("select * from question where questionnaire_id=" + questionnaireTDId, null);

        List<Question> questions = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            CursorReader reader = new CursorReader(cursor);

            String text = reader.getString("text");
            String help = reader.getString("help");
            boolean isRequired = reader.getBoolean("is_required");
            String inputType = reader.getString("input_type");
            int sort = reader.getInt("sort");
            long dependsOn = reader.getInt("depends_on");
            long tdId = reader.getInt("td_id");

            Question question = new Question(text, help, isRequired, inputType, sort, dependsOn, tdId);
            question._dbInfo.rowId = reader.getLong("id");
            questions.add(question);
            cursor.moveToNext();
        }
        cursor.close();
        return questions;
    }

    /**
     * This is a utility class for preparing a where clause
     */
    private static class WhereClause {
        public final String statement;
        public final String[] arguments;

        private WhereClause(String statement, String[] values) {
            this.statement = statement;
            this.arguments = values;
        }

        /**
         * Performs a bunch of magical operations to convert a set of values and the specified unique columns
         * into a valid where clause with supporting values.
         *
         *
         *
         * @param values
         * @param uniqueColumns
         * @return
         */
        public static WhereClause prepare(ContentValues values, String[] uniqueColumns) {
            List<String> stringColumns = new ArrayList<>();
            List<String> numberColumns = new ArrayList<>();

            // split columns into sets by type
            for(String key:uniqueColumns) {
                if(values.get(key) instanceof String) {
                    stringColumns.add(key);
                } else {
                    numberColumns.add(key);
                }
            }

            // build the statement
            String whereStmt = "";
            if(stringColumns.size() > 0) {
                whereStmt = TextUtils.join("=? and ", stringColumns) + "=?";
            }
            if(numberColumns.size() > 0) {
                if (!whereStmt.isEmpty()) whereStmt += " and ";
                List<String> expressions = new ArrayList<>();
                for(String key:numberColumns) {
                    expressions.add(key + "=" + values.get(key));
                }
                whereStmt += TextUtils.join(" and ", expressions);
            }

            // build the values
            String[] uniqueValues = new String[stringColumns.size()];
            for(int i = 0; i < stringColumns.size(); i ++) {
                uniqueValues[i] = String.valueOf(values.get(stringColumns.get(i)));
            }

            return new WhereClause(whereStmt, uniqueValues);
        }
    }

    /**
     * A helper class to make reading from a cursor easier.
     */
    private static class CursorReader {
        private final Cursor cursor;

        public CursorReader(Cursor cursor) {
            this.cursor =cursor;
        }

        public String getString(String key)  {
            return this.cursor.getString(this.cursor.getColumnIndexOrThrow(key));
        }

        public long getLong(String key) {
            return this.cursor.getLong(this.cursor.getColumnIndexOrThrow(key));
        }

        public int getInt(String key) {
            return this.cursor.getInt(this.cursor.getColumnIndexOrThrow(key));
        }

        public boolean getBoolean(String key) {
            return this.cursor.getInt(this.cursor.getColumnIndexOrThrow(key)) > 0;
        }
    }
}