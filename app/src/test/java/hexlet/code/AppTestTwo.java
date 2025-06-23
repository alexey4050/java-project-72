//package hexlet.code;
//
//import hexlet.code.model.Url;
//import hexlet.code.model.UrlCheck;
//import hexlet.code.repository.UrlCheckRepository;
//import hexlet.code.repository.UrlRepository;
//import hexlet.code.util.NamedRoutes;
//import io.javalin.Javalin;
//import io.javalin.testtools.JavalinTest;
//import okhttp3.mockwebserver.MockResponse;
//import okhttp3.mockwebserver.MockWebServer;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.AfterAll;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.sql.SQLException;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//
//public class AppTestTwo {
//    private static MockWebServer mockWebServer;
//    private Javalin app;
//    private Url testUrl;
//    private UrlCheck testUrlCheck;
//
//    public static String readFileFixtures(String fileName) throws IOException {
//        Path filePath = Paths.get("src/test/resources/", fileName);
//        return new String(Files.readAllBytes(filePath));
//    }
//
//    @BeforeAll
//    public static void startMockServer() throws IOException {
//        mockWebServer = new MockWebServer();
//        mockWebServer.enqueue(new MockResponse().setBody(readFileFixtures("mock_response.html")));
//        mockWebServer.start();
//    }
//
//    @BeforeEach
//    public final void setUp() throws IOException, SQLException {
//        app = App.getApp();
//
//        testUrl = new Url("https://en.hexlet.io");
//        UrlRepository.save(testUrl);
//
//        testUrlCheck = new UrlCheck(
//                200,
//                "Test Page Title",
//                "Test H1 Heading",
//                "Test Description",
//                testUrl.getId()
//        );
//        UrlCheckRepository.save(testUrlCheck);
//        }
//
//    @AfterAll
//    public static void tearDown() throws IOException {
//        mockWebServer.shutdown();
//    }
//
//    @Test
//    public void testRootPage() {
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get("/");
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string()).contains("Анализатор");
//        });
//    }
//
//    @Test
//    public void testUrlCreation() {
//        JavalinTest.test(app, (server, client) -> {
//            String fixture = "https://en.hexlet.io";
//            String urlForAdding = "url=" + fixture;
//            var response = client.post(NamedRoutes.urlsPath(), urlForAdding);
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string())
//                    .contains(fixture);
//            response = client.get(NamedRoutes.urlPath(1L));
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string())
//                    .contains(fixture);
//            assertFalse(UrlRepository.findByName(fixture).isEmpty());
//        });
//    }
//
//    @Test
//    void testShowUrlWithMultipleChecks() throws SQLException {
//        UrlCheck secondCheck = new UrlCheck(
//                200,
//                "Second Page Title",
//                "Second H1",
//                "Second Description",
//                testUrl.getId()
//        );
//        UrlCheckRepository.save(secondCheck);
//
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get(NamedRoutes.urlPath(testUrl.getId()));
//            String body = response.body().string();
//
//            assertThat(body)
//                    .contains(testUrlCheck.getTitle())
//                    .contains(secondCheck.getTitle());
//
//            assertThat(body.indexOf(secondCheck.getTitle()))
//                    .isLessThan(body.indexOf(testUrlCheck.getTitle()));
//        });
//    }
//
//    @Test
//    public void testShowUrl() {
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get(NamedRoutes.urlsPath());
//            assertThat(response.code()).isEqualTo(200);
//            String body = response.body().string();
//            assertThat(body).contains(testUrl.getName());
//            assertThat(body).contains(String.valueOf(testUrlCheck.getStatusCode()));
//        });
//    }
//
//    @Test
//    void testUrlCheckCreation() throws SQLException {
//        var mockUrlString = mockWebServer.url("/").toString();
//        Url mockUrl = new Url(mockUrlString);
//        UrlRepository.save(mockUrl);
//
//        var idInBase = mockUrl.getId();
//
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.post(NamedRoutes.urlChecksPath(idInBase));
//            assertThat(response.code()).isEqualTo(200);
//
//            response = client.get(NamedRoutes.urlPath(idInBase));
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string())
//                    .contains("Test Description");
//            UrlCheck check = UrlCheckRepository.getLastChecksForAllUrls().get(1L);
//            assertThat(check.getTitle()).isEqualTo("Test Page Title");
//            assertThat(check.getH1()).isEqualTo("Test H1 Heading");
//
//        });
//    }
//
//    @Test
//    void testNonExistingUrl() {
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get("/urls/999");
//            assertThat(response.code()).isEqualTo(404);
//        });
//    }

