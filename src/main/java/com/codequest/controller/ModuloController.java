package com.codequest.controller;

import com.codequest.model.Modulo;
import com.codequest.service.ModuloService;
import io.javalin.http.Context;

import java.util.Map;
import java.util.NoSuchElementException;

public class ModuloController {

    private final ModuloService service;

    public ModuloController() { this(new ModuloService()); }

    public ModuloController(ModuloService service) { this.service = service; }

    /** GET /admin/modulos — lista todos os módulos */
    public void listar(Context ctx) {
        ctx.render("admin/modulos-list.html", Map.of("modulos", service.listar()));
    }

    /** GET /admin/modulos/novo — formulário de criação */
    public void formNovo(Context ctx) {
        ctx.render("admin/modulo-form.html", Map.of("modulo", new Modulo(), "modo", "novo"));
    }

    /** POST /admin/modulos — cria um módulo */
    public void criar(Context ctx) {
        Modulo modulo = fromForm(ctx);
        try {
            service.criar(modulo);
            ctx.redirect("/admin/modulos");
        } catch (IllegalArgumentException e) {
            ctx.status(400).render("admin/modulo-form.html",
                    Map.of("modulo", modulo, "modo", "novo", "erro", e.getMessage()));
        }
    }

    /** GET /admin/modulos/{id}/editar — formulário de edição */
    public void formEditar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        try {
            Modulo modulo = service.buscar(id);
            ctx.render("admin/modulo-form.html", Map.of("modulo", modulo, "modo", "editar"));
        } catch (NoSuchElementException e) {
            ctx.status(404).result("Módulo não encontrado");
        }
    }

    /** POST /admin/modulos/{id} — atualiza um módulo */
    public void atualizar(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        Modulo modulo = fromForm(ctx);
        try {
            service.atualizar(id, modulo);
            ctx.redirect("/admin/modulos");
        } catch (IllegalArgumentException e) {
            modulo.setId(id);
            ctx.status(400).render("admin/modulo-form.html",
                    Map.of("modulo", modulo, "modo", "editar", "erro", e.getMessage()));
        } catch (NoSuchElementException e) {
            ctx.status(404).result("Módulo não encontrado");
        }
    }

    /** POST /admin/modulos/{id}/excluir — remove um módulo */
    public void excluir(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));
        service.excluir(id);
        ctx.redirect("/admin/modulos");
    }

    private Modulo fromForm(Context ctx) {
        Modulo modulo = new Modulo();
        modulo.setNumero(Integer.parseInt(ctx.formParam("numero")));
        modulo.setTitulo(ctx.formParam("titulo"));
        modulo.setDescricao(ctx.formParam("descricao"));
        modulo.setTopicos(parseIntOrZero(ctx.formParam("topicos")));
        modulo.setOrdem(parseIntOrZero(ctx.formParam("ordem")));
        modulo.setDisponivel("on".equals(ctx.formParam("disponivel")));
        return modulo;
    }

    private int parseIntOrZero(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
