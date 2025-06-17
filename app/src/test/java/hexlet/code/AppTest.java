package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.BaseRepository;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.DataBase;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtil;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.assertFalse;
import static hexlet.code.repository.BaseRepository.dataSource;
import static org.assertj.core.api.Assertions.assertThat;


public class AppTest {

    private static MockWebServer mockWebServer;
    private static String mockUrl;

    @BeforeAll
    static void setupAll() throws IOException, SQLException {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource = new HikariDataSource(config);
        DataBase.runMigrations();

        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockUrl = mockWebServer.url("/").toString();

        Unirest.config()
                .socketTimeout(500)
                .connectTimeout(500)
                .defaultBaseUrl(mockUrl);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
        if (dataSource != null) {
            dataSource.close();
        }
        Unirest.shutDown();
    }

    @BeforeEach
    void setupEach() throws SQLException {
        DataBase.cleanBase();
    }

    @Test
    public void testRootPage() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains("Анализатор");
        });
    }

    @Test
    public void testShowUrl() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var url = new Url(mockUrl);
            UrlRepository.save(url);

            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains(mockUrl);
        });
    }

    @Test
    public void testCheckNonExistingUrl() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/urls/999");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    public void testUrlsIndexPage() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var url = new Url("https://example.com");
            UrlRepository.save(url);

            var check = new UrlCheck(200, "Test Title", "Test H1", "Test Description", url.getId());
            UrlCheckRepository.save(check);

            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            String body = response.body().string();

            assertThat(body).contains(url.getName());
            assertThat(body).contains("Test Title");
            assertThat(body).contains("Test H1");
            assertThat(body).contains("Test Description");
            assertThat(body).contains("200");
        });
    }

    @Test
    public void testStoreUrlAndCheck() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(html)
                    .addHeader("Content-Type", "text/html"));

            var createResponse = client.post("/urls", "url=" + mockUrl);
            assertThat(createResponse.code()).isEqualTo(200);

            String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
            var savedUrl = UrlRepository.findByName(normalizedUrl)
                    .orElseThrow(() -> new AssertionError("URL должен быть сохранен"));

            var checkResponse = client.post("/urls/" + savedUrl.getId() + "/checks");
            assertThat(checkResponse.code()).isEqualTo(200);

            var checks = UrlCheckRepository.getChecksByUrlId(savedUrl.getId());
            assertThat(checks).isNotEmpty();

            var check = checks.get(0);
            assertThat(check.getStatusCode()).isEqualTo(200);
            assertThat(check.getTitle()).isEqualTo("Test Page Title");
            assertThat(check.getH1()).isEqualTo("Test H1 Header");
            assertThat(check.getDescription()).isEqualTo("Test Description");

            var showResponse = client.get("/urls/" + savedUrl.getId());
            assertThat(showResponse.code()).isEqualTo(200);
            String showBody = showResponse.body().string();

            assertThat(showBody).contains(normalizedUrl);
            assertThat(showBody).contains("Test Page Title");
            assertThat(showBody).contains("Test H1 Header");
            assertThat(showBody).contains("Test Description");
        });
    }

    @Test
    void testSaveAndFind() throws SQLException {
        Url url = new Url("https://example.com");
        UrlRepository.save(url);

        Optional<Url> found = UrlRepository.findByName("https://example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("https://example.com");
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        try (var conn = dataSource.getConnection()) {
            assertThat(conn.isValid(1)).isTrue();

            // Проверка возможности записи
            try (var stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INT)");
                stmt.execute("INSERT INTO test_table VALUES (1)");
                ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void testConnection() throws SQLException {
        try (var conn = BaseRepository.dataSource.getConnection()) {
            assertThat(conn.isValid(1)).isTrue();
        }
    }
//    @BeforeAll
//    static void setupAll() throws IOException, SQLException {
//        var config = new HikariConfig();
//        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
//        BaseRepository.dataSource = new HikariDataSource(config);
//        DataBase.runMigrations();
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//        mockUrl = mockWebServer.url("/").toString();
//
//        Unirest.config()
//                .socketTimeout(500)
//                .connectTimeout(500)
//                .defaultBaseUrl(mockUrl);
//        //app = App.getApp();
//        //app.start(0);
//    }
//
//    @AfterAll
//    static void tearDown() throws IOException {
//        if (app != null) {
//            app.stop();
//        }
//        mockWebServer.shutdown();
//        if (BaseRepository.dataSource != null) {
//            BaseRepository.dataSource.close();
//        }
//        Unirest.shutDown();
//    }
//
//    @BeforeEach
//    void setupEach() throws SQLException{
//        DataBase.cleanBase();
//    }
//
//    @AfterEach
//    void tearDownEach() {
//        if (app != null) {
//            app.stop();
//        }
//    }
//    @Test
//    public void testCreateUrl() throws Exception {
//        Javalin app = App.getApp();
//        String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody(html)
//                .addHeader("Content-Type", "text/html")
//        );
//
//        String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
//        var url = new Url(normalizedUrl);
//        UrlRepository.save(url);
//
//        var savedUrl = UrlRepository.findById(url.getId()).orElse(null);
//        assertNotNull(savedUrl, "URL должен быть сохранен перед проверкой");
//
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + url.getId() + "/checks"))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        assertEquals(302, response.statusCode());
//        assertTrue(response.headers().firstValue("Location").isPresent());
//
//        var checks = UrlCheckRepository.getChecksByUrlId(url.getId());
//        assertFalse(checks.isEmpty(), "Проверка должна быть создана");
//
//        var check = checks.get(0);
//        assertEquals(200, check.getStatusCode());
//        assertEquals("Test Page Title", check.getTitle());
//        assertEquals("Test H1 Header", check.getH1());
//        assertEquals("Test Description", check.getDescription());
//    }
//
//    @Test
//    public void testCreateCheck() throws Exception {
//        String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));
//
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody(html)
//                .addHeader("Content-Type", "text/html")
//        );
//
//        String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
//        var url = new Url(normalizedUrl);
//        UrlRepository.save(url);
//
//        var savedUrl = UrlRepository.findById(url.getId()).orElse(null);
//        assertNotNull(savedUrl, "URL должен быть сохранен перед проверкой");
//
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + url.getId() + "/checks"))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        assertEquals(302, response.statusCode(), "После проверки URL должен быть редирект");
//        var locationHeader = response.headers().firstValue("Location");
//        assertTrue(locationHeader.isPresent(), "Должен быть заголовок Location");
//        assertEquals("/urls/" + url.getId(), locationHeader.get(),
//                "Location должен вести на страницу URL");
//
//        var checks = UrlCheckRepository.getChecksByUrlId(url.getId());
//        assertFalse(checks.isEmpty(), "Должна быть создана хотя бы одна проверка");
//        var check = checks.get(0);
//
//        assertEquals(200, check.getStatusCode());
//        assertEquals("Test Page Title", check.getTitle());
//        assertEquals("Test H1 Header", check.getH1());
//        assertEquals("Test Description", check.getDescription());
//    }
//
//    @Test
//    public void testIndexPage() throws Exception {
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/"))
//                .GET()
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        assertEquals(200, response.statusCode());
//        assertTrue(response.body().contains("Анализатор страниц"),
//                "Главная страница должна содержать заголовок");
//    }
//
//    @Test
//    public void testShowUrl() throws Exception {
//        var url = new Url(mockUrl);
//        UrlRepository.save(url);
//
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + url.getId()))
//                .GET()
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        assertEquals(200, response.statusCode());
//        assertTrue(response.body().contains(mockUrl));
//    }
//
//    @Test
//    public void testCheckNonExistingUrl() throws Exception {
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/999/checks"))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        assertEquals(302, response.statusCode());
//    }
//
//    @Test
//    public void testCreateUrlWithInvalidData() throws Exception {
//        var client = HttpClient.newHttpClient();
//        var formData = "url=invalid-url";
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls"))
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .POST(HttpRequest.BodyPublishers.ofString(formData))
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//        assertEquals(302, response.statusCode());
//        assertFalse(response.body().contains("Некорректный URL"));
//    }
//
//    @Test
//    public void testCreateCheckWithMissingTags() throws Exception {
//        String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));
//
//
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody(html)
//                .addHeader("Content-Type", "text/html"));
//
//        var client = HttpClient.newHttpClient();
//        var createRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls"))
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .POST(HttpRequest.BodyPublishers.ofString("url=" + mockUrl))
//                .build();
//
//        var createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(302, createResponse.statusCode());
//
//        String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
//        var savedUrl = UrlRepository.findByName(normalizedUrl)
//                .orElseThrow(() -> new AssertionError("URL должен быть сохранен"));
//
//        var checkRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + savedUrl.getId() + "/checks"))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//        var checkResponse = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(302, checkResponse.statusCode());
//
//        var checks = UrlCheckRepository.getChecksByUrlId(savedUrl.getId());
//        assertFalse(checks.isEmpty(), "Проверка должна быть создана");
//
//        var check = checks.get(0);
//        assertEquals(200, check.getStatusCode());
//        assertEquals("Test Page Title", check.getTitle());
//        assertEquals("Test H1 Header", check.getH1());
//        assertEquals("Test Description", check.getDescription());
//
//        var showRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + savedUrl.getId()))
//                .GET()
//                .build();
//
//        var showResponse = client.send(showRequest, HttpResponse.BodyHandlers.ofString());
//        assertTrue(showResponse.body().contains("Test Page Title"));
//        assertTrue(showResponse.body().contains("Test H1 Header"));
//        assertTrue(showResponse.body().contains("Test Description"));
//    }
//
//    @Test
//    public void testUrlsIndexPageBasic() throws Exception {
//        var url = new Url("https://example.com");
//        UrlRepository.save(url);
//
//        var check = new UrlCheck(200, "Test Title", "Test H1", "Test Description", url.getId());
//        UrlCheckRepository.save(check);
//
//        var client = HttpClient.newHttpClient();
//        var request = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + url.getId()))
//                .GET()
//                .build();
//
//        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        String body = response.body();
//
//        assertEquals(200, response.statusCode());
//        assertTrue(body.contains(url.getName()));
//        assertTrue(body.contains("Test Title"));
//        assertTrue(body.contains("Test H1"));
//        assertTrue(body.contains("Test Description"));
//        assertTrue(body.contains("200"));
//    }
//
//    @Test
//    public void testStoreUrlAndCheck() throws Exception {
//        String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));
//        mockWebServer.enqueue(new MockResponse()
//                .setResponseCode(200)
//                .setBody(html)
//                .addHeader("Content-Type", "text/html"));
//
//        var client = HttpClient.newHttpClient();
//
//        String formData = "url=" + mockUrl;
//        var createRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls"))
//                .header("Content-Type", "application/x-www-form-urlencoded")
//                .POST(HttpRequest.BodyPublishers.ofString(formData))
//                .build();
//
//        var createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(302, createResponse.statusCode(), "After URL creation should be redirect");
//
//        String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
//        var savedUrl = UrlRepository.findByName(normalizedUrl)
//                .orElseThrow(() -> new AssertionError("URL should be saved"));
//        assertNotNull(savedUrl, "URL should be saved in database");
//        assertEquals(normalizedUrl, savedUrl.getName(), "Saved URL should match the input");
//
//        var checkRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + savedUrl.getId() + "/checks"))
//                .POST(HttpRequest.BodyPublishers.noBody())
//                .build();
//
//        var checkResponse = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(302, checkResponse.statusCode(), "After URL check should be redirect");
//
//        var checks = UrlCheckRepository.getChecksByUrlId(savedUrl.getId());
//        assertFalse(checks.isEmpty(), "At least one check should be created");
//        var check = checks.get(0);
//
//        assertEquals(200, check.getStatusCode());
//        assertEquals("Test Page Title", check.getTitle());
//        assertEquals("Test H1 Header", check.getH1());
//        assertEquals("Test Description", check.getDescription());
//
//        var showRequest = HttpRequest.newBuilder()
//                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + savedUrl.getId()))
//                .GET()
//                .build();
//
//        var showResponse = client.send(showRequest, HttpResponse.BodyHandlers.ofString());
//        assertEquals(200, showResponse.statusCode(), "Show URL page should return 200");
//
//        String showBody = showResponse.body();
//        assertTrue(showBody.contains(normalizedUrl));
//        assertTrue(showBody.contains("Test Page Title"));
//        assertTrue(showBody.contains("Test H1 Header"));
//        assertTrue(showBody.contains("Test Description"));
//    }
//
//    @Test
//    public void testRootPage() throws SQLException, IOException {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get(NamedRoutes.rootPath());
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string())
//                    .contains("Анализатор");
//        });
//    }
}
