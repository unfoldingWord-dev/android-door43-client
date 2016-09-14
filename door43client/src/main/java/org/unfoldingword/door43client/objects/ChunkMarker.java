package org.unfoldingword.door43client.objects;

/**
 * Represents the beginning of a chunk in a chapter
 */
public class ChunkMarker {
    public static String chapter;
    public static String verse;

    /**
     *
     * @param chapter the chapter this chunk exists in
     * @param verse the verse at which this chunk starts
     */
    public ChunkMarker(String chapter, String verse) {
        this.chapter = chapter;
        this.verse = verse;
    }
}
