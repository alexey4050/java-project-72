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

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public final class UrlChecksController {
    private static final String SUCCESS_TYPE = "success";
    private static final String INFO_TYPE = "info";

    private UrlChecksController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx) {
        Long urlId;
        try {
            urlId = ctx.pathParamAsClass("id", Long.class).get();
        } catch (NumberFormatException e) {
            UrlsController.handleError(ctx, "Некорректный ID страницы", NamedRoutes.urlsPath(), e);
            return;
        }

        try {
            Url url = UrlRepository.findById(urlId)
                    .orElseThrow(() -> new NotFoundResponse("Страница не найдена"));

            if (UrlsController.EXAMPLE_URL.equals(url.getName())) {
                UrlsController.createExampleCheck(urlId);
                UrlsController.setFlashAndRedirect(ctx, INFO_TYPE,
                        "Данные example.com добавлены автоматически", NamedRoutes.urlPath(urlId));
                return;
            }

            Document doc = Jsoup.connect(url.getName()).timeout(5000).get();
            UrlCheck check = new UrlCheck(
                    doc.connection().response().statusCode(),
                    doc.title(),
                    Optional.ofNullable(doc.selectFirst("h1")).map(Element::text).orElse(null),
                    Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                            .map(el -> el.attr("content").trim()).orElse(null),
                    url.getId()
            );
            UrlCheckRepository.save(check);

        } catch (SQLException e) {
            UrlsController.handleError(ctx, "Ошибка базы данных", NamedRoutes.urlsPath(), e);
        } catch (IOException e) {
            UrlsController.handleError(ctx, "Ошибка при проверке URL",
                    NamedRoutes.urlPath(urlId.toString()), e);
        } catch (Exception e) {
            UrlsController.handleError(ctx, "Непредвиденная ошибка", NamedRoutes.urlsPath(), e);
        }
        UrlsController.setFlashAndRedirect(ctx, SUCCESS_TYPE,
                "Страница успешно проверена", NamedRoutes.urlPath(urlId));
    }
}

