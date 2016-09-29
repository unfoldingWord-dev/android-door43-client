package org.unfoldingword.door43client.models;

import org.unfoldingword.resourcecontainer.Language;

/**
 * Represents a language that a resource exists in  (for the purpose of source content)
 */
public class SourceLanguage extends Language implements DatabaseObject {
    private DBInfo dbInfo = new DBInfo();

    /**
     * Creates a new source language
     * @param slug the language code
     * @param name the name of the language
     * @param direction the written direction of the language
     */
    public SourceLanguage(String slug, String name, String direction) {
        super(slug, name, direction);
    }

    @Override
    public DBInfo _dbInfo() {
        return dbInfo;
    }
}
