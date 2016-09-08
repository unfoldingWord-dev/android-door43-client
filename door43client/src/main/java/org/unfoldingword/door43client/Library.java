package org.unfoldingword.door43client;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
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
    public long addSourceLanguage(DummySourceLanguage language) throws Exception {
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
    public boolean addTargetLanguage(DummyTargetLanguage language) throws Exception {
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
    public boolean addTempTargetLanguage(DummyTargetLanguage language) throws Exception {
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
        if (rowsAffected > 0){
            return true;
        } else {
            return false;
        }
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
     *Inserts or updates a questionnaire in the library.
     *
     * @param questionnaire the questionnaire to add
     * @return the id of the questionnaire row
     * @throws Exception
     */
    public long addQuestionaire(Questionnaire questionnaire) throws Exception {
        validateNotEmpty(questionnaire.language_slug);
        validateNotEmpty(questionnaire.language_name);
        validateNotEmpty(questionnaire.language_direction);

        ContentValues values = new ContentValues();
        values.put("language_slug", questionnaire.language_slug);
        values.put("language_name", questionnaire.language_name);
        values.put("language_direction", questionnaire.language_direction);
        values.put("td_id", questionnaire.td_id);

        return insertOrUpdate("questionnaire", values, new String[]{"td_id","language_slug"});
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

        return insertOrUpdate("question", values, new String[]{"td_id","language_slug"});
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
    public List<DummyTargetLanguage> getTargetLanguages() {
        Cursor cursor = db.rawQuery("select * from (" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from target_language" +
                "  union" +
                "  select slug, name, anglicized_name, direction, region, is_gateway_language from temp_target_language" +
                "  where approved_target_language_slug is null" +
                ") order by slug asc, name desc", new String[0]);

        List<DummyTargetLanguage> languages = new ArrayList<>();
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            String slug = cursor.getString(0);
            String name = cursor.getString(1);
            String angName = cursor.getString(2);
            String dir = cursor.getString(3);
            String region = cursor.getString(4);
            boolean isGate = cursor.getInt(5) == 1;

            DummyTargetLanguage newLang = new DummyTargetLanguage(slug, name, angName, dir, region, isGate);
            languages.add(newLang);
            cursor.moveToNext();
        }
        cursor.close();
        return languages;
    }
}