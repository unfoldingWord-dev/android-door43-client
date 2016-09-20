package org.unfoldingword.door43client;

import android.app.Application;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    private Door43Client client;

    @Before
    public void setUp() throws Exception {
        this.context = RuntimeEnvironment.application;
        client = new Door43Client(context, "index", resourceDir.getRoot());
    }

    @Test
    public void updatePrimaryIndex() throws Exception {
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

        client.setGlobalCatalogServer("http://localhost:" + wireMockRule.port());
        client.updatePrimaryIndex("http://localhost:" + wireMockRule.port() + "/catalog", null);
        client.updateCatalogIndex("langnames", null);

        assertEquals(3, client.index().getSourceLanguages().size());
        // TRICKY: counts also include tw-bible and/or tw-obs
        assertEquals(4, client.index().getProjects("en").size());
        assertEquals(2, client.index().getProjects("es").size());
        assertEquals(1, client.index().getProjects("ru").size());
        // TRIKCY: counts may also includes resources for helps
        assertEquals(4, client.index().getResources("en", "gen").size());
        assertEquals(3, client.index().getResources("en", "obs").size());
        assertEquals(5, client.index().getTargetLanguages().size());
        assertEquals(1, client.index().getQuestionnaires().size());

        verify(getRequestedFor(urlMatching("/catalog")));
    }
}