//    @Test
//    void testUrlsIndex() {
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get("/urls");
//            assertThat(response.code()).isEqualTo(200);
//            String body = response.body().string();
//
//            assertThat(body).contains(existingUrl.getName());
//            assertThat(body).contains(String.valueOf(existingUrlCheck.getStatusCode()));
//        });
//    }
//    @BeforeAll
//    static void setupAll() throws SQLException, IOException {
//        App.initDataSource(); // Используем DataSource из App
//    }
//
//    @BeforeEach
//    void setup() throws IOException, SQLException {
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//        mockUrl = mockWebServer.url("/").toString().replaceAll("/$", "");
//
//        // Очищаем только тестовые данные
//        try (var conn = BaseRepository.dataSource.getConnection();
//             var stmt = conn.createStatement()) {
//            stmt.execute("DELETE FROM url_checks");
//            stmt.execute("DELETE FROM urls");
//        }
//
//        existingUrl = new Url("https://en.hexlet.io");
//        UrlRepository.save(existingUrl);
//        System.out.println("Saved URL ID: " + existingUrl.getId());
//
//        existingUrlCheck = new UrlCheck(200, "Test page",
//                "Do not expect a miracle, miracles yourself!",
//                "statements of great people", existingUrl.getId());
//        UrlCheckRepository.save(existingUrlCheck);
//    }
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

//    @Test
//    public void testRootPage() throws SQLException, IOException {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get(NamedRoutes.rootPath());
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string()).contains("Анализатор");
//        });
//    }

//    @Test
//    void testServerError() {
//        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
//
//        HttpResponse<String> response = Unirest.get(mockUrl)
//                .asString();
//
//        assertThat(response.getStatus()).isEqualTo(500);
//    }

//    @Test
//    public void testShowUrl() throws SQLException, IOException {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            System.out.println("Existing URL ID: " + existingUrl.getId());
//            List<Url> allUrls = UrlRepository.getEntities();
//            System.out.println("All URLs in DB: " + allUrls);
//            Optional<Url> urlFromDb = UrlRepository.findById(existingUrl.getId());
//            assertThat(urlFromDb).isPresent();
//            var response = client.get("/urls/" + existingUrl.getId());
//            assertThat(response.code()).isEqualTo(200);
//            assertThat(response.body().string())
//                    .contains(existingUrl.getName())
//                    .contains(String.valueOf(existingUrlCheck.getStatusCode()));
//        });
//    }
//
//    @Test
//    void testUrlCreation() throws SQLException, IOException {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.post("/urls", "url=https://example.com");
//            assertThat(response.code()).isEqualTo(200);
//
//            Optional<Url> savedUrl = UrlRepository.findByName("https://example.com");
//            assertThat(savedUrl).isPresent();
//        });
//    }

