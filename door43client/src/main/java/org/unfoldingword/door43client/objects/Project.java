package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/8/2016.
 */
public class Project {
    public final String slug;
    public final String name;
    public final String description;
    public final String icon;
    public final int sort;
    public final String chunksUrl;

    public Project(String slug, String name, String description, String icon, int sort, String chunksUrl) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.sort = sort;
        this.chunksUrl = chunksUrl;
    }
}
