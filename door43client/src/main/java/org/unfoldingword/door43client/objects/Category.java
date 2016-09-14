package org.unfoldingword.door43client.objects;

/**
 * Represents a project category. e.g. a group of projects.
 */
public class Category {
    public final String slug;
    public final String name;

    public Category(String slug, String name) {
        this.name = name;
        this.slug = slug;
    }
}
