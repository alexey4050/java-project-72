package hexlet.code.controller;

import hexlet.code.dto.BasePage;
import io.javalin.http.Context;

import static io.javalin.rendering.template.TemplateUtil.model;

public class RootController {
    public static void index(Context ctx) {
        var page = new BasePage();
        if (ctx.sessionAttribute("flashType") != null) {
            page.setFlash(
                    ctx.sessionAttribute("flashType"),
                    ctx.sessionAttribute("flashMessage")
            );
            ctx.sessionAttribute("flashType", null);
            ctx.sessionAttribute("flashMessage", null);
        }
        ctx.render("index.jte", model("page", page));
    }
}
