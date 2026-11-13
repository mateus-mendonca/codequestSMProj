package com.codequest.controller;

import com.codequest.model.Questao;
import com.codequest.service.ModuloService;
import com.codequest.service.QuestaoService;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class QuestaoController {

    private final QuestaoService service;
    private final ModuloService moduloService;

    public QuestaoController() { this(new QuestaoService(), new ModuloService()); }

    public QuestaoController(QuestaoService service, ModuloService moduloService) {
        this.service = service;
        this.moduloService = moduloService;
    }

    /** GET /admin/modulos/{moduloId}/questoes — lista questões de um módulo */
    public void listar(Context ctx) {
        int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
        Map<String, Object> model = new HashMap<>();
        model.put("modulo", moduloService.buscar(moduloId));
        model.put("questoes", service.listarPorModulo(moduloId));
        ctx.render("admin/questoes-list.html", model);
    }

    /** GET /admin/modulos/{moduloId}/questoes/novo — formulário de criação */
    public void formNovo(Context ctx) {
        int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
        Questao questao = new Questao();
        questao.setModuloId(moduloId);

        Map<String, Object> model = new HashMap<>();
        model.put("modulo", moduloService.buscar(moduloId));
        model.put("questao", questao);
        model.put("modo", "novo");
        ctx.render("admin/questao-form.html", model);
    }

    /** POST /admin/modulos/{moduloId}/questoes — cria uma questão */
    public void criar(Context ctx) {
        int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
        Questao questao = fromForm(ctx, moduloId);
        try {
            service.criar(questao);
            ctx.redirect("/admin/modulos/" + moduloId + "/questoes");
        } catch (IllegalArgumentException e) {
            Map<String, Object> model = new HashMap<>();
            model.put("modulo", moduloService.buscar(moduloId));
            model.put("questao", questao);
            model.put("modo", "novo");
            model.put("erro", e.getMessage());
            ctx.status(400).render("admin/questao-form.html", model);
        }
    }

    /** GET /admin/modulos/{moduloId}/questoes/{id}/editar — formulário de edição */
    public void formEditar(Context ctx) {
        int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
        int id = Integer.parseInt(ctx.pathParam("id"));
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("modulo", moduloService.buscar(moduloId));
            model.put("questao", service.buscar(id));
            model.put("modo", "editar");
            ctx.render("admin/questao-form.html", model);
        } catch (NoSuchElementException e) {
            ctx.status(404).result("Questão não encontrada");
        }
    }

    /** POST /admin/modulos/{moduloId}/questoes/{id} — atualiza uma questão */
    public void atualizar(Context ctx) {
        int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
        int id = Integer.parseInt(ctx.pathParam("id"));
        Questao questao = fromForm(ctx, moduloId);
        try {
            service.atualizar(id, questao);
            ctx.redirect("/admin/modulos/" + moduloId + "/questoes");
        } catch (IllegalArgumentException e) {
            questao.setId(id);
            Map<String, Object> model = new HashMap<>();
            model.put("modulo", moduloService.buscar(moduloId));
            model.put("questao", questao);
            model.put("modo", "editar");
            model.put("erro", e.getMessage());
            ctx.status(400).render("admin/questao-form.html", model);
        } catch (NoSuchElementException e) {
            ctx.status(404).result("Questão não encontrada");
        }
    }

    /** POST /admin/modulos/{moduloId}/questoes/{id}/excluir — remove uma questão */
    public void excluir(Context ctx) {
        int moduloId = Integer.parseInt(ctx.pathParam("moduloId"));
        int id = Integer.parseInt(ctx.pathParam("id"));
        service.excluir(id);
        ctx.redirect("/admin/modulos/" + moduloId + "/questoes");
    }

    private Questao fromForm(Context ctx, int moduloId) {
        Questao questao = new Questao();
        questao.setModuloId(moduloId);
        questao.setEnunciado(ctx.formParam("enunciado"));
        questao.setAlternativaA(ctx.formParam("alternativaA"));
        questao.setAlternativaB(ctx.formParam("alternativaB"));
        questao.setAlternativaC(ctx.formParam("alternativaC"));
        questao.setAlternativaD(ctx.formParam("alternativaD"));
        questao.setCorreta(ctx.formParam("correta"));
        questao.setOrdem(parseIntOrZero(ctx.formParam("ordem")));
        return questao;
    }

    private int parseIntOrZero(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
