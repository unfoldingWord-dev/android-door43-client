package org.unfoldingword.door43client.objects;

/**
 * Represents a language that a resource exists in  (for the purpose of source content)
 */
public class SourceLanguage extends DatabaseObject {
    public final String slug;
    public final String name;
    public final String direction;

    /**
     *
     * @param slug the language code
     * @param name the name of the language
     * @param direction the written direction of the language
     */
    public SourceLanguage(String slug, String name, String direction) {
        this.slug = slug;
        this.name = name;
        this.direction = direction;
    }
}
