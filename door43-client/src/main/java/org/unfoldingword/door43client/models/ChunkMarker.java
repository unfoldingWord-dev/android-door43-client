package org.unfoldingword.door43client.models;

/**
 * Represents the beginning of a chunk in a chapter
 */
public class ChunkMarker implements DatabaseObject {
    public static String chapter;
    public static String verse;
    private DBInfo dbInfo = new DBInfo();

    /**
     *
     * @param chapter the chapter this chunk exists in
     * @param verse the verse at which this chunk starts
     */
    public ChunkMarker(String chapter, String verse) {
        this.chapter = chapter;
        this.verse = verse;
    }

    @Override
    public DBInfo _dbInfo() {
        return dbInfo;
    }
}
