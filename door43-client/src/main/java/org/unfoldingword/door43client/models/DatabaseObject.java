package org.unfoldingword.door43client.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides some extra utilties for objects that originated from the database
 */
interface DatabaseObject {
    /**
     * Returns the database info for this object
     * @return
     */
    DBInfo _dbInfo();
}
