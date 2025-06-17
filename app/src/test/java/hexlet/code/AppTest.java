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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
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
    void testUrlsControllerCreateInvalidUrlWithFlash() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            // Делаем POST запрос (с автоматическим редиректом)
            var response = client.post("/urls", "url=invalid-url");

            // Проверяем что в итоге оказались на главной странице
            assertThat(response.code()).isEqualTo(200);

            // Проверяем что есть flash-сообщение об ошибке
            assertThat(response.body().string())
                    .contains("URL не может быть пустым")
                    .contains("danger");
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

    @Test
    void testConnection() throws SQLException {
        try (var conn = BaseRepository.dataSource.getConnection()) {
            assertThat(conn.isValid(1)).isTrue();
        }
    }

    @Test
    void testUrlRepositoryFindNonExisting() throws SQLException {
        var found = UrlRepository.findById(999L);
        assertThat(found).isEmpty();
    }
}
