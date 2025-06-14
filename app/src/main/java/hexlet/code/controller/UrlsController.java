package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtil;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public final class UrlsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsController.class);
    public static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String DANGER_TYPE = "danger";

    private UrlsController() {
        throw new UnsupportedOperationException("Это служебный класс, создание экземпляров запрещено");
    }

    public static void create(Context ctx) {
        String urlString = ctx.formParam("url");

        if (urlString == null || urlString.isBlank()) {
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "URL не может быть пустым");
            ctx.redirect(NamedRoutes.rootPath());
            return;
        }
        try {
            String normalizedUrl = UrlUtil.normalizeUrl(urlString);

            if (UrlRepository.findByName(normalizedUrl).isPresent()) {
                ctx.sessionAttribute("flashMessage", "Страница уже существует");
                ctx.sessionAttribute("flashType", "info");
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }

            Url url = new Url(normalizedUrl);
            UrlRepository.save(url);

            ctx.sessionAttribute("flashMessage", "Страница успешно добавлена");
            ctx.sessionAttribute("flashType", "success");
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.error("Invalid URL: {}", urlString, e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Некорректный URL: " + urlString);
            ctx.status(400);
            ctx.redirect(NamedRoutes.rootPath());
        } catch (SQLException e) {
            LOGGER.error("Database error", e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при сохранении URL");
            ctx.redirect(NamedRoutes.rootPath());
        } catch (Exception e) {
            LOGGER.error("Unexpected error", e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Произошла непредвиденная ошибка: " + e.getMessage());
            ctx.status(500);
            ctx.result("Ошибка" + e.getMessage());
            ctx.redirect(NamedRoutes.rootPath());
        }
    }

    public static void index(Context ctx) {
        LOGGER.info("Loading URLs index page");
        try {
            var urls = UrlRepository.getEntities();
            Map<Long, UrlCheck> lastChecks = new HashMap<>();
            for (Url url : urls) {
                UrlCheckRepository.getLastCheckByUrlId(url.getId()).ifPresent(check -> {
                    lastChecks.put(url.getId(), check);
                });
            }

            var page = new UrlsPage(urls, lastChecks);

            String flashType = ctx.sessionAttribute(FLASH_TYPE);
            String flashMessage = ctx.sessionAttribute(FLASH_MESSAGE);

            if (flashType != null && flashMessage != null) {
                page.setFlash(flashType, flashMessage);
                ctx.sessionAttribute(FLASH_TYPE, null);
                ctx.sessionAttribute(FLASH_MESSAGE, null);
            }
            ctx.render("urls/index.jte", model("page", page));
        } catch (SQLException e) {
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при загрузке списка сайтов");
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }

    public static void show(Context ctx) {
        try {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            var urlOptional = UrlRepository.findById(id);

            if (urlOptional.isEmpty()) {
                ctx.status(404);
                ctx.render("errors/404.jte");
                return;
            }

            var checks = UrlCheckRepository.getChecksByUrlId(id);
            var page = new UrlPage(urlOptional.get(), checks);
            ctx.render("urls/show.jte", model("page", page));

        } catch (NumberFormatException e) {
            ctx.status(400);
            ctx.render("errors/400.jte");
        } catch (SQLException e) {
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка базы данных: " + e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }
}
