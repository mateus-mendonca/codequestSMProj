package com.codequest.controller;

import com.codequest.model.Etapa;
import com.codequest.model.Modulo;
import com.codequest.service.EtapaService;
import com.codequest.service.ModuloService;
import com.codequest.service.ProgressoService;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class EtapaController {

    private final ModuloService moduloService;
    private final EtapaService etapaService;
    private final ProgressoService progressoService;

    public EtapaController() {
        this.moduloService = new ModuloService();
        this.etapaService = new EtapaService();
        this.progressoService = new ProgressoService();
    }

    public EtapaController(ModuloService moduloService, EtapaService etapaService, ProgressoService progressoService) {
        this.moduloService = moduloService;
        this.etapaService = etapaService;
        this.progressoService = progressoService;
    }

    public void verModuloDetails(Context ctx) {
        try {
            int moduloId = Integer.parseInt(ctx.pathParam("id"));
            String usuarioId = ctx.queryParam("usuario") != null ? ctx.queryParam("usuario") : "aluno_demo";

            if (!progressoService.podeAcessarModulo(usuarioId, moduloId)) {
                ctx.redirect("/modulos?bloqueado=true");
                return;
            }

            Modulo modulo = moduloService.buscar(moduloId);
            List<Etapa> etapas = etapaService.listarPorModulo(moduloId);
            int etapaMaxima = progressoService.obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
            boolean quizDesbloqueado = progressoService.podeAcessarQuiz(usuarioId, moduloId, etapas.size());
            com.codequest.dto.ResumoJogadorDTO resumoJogador = progressoService.obterResumoJogador(usuarioId);

            Map<String, Object> model = new HashMap<>();
            model.put("modulo", modulo);
            model.put("etapas", etapas);
            model.put("etapaMaxima", etapaMaxima);
            model.put("quizDesbloqueado", quizDesbloqueado);
            model.put("resumoJogador", resumoJogador);

            ctx.render("modulo-detalhes.html", model);
        } catch (NoSuchElementException | NumberFormatException e) {
            ctx.status(404).result("Módulo não encontrado");
        }
    }

    public void verEtapa(Context ctx) {
        try {
            int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
            int numero = Integer.parseInt(ctx.pathParam("numero"));
            String usuarioId = ctx.queryParam("usuario") != null ? ctx.queryParam("usuario") : "aluno_demo";

            if (!progressoService.podeAcessarModulo(usuarioId, moduloId)) {
                ctx.redirect("/modulos?bloqueado=true");
                return;
            }

            Modulo modulo = moduloService.buscar(moduloId);
            List<Etapa> todasEtapas = etapaService.listarPorModulo(moduloId);

            // Validação de segurança: se a etapa solicitada estiver bloqueada, redireciona
            if (!progressoService.podeAcessarEtapa(usuarioId, moduloId, numero)) {
                int etapaPermitida = progressoService.obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
                ctx.redirect("/modulos/" + moduloId + "/etapas/" + etapaPermitida + "?bloqueado=true");
                return;
            }

            Etapa etapa = etapaService.buscarPorModuloENumero(moduloId, numero);

            // Ao acessar a etapa com sucesso, registra avanço para liberar a próxima
            progressoService.registrarAvancoEtapa(usuarioId, moduloId, numero, todasEtapas.size());

            int etapaMaximaAtual = progressoService.obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
            boolean quizDesbloqueado = progressoService.podeAcessarQuiz(usuarioId, moduloId, todasEtapas.size());
            com.codequest.dto.ResumoJogadorDTO resumoJogador = progressoService.obterResumoJogador(usuarioId);

            Map<String, Object> model = new HashMap<>();
            model.put("modulo", modulo);
            model.put("etapa", etapa);
            model.put("totalEtapas", todasEtapas.size());
            model.put("etapaAnterior", (numero > 1) ? numero - 1 : null);
            model.put("proximaEtapa", (numero < todasEtapas.size()) ? numero + 1 : null);
            model.put("etapaMaxima", etapaMaximaAtual);
            model.put("quizDesbloqueado", quizDesbloqueado);
            model.put("resumoJogador", resumoJogador);

            ctx.render("etapa-detalhes.html", model);
        } catch (NoSuchElementException | NumberFormatException e) {
            ctx.status(404).result("Etapa não encontrada");
        }
    }
}
