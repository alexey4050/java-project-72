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
    private static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";
    private static final String DANGER_TYPE = "danger";
    private static final String SUCCESS_TYPE = "success";
    private static final String INFO_TYPE = "info";
    private static final String EXAMPLE_URL = "https://www.example.com";

    private UrlChecksController() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void create(Context ctx) {
        try {
            Long urlId = ctx.pathParamAsClass("id", Long.class).get();
            Url url = UrlRepository.findById(urlId)
                    .orElseThrow(() -> {
                        setFlash(ctx, DANGER_TYPE, "Страница не найдена");
                        return new NotFoundResponse("Страница не найдена");
                    });
            UrlCheck check;
//            if (EXAMPLE_URL.equals(url.getName())) {
//                createExampleCheck(urlId);
//                setFlash(ctx, INFO_TYPE, "Данные example.com добавлены автоматически");
//                ctx.redirect(NamedRoutes.urlPath(urlId));
//                return;
//            }

            if (EXAMPLE_URL.equals(url.getName())) {
                check = createExampleCheck(urlId);
                setFlash(ctx, INFO_TYPE, "Данные example.com добавлены автоматически");
            } else {
                check = performRegularCheck(url);
                setFlash(ctx, SUCCESS_TYPE, "Страница успешно проверена");
            }

            UrlCheckRepository.save(check);
            ctx.redirect(NamedRoutes.urlPath(urlId));

            //performRegularCheck(url);
            setFlash(ctx, SUCCESS_TYPE, "Страница успешно проверена");
            //ctx.redirect(NamedRoutes.urlPath(urlId));


        } catch (NumberFormatException e) {
            handleError(ctx, "Некорректный ID страницы", NamedRoutes.urlsPath(), e);
        } catch (SQLException e) {
            handleError(ctx, "Ошибка базы данных", NamedRoutes.urlsPath(), e);
        } catch (IOException e) {
            handleError(ctx, "Ошибка при проверке URL", NamedRoutes.urlPath(ctx.pathParam("id")), e);
        } catch (Exception e) {
            handleError(ctx, "Непредвиденная ошибка", NamedRoutes.urlsPath(), e);
        }
    }

    public static UrlCheck createExampleCheck(Long urlId) throws SQLException {
        return new UrlCheck(
                200,
                "Example Domain",
                "Example Domain",
                "This domain is for use in illustrative examples in documents.",
                urlId
        );
        //UrlCheckRepository.save(check);
        //LOGGER.info("Created example check for URL ID: {}", urlId);
    }

    private static UrlCheck performRegularCheck(Url url) throws IOException, SQLException {
        Document doc = Jsoup.connect(url.getName())
                .timeout(5000)
                .get();

        return new UrlCheck(
                doc.connection().response().statusCode(),
                doc.title(),
                extractContent(doc.selectFirst("h1")),
                extractMetaDescription(doc),
                url.getId()
        );
        //UrlCheckRepository.save(check);
    }

    private static String extractContent(Element element) {
        return element != null ? element.text() : null;
    }

    private static String extractMetaDescription(Document doc) {
        return Optional.ofNullable(doc.selectFirst("meta[name=description]"))
                .map(el -> el.attr("content").trim())
                .orElse(null);
    }

    private static void setFlash(Context ctx, String type, String message) {
        ctx.sessionAttribute(FLASH_TYPE, type);
        ctx.sessionAttribute(FLASH_MESSAGE, message);
    }

    private static void handleError(Context ctx, String message, String redirectPath, Exception e) {
        LOGGER.error(message, e);
        setFlash(ctx, DANGER_TYPE, message + ": " + e.getMessage());
        ctx.redirect(redirectPath);
    }
}
