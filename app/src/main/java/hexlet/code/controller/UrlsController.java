package hexlet.code.controller;

import hexlet.code.dto.BasePage;
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
import java.util.HashMap;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public final class UrlsController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsController.class);
    public static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String DANGER_TYPE = "danger";
    private static final String SUCCESS_TYPE = "success";
    private static final String INFO_TYPE = "info";
    private static final String EXAMPLE_URL = "https://www.example.com";

    private UrlsController() {
        throw new UnsupportedOperationException("Это служебный класс, создание экземпляров запрещено");
    }

    public static void create(Context ctx) {
        String urlString = ctx.formParam("url");

        if (urlString == null || urlString.isBlank()) {
            setFlashAndRedirect(ctx, DANGER_TYPE, "URL не может быть пустым", NamedRoutes.rootPath());
            return;
        }

        try {
            String normalizedUrl = UrlUtil.normalizeUrl(urlString);
            var existingUrl = UrlRepository.findByName(normalizedUrl);

            if (existingUrl.isPresent()) {
                setFlashAndRedirect(ctx, INFO_TYPE, "Страница уже существует",
                        NamedRoutes.urlPath(existingUrl.get().getId()));
                return;
            }

            Url url = new Url(normalizedUrl);
            UrlRepository.save(url);

            if (EXAMPLE_URL.equals(normalizedUrl)) {
                UrlChecksController.createExampleCheck(url.getId());
                setFlashAndRedirect(ctx, INFO_TYPE, "Example.com добавлен с тестовыми данными",
                        NamedRoutes.urlsPath());

            } else {
                setFlashAndRedirect(ctx, SUCCESS_TYPE, "Страница успешно добавлена",
                        NamedRoutes.urlsPath());
            }

        } catch (MalformedURLException | URISyntaxException e) {
            handleError(ctx, "Некорректный URL", NamedRoutes.rootPath(), e);
        } catch (SQLException e) {
            handleError(ctx, "Ошибка базы данных", NamedRoutes.rootPath(), e);
        } catch (Exception e) {
            handleError(ctx, "Непредвиденная ошибка", NamedRoutes.rootPath(), e);
        }
    }

    public static void index(Context ctx) {
        try {
            var urls = UrlRepository.getEntities();
            Map<Long, UrlCheck> lastChecks = new HashMap<>();

            for (Url url : urls) {
                var lastCheck = UrlCheckRepository.getLastCheckByUrlId(url.getId());
                lastCheck.ifPresent(check -> lastChecks.put(url.getId(), check));
            }

            var page = new UrlsPage(urls, lastChecks);
            transferFlashAttributes(ctx, page);

            ctx.render("urls/index.jte", model("page", page));
        } catch (SQLException e) {
            handleError(ctx, "Ошибка при загрузке списка сайтов", NamedRoutes.urlsPath(), e);
        }
    }

    public static void show(Context ctx) {
        try {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            Url url = UrlRepository.findById(id)
                    .orElseThrow(() -> new NotFoundResponse("Страница не найдена"));

            var checks = UrlCheckRepository.getChecksByUrlId(id);
            var page = new UrlPage(url, checks);
            transferFlashAttributes(ctx, page);

            ctx.render("urls/show.jte", model("page", page));
        } catch (NumberFormatException e) {
            ctx.status(400).render("errors/400.jte");
        } catch (SQLException e) {
            handleError(ctx, "Ошибка базы данных", NamedRoutes.urlsPath(), e);
        }
    }

    private static void transferFlashAttributes(Context ctx, BasePage page) {
        String flashType = ctx.consumeSessionAttribute(FLASH_TYPE);
        String flashMessage = ctx.consumeSessionAttribute(FLASH_MESSAGE);
        if (flashType != null && flashMessage != null) {
            page.setFlash(flashType, flashMessage);
        }
    }

    private static void setFlashAndRedirect(Context ctx, String type, String message, String path) {
        ctx.sessionAttribute(FLASH_TYPE, type);
        ctx.sessionAttribute(FLASH_MESSAGE, message);
        ctx.redirect(path);
    }

    private static void handleError(Context ctx, String message, String redirectPath, Exception e) {
        LOGGER.error(message, e);
        setFlashAndRedirect(ctx, DANGER_TYPE, message + ": " + e.getMessage(), redirectPath);
    }
}
