package org.unfoldingword.door43client.objects;

/**
 * Represents the beginning of a chunk in a chapter
 */
public class ChunkMarker {
    public static String chapter;
    public static String verse;

    public ChunkMarker(String chapter, String verse) {
        this.chapter = chapter;
        this.verse = verse;
    }
}
