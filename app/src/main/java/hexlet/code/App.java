package hexlet.code;

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

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

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
    public static Javalin getApp() {
        LOGGER.info("Starting application configuration...");

        DataSource dataSource;
        try {
            dataSource = DataBase.getDataSource();
            DataBase.runMigrations(dataSource);
        } catch (SQLException | IOException e) {
            LOGGER.error("Ошибка при инициализации базы данных: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Не удалось инициализировать базу данных", e);
        }
        BaseRepository.setDataSource((HikariDataSource) dataSource);

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
        });

        app.before(ctx -> {
            ctx.attribute("flashMessage", ctx.sessionAttribute("flashMessage"));
            ctx.attribute("flashType", ctx.sessionAttribute("flashType"));
        });

        app.get(NamedRoutes.rootPath(), RootController::index);
        app.get(NamedRoutes.urlsPath(), UrlsController::index);
        app.post(NamedRoutes.urlsPath(), UrlsController::create);
        app.get(NamedRoutes.urlPath("{id}"), UrlsController::show);
        app.post(NamedRoutes.urlChecksPath("{id}"), UrlChecksController::create);

        return app;
    }
}
