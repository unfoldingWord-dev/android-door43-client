package org.unfoldingword.door43client;

import android.app.Application;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.unfoldingword.door43client.models.TargetLanguage;
import org.unfoldingword.resourcecontainer.ContainerTools;
import org.unfoldingword.resourcecontainer.ResourceContainer;
import org.unfoldingword.tools.http.GetRequest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by joel on 9/19/16.
 */
@RunWith(RobolectricTestRunner.class)
public class ClientIndexTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);
    @Rule
    public TemporaryFolder resourceDir = new TemporaryFolder();

    private Application context;
    private API client;

    @Before
    public void setUp() throws Exception {
        this.context = RuntimeEnvironment.application;

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

        client = new API(context, sb.toString(), resourceDir.getRoot(), resourceDir.getRoot());
    }

    @After
    public void tearDown() {
        client.tearDown();
    }

    private void stubAPI() throws IOException {
        String catalog = Util.loadResource(this.getClass().getClassLoader(), "catalog.json");
        stubFor(get(urlEqualTo("/catalog"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(catalog)));
        String obsLangCatalog = Util.loadResource(this.getClass().getClassLoader(), "obs/languages.json");
        stubFor(get(urlEqualTo("/ts/txt/2/obs/languages.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(obsLangCatalog)));
        String genLangCatalog = Util.loadResource(this.getClass().getClassLoader(), "gen/languages.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/languages.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genLangCatalog)));
        String genEnResCatalog = Util.loadResource(this.getClass().getClassLoader(), "gen/en/resources.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/en/resources.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genEnResCatalog)));
        String genRuResCatalog = Util.loadResource(this.getClass().getClassLoader(), "gen/ru/resources.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/ru/resources.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genRuResCatalog)));
        String obsEnResCatalog = Util.loadResource(this.getClass().getClassLoader(), "obs/en/resources.json");
        stubFor(get(urlEqualTo("/ts/txt/2/obs/en/resources.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(obsEnResCatalog)));
        String obsEsResCatalog = Util.loadResource(this.getClass().getClassLoader(), "obs/es/resources.json");
        stubFor(get(urlEqualTo("/ts/txt/2/obs/es/resources.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(obsEsResCatalog)));
        String langnamesCatalog = Util.loadResource(this.getClass().getClassLoader(), "langnames.json");
        stubFor(get(urlEqualTo("/exports/langnames.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(langnamesCatalog)));
        String questionnairesCatalog = Util.loadResource(this.getClass().getClassLoader(), "questionnaires.json");
        stubFor(get(urlEqualTo("/api/questionnaire/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(questionnairesCatalog)));
        String tempLangnamesCatalog = Util.loadResource(this.getClass().getClassLoader(), "temp_langnames.json");
        stubFor(get(urlEqualTo("/api/templanguages/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(tempLangnamesCatalog)));
        String approvedLangnamesCatalog = Util.loadResource(this.getClass().getClassLoader(), "approved_temp_langnames.json");
        stubFor(get(urlEqualTo("/api/templanguages/assignment/changed/"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(approvedLangnamesCatalog)));
        // this is supposed to fail
        stubFor(get(urlEqualTo("/ts/txt/2/gen/en/udb/source.json"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("not found")));
        String genEnUlbSource = Util.loadResource(this.getClass().getClassLoader(), "gen/en/ulb/source.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/en/ulb/source.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genEnUlbSource)));
        String genEnUlbNotes = Util.loadResource(this.getClass().getClassLoader(), "gen/en/ulb/notes.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/en/notes.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genEnUlbNotes)));
        String genEnUlbQuestions = Util.loadResource(this.getClass().getClassLoader(), "gen/en/ulb/questions.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/en/questions.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genEnUlbQuestions)));
        String genEnUlbWords = Util.loadResource(this.getClass().getClassLoader(), "gen/en/ulb/words.json");
        stubFor(get(urlEqualTo("/ts/txt/2/bible/en/terms.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genEnUlbWords)));
        String genEnUlbAssignments = Util.loadResource(this.getClass().getClassLoader(), "gen/en/ulb/assignments.json");
        stubFor(get(urlEqualTo("/ts/txt/2/gen/en/tw_cat.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(genEnUlbAssignments)));
    }

    @Test
    public void updatePrimaryIndex() throws Exception {
        stubAPI();
        client.setGlobalCatalogServer("http://localhost:" + wireMockRule.port());
        client.updateSources("http://localhost:" + wireMockRule.port() + "/catalog", null);
        client.updateCatalog("langnames");
        assertNotNull(client.index().getTargetLanguage("kff-x-dmorla"));
        client.updateCatalog("new-language-questions");
        client.updateCatalog("temp-langnames");
        assertNotNull(client.index().getTargetLanguage("qaa-x-802d08"));
        client.updateCatalog("approved-temp-langnames");

        assertEquals(3, client.index().getSourceLanguages().size());
        // TRICKY: counts also include tw-bible and/or tw-obs and tA
        assertEquals(10, client.index().getProjects("en").size());
        assertEquals(2, client.index().getProjects("es").size());
        assertEquals(1, client.index().getProjects("ru").size());
        // TRIKCY: counts may also includes resources for helps
        assertEquals(4, client.index().getResources("en", "gen").size());
        assertEquals(3, client.index().getResources("en", "obs").size());
        assertTrue(client.index().getTargetLanguages().size() > 0);
        assertEquals(1, client.index().getQuestionnaires().size());
        TargetLanguage approved = client.index().getApprovedTargetLanguage("qaa-x-802d08");
        assertNotNull(approved);
        assertEquals("kff-x-dmorla", approved.slug);

        verify(getRequestedFor(urlMatching("/catalog")));
    }

    @Test
    public void downloadContainer() throws Exception {
        stubAPI();
        client.setGlobalCatalogServer("http://localhost:" + wireMockRule.port());
        client.updateSources("http://localhost:" + wireMockRule.port() + "/catalog", null);

        File path = client.downloadFutureCompatibleResourceContainer("en", "gen", "ulb");
        assertTrue(path.exists());
    }

    @Test
    public void failToDownloadContainer() throws Exception {
        stubAPI();
        client.setGlobalCatalogServer("http://localhost:" + wireMockRule.port());
        client.updateSources("http://localhost:" + wireMockRule.port() + "/catalog", null);

        try {
            File path = client.downloadFutureCompatibleResourceContainer("en", "gen", "udb");
            assertTrue(!path.exists());
        } catch (Exception e) {
            assertNotNull(e);
            File outputFile = new File(resourceDir.getRoot(), ContainerTools.makeSlug("en", "gen", "udb") + "." + ResourceContainer.fileExtension);
            assertTrue(!outputFile.exists());
        }
    }

    @Test
    public void convertLegacyResource() throws Exception {
        stubAPI();
        client.updateSources("http://localhost:" + wireMockRule.port() + "/catalog", null);

        GetRequest request = new GetRequest(new URL("http://localhost:8090/ts/txt/2/gen/en/ulb/source.json"));
        String data = request.read();

        ResourceContainer container = client.convertLegacyResource("en", "gen", "ulb", data);
        assertNotNull(container);
    }
}
