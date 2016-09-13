package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/7/2016.
 */
public class Catalog {
    public static String slug;
    public static String url;
    public static int modifiedAt;

    public Catalog(String slug, String url, int modifiedAt) {
        this.slug = slug;
        this.url = url;
        this.modifiedAt = modifiedAt;
    }
}
