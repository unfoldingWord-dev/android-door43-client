package org.unfoldingword.door43client.objects;

/**
 * Created by Andrew on 9/2/2016.
 */
public class DummyTargetLanguage {
    public static String slug;
    public static String name;
    public final String anglicizedName;
    public static String direction;
    public static String region;
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
    public DummyTargetLanguage(String slug, String name, String anglicizedName, String direction, String region, boolean isGatewayLanguage) {

        this.slug = slug;
        this.name = name;
        this.anglicizedName = anglicizedName;
        this.direction = direction;
        this.region = region;
        this.isGatewayLanguage = isGatewayLanguage;
    }
}
