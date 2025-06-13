package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

public final class UrlChecksController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlChecksController.class);
    private static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String DANGER_TYPE = "danger";
    private static final String NOT_FOUND_MESSAGE = "Страница не найдена";
    private static final String SUCCESS_TYPE = "success";

    private UrlChecksController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx) throws SQLException {
        Long urlId = ctx.pathParamAsClass("id", Long.class).get();
        var optionalUrl = UrlRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, NOT_FOUND_MESSAGE);
            ctx.redirect(NamedRoutes.urlsPath());
            ctx.status(404);
            ctx.result("URL не найден");
            return;
        }

        try {
            Url url = optionalUrl.get();
            var response = Unirest.get(url.getName()).asString();

            var doc = Jsoup.parse(response.getBody());
            var urlCheck = new UrlCheck(
                    response.getStatus(),
                    doc.title(),
                    Optional.ofNullable(doc.selectFirst("h1")).map(Element::text).orElse(""),
                    Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                            .map(element -> element.attr("content"))
                            .orElse(""),

                    urlId
            );

            UrlCheckRepository.save(urlCheck);
            ctx.status(200);

            ctx.sessionAttribute(FLASH_TYPE, SUCCESS_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Страница успешно проверена");
        } catch (Exception e) {
            LOGGER.error("Check failed for URL ID: " + urlId, e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при проверке: " + e.getMessage());
            ctx.status(500).result("Check failed: " + e.getMessage());
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
