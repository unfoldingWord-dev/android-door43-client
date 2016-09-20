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
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

/**
 * Created by joel on 9/19/16.
 */
@RunWith(RobolectricTestRunner.class)
public class ClientIndexTest {
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);//wireMockConfig().dynamicPort().dynamicHttpsPort());
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

        client.updatePrimaryIndex("http://localhost:" + wireMockRule.port() + "/catalog", null);

        // TODO: run asserts

        verify(getRequestedFor(urlMatching("/catalog")));
    }
}
