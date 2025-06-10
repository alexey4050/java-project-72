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

public class UrlChecksController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlsController.class);
    public static void create(Context ctx) throws SQLException {
        Long urlId = ctx.pathParamAsClass("id", Long.class).get();
        var optionalUrl = UrlRepository.findById(urlId);

        if (optionalUrl.isEmpty()) {
            ctx.sessionAttribute("flashType", "danger");
            ctx.sessionAttribute("flashMessage", "Страница не найдена");
            ctx.redirect(NamedRoutes.urlsPath());
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

            ctx.sessionAttribute("flashType", "success");
            ctx.sessionAttribute("flashMessage", "Страница успешно проверена");
        } catch (Exception e) {
            LOGGER.error("Check failed for URL ID: " + urlId, e);
            ctx.sessionAttribute("flashType", "danger");
            ctx.sessionAttribute("flashMessage", "Ошибка при проверке: " + e.getMessage());
        }
        ctx.redirect(NamedRoutes.urlPath(urlId));
    }
}
