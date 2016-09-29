package org.unfoldingword.door43client.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a project to be translated
 */
public class Project extends org.unfoldingword.resourcecontainer.Project implements DatabaseObject {

    private DBInfo dbInfo = new DBInfo();

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
        super(slug, name, sort);

        this.description = description;
        this.icon = icon;
        this.chunksUrl = chunksUrl;
    }

    /**
     * Creates a new project from json
     * @param json
     * @return
     * @throws JSONException
     */
    public static Project fromJSON(JSONObject json) throws JSONException {
        // TRICKY: we cannot cast a base class to a child class so we must manually convert the object
        org.unfoldingword.resourcecontainer.Project p = org.unfoldingword.resourcecontainer.Project.fromJSON(json);
        Project project = new Project(p.slug, p.name, p.description, p.icon, p.sort, p.chunksUrl);
        return project;
    }

    @Override
    public DBInfo _dbInfo() {
        return this.dbInfo;
    }
}
