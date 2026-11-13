package com.codequest.controller;

import com.codequest.model.Modulo;
import com.codequest.model.ProgressoUsuario;
import com.codequest.repository.EtapaRepository;
import com.codequest.repository.ModuloRepository;
import com.codequest.repository.ProgressoRepository;
import com.codequest.service.EtapaService;
import com.codequest.service.ModuloService;
import com.codequest.service.ProgressoService;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class EtapaControllerTest {

    private EtapaController controller;
    private MockProgressoRepository progressoRepo;
    private MockModuloRepository moduloRepo;

    @BeforeEach
    public void setUp() {
        progressoRepo = new MockProgressoRepository();
        moduloRepo = new MockModuloRepository();
        ModuloService moduloService = new ModuloService(moduloRepo);
        EtapaService etapaService = new EtapaService(new EtapaRepository());
        ProgressoService progressoService = new ProgressoService(progressoRepo, moduloRepo);

        controller = new EtapaController(moduloService, etapaService, progressoService);
    }

    @Test
    @DisplayName("Deve redirecionar para /modulos?bloqueado=true quando o usuário tentar acessar módulo 2 bloqueado")
    public void testAcessoModuloBloqueadoRedireciona() {
        ContextMockHandler handler = new ContextMockHandler(Map.of("id", "2"), Map.of("usuario", "aluno_novo"));
        Context ctx = createContextProxy(handler);

        controller.verModuloDetails(ctx);

        assertEquals("/modulos?bloqueado=true", handler.redirectLocation);
    }

    @Test
    @DisplayName("Deve redirecionar para /modulos?bloqueado=true quando o usuário tentar acessar etapa de módulo 2 bloqueado")
    public void testAcessoEtapaModuloBloqueadoRedireciona() {
        ContextMockHandler handler = new ContextMockHandler(
                Map.of("moduloId", "2", "numero", "1"),
                Map.of("usuario", "aluno_novo")
        );
        Context ctx = createContextProxy(handler);

        controller.verEtapa(ctx);

        assertEquals("/modulos?bloqueado=true", handler.redirectLocation);
    }

    private Context createContextProxy(ContextMockHandler handler) {
        return (Context) Proxy.newProxyInstance(
                Context.class.getClassLoader(),
                new Class<?>[]{Context.class},
                handler
        );
    }

    private static class ContextMockHandler implements java.lang.reflect.InvocationHandler {
        private final Map<String, String> pathParams;
        private final Map<String, String> queryParams;
        public String redirectLocation;
        public String renderedTemplate;

        public ContextMockHandler(Map<String, String> pathParams, Map<String, String> queryParams) {
            this.pathParams = pathParams;
            this.queryParams = queryParams;
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
            String name = method.getName();
            if ("pathParam".equals(name)) {
                return pathParams.get(args[0]);
            }
            if ("queryParam".equals(name)) {
                return queryParams.get(args[0]);
            }
            if ("redirect".equals(name)) {
                this.redirectLocation = (String) args[0];
                return proxy;
            }
            if ("render".equals(name)) {
                this.renderedTemplate = (String) args[0];
                return proxy;
            }
            if ("status".equals(name)) {
                return proxy;
            }
            if ("result".equals(name)) {
                return proxy;
            }
            return null;
        }
    }

    private static class MockModuloRepository extends ModuloRepository {
        @Override
        public List<Modulo> findAll() {
            return List.of(
                    new Modulo(1, 1, "Introdução à Lógica", "Descrição", 5, true, 1),
                    new Modulo(2, 2, "Variáveis", "Descrição", 5, true, 2)
            );
        }

        @Override
        public Optional<Modulo> findById(int id) {
            if (id == 1) return Optional.of(new Modulo(1, 1, "Introdução", "Desc", 5, true, 1));
            if (id == 2) return Optional.of(new Modulo(2, 2, "Variáveis", "Desc", 5, true, 2));
            return Optional.empty();
        }
    }

    private static class MockProgressoRepository extends ProgressoRepository {
        public Map<String, ProgressoUsuario> dados = new HashMap<>();

        @Override
        public Optional<ProgressoUsuario> findByUsuarioEModulo(String usuarioId, int moduloId) {
            return Optional.ofNullable(dados.get(usuarioId + ":" + moduloId));
        }
    }
}
