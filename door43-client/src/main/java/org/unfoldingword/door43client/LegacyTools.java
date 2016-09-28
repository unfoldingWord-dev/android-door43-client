package org.unfoldingword.door43client;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.unfoldingword.door43client.models.Catalog;
import org.unfoldingword.door43client.models.Category;
import org.unfoldingword.door43client.models.ChunkMarker;
import org.unfoldingword.door43client.models.Project;
import org.unfoldingword.door43client.models.Resource;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.Versification;
import org.unfoldingword.resourcecontainer.ContainerTools;
import org.unfoldingword.resourcecontainer.ResourceContainer;
import org.unfoldingword.tools.http.GetRequest;

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

    /**
     *
     * @param library
     * @param host the host to use for the catalogs
     * @throws Exception
     */
    public static void injectGlobalCatalogs(Library library, String host) throws Exception {
        host = host != null && !host.trim().isEmpty() ? host : "http://td.unfoldingword.org";

        library.addCatalog(new Catalog("langnames", host + "/exports/langnames.json", 0));
        // TRICKY: the trailing / is required on these urls
        library.addCatalog(new Catalog("new-language-questions", host + "/api/questionnaire/", 0));
        library.addCatalog(new Catalog("temp-langnames", host + "/api/templanguages/", 0));
        // TRICKY: this catalog should always be indexed after langnames and temp-langnames otherwise the linking will fail!
        library.addCatalog(new Catalog("approved-temp-langnames", host + "/api/templanguages/assignment/changed/", 0));
    }

    public static void processCatalog(Library library, String data, OnProgressListener listener) throws Exception {
        JSONArray projects = new JSONArray(data);
        for(int i = 0; i < projects.length(); i ++) {
            JSONObject pJson = projects.getJSONObject(i);
            if(listener != null) listener.onProgress(pJson.getString("slug"), projects.length(), i + 1);
            downloadSourceLanguages(library, pJson, null);
        }

        // tA
        updateTA(library, listener);
    }

    private static void updateTA(Library library, OnProgressListener listener) throws Exception {
        String[] urls = new String[]{
                "https://api.unfoldingword.org/ta/txt/1/en/audio_2.json",
                "https://api.unfoldingword.org/ta/txt/1/en/checking_1.json",
                "https://api.unfoldingword.org/ta/txt/1/en/checking_2.json",
                "https://api.unfoldingword.org/ta/txt/1/en/gateway_3.json",
                "https://api.unfoldingword.org/ta/txt/1/en/intro_1.json",
                "https://api.unfoldingword.org/ta/txt/1/en/process_1.json",
                "https://api.unfoldingword.org/ta/txt/1/en/translate_1.json",
                "https://api.unfoldingword.org/ta/txt/1/en/translate_2.json"
        };
        for(int i = 0; i < urls.length; i ++) {
            downloadTA(library, urls[i]);
            if(listener != null) listener.onProgress("ta", urls.length, i + 1);
        }
    }

    private static void downloadTA(Library library, String url) throws Exception {
        GetRequest get = new GetRequest(new URL(url));
        String data = get.read();
        if(get.getResponseCode() != 200) throw new Exception(get.getResponseMessage());
        JSONObject ta = new JSONObject(data);

        // add language (right now only english)
        long languageId = library.addSourceLanguage(new SourceLanguage("en", "English", "ltr"));

        // add project
        String rawSlug = ta.getJSONObject("meta").getString("manual").replaceAll("\\_", "-");
        String name  = (rawSlug.charAt(0) + "").toUpperCase() + rawSlug.substring(1);
        Project p = new Project("ta-" + rawSlug, name, "", "", 0, "");
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("ta", "translationAcademy"));
        long projectId = library.addProject(p, categories, languageId);

        // TODO: add resource
    }

    /**
     * This will download the source languages for a project.
     * Some of the project info is mixed with languages
     * so we are creating the projects and langauges here
     *
     * @param library
     * @param pJson the project json
     * @param listener
     * @throws Exception
     */
    private static void downloadSourceLanguages(Library library, JSONObject pJson, OnProgressListener listener) throws Exception {
        GetRequest request = new GetRequest(new URL(pJson.getString("lang_catalog")));
        String response = request.read();
        if(request.getResponseCode() != 200) throw new Exception(request.getResponseMessage());

        String chunksUrl = "";
        if(!pJson.getString("slug").toLowerCase().equals("obs")) {
            chunksUrl = "https://api.unfoldingword.org/bible/txt/1/" + pJson.getString("slug") + "/chunks.json";
        }

        JSONArray languages = new JSONArray(response);
        for(int i = 0; i < languages.length(); i ++) {
            JSONObject lJson = languages.getJSONObject(i);

            if(listener != null) listener.onProgress(lJson.getJSONObject("language").getString("slug") + pJson.getString("slug"), languages.length(), i + 1);

            SourceLanguage sl = new SourceLanguage(lJson.getJSONObject("language").getString("slug"),
                    lJson.getJSONObject("language").getString("name"),
                    lJson.getJSONObject("language").getString("direction"));
            long languageId = library.addSourceLanguage(sl);

            // TODO: retrieve the correct versification name(s) from the source language
            library.addVersification(new Versification("en-US", "American English"), languageId);

            Project project = new Project(pJson.getString("slug"),
                    lJson.getJSONObject("project").getString("name"),
                    lJson.getJSONObject("project").getString("desc"),
                    null,
                    pJson.getInt("sort"),
                    chunksUrl);
            List<Category> categories = new ArrayList<>();
            if(pJson.has("meta")) {
                for(int j = 0; j < pJson.getJSONArray("meta").length(); j ++) {
                    String slug = pJson.getJSONArray("meta").getString(j);
                    categories.add(new Category(slug, lJson.getJSONObject("project").getJSONArray("meta").getString(j)));
                }
            }

            long projectId = library.addProject(project, categories, languageId);

            downloadResources(library, projectId, pJson, languageId, lJson);
        }

        // chunks
        if(!chunksUrl.isEmpty()) {
            downloadChunks(library, chunksUrl, "en", pJson.getString("slug"));
        }
    }

    /**
     * Downloads resources for a project.
     * This will split notes and questions into their own resource.
     * words are added as a new project.
     *
     * @param library
     * @param projectId
     * @param pJson
     * @param languageId
     * @param lJson
     * @throws Exception
     */
    private static void downloadResources(Library library, long projectId, JSONObject pJson, long languageId, JSONObject lJson) throws Exception {
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
            Resource.Format format = new Resource.Format(ResourceContainer.version, ContainerTools.typeToMime("book"), rJson.getInt("date_modified"), rJson.getString("source"));
            resource.addFormat(format);

            long resourceId = library.addResource(resource, projectId);

            // coerce notes to resource
            if(rJson.has("notes") && !rJson.getString("notes").isEmpty()) {
                status.put("translate_mode", "gl");

                List<Map> sourceTranslations = new ArrayList();
                Map<String, Object> tnSourceTranslation = new HashMap();
                tnSourceTranslation.put("language_slug", lJson.getJSONObject("language").getString("slug"));
                tnSourceTranslation.put("resource_slug", "tn");
                tnSourceTranslation.put("version", resource.status.get("version"));
                sourceTranslations.add(tnSourceTranslation);
                status.put("source_translations", sourceTranslations);

                Resource tnResource = new Resource("tn", "translationNotes", "help", null, status);
                Resource.Format tnFormat = new Resource.Format(ResourceContainer.version, ContainerTools.typeToMime("help"), rJson.getInt("date_modified"), rJson.getString("notes"));
                tnResource.addFormat(tnFormat);
                library.addResource(tnResource, projectId);
            }

            // coerce questions to resource
            if(rJson.has("checking_questions") && !rJson.getString("checking_questions").isEmpty()) {
                status.put("translate_mode", "gl");

                List<Map> sourceTranslations = new ArrayList();
                Map<String, Object> tnSourceTranslation = new HashMap();
                tnSourceTranslation.put("language_slug", lJson.getJSONObject("language").getString("slug"));
                tnSourceTranslation.put("resource_slug", "tq");
                tnSourceTranslation.put("version", resource.status.get("version"));
                sourceTranslations.add(tnSourceTranslation);
                status.put("source_translations", sourceTranslations);

                Resource tqResource = new Resource("tq", "translationQuestions", "help", null, status);
                Resource.Format tqFormat = new Resource.Format(ResourceContainer.version, ContainerTools.typeToMime("help"), rJson.getInt("date_modified"), rJson.getString("checking_questions"));
                tqResource.addFormat(tqFormat);
                library.addResource(tqResource, projectId);
            }

            // add words project (this is insert/update so it will only be added once)
            // TRICKY: obs tw has not been unified with bible tw yet so we add it as separate project.
            if(rJson.has("terms") && !rJson.getString("terms").isEmpty()) {
                String slug = pJson.getString("slug").equals("obs") ? "bible-obs" : "bible";
                String name = "translationWords" + (pJson.getString("slug").equals("obs") ? " OBS" : "");
                Project wordsProject = new Project(slug, name, "", null, 100, "");
                long wordsProjectId = library.addProject(wordsProject, null, languageId);

                // add resource to words project
                status.put("translate_mode", "gl");

                List<Map> sourceTranslations = new ArrayList();
                Map<String, Object> twSourceTranslation = new HashMap();
                twSourceTranslation.put("language_slug", lJson.getJSONObject("language").getString("slug"));
                twSourceTranslation.put("resource_slug", "tw");
                twSourceTranslation.put("version", resource.status.get("version"));
                sourceTranslations.add(twSourceTranslation);
                status.put("source_translations", sourceTranslations);

                Resource twResource = new Resource("tw", "translationWords", "dict", null, status);
                Resource.Format twFormat = new Resource.Format(ResourceContainer.version, ContainerTools.typeToMime("dict"), rJson.getInt("date_modified"), rJson.getString("terms"));
                twResource.addFormat(twFormat);
                library.addResource(twResource, wordsProjectId);
            }
        }
    }

    /**
     * Downloads chunks for a project
     * @param library
     * @param chunksUrl
     * @param sourceLanguageSlug
     * @param projectSlug
     * @throws Exception
     */
    private static void downloadChunks(Library library, String chunksUrl, String sourceLanguageSlug, String projectSlug) throws Exception {
        String versificationSlug = "en-US"; // TODO: pull the correct versification slug from the data. For now there is only one versification
        Versification v = library.getVersification(sourceLanguageSlug, versificationSlug);
        if(v != null) {
            GetRequest request = new GetRequest(new URL(chunksUrl));
            String data = request.read();
            JSONArray chunks = new JSONArray(data);
            for(int i = 0; i < chunks.length(); i ++) {
                JSONObject chunk = chunks.getJSONObject(i);
                ChunkMarker cm = new ChunkMarker(chunk.getString("chp"), chunk.getString("firstvs"));
                library.addChunkMarker(cm, projectSlug, v._dbInfo.rowId);
            }
        } else {
            System.console().writer().write("Unknown versification " + versificationSlug + " while downloading chunks for project " + projectSlug);
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
