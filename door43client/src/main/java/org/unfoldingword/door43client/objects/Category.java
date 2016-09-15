package org.unfoldingword.door43client.objects;

/**
 * Represents a project category. e.g. a group of projects.
 */
public class Category extends DatabaseObject {
    public final String slug;
    public final String name;

    /**
     *
     * @param slug the category code
     * @param name the name of the category
     */
    public Category(String slug, String name) {
        this.name = name;
        this.slug = slug;
    }
}
