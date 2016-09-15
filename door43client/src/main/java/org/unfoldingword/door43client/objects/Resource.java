package org.unfoldingword.door43client.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a resource that can be translated
 */
public class Resource extends DatabaseObject {
    public String slug;
    public String name;
    public String type;
    public String translateMode;
    public String checkingLevel;
    public String comments;
    public long pubDate;
    public String license;
    public String version;
    public int projectId;
    public Map status;
    public List<Format> formats = new ArrayList<>();
    public String translationWordsAssignmentsUrl;

    public Resource(String slug, String name, String type, String translateMode, String checkingLevel, String comments, long pubDate, String license, String version, int projectId, Map status) {
        this.slug = slug;
        this.name = name;
        this.type = type;
        this.translateMode = translateMode;
        this.checkingLevel = checkingLevel;
        this.comments = comments;
        this.pubDate = pubDate;
        this.license = license;
        this.version = version;
        this.projectId = projectId;
        this.status = status;
    }

    public void addFormat(Format format) {
        formats.add(format);
    }

    public void addTranslationWordsAssignmentsUrl(String url) {
        this.translationWordsAssignmentsUrl = url;
    }

    public static class Format {
        public int packageVersion;
        public String mimeType;
        public int modifiedAt;
        public String url;
        public int resourceId;

        public Format(int packageVersion, String mimeType, int modifiedAt, String url, int resourceId) {
            this.packageVersion = packageVersion;
            this.mimeType = mimeType;
            this.modifiedAt = modifiedAt;
            this.url = url;
            this.resourceId = resourceId;
        }
    }
}
