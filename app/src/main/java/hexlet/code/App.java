package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlChecksController;
import hexlet.code.controller.UrlsController;
import hexlet.code.repository.BaseRepository;
import hexlet.code.util.DataBase;
import hexlet.code.util.NamedRoutes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        LOGGER.debug("Using port: {}", port);
        return Integer.valueOf(port);
    }

    private static TemplateEngine createTemplateEngine() {
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates");
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    public static void main(String[] args) {
        Javalin app = null;
        try {
            app = getApp();
            app.start(getPort());
            LOGGER.info("Application configured successfully");
        } catch (Exception e) {
            LOGGER.error("Failed to start application: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public static Javalin getApp() throws SQLException, IOException {
        LOGGER.info("Starting application configuration...");

        var config = new HikariConfig();
        config.setJdbcUrl(System.getProperty("JDBC_DATABASE_URL", "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1"));
        config.setMaximumPoolSize(10);
        BaseRepository.dataSource = new HikariDataSource(config);

        DataBase.runMigrations();


        var app = Javalin.create(conf -> {
            conf.bundledPlugins.enableDevLogging();
            conf.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.before(ctx -> {
            ctx.attribute(FLASH_MESSAGE, ctx.sessionAttribute(FLASH_MESSAGE));
            ctx.attribute(FLASH_TYPE, ctx.sessionAttribute(FLASH_TYPE));
        });

        app.get(NamedRoutes.rootPath(), RootController::index);
        app.get(NamedRoutes.urlsPath(), UrlsController::index);
        app.post(NamedRoutes.urlsPath(), UrlsController::create);
        app.get(NamedRoutes.urlPath("{id}"), UrlsController::show);
        app.post(NamedRoutes.urlChecksPath("{id}"), UrlChecksController::create);

        return app;

    }
}
