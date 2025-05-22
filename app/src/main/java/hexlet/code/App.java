package hexlet.code;

import io.javalin.Javalin;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "7070");
        LOGGER.debug("Using port: " + port);
        return Integer.valueOf(port);
    }
    public static Javalin getApp() {
        LOGGER.info("Starting application configuration...");

        var app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
        });

        app.get("/", ctx -> {
            LOGGER.debug("Received request to root endpoint");
            ctx.result("Hello World");
        });

        return app;

    }

    public static void main(String[] args) {
        var app = getApp();
        app.start(getPort());
        LOGGER.info("Application configured successfully");
    }
}
