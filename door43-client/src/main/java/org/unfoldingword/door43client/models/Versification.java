package org.unfoldingword.door43client.models;

/**
 * Represents a versification system.
 * This is what chunk markers are based on.
 */
public class Versification implements DatabaseObject {
    public static String slug;
    public static String name;
    private DBInfo dbInfo = new DBInfo();

    public Versification(String slug, String name) {
        this.slug = slug;
        this.name = name;
    }

    @Override
    public DBInfo _dbInfo() {
        return dbInfo;
    }
}
