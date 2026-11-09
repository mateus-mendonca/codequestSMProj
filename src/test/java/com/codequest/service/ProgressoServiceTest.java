package com.codequest.service;

import com.codequest.model.ProgressoUsuario;
import com.codequest.model.Modulo;
import com.codequest.repository.ProgressoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
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

    @Test
    @DisplayName("Deve calcular resumo do jogador com base no histórico de progresso")
    public void testObterResumoJogador() {
        ProgressoUsuario p1 = new ProgressoUsuario(1, "aluno_resumo", 1, 150, 3, true, true, true, 1, 4);
        ProgressoUsuario p2 = new ProgressoUsuario(2, "aluno_resumo", 2, 50, 1, false, false, false, 1, 2);
        stubRepository.dados.put("aluno_resumo:1", p1);
        stubRepository.dados.put("aluno_resumo:2", p2);

        com.codequest.model.ResumoJogadorDTO resumo = progressoService.obterResumoJogador("aluno_resumo");
        assertNotNull(resumo);
        assertEquals(200, resumo.getXpTotal());
        assertEquals(4, resumo.getFasesConcluidas()); // (4 - 1) + (2 - 1) = 3 + 1 = 4
        assertEquals(1, resumo.getMundosConquistados());
    }

    @Test
    @DisplayName("Jogador novo deve acessar somente o primeiro módulo publicado")
    public void testUsuarioNovoAcessaSomentePrimeiroModulo() {
        List<Modulo> modulos = criarModulosPublicados();

        assertTrue(progressoService.podeAcessarModulo("novo", 1, modulos));
        assertFalse(progressoService.podeAcessarModulo("novo", 2, modulos));

        progressoService.aplicarDisponibilidadeIndividual("novo", modulos);
        assertTrue(modulos.get(0).isDisponivel());
        assertFalse(modulos.get(1).isDisponivel());
    }

    @Test
    @DisplayName("Conclusão do módulo anterior deve liberar o próximo")
    public void testConclusaoDoModuloLiberaProximo() {
        ProgressoUsuario concluido = new ProgressoUsuario(1, "jogador", 1, 200, 2, true, false, false, 1, 6);
        stubRepository.dados.put("jogador:1", concluido);
        List<Modulo> modulos = criarModulosPublicados();

        assertTrue(progressoService.podeAcessarModulo("jogador", 2, modulos));
        progressoService.aplicarDisponibilidadeIndividual("jogador", modulos);
        assertTrue(modulos.get(1).isDisponivel());
    }

    private List<Modulo> criarModulosPublicados() {
        return List.of(
                new Modulo(1, 1, "Mundo 1", "", 5, true, 1),
                new Modulo(2, 2, "Mundo 2", "", 5, true, 2)
        );
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

        @Override
        public com.codequest.model.ResumoJogadorDTO obterResumoJogador(String usuarioId) {
            int xp = 0;
            int fases = 0;
            int mundos = 0;
            for (ProgressoUsuario p : dados.values()) {
                if (p.getUsuarioId().equals(usuarioId)) {
                    xp += p.getXpTotal();
                    if (p.getEtapaMaximaDesbloqueada() > 1) {
                        fases += (p.getEtapaMaximaDesbloqueada() - 1);
                    }
                    if (p.isConcluido()) {
                        mundos++;
                    }
                }
            }
            return new com.codequest.model.ResumoJogadorDTO(xp, fases, mundos);
        }
    }
}
