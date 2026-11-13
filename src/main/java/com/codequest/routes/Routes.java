package com.codequest.routes;

import com.codequest.controller.EtapaController;
import com.codequest.controller.ModuloController;
import com.codequest.controller.QuestaoController;
import com.codequest.controller.QuizController;
import com.codequest.service.ModuloService;
import com.codequest.service.ProgressoService;
import com.codequest.config.ApplicationServices;
import io.javalin.Javalin;
import java.util.Map;

public final class Routes {
    private Routes() { }

    public static void register(Javalin app) {
        register(app, ApplicationServices.create());
    }

    public static void register(Javalin app, ApplicationServices services) {
        ModuloService moduloService = services.modulos();
        ProgressoService progressoService = services.progresso();
        ModuloController moduloController = new ModuloController(moduloService);
        QuestaoController questaoController = new QuestaoController(services.questoes(), moduloService);
        EtapaController etapaController = new EtapaController(moduloService, services.etapas(), progressoService);
        QuizController quizController = new QuizController(moduloService, services.quiz(), progressoService, services.etapas());

        registerPublicPages(app);
        registerLearning(app, progressoService, etapaController, quizController);
        registerAdministration(app, moduloController, questaoController);
    }

    private static void registerPublicPages(Javalin app) {
        // ---------- Rotas públicas ----------

        app.get("/", ctx -> ctx.render("index.html"));

        app.get("/login", ctx -> ctx.render("login.html"));
        app.get("/cadastro", ctx -> ctx.render("login.html", Map.of("cadastro", true)));

    }

    private static void registerLearning(Javalin app, ProgressoService progressoService,
                                         EtapaController etapaController, QuizController quizController) {
        // Página de módulos e navegação de etapas
        app.get("/modulos", ctx -> {
            String usuarioId = ctx.queryParam("usuario") != null ? ctx.queryParam("usuario") : "aluno_demo";
            boolean bloqueado = ctx.queryParam("bloqueado") != null;
            ctx.render("modulos.html", Map.of(
                    "modulos", progressoService.listarModulosParaUsuario(usuarioId),
                    "resumoJogador", progressoService.obterResumoJogador(usuarioId),
                    "bloqueado", bloqueado));
        });
        app.get("/modulos/{id}", etapaController::verModuloDetails);
        app.get("/modulos/{moduloId}/etapas/{numero}", etapaController::verEtapa);

        // Rotas do Quiz Final e Gamificação
        app.get("/modulos/{moduloId}/quiz", quizController::verQuizPage);
        app.get("/api/modulos/{moduloId}/quiz/questoes", quizController::obterQuestoes);
        app.post("/api/modulos/{moduloId}/quiz/finalizar", quizController::finalizarQuiz);

    }

    private static void registerAdministration(Javalin app, ModuloController moduloController,
                                              QuestaoController questaoController) {
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

    }
}
