package com.codequest;

import com.codequest.config.Database;
import com.codequest.controller.EtapaController;
import com.codequest.controller.ModuloController;
import com.codequest.controller.QuestaoController;
import com.codequest.controller.QuizController;
import com.codequest.service.ModuloService;
import com.codequest.service.ProgressoService;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

public class App {

    public static void main(String[] args) {

        Database.init();

        ModuloController moduloController = new ModuloController();
        QuestaoController questaoController = new QuestaoController();
        EtapaController etapaController = new EtapaController();
        QuizController quizController = new QuizController();
        ModuloService moduloService = new ModuloService();

        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver resolverWithNoSuffix = new ClassLoaderTemplateResolver();
        resolverWithNoSuffix.setPrefix("templates/");
        resolverWithNoSuffix.setSuffix("");
        resolverWithNoSuffix.setCharacterEncoding("UTF-8");
        resolverWithNoSuffix.setOrder(1);
        resolverWithNoSuffix.setCheckExistence(true);

        ClassLoaderTemplateResolver resolverWithHtmlSuffix = new ClassLoaderTemplateResolver();
        resolverWithHtmlSuffix.setPrefix("templates/");
        resolverWithHtmlSuffix.setSuffix(".html");
        resolverWithHtmlSuffix.setCharacterEncoding("UTF-8");
        resolverWithHtmlSuffix.setOrder(2);
        resolverWithHtmlSuffix.setCheckExistence(true);

        templateEngine.addTemplateResolver(resolverWithNoSuffix);
        templateEngine.addTemplateResolver(resolverWithHtmlSuffix);

        int port = 7000;
        try {
            port = Integer.parseInt(Database.env("PORT", "7000"));
        } catch (NumberFormatException ignored) {
        }

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static");
            config.fileRenderer(new JavalinThymeleaf(templateEngine));
        }).start(port);

        // ---------- Rotas públicas ----------

        app.get("/", ctx -> ctx.render("index.html"));

        app.get("/login", ctx -> ctx.render("login.html"));
        app.get("/cadastro", ctx -> ctx.render("login.html", Map.of("cadastro", true)));

        // Página de módulos e navegação de etapas
        ProgressoService progressoService = new ProgressoService();
        app.get("/modulos", ctx -> {
            String usuarioId = ctx.queryParam("usuario") != null ? ctx.queryParam("usuario") : "aluno_demo";
            ctx.render("modulos.html", Map.of(
                    "modulos", moduloService.listar(),
                    "resumoJogador", progressoService.obterResumoJogador(usuarioId)));
        });
        app.get("/modulos/{id}", etapaController::verModuloDetails);
        app.get("/modulos/{moduloId}/etapas/{numero}", etapaController::verEtapa);

        // Rotas do Quiz Final e Gamificação
        app.get("/modulos/{moduloId}/quiz", quizController::verQuizPage);
        app.get("/api/modulos/{moduloId}/quiz/questoes", quizController::obterQuestoes);
        app.post("/api/modulos/{moduloId}/quiz/finalizar", quizController::finalizarQuiz);

        // ---------- Rotas de administração (CRUD) ----------

        app.get("/admin/modulos", moduloController::listar);
        app.get("/admin/modulos/novo", moduloController::formNovo);
        app.post("/admin/modulos", moduloController::criar);
        app.get("/admin/modulos/{id}/editar", moduloController::formEditar);
        app.post("/admin/modulos/{id}", moduloController::atualizar);
        app.post("/admin/modulos/{id}/excluir", moduloController::excluir);

        app.get("/admin/modulos/{moduloId}/questoes", questaoController::listar);
        app.get("/admin/modulos/{moduloId}/questoes/novo", questaoController::formNovo);
        app.post("/admin/modulos/{moduloId}/questoes", questaoController::criar);
        app.get("/admin/modulos/{moduloId}/questoes/{id}/editar", questaoController::formEditar);
        app.post("/admin/modulos/{moduloId}/questoes/{id}", questaoController::atualizar);
        app.post("/admin/modulos/{moduloId}/questoes/{id}/excluir", questaoController::excluir);

        System.out.println("CodeQuest rodando na porta " + port + " (http://localhost:" + port + ")");
        System.out.println("Admin de módulos em http://localhost:" + port + "/admin/modulos");
    }
}
