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
     * Inserts or updates a source language in the library.
     *
     * @param language
     * @return the id of the source language row
     * @throws Exception
     */
    public long addSourceLanguage(DummyLanguage language) throws Exception {
        if(language.slug == null || language.slug.isEmpty()
                || language.name == null || language.name.isEmpty()
                || language.direction == null || language.direction.isEmpty()) throw new Exception("Invalid source language parameters");

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


}
