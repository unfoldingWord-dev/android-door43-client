package org.unfoldingword.door43client.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides some extra utilties for objects that originated from the database
 */
abstract class DatabaseObject {
    public DBInfo _dbInfo = new DBInfo();

    public static class DBInfo {

        /**
         * The database row id of this object
         */
        public long rowId = -1;

        /**
         * Slugs to objects that are related to this one
         */
        public Map<String, String> relatedSlugs = new HashMap<>();
    }
}
