package org.unfoldingword.door43client.objects;

import java.util.List;

/**
 * Created by Andrew on 9/8/2016.
 */
public class Project {
    public static String slug;
    public static String name;
    public static String description;
    public static String icon;
    public static int sort;
    public static String chunksUrl;
    public static int sourceLanguageId;
    public static int categoryId;


    public Project(String slug, String name, String description, String icon, int sort, String chunksUrl, int sourceLanguageId, int categoryId) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.sort = sort;
        this.chunksUrl = chunksUrl;
        this.sourceLanguageId = sourceLanguageId;
        this.categoryId = categoryId;
    }
}
