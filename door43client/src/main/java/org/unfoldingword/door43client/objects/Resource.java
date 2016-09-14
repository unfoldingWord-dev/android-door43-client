package org.unfoldingword.door43client.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 9/8/2016.
 */
public class Resource {
    public String slug;
    public String name;
    public String type;
    public String translateMode;
    public String checkingLevel;
    public String comments;
    public long pubDate;
    public String license;
    public String version;
    public String projectId;
    public Map status;
    public List<Format> formats = new ArrayList<>();

    public Resource() {
        // TODO: load args
    }

    public void addFormat(Format format) {
        // TODO: add format
    }


    public static class Format {
        public int packageVersion;
        public String mimeType;
        public int modifiedAt;
        public String url;
        public int resourceId;
    }
}
