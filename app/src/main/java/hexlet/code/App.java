package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String SCHEMA_FILE = "schema.sql";

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        LOGGER.debug("Using port: " + port);
        return Integer.valueOf(port);
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    private static String readResourceFile(String fileName) throws IOException {
        try (InputStream inputStream = App.class.getClassLoader().getResourceAsStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining(""));
        }
    }

    public static void main(String[] args) throws IOException, SQLException {
        var app = getApp();
        app.start(getPort());
        LOGGER.info("Application configured successfully");
    }

    private static HikariDataSource initDataSource() throws IOException, SQLException {
        var hikariConfig = new HikariConfig();

        String jdbcUrl = System.getenv().getOrDefault("JDBC_DATABASE_URL", DEFAULT_JDBC_URL);
        hikariConfig.setJdbcUrl(jdbcUrl);

        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            hikariConfig.setDriverClassName("org.postgresql.Driver");
        }

        var dataSource = new HikariDataSource(hikariConfig);
        initializeDatabase(dataSource);
        return dataSource;
    }

    private static void initializeDatabase(HikariDataSource dataSource) throws IOException, SQLException {
        String sql = readResourceFile(SCHEMA_FILE);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
    public static Javalin getApp() throws IOException, SQLException {
        LOGGER.info("Starting application configuration...");

        var dataSource = initDataSource();
        BaseRepository.setDataSource(dataSource);

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.get("/", ctx -> {
            ctx.render("index.jte");
        });

        return app;
    }
}
