package org.unfoldingword.door43client;

import android.content.Context;
import android.provider.ContactsContract;

import org.json.JSONArray;
import org.json.JSONObject;
import org.unfoldingword.door43client.models.Catalog;
import org.unfoldingword.door43client.models.Project;
import org.unfoldingword.door43client.models.Question;
import org.unfoldingword.door43client.models.Questionnaire;
import org.unfoldingword.door43client.models.Resource;
import org.unfoldingword.door43client.models.SourceLanguage;
import org.unfoldingword.door43client.models.TargetLanguage;
import org.unfoldingword.resourcecontainer.ContainerTools;
import org.unfoldingword.resourcecontainer.ResourceContainer;
import org.unfoldingword.tools.http.GetRequest;
import org.unfoldingword.tools.http.Request;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * Created by joel on 8/30/16.
 */
public class Door43Client {
    private static final OnLogListener defaultLogListener;

    static {
        defaultLogListener = new OnLogListener() {
            @Override
            public void onInfo(String message) {
            }

            @Override
            public void onWarning(String message) {
            }

            @Override
            public void onError(String message, Exception ex) {
            }
        };
    }

    private final File resourceDir;
    private final Library library;
    private String globalCatalogHost = null;
    private OnLogListener logListener = defaultLogListener;

    /**
     * Initializes the new api client
     * @param context the application context
     * @param databasePath the name of the database where information will be indexed
     * @param resourceDir the directory where resource containers will be stored
     */
    public Door43Client(Context context, File databasePath, File resourceDir) throws IOException {
        this.resourceDir = resourceDir;

        // load schema
        URL resource = this.getClass().getClassLoader().getResource("schema.sqlite");
        File sqliteFile = new File(resource.getPath());

        // read schema
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sqliteFile)));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        DatabaseContext databaseContext = new DatabaseContext(context,databasePath);
        SQLiteHelper helper = new SQLiteHelper(databaseContext, sb.toString(), databaseContext.getPackageName());
        this.library = new Library(helper);
    }

    /**
     * Attaches a listener to receive log events
     * @param listener
     */
    public void setLogger(OnLogListener listener) {
        if(listener == null) {
            this.logListener = defaultLogListener;
        } else {
            this.logListener = listener;
        }
    }

    /**
     * Sets the host to use when injecting the global catalogs.
     * This is only valid until we migrate to the use api.
     * @param host
     */
    @Deprecated
    public void setGlobalCatalogServer(String host) {
        this.globalCatalogHost = host;
    }

    /**
     * Returns the read only index
     * @return
     */
    public Index index() {
        return library;
    }

    /**
     * Indexes the Door43 catalog.
     *
     * @param url the entry resource api catalog
     * @param listener an optional progress listener. This should receive progress id, total, completed
     */
    public void updatePrimaryIndex(String url, final OnProgressListener listener) throws Exception {
        // inject missing global catalogs
        library.beginTransaction();
        try {
            LegacyTools.injectGlobalCatalogs(library, globalCatalogHost);
            GetRequest getPrimaryCatalog = new GetRequest(new URL(url));
            getPrimaryCatalog.setProgressListener(new Request.OnProgressListener() {
                @Override
                public void onProgress(long max, long progress) {
//                    if (listener != null) listener.onProgress("catalog", max, progress);
                }

                @Override
                public void onIndeterminate() {
                }
            });
            String data = getPrimaryCatalog.read();
            // process legacy catalog data
            LegacyTools.processCatalog(library, data, listener);
        } catch(Exception e) {
            library.endTransaction(false);
            throw e;
        }
        library.endTransaction(true);
    }

    /**
     * Downloads a global catalog and indexes it.
     *
     * @param catalogSlug the slug of the catalog to download. Or an object containing all the args.
     * @param listener an optional progress listener. This should receive progress id, total, completed
     */
    public void updateCatalogIndex(String catalogSlug, OnProgressListener listener) throws Exception {
        Catalog cat = library.getCatalog(catalogSlug);
        if(cat == null) throw new Exception("Unknown catalog");
        GetRequest request = new GetRequest(new URL(cat.url));
        String data = request.read();
        if(request.getResponseCode() != 200) throw new Exception(request.getResponseMessage());
        library.beginTransaction();
        try {
            switch (catalogSlug) {
                case "langnames":
                    indexTargetLanguageCatalog(data, listener);
                    break;
                case "new-language-questions":
                    indexNewLanguageQuestionsCatalog(data, listener);
                    break;
                case "temp-langnames":
                    indexTempLanguagesCatalog(data, listener);
                    break;
                case "approved-temp-langnames":
                    indexApprovedTempLanguagesCatalog(data, listener);
                    break;
                default:
                    throw new Exception("Parsing this catalog has not been implemented");
            }
        } catch (Exception e) {
            library.endTransaction(false);
            throw e;
        }
        library.endTransaction(true);
    }

    /**
     * parses the target language catalog and indexes it
     * @param data
     * @param listener
     */
    private void indexTargetLanguageCatalog(String data, OnProgressListener listener) throws Exception {
        JSONArray languages = new JSONArray(data);
        for(int i = 0; i < languages.length(); i ++) {
            JSONObject l = languages.getJSONObject(i);
            boolean isGateway = l.has("gl") ? l.getBoolean("gl") : false;
            TargetLanguage language = new TargetLanguage(l.getString("lc"), l.getString("ln"),
                    l.getString("ang"), l.getString("ld"), l.getString("lr"), isGateway);
            if(!library.addTargetLanguage(language)) {
                logListener.onWarning("Failed to add the target language: " + language.slug);
            }
            if(listener != null) listener.onProgress("langnames", languages.length(), i + 1);
        }
    }

    /**
     * Parses the new language questions catalog and indexes it
     * @param data
     * @param listener
     */
    private void indexNewLanguageQuestionsCatalog(String data, OnProgressListener listener) throws Exception {
        JSONObject obj = new JSONObject(data);
        JSONArray languages = obj.getJSONArray("languages");
        for(int i = 0; i < languages.length(); i ++) {
            JSONObject qJson = languages.getJSONObject(i);
            Questionnaire questionnaire = new Questionnaire(qJson.getString("slug"), qJson.getString("name"), qJson.getString("dir"), qJson.getLong("questionnaire_id"));
            long questionnaireId = library.addQuestionnaire(questionnaire);

            // add questions
            for(int j = 0; j < qJson.getJSONArray("questions").length(); j ++) {
                JSONObject questionJson = qJson.getJSONArray("questions").getJSONObject(j);
                long dependsOnId = questionJson.isNull("depends_on") ? 0 : questionJson.getLong("depends_on");
                Question question = new Question(questionJson.getString("text"),
                        questionJson.getString("help"), questionJson.getBoolean("required"),
                        questionJson.getString("input_type"), questionJson.getInt("sort"),
                        dependsOnId, questionJson.getLong("id"));
                library.addQuestion(question, questionnaireId);

                // broadcast itemized progress if there is only one questionnaire
                if(languages.length() == 1 && listener != null) {
                    listener.onProgress("new-language-questions", qJson.getJSONArray("questions").length(), j + 1);
                }
            }
            // broadcast overall progress if there are multiple questionnaires.
            if(languages.length() > 1 && listener != null) {
                listener.onProgress("new-language-questions", qJson.getJSONArray("questions").length(), i + 1);
            }
        }
    }

    /**
     * Parses the temporary language codes catalog and indexes it
     * @param data
     * @param listener
     */
    private void indexTempLanguagesCatalog(String data, OnProgressListener listener) throws Exception {
        JSONArray languages = new JSONArray(data);
        for(int i = 0; i < languages.length(); i ++) {
            JSONObject l = languages.getJSONObject(i);
            boolean isGateway = l.has("gl") ? l.getBoolean("gl") : false;
            TargetLanguage language = new TargetLanguage(l.getString("lc"), l.getString("ln"),
                    l.getString("ang"), l.getString("ld"), l.getString("lr"), isGateway);
            if(!library.addTempTargetLanguage(language)) {
                logListener.onWarning("Failed to add the temp target language: " + language.slug);
            }
            if(listener != null) listener.onProgress("temp-langnames", languages.length(), i + 1);
        }
    }

    /**
     * Parses the approved temporary language codes catalog and indexes it
     * @param data
     * @param listener
     */
    private void indexApprovedTempLanguagesCatalog(String data, OnProgressListener listener) throws Exception {
        JSONArray languages = new JSONArray(data);
        for(int i = 0; i < languages.length(); i ++) {
            JSONObject l = languages.getJSONObject(i);
            Iterator<String> keys = l.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if(!library.setApprovedTargetLanguage(key, l.getString(key))) {
                    logListener.onWarning("Failed to approve the temp target language: " + key + " as " + l.getString(key));
                }
            }
            if(listener != null) listener.onProgress("approved-temp-langnames", languages.length(), i + 1);
        }
    }

    /**
     * Downloads a resource container.
     *
     * TRICKY: to keep the interface stable we've abstracted some things.
     * once the api supports real resource containers this entire method can go away and be replace
     * with downloadContainer_Future (which should be renamed to downloadContainer).
     * convertLegacyResourceToContainer will also become deprecated at that time though it may be handy to keep around.
     *
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return The new resource container
     */
    public ResourceContainer downloadResourceContainer(String sourceLanguageSlug, String projectSlug, String resourceSlug) throws Exception {
        File path = downloadFutureCompatibleResourceContainer(sourceLanguageSlug, projectSlug, resourceSlug);

        // migrate to resource container
        Resource r = library.getResource(sourceLanguageSlug, projectSlug, resourceSlug);
        if(r == null) throw new Exception("Unknown resource");
        String data = FileUtil.readFileToString(path);

        // clean downloaded file
        FileUtil.deleteQuietly(path);
        return convertLegacyResource(sourceLanguageSlug, projectSlug, resourceSlug, data);
    }

    /**
     * Downloads a resource container.
     * This expects a correctly formatted resource container
     * and will download it directly to the disk
     *
     * once the api can deliver proper resource containers this method
     * should be renamed to downloadContainer and the current downloadResourceContainer method removed.
     *
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return the path to the downloaded resource container
     */
    public File downloadFutureCompatibleResourceContainer(String sourceLanguageSlug, String projectSlug, String resourceSlug) throws Exception {
        Resource r = library.getResource(sourceLanguageSlug, projectSlug, resourceSlug);
        if(r == null) throw new Exception("Unknown resource");
        Resource.Format containerFormat = getResourceContainerFormat(r.formats);
        if(containerFormat == null) throw new Exception("Missing resource container format");
        String containerSlug = ContainerTools.makeSlug(sourceLanguageSlug, projectSlug, resourceSlug);
        File containerDir = new File(resourceDir, containerSlug);
        File destFile = new File(resourceDir, containerSlug + "." + ResourceContainer.fileExtension);

        FileUtil.deleteQuietly(destFile);
        FileUtil.deleteQuietly(containerDir);

        destFile.getParentFile().mkdirs();
        if(containerFormat.url == null || containerFormat.url.isEmpty()) throw new Exception("Missing resource format url");
        GetRequest request = new GetRequest(new URL(containerFormat.url));
        request.download(destFile);
        if(request.getResponseCode() != 200) {
            FileUtil.deleteQuietly(destFile);
            throw new Exception(request.getResponseMessage());
        }

        return destFile;
    }

    /**
     * Returns the first resource container format found in the list.
     * E.g. the array may contain binary formats such as pdf, mp3, etc. This basically filters those.
     *
     * @param formats a list of resource formats
     * @return
     */
    private static Resource.Format getResourceContainerFormat(List<Resource.Format> formats) {
        for(Resource.Format f:formats) {
            if(f.mimeType.matches(ResourceContainer.baseMimeType + "\\+.+")) {
                return f;
            }
        }
        return null;
    }

    /**
     * Converts a legacy resource catalog into a resource container.
     * The container will be placed in.
     *
     * This will be deprecated once the api is updated to support proper resource containers.
     *
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @param data the legacy data that will be converted
     * @return
     */
    @Deprecated
    public ResourceContainer convertLegacyResource(String sourceLanguageSlug, String projectSlug, String resourceSlug, String data) throws Exception {
        String containerSlug = ContainerTools.makeSlug(sourceLanguageSlug, projectSlug, resourceSlug);
        File containerDir = new File(resourceDir, containerSlug);

        SourceLanguage language = library.getSourceLanguage(sourceLanguageSlug);
        if(language == null) throw new Exception("Missing language");
        JSONObject lJson = language.toJSON();

        Project project = library.getProject(sourceLanguageSlug, projectSlug);
        if(project == null) throw new Exception("Missing project");
        JSONObject pJson = project.toJSON();
        // TODO: 9/20/16 need to load the project categories into the json
//        List<Category> categories = library.getCategories(project.slug);

        Resource resource = library.getResource(sourceLanguageSlug, projectSlug, resourceSlug);
        if(resource == null) throw new Exception("Missing resource");
        Resource.Format format = getResourceContainerFormat(resource.formats);
        if(format == null) throw new Exception("Missing resource container format");
        JSONObject rJson = resource.toJSON();

        JSONObject properties = new JSONObject();
        properties.put("language", lJson);
        properties.put("project", pJson);
        properties.put("resource", rJson);
        properties.put("modified_at", format.modifiedAt);

        // grab the tW assignments
        if(resource.wordsAssignmentsUrl != null && !resource.wordsAssignmentsUrl.isEmpty()) {
            GetRequest request = new GetRequest(new URL(resource.wordsAssignmentsUrl));
            String wordsData = request.read();
            if(request.getResponseCode() < 300) {
                try {
                    JSONArray words = new JSONArray(wordsData);
                    JSONObject assignmentsJson = new JSONObject();
                    for(int c = 0; c < words.length(); c ++) {
                        JSONObject chapter = words.getJSONObject(c);
                        JSONObject chapterAssignment = new JSONObject();
                        for(int f = 0; f < chapter.getJSONArray("frames").length(); f ++) {
                            JSONObject frame = chapter.getJSONArray("frames").getJSONObject(f);
                            JSONArray frameAssignment = new JSONArray();
                            for(int w = 0; w < frame.getJSONArray("items").length(); w ++) {
                                JSONObject word = frame.getJSONArray("items").getJSONObject(w);
                                String twProjSlug = projectSlug.equals("obs") ? "bible-obs" : "bible";
                                frameAssignment.put("//" + twProjSlug + "/tw/" + word.getString("id"));
                            }
                            chapterAssignment.put(frame.getString("id"), frameAssignment);
                        }
                        assignmentsJson.put(chapter.getString("id"), chapterAssignment);
                    }
                    properties.put("tw_assignments", assignmentsJson);
                } catch (Exception e) {
                    logListener.onWarning(e.getMessage());
                }
            }
        }

        ResourceContainer container = ContainerTools.convertResource(data, containerDir, properties);
        ResourceContainer.close(new File(containerDir.getPath()));
        return container;
    }

    /**
     * Opens a resource container archive so it's contents can be read.
     * The index will be referenced to validate the resource and retrieve the container type.
     *
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return
     */
    public ResourceContainer openResourceContainer(String sourceLanguageSlug, String projectSlug, String resourceSlug) {
        return null;
    }

    /**
     * Closes a resource container archive.
     *
     * @param sourceLanguageSlug
     * @param projectSlug
     * @param resourceSlug
     * @return
     */
    public String closeResourceContainer(String sourceLanguageSlug, String projectSlug, String resourceSlug) {
        return null;
    }

    /**
     * Returns a list of resource containers that have been downloaded
     * @return an array of resource container info objects (package.json).
     */
    public List<JSONObject> listResourceContainers() {
        return null;
    }

    /**
     * Returns a list of projects that are eligible for updates.
     * If the language is given as null the results will include all projects in all languages.
     * This is helpful if you need to view updates based on project first rather than source language first.
     *
     * @param sourceLanguageSlug the slug of a source language who's projects will be checked.
     * @return An array of project slugs
     */
    public List<String> getProjectUpdates(String sourceLanguageSlug) {
        return null;
    }

    /**
     * Returns a list of source languages that are eligible for updates.
     *
     * @return An array of source language slugs
     */
    public List<String> getSourceLanguageUpdates() {
        return null;
    }

    /**
     * A utility to get progress updates durring long operations
     */
    public interface OnProgressListener {
        /**
         *
         * @param tag used to identify what progress event is occurring
         * @param max the total number of items being processed
         * @param complete the number of items that have been successfully processed
         */
        void onProgress(String tag, long max, long complete);
    }

    /**
     * A utility to receive log events from the module
     */
    public interface OnLogListener {
        void onInfo(String message);
        void onWarning(String message);
        void onError(String message, Exception ex);
    }
}
