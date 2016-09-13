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
    public String translate_mode;
    public String checking_level;
    public String comments;
    public String pub_date;  // long??
    public String license;
    public String version;
    public String project_id;
    public Map status;
    public List<Format> formats = new ArrayList<>();

    public Resource() {
        // TODO: load args
    }

    public void addFormat(Format format) {
        // TODO: add format
    }


    public static class Format {

        // todo add properties
    }
}
