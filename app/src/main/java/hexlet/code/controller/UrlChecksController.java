package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public final class UrlChecksController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlChecksController.class);
    private static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String DANGER_TYPE = "danger";
    private static final String SUCCESS_TYPE = "success";

    private UrlChecksController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx) {
        try {
            Long urlId = ctx.pathParamAsClass("id", Long.class).get();
            Optional<Url> urlOptional = UrlRepository.findById(urlId);

            if (urlOptional.isEmpty()) {
                ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
                ctx.sessionAttribute(FLASH_MESSAGE, "Страница не найдена");
                ctx.redirect(NamedRoutes.urlsPath());
                return;
            }
            Url url = urlOptional.get();
            String normalizedUrl = url.getName();

            var doc = Jsoup.connect(normalizedUrl).get();

            UrlCheck urlCheck = new UrlCheck(
                    doc.connection().response().statusCode(),
                    doc.title(),
                    doc.selectFirst("h1") != null ? doc.selectFirst("h1").text() : "",
                    doc.selectFirst("meta[name=description]") != null
                            ? doc.selectFirst("meta[name=description]").attr("content") : "",
                    urlId
            );

            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute(FLASH_TYPE, SUCCESS_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Страница успешно проверена");
            ctx.redirect(NamedRoutes.urlPath(String.valueOf(urlId)));
        } catch (NumberFormatException e) {
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Некорректный ID страницы");
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (SQLException e) {
            LOGGER.error("Database error", e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при сохранении проверки");
            ctx.redirect(NamedRoutes.urlsPath());
        } catch (IOException e) {
            LOGGER.error("URL check error", e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при проверке URL: " + e.getMessage());
            ctx.redirect(NamedRoutes.urlPath(ctx.pathParam("id")));
        }
    }
}
