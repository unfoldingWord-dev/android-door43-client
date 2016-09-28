package org.unfoldingword.door43client;


import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Custom wrapper to provide a custom database path
 * http://stackoverflow.com/questions/5332328/sqliteopenhelper-problem-with-fully-qualified-db-path-name
 */
class DatabaseContext extends ContextWrapper {

    private final File dir;

    public DatabaseContext(Context base, File databaseDir) {
        super(base);
        this.dir = databaseDir;
    }

    @Override
    public File getDatabasePath(String name) {
        String dbfile = dir.getAbsolutePath() + File.separator + name;

        if (!dbfile.endsWith(".db")) {
            dbfile += ".db" ;
        }

        File result = new File(dbfile);

        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs();
        }

        return result;
    }

    /**
     * for devices greater than or equal to  api v11
     * @param name
     * @param mode
     * @param factory
     * @param errorHandler
     * @return
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return openOrCreateDatabase(name, mode, factory);
    }

    /**
     * For devices less than api v11
     * @param name
     * @param mode
     * @param factory
     * @return
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
    }
}