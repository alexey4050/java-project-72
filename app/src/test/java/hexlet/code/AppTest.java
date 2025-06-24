package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AppTest {
    private static MockWebServer mockWebServer;
    private Javalin app;
    private Url testUrl;
    private UrlCheck testUrlCheck;

    public static String readFileFixtures(String fileName) throws IOException {
        Path filePath = Paths.get("src/test/resources/", fileName);
        return new String(Files.readAllBytes(filePath));
    }

    @BeforeAll
    public static void startMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setBody(readFileFixtures("mock_response.html")));
        mockWebServer.start();
    }

    @BeforeEach
    public final void setUp() throws IOException, SQLException {
        app = App.getApp();

        testUrl = new Url("https://en.hexlet.io");
        UrlRepository.save(testUrl);

        testUrlCheck = new UrlCheck(
                200,
                "Test Page Title",
                "Test H1 Heading",
                "Test Description",
                testUrl.getId()
        );
        UrlCheckRepository.save(testUrlCheck);
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testRootPage() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("Анализатор");
        });
    }

    @Test
    public void testUrlCreation() {
        JavalinTest.test(app, (server, client) -> {
            String fixture = "https://en.hexlet.io";
            String urlForAdding = "url=" + fixture;
            var response = client.post(NamedRoutes.urlsPath(), urlForAdding);
            assertThat(response).isNotNull();
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains(fixture);
            response = client.get(NamedRoutes.urlPath(1L));
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains(fixture);
            assertFalse(UrlRepository.findByName(fixture).isEmpty());
        });
    }

    @Test
    void testShowUrlWithMultipleChecks() throws SQLException {
        UrlCheck secondCheck = new UrlCheck(
                200,
                "Second Page Title",
                "Second H1",
                "Second Description",
                testUrl.getId()
        );
        UrlCheckRepository.save(secondCheck);

        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlPath(testUrl.getId()));
            String body = response.body().string();

            assertThat(body)
                    .contains(testUrlCheck.getTitle())
                    .contains(secondCheck.getTitle());

            assertThat(body.indexOf(secondCheck.getTitle()))
                    .isLessThan(body.indexOf(testUrlCheck.getTitle()));
        });
    }

    @Test
    public void testShowUrl() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get(NamedRoutes.urlsPath());
            assertThat(response.code()).isEqualTo(200);
            String body = response.body().string();
            assertThat(body).contains(testUrl.getName());
            assertThat(body).contains(String.valueOf(testUrlCheck.getStatusCode()));
        });
    }

    @Test
    void testUrlCheckCreation() throws SQLException {
        var mockUrlString = mockWebServer.url("/").toString();
        Url mockUrl = new Url(mockUrlString);
        UrlRepository.save(mockUrl);

        var idInBase = mockUrl.getId();

        JavalinTest.test(app, (server, client) -> {
            var response = client.post(NamedRoutes.urlChecksPath(idInBase));
            assertThat(response.code()).isEqualTo(200);

            response = client.get(NamedRoutes.urlPath(idInBase));
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string())
                    .contains("Test Description");
            UrlCheck check = UrlCheckRepository.getLastChecksForAllUrls().get(1L);
            assertThat(check.getTitle()).isEqualTo("Test Page Title");
            assertThat(check.getH1()).isEqualTo("Test H1 Heading");

        });
    }

    @Test
    void testNonExistingUrl() {
        JavalinTest.test(app, (server, client) -> {
            var response = client.get("/urls/999");
            assertThat(response.code()).isEqualTo(404);
        });
    }
}
