package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtil;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private Javalin app;

    @BeforeEach
    public void setUp() throws SQLException {
        app = App.getApp();
        UrlRepository.removeAll();
    }
    @AfterEach
    void tearDown() {
        if (app != null) {
            app.stop();
        }
        try {
            UrlRepository.removeAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMainPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор страниц");
        });
    }

    @Test
    public void testUrlsPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Сайты");
        });
    }

    @Test
    public void testShowExistingUrlPage() {
        JavalinTest.test(app, (server, client) -> {
            var url = new Url("https://example.com");
            UrlRepository.save(url);

            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("https://example.com");
        });
    }

    @Test
    public void testShowUrlPage() throws SQLException {
        JavalinTest.test(app, (server, client) -> {
            String urlName = "https://example.com";
            var url = new Url(urlName);
            UrlRepository.save(url);

            var savedUrl = UrlRepository.findByName(urlName).orElseThrow();
            assertThat(savedUrl.getId()).isNotNull();

            var response = client.get(NamedRoutes.urlPath(String.valueOf(savedUrl.getId())));
            String responseBody = response.body().string();

            assertThat(response.code()).isEqualTo(200);
            assertThat(responseBody).contains(urlName);
            assertThat(responseBody).contains("Сайты");
        });
    }

    @Test
    public void testCreateInvalidUrl() throws IOException {
        JavalinTest.test(app, (server, client) -> {
            String invalidUrl = "invalid-url";
            var response = client.post(NamedRoutes.urlsPath(), "url=" + invalidUrl);

            assertThat(response.code()).isEqualTo(200);
            assertThat(UrlRepository.getEntities()).isEmpty();
        });
    }

    @Test
    void testNormalizeUrl() throws URISyntaxException, MalformedURLException {
        String url = "http://example.com/path?query=1";
        String normalized = UrlUtil.normalizeUrl(url);
        assertThat(normalized).isEqualTo("http://example.com");
    }
}
