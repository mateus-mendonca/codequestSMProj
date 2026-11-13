package com.codequest.browser;

import com.codequest.config.ApplicationServices;
import com.codequest.config.TemplateConfig;
import com.codequest.dto.ResumoJogadorDTO;
import com.codequest.model.*;
import com.codequest.repository.*;
import com.codequest.routes.Routes;
import com.codequest.service.*;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.util.*;

/** Browser fixtures use only memory; they never connect to the application's database. */
public final class BrowserFixtureServer {
    public static void main(String[] args) {
        ModuloRepository modulos = new ModuloRepository() {
            public List<Modulo> findAll() {
                return List.of(new Modulo(1, 1, "Mundo de teste", "Conteúdo de teste", 2, true, 1),
                        new Modulo(2, 2, "Próximo mundo", "Teste", 2, true, 2));
            }
            public Optional<Modulo> findById(int id) { return findAll().stream().filter(m -> m.getId() == id).findFirst(); }
        };
        EtapaRepository etapas = new EtapaRepository() {
            public List<Etapa> findByModuloId(int id) {
                return List.of(new Etapa(1, id, 1, "Primeira fase", "Objetivo", "Explicação", "Exemplo", 1),
                        new Etapa(2, id, 2, "Segunda fase", "Objetivo", "Explicação", "Exemplo", 2));
            }
            public Optional<Etapa> findByModuloIdAndNumero(int id, int numero) {
                return findByModuloId(id).stream().filter(e -> e.getNumero() == numero).findFirst();
            }
        };
        ProgressoRepository progresso = new ProgressoRepository() {
            public Optional<ProgressoUsuario> findByUsuarioEModulo(String usuario, int id) {
                return Optional.of(new ProgressoUsuario(1, usuario, id, 100, 0, false, false, false, 0, 3));
            }
            public ResumoJogadorDTO obterResumoJogador(String usuario) { return new ResumoJogadorDTO(100, 2, 0); }
            public void atualizarEtapaMaxima(String usuario, int id, int numero) { }
            public ProgressoUsuario saveOrUpdate(ProgressoUsuario value) { return value; }
        };
        QuestaoRepository questoes = new QuestaoRepository() {
            public List<Questao> findByModulo(int id) {
                List<Questao> values = new ArrayList<>();
                for (int i = 1; i <= 10; i++) values.add(new Questao(i, id, "Algoritmos", "Fácil", "Questão " + i,
                        "Resposta correta", "Resposta incorreta", "Outra resposta", "Última resposta", "A", "Explicação da questão", i));
                return values;
            }
        };
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/static");
            config.fileRenderer(new JavalinThymeleaf(TemplateConfig.create()));
        });
        Routes.register(app, new ApplicationServices(new ModuloService(modulos), new EtapaService(etapas),
                new ProgressoService(progresso, modulos), new QuestaoService(questoes), new QuizService(questoes, progresso)));
        app.start(Integer.parseInt(System.getProperty("test.port", "7001")));
    }
}
