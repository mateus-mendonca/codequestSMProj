package com.codequest;

import com.codequest.config.Database;
import com.codequest.config.TemplateConfig;
import com.codequest.routes.Routes;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;

public class App {

    public static void main(String[] args) {

        Database.init();

        int port = 7000;
        try {
            port = Integer.parseInt(Database.env("PORT", "7000"));
        } catch (NumberFormatException ignored) {
        }

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static");
            config.fileRenderer(new JavalinThymeleaf(TemplateConfig.create()));
        }).start(port);

        Routes.register(app);

        System.out.println("CodeQuest rodando na porta " + port + " (http://localhost:" + port + ")");
        System.out.println("Admin de módulos em http://localhost:" + port + "/admin/modulos");
    }
}
