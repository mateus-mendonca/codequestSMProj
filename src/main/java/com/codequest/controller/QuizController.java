package com.codequest.controller;

import com.codequest.model.Modulo;
import com.codequest.dto.QuizQuestionDTO;
import com.codequest.dto.QuizResultDTO;
import com.codequest.dto.QuizSubmission;
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
            String usuarioId = ctx.queryParam("usuario") != null ? ctx.queryParam("usuario") : "aluno_demo";

            if (!progressoService.podeAcessarModulo(usuarioId, moduloId)) {
                ctx.redirect("/modulos?bloqueado=true");
                return;
            }

            Modulo modulo = moduloService.buscar(moduloId);

            int totalEtapas = etapaService.listarPorModulo(moduloId).size();
            if (!progressoService.podeAcessarQuiz(usuarioId, moduloId, totalEtapas)) {
                ctx.redirect("/modulos/" + moduloId + "?quizBloqueado=true");
                return;
            }

            com.codequest.dto.ResumoJogadorDTO resumoJogador = progressoService.obterResumoJogador(usuarioId);
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
            String usuarioId = ctx.queryParam("usuario") != null ? ctx.queryParam("usuario") : "aluno_demo";

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

    public void finalizarQuiz(Context ctx) {
        try {
            int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
            QuizSubmission submission = ctx.bodyAsClass(QuizSubmission.class);

            QuizResultDTO resultado = quizService.processarResultado(
                    moduloId,
                    submission.usuarioId != null ? submission.usuarioId : "aluno_demo",
                    submission.respostas,
                    submission.vidasRestantes
            );

            ctx.json(resultado);
        } catch (Exception e) {
            ctx.status(400).result("Erro ao processar quiz: " + e.getMessage());
        }
    }
}
