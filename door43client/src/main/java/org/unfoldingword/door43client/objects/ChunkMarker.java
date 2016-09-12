package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/12/2016.
 */
public class ChunkMarker {
    public static String chapter;
    public static String verse;
    public static String project_slug;
    public static int versification_id;

    public ChunkMarker(String chapter, String verse, String projectSlug, int versificationId) {
        this.chapter = chapter;
        this.verse = verse;
        this.project_slug = projectSlug;
        this.versification_id = versificationId;
    }
}
