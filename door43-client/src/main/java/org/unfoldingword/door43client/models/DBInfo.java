package org.unfoldingword.door43client.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joel on 9/29/16.
 */

public class DBInfo {
    /**
     * The database row id of this object
     */
    public long rowId = -1;

    /**
     * Slugs to objects that are related to this one
     */
    public Map<String, String> relatedSlugs = new HashMap<>();
}
