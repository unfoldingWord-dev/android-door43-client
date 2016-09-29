package org.unfoldingword.door43client.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.unfoldingword.resourcecontainer.Language;

/**
 * Represents a language that a resource will be translated into
 */
public class TargetLanguage extends Language implements DatabaseObject {
    public final String anglicizedName;
    public final String region;
    public final boolean isGatewayLanguage;

    private DBInfo dbInfo = new DBInfo();

    /**
     * Creates a new target language
     * @param slug the language code
     * @param name the name of the language
     * @param anglicizedName the english form of the language name
     * @param direction the writen direction of the language
     * @param region the region in which the language belongs
     * @param isGatewayLanguage indicates if this target language is a gateway language
     */
    public TargetLanguage(String slug, String name, String anglicizedName, String direction, String region, boolean isGatewayLanguage) {
        super(slug, name, direction);
        this.anglicizedName = anglicizedName;
        this.region = region;
        this.isGatewayLanguage = isGatewayLanguage;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject json = super.toJSON();
        json.put("anglicized_name", anglicizedName);
        json.put("region", region);
        json.put("is_gateway_language", isGatewayLanguage);
        return json;
    }

    @Override
    public DBInfo _dbInfo() {
        return dbInfo;
    }
}
