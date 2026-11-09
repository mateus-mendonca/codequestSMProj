package com.codequest.controller;

import com.codequest.model.Modulo;
import com.codequest.model.QuizQuestionDTO;
import com.codequest.model.QuizResultDTO;
import com.codequest.config.UsuarioContexto;
import com.codequest.service.EtapaService;
import com.codequest.service.ModuloService;
import com.codequest.service.ProgressoService;
import com.codequest.service.QuizService;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class QuizController {

    private final ModuloService moduloService;
    private final QuizService quizService;
    private final ProgressoService progressoService;
    private final EtapaService etapaService;

    public QuizController() {
        this.moduloService = new ModuloService();
        this.quizService = new QuizService();
        this.progressoService = new ProgressoService();
        this.etapaService = new EtapaService();
    }

    public QuizController(ModuloService moduloService, QuizService quizService, ProgressoService progressoService, EtapaService etapaService) {
        this.moduloService = moduloService;
        this.quizService = quizService;
        this.progressoService = progressoService;
        this.etapaService = etapaService;
    }

    public void verQuizPage(Context ctx) {
        try {
            int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
            Modulo modulo = moduloService.buscar(moduloId);
            String usuarioId = UsuarioContexto.obterId(ctx);

            if (!progressoService.podeAcessarModulo(usuarioId, moduloId, moduloService.listar())) {
                ctx.redirect("/modulos?moduloBloqueado=true");
                return;
            }

            int totalEtapas = etapaService.listarPorModulo(moduloId).size();
            if (!progressoService.podeAcessarQuiz(usuarioId, moduloId, totalEtapas)) {
                ctx.redirect("/modulos/" + moduloId + "?quizBloqueado=true");
                return;
            }

            com.codequest.model.ResumoJogadorDTO resumoJogador = progressoService.obterResumoJogador(usuarioId);
            ctx.render("quiz.html", Map.of(
                    "modulo", modulo,
                    "resumoJogador", resumoJogador
            ));
        } catch (NoSuchElementException | NumberFormatException e) {
            ctx.status(404).result("Módulo não encontrado");
        }
    }

    public void obterQuestoes(Context ctx) {
        try {
            int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
            String usuarioId = UsuarioContexto.obterId(ctx);

            moduloService.buscar(moduloId);
            if (!progressoService.podeAcessarModulo(usuarioId, moduloId, moduloService.listar())) {
                ctx.status(403).result("Este módulo ainda está bloqueado. Conclua o módulo anterior primeiro.");
                return;
            }

            int totalEtapas = etapaService.listarPorModulo(moduloId).size();
            if (!progressoService.podeAcessarQuiz(usuarioId, moduloId, totalEtapas)) {
                ctx.status(403).result("O Quiz deste módulo ainda está bloqueado. Complete todas as etapas primeiro.");
                return;
            }

            List<QuizQuestionDTO> questoes = quizService.gerarQuizParaModulo(moduloId);
            ctx.json(questoes);
        } catch (Exception e) {
            ctx.status(500).result("Erro ao gerar quiz: " + e.getMessage());
        }
    }

    public static class QuizSubmission {
        public Map<Integer, String> respostas = new HashMap<>();
        public int vidasRestantes = 3;
    }

    public void finalizarQuiz(Context ctx) {
        try {
            int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
            QuizSubmission submission = ctx.bodyAsClass(QuizSubmission.class);
            String usuarioId = UsuarioContexto.obterId(ctx);

            moduloService.buscar(moduloId);
            if (!progressoService.podeAcessarModulo(usuarioId, moduloId, moduloService.listar())) {
                ctx.status(403).result("Este módulo ainda está bloqueado. Conclua o módulo anterior primeiro.");
                return;
            }

            QuizResultDTO resultado = quizService.processarResultado(
                    moduloId,
                    usuarioId,
                    submission.respostas,
                    submission.vidasRestantes
            );

            ctx.json(resultado);
        } catch (Exception e) {
            ctx.status(400).result("Erro ao processar quiz: " + e.getMessage());
        }
    }
}
