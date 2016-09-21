package org.unfoldingword.door43client.models;

import org.json.JSONException;
import org.json.JSONObject;
import org.unfoldingword.door43client.LegacyTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource that can be translated
 */
public class Resource extends DatabaseObject {
    public final String slug;
    public final String name;
    public final String type;
    public final Map<String, Object> status;
    public final List<Format> formats = new ArrayList<>();
    public final String wordsAssignmentsUrl;

    public Resource(String slug, String name, String type, String wordsAssignmentsUrl, Map status) {
        this.slug = slug;
        this.name = name;
        this.type = type;
        this.status = status;
        this.wordsAssignmentsUrl = wordsAssignmentsUrl;
    }

    public void addFormat(Format format) {
        formats.add(format);
    }

    /**
     * Returns the object serialized to json
     * @return
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("slug", slug);
        json.put("name", name);
        json.put("type", type);
        JSONObject statusJson = new JSONObject();
        statusJson.put("translate_mode", status.get("translate_mode"));
        statusJson.put("checking_level", status.get("checking_level"));
        statusJson.put("license", status.get("license"));
        // TODO: add the rest of the status
        json.put("status", statusJson);
        return json;
    }

    /**
     * Represents a physical form of the resource
     */
    public static class Format {
        public int packageVersion;
        public String mimeType;
        public int modifiedAt;
        public String url;

        public Format(int packageVersion, String mimeType, int modifiedAt, String url) {
            this.packageVersion = packageVersion;
            this.mimeType = mimeType;
            this.modifiedAt = modifiedAt;
            this.url = url;
        }
    }
}
