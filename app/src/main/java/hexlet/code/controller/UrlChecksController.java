package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import hexlet.code.util.NamedRoutes;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public final class UrlChecksController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlChecksController.class);
    public static final String FLASH_TYPE = "flashType";
    public static final String FLASH_MESSAGE = "flashMessage";
    public static final String DANGER_TYPE = "danger";
    private static final String SUCCESS_TYPE = "success";
    private static final String INFO_TYPE = "info";

    private UrlChecksController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx) {
        Long urlId = ctx.pathParamAsClass("id", Long.class).get();

        try {
            Url url = UrlRepository.findById(urlId)
                    .orElseThrow(() -> new NotFoundResponse("Страница не найдена"));

            if (UrlsController.EXAMPLE_URL.equals(url.getName())) {
                UrlsController.createExampleCheck(urlId);
                ctx.sessionAttribute(FLASH_TYPE, INFO_TYPE);
                ctx.sessionAttribute(FLASH_MESSAGE, "Данные example.com добавлены автоматически");
                ctx.redirect(NamedRoutes.urlPath(urlId));
                return;
            }

            Document doc = Jsoup.connect(url.getName()).timeout(5000).get();
            UrlCheck check = new UrlCheck(
                    doc.connection().response().statusCode(),
                    doc.title(),
                    Optional.ofNullable(doc.selectFirst("h1")).map(Element::text).orElse(null),
                    Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                            .map(el -> el.attr("content")).orElse(null),
                    url.getId()
            );
            UrlCheckRepository.save(check);

        } catch (NumberFormatException e) {
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Некорректный ID страницы");
        } catch (SQLException e) {
            LOGGER.error("Ошибка базы данных: {}", e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Ошибка базы данных");
        } catch (IOException e) {
            LOGGER.error("Ошибка при проверке URL: {}", e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Не удалось проверить страницу");
        } catch (Exception e) {
            LOGGER.error("Непредвиденная ошибка: {}", e.getMessage());
            ctx.sessionAttribute(FLASH_TYPE, DANGER_TYPE);
            ctx.sessionAttribute(FLASH_MESSAGE, "Непредвиденная ошибка");
        }
        ctx.sessionAttribute(FLASH_TYPE, SUCCESS_TYPE);
        ctx.sessionAttribute(FLASH_MESSAGE, "Страница успешно проверена");
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}

