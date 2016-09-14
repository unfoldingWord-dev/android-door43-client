package org.unfoldingword.door43client.objects;

/**
 * Represents a language that a resource will be translated into
 */
public class TargetLanguage {
    public final String slug;
    public final String name;
    public final String anglicizedName;
    public final String direction;
    public final String region;
    public final boolean isGatewayLanguage;

    /**
     *
     * @param slug the language code
     * @param name the name of the language
     * @param anglicizedName the english form of the language name
     * @param direction the writen direction of the language
     * @param region the region in which the language belongs
     * @param isGatewayLanguage indicates if this target language is a gateway language
     */
    public TargetLanguage(String slug, String name, String anglicizedName, String direction, String region, boolean isGatewayLanguage) {

        this.slug = slug;
        this.name = name;
        this.anglicizedName = anglicizedName;
        this.direction = direction;
        this.region = region;
        this.isGatewayLanguage = isGatewayLanguage;
    }
}
