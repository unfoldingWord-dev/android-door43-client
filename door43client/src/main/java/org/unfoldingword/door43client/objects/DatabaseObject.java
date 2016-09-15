package org.unfoldingword.door43client.objects;

/**
 * Provides some extra utilties for objects that originated from the database
 */
public abstract class DatabaseObject {
    public DBInfo _info = new DBInfo();

    public static class DBInfo {
        public long id = -1;
    }
}
