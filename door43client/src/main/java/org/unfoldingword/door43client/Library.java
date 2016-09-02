package org.unfoldingword.door43client;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.Map;

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
                } else {
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
    public long addSourceLanguage(DummyLanguage language) throws Exception {
        validateNotEmpty(language.slug);
        validateNotEmpty(language.name);
        validateNotEmpty(language.direction);

        ContentValues values = new ContentValues();
        values.put("slug", language.slug);
        values.put("name", language.name);
        values.put("direction", language.direction);

        return insertOrUpdate("source_language", values, new String[]{"slug"});
    }


}
