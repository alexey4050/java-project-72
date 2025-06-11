package hexlet.code;

import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.model.Url;
import hexlet.code.repository.BaseRepository;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.DataBase;
import io.javalin.Javalin;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
    private Javalin app;
    private static MockWebServer mockWebServer;
    private static String mockUrl;

    @BeforeAll
    static void setupAll() throws IOException, SQLException {
        System.setProperty("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;");
        var dataSource = DataBase.getDataSource();
        BaseRepository.setDataSource((HikariDataSource) dataSource);
        DataBase.runMigrations(dataSource);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        mockUrl = mockWebServer.url("/").toString();

        Unirest.config()
                .socketTimeout(500)
                .connectTimeout(500)
                .defaultBaseUrl(mockWebServer.url("/").toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
        DataBase.closeDataSource();
        Unirest.shutDown();
    }

    @BeforeEach
    void setupEach() throws SQLException, IOException {
        DataBase.cleanBase();
        app = App.getApp();
        app.start(0);
    }

    @AfterEach
    void tearDownEach() {
        app.stop();
    }

    @Test
    public void testCreateCheck() throws Exception {
        String html = Files.readString(Paths.get("src/test/resources/mock_response.html"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody(html)
                .addHeader("Content-Type", "text/html")
        );

        var url = new Url(mockUrl);
        UrlRepository.save(url);

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + url.getId() + "/checks"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(302, response.statusCode(), "После проверки URL должен быть редирект");
        var locationHeader = response.headers().firstValue("Location");
        assertTrue(locationHeader.isPresent(), "Должен быть заголовок Location");
        assertEquals("/urls/" + url.getId(), locationHeader.get(),
                "Location должен вести на страницу URL");

        var checks = UrlCheckRepository.getChecksByUrlId(url.getId());
        assertFalse(checks.isEmpty(), "Должна быть создана хотя бы одна проверка");
        var check = checks.get(0);

        assertEquals(200, check.getStatusCode());
        assertEquals("Test Page Title", check.getTitle());
        assertEquals("Test H1 Header", check.getH1());
        assertEquals("Test Description", check.getDescription());
    }

    @Test
    public void testIndexPage() throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/"))
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Анализатор страниц"));
    }

    @Test
    public void testShowUrl() throws Exception {
        var url = new Url(mockUrl);
        UrlRepository.save(url);

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/urls/" + url.getId()))
                .GET()
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(mockUrl));
    }

    @Test
    public void testCheckNonExistingUrl() throws Exception {
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/urls/999/checks"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(302, response.statusCode());
    }

    @Test
    public void testCreateUrlWithInvalidData() throws Exception {
        var client = HttpClient.newHttpClient();
        var formData = "url=invalid-url";
        var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formData))
                .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(302, response.statusCode());
        assertFalse(response.body().contains("Некорректный URL"));
    }
}
