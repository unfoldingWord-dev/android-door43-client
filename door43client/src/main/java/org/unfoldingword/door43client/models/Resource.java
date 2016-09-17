package org.unfoldingword.door43client.models;

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
    public final Map status;
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
