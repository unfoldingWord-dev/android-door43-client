package org.unfoldingword.door43client.models;

/**
 * Represents a global catalog
 */
public class Catalog implements DatabaseObject {
    public final String slug;
    public final String url;
    public final int modifiedAt;
    private DBInfo dbInfo = new DBInfo();

    /**
     *
     * @param slug the catalog code
     * @param url the url where the catalog exists
     * @param modifiedAt when the catalog was last modified
     */
    public Catalog(String slug, String url, int modifiedAt) {
        this.slug = slug;
        this.url = url;
        this.modifiedAt = modifiedAt;
    }

    @Override
    public DBInfo _dbInfo() {
        return dbInfo;
    }
}
