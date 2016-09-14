package org.unfoldingword.door43client.objects;

/**
 * Represents a single entry in the list of project/categories
 * i.e. when you are choosing project to translate.
 */
public class CategoryEntry {

    private final Type entryType;
    private final long id;
    private final String slug;
    private final String name;
    private final String sourceLanguageSlug;
    private final long parentCategortId;

    /**
     *
     * @param entryType the type of entry this is e.g. a project or category
     * @param id the db id of the project/category
     * @param slug the slug of the project/category
     * @param name the human readable name of the project/category
     * @param sourceLanguageSlug the slug of the source language in which the name is given (e.g. the name is translated in German, or French)
     * @param parentCategortId the db id of the parent category id (only used when the entry type is category
     */
    public CategoryEntry(Type entryType, long id, String slug, String name, String sourceLanguageSlug, long parentCategortId) {

        this.entryType = entryType;
        this.id = id;
        this.slug = slug;
        this.name = name;
        this.sourceLanguageSlug = sourceLanguageSlug;
        this.parentCategortId = parentCategortId;
    }

    public enum Type {
        PROJECT,
        CATEGORY
    }
}
