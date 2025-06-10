package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import io.javalin.http.Context;

import static io.javalin.rendering.template.TemplateUtil.model;

public class RootController {
    private static final String FLASH_TYPE = "flashType";
    private static final String FLASH_MESSAGE = "flashMessage";

    private RootController() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void index(Context ctx) {

        var page = new BasePage();
        String flashType = ctx.attribute(FLASH_TYPE);
        String flashMessage = ctx.attribute(FLASH_MESSAGE);

        if (flashType != null && flashMessage != null) {
            page.setFlash(flashType, flashMessage);
        }
        ctx.render("index.jte", model("page", page));
    }
}
