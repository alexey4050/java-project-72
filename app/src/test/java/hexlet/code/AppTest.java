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
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {
    private static HikariDataSource dataSource;
    private MockWebServer mockWebServer;
    private String mockUrl;

    @BeforeAll
    static void setupAll() throws SQLException, IOException {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource = new HikariDataSource(config);
        BaseRepository.dataSource = dataSource;
        DataBase.runMigrations();
    }

    @BeforeEach
    void setup() throws IOException, SQLException {
        DataBase.cleanBase();
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @AfterAll
    static void tearDownAll() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    @Test
    public void testRootPage() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор");
        });
    }

    //проверка ошибок сервера
    @Test
    void testServerError() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        HttpResponse<String> response = Unirest.get(mockUrl)
                .asString();

        assertThat(response.getStatus()).isEqualTo(500);
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
    void testUrlCreation() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var response = client.post("/urls", "url=https://example.com");
            assertThat(response.code()).isEqualTo(200);

            Optional<Url> savedUrl = UrlRepository.findByName("https://example.com");
            assertThat(savedUrl).isPresent();
        });
    }

    @Test
    public void testStore() throws Exception {
        Javalin app = App.getApp();

        String testHtml = Files.readString(
                Paths.get("src/test/resources/mock_response.html"),
                StandardCharsets.UTF_8
        );
        mockWebServer.enqueue(new MockResponse()
                .setBody(testHtml)
                .setResponseCode(200));

        String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);

        JavalinTest.test(app, (server, client) -> {
            var requestBody = "url=" + mockUrl;
            assertThat(client.post("/urls", requestBody).code()).isEqualTo(200);

            Optional<Url> actualUrl = UrlRepository.findByName(normalizedUrl);
            assertThat(actualUrl).isNotNull();
            System.out.println("\n!!!!!");
            System.out.println(actualUrl.get());

            System.out.println("\n");
            assertThat(actualUrl.get().getName()).isEqualTo(normalizedUrl);

            var checkResponse = client.post("/urls/" + actualUrl.get().getId() + "/checks");
            assertThat(checkResponse.code()).isEqualTo(200);

            assertThat(client.get("/urls/" + actualUrl.get().getId()).code())
                    .isEqualTo(200);

            var checks = UrlCheckRepository.getChecksByUrlId(actualUrl.get().getId());
            assertThat(checks).hasSize(1);

            UrlCheck actualCheck = checks.get(0);
            assertThat(actualCheck).isNotNull();
            assertThat(actualCheck.getTitle()).isEqualTo("Test Page Title");
            assertThat(actualCheck.getH1()).isEqualTo("Test H1 Heading");
            assertThat(actualCheck.getDescription()).isEqualTo("Test Description");
        });
    }

    @Test
    void testUrlCheckCreationAndFields() throws Exception {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            String testHtml = Files.readString(
                    Paths.get("src/test/resources/mock_response.html"),
                    StandardCharsets.UTF_8
            );

            mockWebServer.enqueue(new MockResponse()
                    .setBody(testHtml)
                    .setResponseCode(200));

            String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);

            var createResponse = client.post("/urls", "url=" + mockUrl);
            assertThat(createResponse.code()).isEqualTo(200);

            Optional<Url> savedUrl = UrlRepository.findByName(normalizedUrl);
            assertThat(savedUrl).as("URL должен быть сохранен").isPresent();
            Long urlId = savedUrl.get().getId();

            var checkResponse = client.post("/urls/" + urlId + "/checks");
            assertThat(checkResponse.code()).isEqualTo(200);

            var checks = UrlCheckRepository.getChecksByUrlId(urlId);
            assertThat(checks)
                    .as("Проверка URL должна быть сохранена")
                    .hasSize(1);

            UrlCheck check = checks.get(0);
            assertThat(check.getUrlId()).isEqualTo(urlId);
            assertThat(check.getStatusCode()).isEqualTo(200);
            assertThat(check.getTitle()).isEqualTo("Test Page Title");
            assertThat(check.getH1()).isEqualTo("Test H1 Heading");
            assertThat(check.getDescription()).isEqualTo("Test Description");
            assertThat(check.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());

            assertThat(check.getId()).isNotNull();

            var showResponse = client.get("/urls/" + urlId);
            assertThat(showResponse.code()).isEqualTo(200);
            String body = showResponse.body().string();

            assertThat(body).contains("Test Page Title");
            assertThat(body).contains("Test H1 Heading");
            assertThat(body).contains("Test Description");
            assertThat(body).contains("200");
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
    void testUrlCheckRepository() throws SQLException {
        Url url = new Url("https://test.com");
        UrlRepository.save(url);

        UrlCheck check = new UrlCheck(
                200,
                "Test Title",
                "Test H1",
                "Test Description",
                url.getId(),
                LocalDateTime.now()
        );
        UrlCheckRepository.save(check);

        var checks = UrlCheckRepository.getChecksByUrlId(url.getId());
        assertThat(checks).hasSize(1);
        assertThat(checks.get(0).getTitle()).isEqualTo("Test Title");
    }

    @Test
    void testUrlRepositorySaveAndFind() throws SQLException {
        Url url = new Url("https://example.com");
        UrlRepository.save(url);

        var found = UrlRepository.findById(url.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("https://example.com");
        assertThat(found.get().getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
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
    void testUrlChecksControllerCreateCheck() throws Exception {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            String testUrl = "http://test.example.com";

            String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(html)
                    .setResponseCode(200));

            var response = client.post("/urls", "url=" + testUrl);
            assertThat(response.code()).isEqualTo(200);

            Optional<Url> savedUrl = UrlRepository.findByName(testUrl);
            assertThat(savedUrl).as("URL должен быть сохранен").isPresent();
        });
    }

    @Test
    void testUrlRepository() throws SQLException {
        Url url = new Url("https://test.com");
        UrlRepository.save(url);

        Optional<Url> foundUrl = UrlRepository.findById(url.getId());
        assertThat(foundUrl).isPresent();
        assertThat(foundUrl.get().getName()).isEqualTo("https://test.com");

        Optional<Url> foundByName = UrlRepository.findByName("https://test.com");
        assertThat(foundByName).isPresent();
    }

    @Test
    void testNotFoundHandler() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/non-existing");
            assertThat(response.code()).isEqualTo(404);
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

            try (var stmt = conn.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INT)");
                stmt.execute("INSERT INTO test_table VALUES (1)");
                ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }
}
