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

    public static void create(Context ctx) {
        String urlString = ctx.formParam("url");
        try {
            if (urlString == null || urlString.isBlank()) {
                ctx.sessionAttribute("flashType", "danger");
                ctx.sessionAttribute("flashMessage", "URL не может быть пустым");
                ctx.redirect(NamedRoutes.rootPath());
                return;
            }

            String normalizedUrl = UrlUtil.normalizeUrl(urlString);

            if (UrlRepository.findByName(normalizedUrl).isPresent()) {
                ctx.sessionAttribute("flashType", "info");
                ctx.sessionAttribute("flashMessage", "Сайт уже добавлен");
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }

            Url url = new Url(normalizedUrl);
            UrlRepository.save(url);

            ctx.sessionAttribute("flashType", "success");
            ctx.sessionAttribute("flashMessage", "Сайт успешно добавлен!");
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (MalformedURLException | URISyntaxException e) {
            ctx.sessionAttribute("flashType", "danger");
            ctx.sessionAttribute("flashMessage", "Некорректный URL: " + urlString);
            ctx.redirect(NamedRoutes.rootPath());
        } catch (Exception e) {
            ctx.sessionAttribute("flashType", "danger");
            ctx.sessionAttribute("flashMessage", "Произошла непредвиденная ошибка: " + e.getMessage());
            ctx.redirect(NamedRoutes.rootPath());
        }
    }

    public static void index(Context ctx) throws SQLException {
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

            String flashType = ctx.sessionAttribute("flashType");
            String flashMessage = ctx.sessionAttribute("flashMessage");

            if (flashType != null && flashMessage != null) {
                page.setFlash(flashType, flashMessage);
                ctx.sessionAttribute("flashType", null);
                ctx.sessionAttribute("flashMessage", null);
            }
            ctx.render("urls/index.jte", model("page", page));
        } catch (SQLException e) {
            ctx.sessionAttribute("flashMessage", "Ошибка при загрузке списка сайтов");
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlsPath());
    }
        }

    public static void show(Context ctx) throws SQLException {
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
            ctx.sessionAttribute("flashMessage", "Ошибка базы данных: " + e.getMessage());
            ctx.sessionAttribute("flashType", "danger");
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }
}
