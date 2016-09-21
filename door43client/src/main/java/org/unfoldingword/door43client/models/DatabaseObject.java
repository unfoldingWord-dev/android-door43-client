package org.unfoldingword.door43client.models;

/**
 * Provides some extra utilties for objects that originated from the database
 */
abstract class DatabaseObject {
    public DBInfo _dbInfo = new DBInfo();

    public static class DBInfo {
        public long rowId = -1;
    }
}
