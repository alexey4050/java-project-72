package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
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
    private static final String SUCCESS_TYPE = "success";

    private UrlChecksController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx) throws SQLException {
        Long urlId = ctx.pathParamAsClass("id", Long.class).get();
        try {
            var url = UrlRepository.findById(urlId)
                    .orElseThrow(() -> {
                        ctx.sessionAttribute("flashType", "danger");
                        ctx.sessionAttribute("flashMessage", "Страница не найдена");
                        ctx.status(404);
                        return new NotFoundResponse("URL не найден");
                    });

            HttpResponse<String> response = Unirest.get(url.getName())
                    .connectTimeout(5000)
                    .socketTimeout(10000)
                    .asString();

            if (response.getStatus() != 200) {
                throw new RuntimeException("Сервер вернул статус: " + response.getStatus());
            }

            var doc = Jsoup.parse(response.getBody());
            String title = doc.title()!= null ? doc.title() : "";
            String h1 = Optional.ofNullable(doc.selectFirst("h1"))
                    .map(Element::text)
                    .orElse("");

            String description = Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                    .map(el -> el.attr("content"))
                    .orElse("");

            var urlCheck = new UrlCheck(
                    response.getStatus(),
                    title,
                    h1,
                    description,
                    urlId
            );

            UrlCheckRepository.save(urlCheck);

            ctx.sessionAttribute(FLASH_TYPE, SUCCESS_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Страница успешно проверена");
            ctx.status(200);
        } catch (Exception e) {
            LOGGER.error("Check failed for URL ID: " + urlId, e);
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка при проверке: " + e.getMessage());
            ctx.status(500).result("Check failed: " + e.getMessage());
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
