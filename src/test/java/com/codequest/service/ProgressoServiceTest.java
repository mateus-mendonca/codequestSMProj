package com.codequest.service;

import com.codequest.model.ProgressoUsuario;
import com.codequest.repository.ProgressoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ProgressoServiceTest {

    private ProgressoService progressoService;
    private StubProgressoRepository stubRepository;

    @BeforeEach
    public void setUp() {
        stubRepository = new StubProgressoRepository();
        progressoService = new ProgressoService(stubRepository);
    }

    @Test
    @DisplayName("Usuário novo sem histórico deve iniciar com etapa 1 desbloqueada e quiz bloqueado")
    public void testUsuarioNovoIniciaNaEtapa1() {
        int etapaMaxima = progressoService.obterEtapaMaximaDesbloqueada("aluno_novo", 1);
        assertEquals(1, etapaMaxima);

        assertTrue(progressoService.podeAcessarEtapa("aluno_novo", 1, 1));
        assertFalse(progressoService.podeAcessarEtapa("aluno_novo", 1, 2));
        assertFalse(progressoService.podeAcessarQuiz("aluno_novo", 1, 5));
    }

    @Test
    @DisplayName("Ao avançar na etapa 1, a etapa 2 deve ser liberada")
    public void testAvancoDeEtapaDesbloqueiaProxima() {
        progressoService.registrarAvancoEtapa("aluno_1", 1, 1, 5);

        int etapaMaxima = progressoService.obterEtapaMaximaDesbloqueada("aluno_1", 1);
        assertEquals(2, etapaMaxima);

        assertTrue(progressoService.podeAcessarEtapa("aluno_1", 1, 1));
        assertTrue(progressoService.podeAcessarEtapa("aluno_1", 1, 2));
        assertFalse(progressoService.podeAcessarEtapa("aluno_1", 1, 3));
        assertFalse(progressoService.podeAcessarQuiz("aluno_1", 1, 5));
    }

    @Test
    @DisplayName("Após concluir a última etapa (5 de 5), o Quiz Final deve ser liberado")
    public void testConclusaoDaUltimaEtapaLiberaQuiz() {
        // Conclui da etapa 1 até a etapa 5
        progressoService.registrarAvancoEtapa("aluno_quase_la", 1, 5, 5);

        int etapaMaxima = progressoService.obterEtapaMaximaDesbloqueada("aluno_quase_la", 1);
        assertEquals(6, etapaMaxima); // 5 + 1 = 6 (marca que todas foram completadas)

        assertTrue(progressoService.podeAcessarEtapa("aluno_quase_la", 1, 5));
        assertTrue(progressoService.podeAcessarQuiz("aluno_quase_la", 1, 5));
    }

    @Test
    @DisplayName("Módulo já concluído previamente deve permitir acesso irrestrito ao quiz")
    public void testModuloConcluidoPermiteQuiz() {
        ProgressoUsuario p = new ProgressoUsuario(1, "aluno_veterano", 1, 200, 3, true, true, true, 1, 1);
        stubRepository.dados.put("aluno_veterano:1", p);

        assertTrue(progressoService.podeAcessarQuiz("aluno_veterano", 1, 5));
    }

    private static class StubProgressoRepository extends ProgressoRepository {
        public Map<String, ProgressoUsuario> dados = new HashMap<>();

        @Override
        public Optional<ProgressoUsuario> findByUsuarioEModulo(String usuarioId, int moduloId) {
            return Optional.ofNullable(dados.get(usuarioId + ":" + moduloId));
        }

        @Override
        public void atualizarEtapaMaxima(String usuarioId, int moduloId, int etapaNumero) {
            String key = usuarioId + ":" + moduloId;
            ProgressoUsuario p = dados.getOrDefault(key, new ProgressoUsuario(null, usuarioId, moduloId, 0, 0, false, false, false, 0, 1));
            p.setEtapaMaximaDesbloqueada(Math.max(p.getEtapaMaximaDesbloqueada(), etapaNumero));
            dados.put(key, p);
        }

        @Override
        public ProgressoUsuario saveOrUpdate(ProgressoUsuario progresso) {
            dados.put(progresso.getUsuarioId() + ":" + progresso.getModuloId(), progresso);
            return progresso;
        }
    }
}
