package org.unfoldingword.door43client.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource that can be translated
 */
public class Resource extends org.unfoldingword.resourcecontainer.Resource {
    public final List<Format> formats = new ArrayList<>();
    public String wordsAssignmentsUrl = "";

    public Resource(String slug, String name, String type, String translateMode, String checkingLevel, String version) {
        super(slug, name, type, translateMode, checkingLevel, version);
    }

    public void addFormat(Format format) {
        formats.add(format);
    }

    /**
     * Creates a resource from json
     * @param json
     * @return
     * @throws JSONException
     */
    public static Resource fromJSON(JSONObject json) throws JSONException {
        // TRICKY: we cannot cast a base class to a child class so we must manually convert the object
        org.unfoldingword.resourcecontainer.Resource r = org.unfoldingword.resourcecontainer.Resource.fromJSON(json);
        Resource resource = new Resource(r.slug, r.name, r.type, r.translateMode, r.checkingLevel, r.version);
        resource.comments = r.comments;
        resource.pubDate = r.pubDate;
        resource.license = r.license;
        return resource;
    }

    /**
     * Represents a physical form of the resource
     */
    public static class Format {
        public String packageVersion;
        public String mimeType;
        public int modifiedAt;
        public String url;

        public Format(String packageVersion, String mimeType, int modifiedAt, String url) {
            this.packageVersion = packageVersion;
            this.mimeType = mimeType;
            this.modifiedAt = modifiedAt;
            this.url = url;
        }
    }
}
