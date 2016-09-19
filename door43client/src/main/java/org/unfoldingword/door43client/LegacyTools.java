package org.unfoldingword.door43client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unfoldingword.door43client.models.Catalog;
import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.Project;
import org.unfoldingword.door43client.models.Resource;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.Versification;
import org.unfoldingword.tools.http.GetRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by joel on 9/19/16.
 */
class LegacyTools {
    public static void injectGlobalCatalogs(Library library) throws Exception {
        library.addCatalog(new Catalog("langnames", "http://td.unfoldingword.org/exports/langnames.json", 0));
        // TRICKY: the trailing / is required on these urls
        library.addCatalog(new Catalog("new-language-questions", "http://td.unfoldingword.org/api/questionnaire/", 0));
        library.addCatalog(new Catalog("temp-langnames", "http://td.unfoldingword.org/api/templanguages/", 0));
        library.addCatalog(new Catalog("approved-temp-langnames", "http://td.unfoldingword.org/api/templanguages/assignment/changed/", 0));
    }

    public static void processCatalog(Library library, String data, Door43Client.OnProgressListener listener) throws Exception {
        JSONArray projects = new JSONArray(data);
        for(int i = 0; i < projects.length(); i ++) {
            JSONObject pJson = projects.getJSONObject(i);
            downloadSourceLanguages(library, pJson, listener);
        }
    }

    private static void downloadSourceLanguages(Library library, JSONObject pJson, Door43Client.OnProgressListener listener) throws Exception {
        GetRequest request = new GetRequest(new URL(pJson.getString("lang_catalog")));
        String response = request.read();
        if(request.getResponseCode() != 200) throw new Exception(request.getResponseMessage());

        JSONArray languages = new JSONArray(response);
        for(int i = 0; i < languages.length(); i ++) {
            JSONObject lJson = languages.getJSONObject(i);
            SourceLanguage sl = new SourceLanguage(lJson.getString("slug"),
                    lJson.getString("name"),
                    lJson.getString("direction"));
            long languageId = library.addSourceLanguage(sl);

            String chunksUrl = "";
            if(!pJson.getString("slug").toLowerCase().equals("obs")) {
                chunksUrl = "https://api.unfoldingword.org/bible/txt/1/" + pJson.getString("slug") + "/chunks.json";
            }

            Project project = new Project(pJson.getString("slug"),
                    lJson.getJSONObject("project").getString("name"),
                    lJson.getJSONObject("project").getString("desc"),
                    pJson.getString("icon"),
                    pJson.getInt("sort"),
                    chunksUrl);
            List<Category> categories = new ArrayList<>();
            if(pJson.has("meta")) {
                for(int j = 0; j < pJson.getJSONArray("meta").length(); j ++) {
                    String slug = pJson.getJSONArray("meta").getString(j);
                    categories.add(new Category(slug, lJson.getJSONObject("project").getJSONObject("meta").getString(slug)));
                }
            }

            // TODO: retrieve the correct versification name(s) from the source language
            library.addVersification(new Versification("en-US", "American English"), languageId);

            long projectId = library.addProject(project, categories, languageId);

            // TODO: 9/19/16 process the chunks
        }
    }

    private void downloadResources(Library library, long projectId, JSONObject pJson, long languageId, JSONObject lJson) throws Exception {
        GetRequest request = new GetRequest(new URL(lJson.getString("res_catalog")));
        String response = request.read();
        if(request.getResponseCode() != 200) throw new Exception(request.getResponseMessage());

        JSONArray resources = new JSONArray(response);
        for(int i = 0; i < resources.length(); i ++) {
            JSONObject rJson = resources.getJSONObject(i);

            String translateMode;
            switch(rJson.getString("slug").toLowerCase()) {
                case "obs":
                case "ulb":
                    translateMode = "all";
                    break;
                default:
                    translateMode = "gl";
            }

            Map<String, Object> status = jsonToMap(rJson.getJSONObject("status"));
            status.put("translate_mode", translateMode);
            status.put("pub_date", rJson.getJSONObject("status").getString("publish_date"));
            Resource resource = new Resource(rJson.getString("slug"),
                    rJson.getString("name"),
                    "book",
                    rJson.getString("tw_cat"),
                    status);

            long resourceId = library.addResource(resource, projectId);

            // coerce notes to resource

            // coerce questions to resource

            // add words project

            // add resource to words project

            // TODO: 9/19/16 finish
        }
    }

    /**
     * Converts a json object to a hash map
     * http://stackoverflow.com/questions/21720759/convert-a-json-string-to-a-hashmap
     * @param json
     * @return
     * @throws JSONException
     */
    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
