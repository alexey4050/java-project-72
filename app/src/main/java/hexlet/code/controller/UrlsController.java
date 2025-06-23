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
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import static io.javalin.rendering.template.TemplateUtil.model;

public final class UrlsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsController.class);
    public static final String FLASH_TYPE = "flashType";
    public static final String FLASH_MESSAGE = "flashMessage";
    public static final String DANGER_TYPE = "danger";
    public static final String SUCCESS_TYPE = "success";
    public static final String INFO_TYPE = "info";
    public static final String EXAMPLE_URL = "https://www.example.com";

    private UrlsController() {
        throw new UnsupportedOperationException("Это служебный класс, создание экземпляров запрещено");
    }

    public static void create(Context ctx) throws SQLException {
        String urlString = ctx.formParam("url");

        if (urlString == null || urlString.isBlank()) {
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "URL не может быть пустым");
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }

        String normalizedUrl;
        try {
            normalizedUrl = UrlUtil.normalizeUrl(urlString);
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.error("Некорректный URL: {}", e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Некорректный URL");
            ctx.redirect(NamedRoutes.urlsPath());
            return;
        }

        var existingUrl = UrlRepository.findByName(normalizedUrl);

        if (existingUrl.isPresent()) {
            ctx.sessionAttribute(FLASH_TYPE, INFO_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Страница уже существует");
            ctx.redirect(NamedRoutes.urlPath(existingUrl.get().getId()));
            return;
        }

        Url url = new Url(normalizedUrl);
        System.out.println(" -------------- ");
        System.out.println(" ------------- " + url.getName() + " ----------------");
        System.out.println(" ----------- ");
        UrlRepository.save(url);

        if (EXAMPLE_URL.equals(normalizedUrl)) {
            createExampleCheck(url.getId());
        }

        ctx.sessionAttribute(FLASH_TYPE, SUCCESS_TYPE);
        ctx.sessionAttribute(FLASH_MESSAGE, "Страница успешно добавлена");
        ctx.redirect(NamedRoutes.urlsPath());
    }

    public static void index(Context ctx) {
        try {
            var urls = UrlRepository.getEntities();
            var lastChecks = UrlCheckRepository.getLastChecksForAllUrls();
            var page = new UrlsPage(urls, lastChecks);

            String flashType = ctx.consumeSessionAttribute(FLASH_TYPE);
            String flashMessage = ctx.consumeSessionAttribute(FLASH_MESSAGE);
            if (flashType != null && flashMessage != null) {
                page.setFlash(flashType, flashMessage);
            }

            ctx.render("urls/index.jte", model("page", page));
        } catch (SQLException e) {
            LOGGER.error("Ошибка при загрузке списка сайтов: {}", e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при загрузке списка сайтов");
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }

    public static void show(Context ctx) {
        try {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            Url url = UrlRepository.findById(id)
                    .orElseThrow(() -> new NotFoundResponse("Страница не найдена"));

            var checks = UrlCheckRepository.getChecksByUrlId(id);
            var page = new UrlPage(url, checks);

            String flashType = ctx.consumeSessionAttribute(FLASH_TYPE);
            String flashMessage = ctx.consumeSessionAttribute(FLASH_MESSAGE);
            if (flashType != null && flashMessage != null) {
                page.setFlash(flashType, flashMessage);
            }

            ctx.render("urls/show.jte", model("page", page));
        } catch (NumberFormatException e) {
            LOGGER.warn("Некорректный ID страницы: {}", ctx.pathParam("id"));
            ctx.status(400).render("errors/400.jte");
        } catch (SQLException e) {
            LOGGER.error("Ошибка базы данных при просмотре страницы: {}", e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка базы данных");
            ctx.redirect(NamedRoutes.urlsPath());
        }
    }

    public static void createExampleCheck(Long urlId) throws SQLException {
        UrlCheck check = new UrlCheck(
                200,
                "Example Domain",
                "Example Domain",
                "This domain is for use in illustrative examples in documents.",
                urlId
        );
        UrlCheckRepository.save(check);
    }
}
