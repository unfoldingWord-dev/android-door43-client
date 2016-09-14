package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/2/2016.
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
     * @param slug
     * @param name
     * @param anglicizedName the english form of the language name
     * @param direction
     * @param region the region in which the language belongs
     * @param isGatewayLanguage
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
