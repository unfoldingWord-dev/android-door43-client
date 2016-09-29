package org.unfoldingword.door43client.models;

/**
 * Represents a questionnaire that can be completed in the app
 */
public class Questionnaire implements DatabaseObject {
    public final String languageSlug;
    public final String languageName;
    public final String languageDirection;
    public final long tdId;
    private DBInfo dbInfo = new DBInfo();

    /**
     *
     * @param languageSlug the language code
     * @param languageName the name of the language in which this questionnaire is presented
     * @param languageDirection the written direction of the language
     * @param tdId the translation database id (server side)
     */
    public Questionnaire(String languageSlug, String languageName, String languageDirection, long tdId) {
        this.languageSlug = languageSlug;
        this.languageName = languageName;
        this.languageDirection = languageDirection;
        this.tdId = tdId;
    }

    @Override
    public DBInfo _dbInfo() {
        return dbInfo;
    }
}
