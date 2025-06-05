package hexlet.code.controller;

import hexlet.code.dto.UrlPage;
import hexlet.code.dto.UrlsPage;
import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import hexlet.code.util.UrlUtil;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;

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

    public static void index(Context ctx) {
        try {
            var page = new UrlsPage(UrlRepository.getEntities());
            String flashType = ctx.attribute("flashType");
            String flashMessage = ctx.attribute("flashMessage");

            if (flashType != null && flashMessage != null) {
                page.setFlash(flashType, flashMessage);
            }
            ctx.render("urls/index.jte", model("page", page));
        } catch (SQLException e) {
            LOGGER.error("Ошибка при получении списка URL: {}", e.getMessage(), e);
            ctx.sessionAttribute("flashType", "danger");
            ctx.sessionAttribute("flashMessage", "Ошибка при получении списка URL");
            ctx.redirect(NamedRoutes.rootPath());
        }
    }

    public static void show(Context ctx) throws SQLException {
        long id = ctx.pathParamAsClass("id", Long.class).get();
        var urlOptional = UrlRepository.find(id);
        if (urlOptional.isPresent()) {
            var url = urlOptional.get();
            var page = new UrlPage(url);
            page.setFlashMessage(ctx.attribute("flashMessage"));
            page.setFlashType(ctx.attribute("flashType"));
            ctx.render("urls/show.jte", model("page", page, "url", url));
        } else {
            ctx.sessionAttribute("flashType", "info");
            ctx.sessionAttribute("flashMessage", "Страница не найдена");
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }
}
