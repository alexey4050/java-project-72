package hexlet.code;

import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.BaseRepository;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtil;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AppTestTwo {
    private static HikariDataSource dataSource;
    private MockWebServer mockWebServer;
    private String mockUrl;
    private Url existingUrl;
    private UrlCheck existingUrlCheck;

    @BeforeAll
    static void setupAll() throws SQLException, IOException {
        App.initDataSource(); // Используем DataSource из App
    }

    @BeforeEach
    void setup() throws IOException, SQLException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

        // Очищаем только тестовые данные
        try (var conn = BaseRepository.dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM url_checks");
            stmt.execute("DELETE FROM urls");
        }

        existingUrl = new Url("https://en.hexlet.io");
        UrlRepository.save(existingUrl);
        System.out.println("Saved URL ID: " + existingUrl.getId());

        existingUrlCheck = new UrlCheck(200, "Test page",
                "Do not expect a miracle, miracles yourself!",
                "statements of great people", existingUrl.getId());
        UrlCheckRepository.save(existingUrlCheck);
    }
//    @BeforeAll
//    static void setupAll() throws SQLException, IOException {
//        var config = new HikariConfig();
//        config.setJdbcUrl(System.getProperty("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1"));
//        dataSource = new HikariDataSource(config);
//        BaseRepository.dataSource = dataSource;
//        DataBase.runMigrations();
//    }
//
//    @BeforeEach
//    void setup() throws IOException, SQLException {
//        //DataBase.cleanBase();
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//        mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
//
//        // Создаем тестовые данные через репозитории
//        existingUrl = new Url("https://en.hexlet.io");
//        UrlRepository.save(existingUrl);
//        System.out.println("Saved URL ID: " + existingUrl.getId());
//
//        existingUrlCheck = new UrlCheck(
//                200,
//                "Test page",
//                "Do not expect a miracle, miracles yourself!",
//                "statements of great people",
//                existingUrl.getId()
//        );
//        UrlCheckRepository.save(existingUrlCheck);
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        mockWebServer.shutdown();
//    }
//
//    @AfterAll
//    static void tearDownAll() {
//        if (dataSource != null) {
//            dataSource.close();
//        }
//    }

    @Test
    public void testRootPage() throws SQLException, IOException {
        Javalin app = App.getApp();
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.rootPath());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор");
        });
    }

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
            System.out.println("Existing URL ID: " + existingUrl.getId());
            List<Url> allUrls = UrlRepository.getEntities();
            System.out.println("All URLs in DB: " + allUrls);
            Optional<Url> urlFromDb = UrlRepository.findById(existingUrl.getId());
            assertThat(urlFromDb).isPresent();
            var response = client.get("/urls/" + existingUrl.getId());
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains(existingUrl.getName())
                    .contains(String.valueOf(existingUrlCheck.getStatusCode()));
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
            assertThat(actualUrl).isPresent();
            assertThat(actualUrl.get().getName()).isEqualTo(normalizedUrl);

            var checkResponse = client.post("/urls/" + actualUrl.get().getId() + "/checks");
            assertThat(checkResponse.code()).isEqualTo(200);

            var checks = UrlCheckRepository.getChecksByUrlId(actualUrl.get().getId());
            assertThat(checks.size()).isEqualTo(1);

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
            assertThat(checks.size()).isEqualTo(1);

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
            var response = client.get("/urls/" + existingUrl.getId());
            assertThat(response.code()).isEqualTo(200);
            String body = response.body().string();

            assertThat(body).contains(existingUrl.getName());
            assertThat(body).contains(existingUrlCheck.getTitle());
            assertThat(body).contains(existingUrlCheck.getH1());
            assertThat(body).contains(existingUrlCheck.getDescription());
            assertThat(body).contains(String.valueOf(existingUrlCheck.getStatusCode()));
        });
    }

    @Test
    void testUrlCheckRepository() throws SQLException {
        var checks = UrlCheckRepository.getChecksByUrlId(existingUrl.getId());
        assertThat(checks.size()).isEqualTo(1);
        assertThat(checks.get(0).getTitle()).isEqualTo("Test page");
    }

    @Test
    void testUrlRepositorySaveAndFind() throws SQLException {
        Optional<Url> found = UrlRepository.findById(existingUrl.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("https://en.hexlet.io");
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
}
//    private static MockWebServer mockServer;
//    private Javalin app;
//    private Map<String, Object> existingUrl;
//    private Map<String, Object> existingUrlCheck;
//    private HikariDataSource dataSource;
//
//    private static Path getFixturePath(String fileName) {
//        return Paths.get("src", "test", "resources", "fixtures", fileName)
//                .toAbsolutePath().normalize();
//    }
//
//    private static String readFixture(String fileName) throws IOException {
//        Path filePath = getFixturePath(fileName);
//        return Files.readString(filePath).trim();
//    }
//
//    private static String getDatabaseUrl() {
//        return System.getenv().getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");
//    }
//
//    @BeforeAll
//    public static void beforeAll() throws IOException {
//        mockServer = new MockWebServer();
//        MockResponse mockedResponse = new MockResponse()
//                .setBody(readFixture("index.html"));
//        mockServer.enqueue(mockedResponse);
//        mockServer.start();
//    }
//
//    @AfterAll
//    public static void afterAll() throws IOException {
//        mockServer.shutdown();
//    }
//
//    @BeforeEach
//    public void setUp() throws IOException, SQLException {
//        app = App.getApp();
//
//        var hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl(getDatabaseUrl());
//
//        dataSource = new HikariDataSource(hikariConfig);
//
//        var schema = AppTest.class.getClassLoader().getResource("schema.sql");
//        var file = new File(schema.getFile());
//        var sql = Files.lines(file.toPath())
//                .collect(Collectors.joining("\n"));
//
//        try (var connection = dataSource.getConnection();
//             var statement = connection.createStatement()) {
//            statement.execute(sql);
//        }
//
//        String url = "https://en.hexlet.io";
//
//        UrlRepository(dataSource, url);
//        existingUrl = UrlRepository.findByName(dataSource, url);
//
//        UrlCheckRepository.getChecksByUrlId(dataSource, (long) existingUrl.get("id"));
//        existingUrlCheck = TestUtils.getUrlCheck(dataSource, (long) existingUrl.get("id"));
//    }
//
//        @Test
//        void testIndex() {
//            JavalinTest.test(app, (server, client) -> {
//                assertThat(client.get("/").code()).isEqualTo(200);
//            });
//        }
//
//
//        @Test
//        void testIndexOne() {
//            JavalinTest.test(app, (server, client) -> {
//                var response = client.get("/urls");
//                assertThat(response.code()).isEqualTo(200);
//                assertThat(response.body().string())
//                        .contains(existingUrl.get("name").toString())
//                        .contains(existingUrlCheck.get("status_code").toString());
//            });
//        }
//
//        @Test
//        void testShow() {
//            JavalinTest.test(app, (server, client) -> {
//                var response = client.get("/urls/" + existingUrl.get("id"));
//                assertThat(response.code()).isEqualTo(200);
//                assertThat(response.body().string())
//                        .contains(existingUrl.get("name").toString())
//                        .contains(existingUrlCheck.get("status_code").toString());
//            });
//        }
//
//        @Test
//        void testStore() {
//
//            String inputUrl = "https://ru.hexlet.io";
//
//            JavalinTest.test(app, (server, client) -> {
//                var requestBody = "url=" + inputUrl;
//                assertThat(client.post("/urls", requestBody).code()).isEqualTo(200);
//
//                var response = client.get("/urls");
//                assertThat(response.code()).isEqualTo(200);
//                assertThat(response.body().string())
//                        .contains(inputUrl);
//
//                var actualUrl = TestUtils.getUrlByName(dataSource, inputUrl);
//                assertThat(actualUrl).isNotNull();
//                assertThat(actualUrl.get("name").toString()).isEqualTo(inputUrl);
//            });
//        }
//
//
//        @Test
//        void testStore() {
//            String url = mockServer.url("/").toString().replaceAll("/$", "");
//
//            JavalinTest.test(app, (server, client) -> {
//                var requestBody = "url=" + url;
//                assertThat(client.post("/urls", requestBody).code()).isEqualTo(200);
//
//                var actualUrl = TestUtils.getUrlByName(dataSource, url);
//                assertThat(actualUrl).isNotNull();
//                System.out.println("\n!!!!!");
//                System.out.println(actualUrl);
//
//                System.out.println("\n");
//                assertThat(actualUrl.get("name").toString()).isEqualTo(url);
//
//                client.post("/urls/" + actualUrl.get("id") + "/checks");
//
//                assertThat(client.get("/urls/" + actualUrl.get("id")).code())
//                        .isEqualTo(200);
//
//                var actualCheck = TestUtils.getUrlCheck(dataSource, (long) actualUrl.get("id"));
//                assertThat(actualCheck).isNotNull();
//                assertThat(actualCheck.get("title")).isEqualTo("Test page");
//                assertThat(actualCheck.get("h1")).isEqualTo("Do not expect a miracle, miracles yourself!");
//                assertThat(actualCheck.get("description")).isEqualTo("statements of great people");
//            });
//        }

