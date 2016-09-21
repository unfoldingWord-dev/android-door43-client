package org.unfoldingword.door43client.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a project to be translated
 */
public class Project extends DatabaseObject {
    public final String slug;
    public final String name;
    public final String description;
    public final String icon;
    public final int sort;
    public final String chunksUrl;

    /**
     *
     * @param slug the project code
     * @param name the name of the project
     * @param description a description of the project
     * @param icon the url to the project icon
     * @param sort the sorting order of the project
     * @param chunksUrl the url to the project chunks definition
     */
    public Project(String slug, String name, String description, String icon, int sort, String chunksUrl) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.sort = sort;
        this.chunksUrl = chunksUrl;
    }

    /**
     * Returns the object serialized to json
     * @return
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("slug", slug);
        json.put("name", name);
        json.put("desc", description);
        json.put("icon", icon);
        json.put("sort", sort);
        return json;
    }
}