//    @Test
//    public void testStore() throws Exception {
//        Javalin app = App.getApp();
//
//        String testHtml = Files.readString(
//                Paths.get("src/test/resources/mock_response.html"),
//                StandardCharsets.UTF_8
//        );
//        mockWebServer.enqueue(new MockResponse()
//                .setBody(testHtml)
//                .setResponseCode(200));
//
//        String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
//
//        JavalinTest.test(app, (server, client) -> {
//            var requestBody = "url=" + mockUrl;
//            assertThat(client.post("/urls", requestBody).code()).isEqualTo(200);
//
//            Optional<Url> actualUrl = UrlRepository.findByName(normalizedUrl);
//            assertThat(actualUrl).isPresent();
//            assertThat(actualUrl.get().getName()).isEqualTo(normalizedUrl);
//
//            var checkResponse = client.post("/urls/" + actualUrl.get().getId() + "/checks");
//            assertThat(checkResponse.code()).isEqualTo(200);
//
//            var checks = UrlCheckRepository.getChecksByUrlId(actualUrl.get().getId());
//            assertThat(checks.size()).isEqualTo(1);
//
//            UrlCheck actualCheck = checks.get(0);
//            assertThat(actualCheck).isNotNull();
//            assertThat(actualCheck.getTitle()).isEqualTo("Test Page Title");
//            assertThat(actualCheck.getH1()).isEqualTo("Test H1 Heading");
//            assertThat(actualCheck.getDescription()).isEqualTo("Test Description");
//        });
//    }
//
//    @Test
//    void testUrlCheckCreationAndFields() throws Exception {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            String testHtml = Files.readString(
//                    Paths.get("src/test/resources/mock_response.html"),
//                    StandardCharsets.UTF_8
//            );
//
//            mockWebServer.enqueue(new MockResponse()
//                    .setBody(testHtml)
//                    .setResponseCode(200));
//
//            String normalizedUrl = UrlUtil.normalizeUrl(mockUrl);
//
//            var createResponse = client.post("/urls", "url=" + mockUrl);
//            assertThat(createResponse.code()).isEqualTo(200);
//
//            Optional<Url> savedUrl = UrlRepository.findByName(normalizedUrl);
//            assertThat(savedUrl).as("URL должен быть сохранен").isPresent();
//            Long urlId = savedUrl.get().getId();
//
//            var checkResponse = client.post("/urls/" + urlId + "/checks");
//            assertThat(checkResponse.code()).isEqualTo(200);
//
//            var checks = UrlCheckRepository.getChecksByUrlId(urlId);
//            assertThat(checks.size()).isEqualTo(1);
//
//            UrlCheck check = checks.get(0);
//            assertThat(check.getUrlId()).isEqualTo(urlId);
//            assertThat(check.getStatusCode()).isEqualTo(200);
//            assertThat(check.getTitle()).isEqualTo("Test Page Title");
//            assertThat(check.getH1()).isEqualTo("Test H1 Heading");
//            assertThat(check.getDescription()).isEqualTo("Test Description");
//            assertThat(check.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
//
//            assertThat(check.getId()).isNotNull();
//
//            var showResponse = client.get("/urls/" + urlId);
//            assertThat(showResponse.code()).isEqualTo(200);
//            String body = showResponse.body().string();
//
//            assertThat(body).contains("Test Page Title");
//            assertThat(body).contains("Test H1 Heading");
//            assertThat(body).contains("Test Description");
//            assertThat(body).contains("200");
//        });
//    }

//    @Test
//    public void testUrlsIndexPage() throws SQLException, IOException {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.get("/urls/" + existingUrl.getId());
//            assertThat(response.code()).isEqualTo(200);
//            String body = response.body().string();
//
//            assertThat(body).contains(existingUrl.getName());
//            assertThat(body).contains(existingUrlCheck.getTitle());
//            assertThat(body).contains(existingUrlCheck.getH1());
//            assertThat(body).contains(existingUrlCheck.getDescription());
//            assertThat(body).contains(String.valueOf(existingUrlCheck.getStatusCode()));
//        });
//    }
//
//    @Test
//    void testUrlCheckRepository() throws SQLException {
//        var checks = UrlCheckRepository.getChecksByUrlId(existingUrl.getId());
//        assertThat(checks.size()).isEqualTo(1);
//        assertThat(checks.get(0).getTitle()).isEqualTo("Test page");
//    }
//
//    @Test
//    void testUrlRepositorySaveAndFind() throws SQLException {
//        Optional<Url> found = UrlRepository.findById(existingUrl.getId());
//        assertThat(found).isPresent();
//        assertThat(found.get().getName()).isEqualTo("https://en.hexlet.io");
//        assertThat(found.get().getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
//    }
//
//    @Test
//    public void testCheckNonExistingUrl() throws SQLException, IOException {
//        Javalin app = App.getApp();
//        JavalinTest.test(app, (server, client) -> {
//            var response = client.post("/urls/999");
//            assertThat(response.code()).isEqualTo(404);
//        });
//    }
//}

