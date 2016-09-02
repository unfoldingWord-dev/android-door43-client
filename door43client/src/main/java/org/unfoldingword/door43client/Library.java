package org.unfoldingword.door43client;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
        long id = db.insertWithOnConflict("source_language", null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if(id == -1) {
            // update existing row
            // TODO: 9/2/16 do we need to unset the unique columns i.e. slug?
            int numRows = db.updateWithOnConflict("source_language", values, "slug=?", new String[]{language.slug}, SQLiteDatabase.CONFLICT_ROLLBACK);
            if(numRows == 0) {
                throw new Exception("Failed to update source language");
            } else {
                // retrieve row id
                Cursor cursor = db.rawQuery("select id from source_language where slug=?", new String[]{language.slug});
                if(cursor.moveToFirst()) {
                    id = cursor.getLong(0);
                } else {
                    throw new Exception("Failed to find source language");
                }
            }
        }

        return id;
    }

    /**
     * Inserts or updates a target language in the library.
     *
     * Note: the result row id is always 0 since you don't need it. See getTargetLanguages for more information
     *
     * @param language
     * @return Boolean 
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
        values.put("anglicized_name", language.anglicized_name);
        values.put("region", language.region);
        values.put("is_gateway_language", language.is_gateway_language);

        long id = db.insertWithOnConflict("target_language", null, values, SQLiteDatabase.CONFLICT_IGNORE );
        if(id == -1) {

            int numRows = db.updateWithOnConflict("target_language", values, "slug=?", new String[]{language.slug}, SQLiteDatabase.CONFLICT_ROLLBACK);
            if(numRows == 0) {
                throw new Exception("Failed to update target language");
            } else {
                Cursor cursor = db.rawQuery("select id from target_language where slug=?", new String[]{language.slug});
                if(cursor.moveToFirst()) {
                    id = cursor.getLong(0);
                } else {
                    throw new Exception("Failed to find target language");
                }
            }
        }

        return id > 0;
    }

    public boolean addTempTargetLanguage(DummyTargetLanguage language) throws Exception {
        validateNotEmpty(language.slug);
        validateNotEmpty(language.name);
        validateNotEmpty(language.direction);

        ContentValues values = new ContentValues();
        values.put("slug", language.slug);
        values.put("name", language.name);
        values.put("direction", language.direction);
        values.put("anglicized_name", language.anglicized_name);
        values.put("region", language.region);
        values.put("is_gateway_language", language.is_gateway_language);


    }
}
