package com.codequest.service;

import com.codequest.model.Modulo;
import com.codequest.dto.ModuloItemDTO;
import com.codequest.model.ProgressoUsuario;
import com.codequest.dto.ResumoJogadorDTO;
import com.codequest.repository.ModuloRepository;
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
    private StubModuloRepository stubModuloRepository;

    @BeforeEach
    public void setUp() {
        stubRepository = new StubProgressoRepository();
        stubModuloRepository = new StubModuloRepository();
        progressoService = new ProgressoService(stubRepository, stubModuloRepository);
    }

    @Test
    @DisplayName("Usuário novo sem histórico deve ter apenas o módulo 1 desbloqueado")
    public void testUsuarioNovoApenasPrimeiroModuloDesbloqueado() {
        assertTrue(progressoService.podeAcessarModulo("aluno_novo", 1));
        assertFalse(progressoService.podeAcessarModulo("aluno_novo", 2));
        assertFalse(progressoService.podeAcessarModulo("aluno_novo", 3));
    }

    @Test
    @DisplayName("Módulo 2 deve ser desbloqueado somente após conclusão do Módulo 1")
    public void testModulo2DesbloqueiaAposConcluirModulo1() {
        String usuario = "aluno_avancado";
        assertFalse(progressoService.podeAcessarModulo(usuario, 2));

        ProgressoUsuario p = new ProgressoUsuario(1, usuario, 1, 200, 3, true, true, true, 1, 6);
        stubRepository.dados.put(usuario + ":1", p);

        assertTrue(progressoService.podeAcessarModulo(usuario, 2));
    }

    @Test
    @DisplayName("Módulo indisponível globalmente deve permanecer bloqueado")
    public void testModuloIndisponivelPermaneceBloqueado() {
        String usuario = "aluno_veterano";
        stubRepository.dados.put(usuario + ":1", new ProgressoUsuario(1, usuario, 1, 200, 3, true, true, true, 1, 6));
        stubRepository.dados.put(usuario + ":2", new ProgressoUsuario(2, usuario, 2, 200, 3, true, true, true, 1, 6));

        assertFalse(progressoService.podeAcessarModulo(usuario, 3));
    }

    @Test
    @DisplayName("Listagem para usuário novo deve bloquear o Módulo 2 com justificativa")
    public void testListarModulosParaUsuarioNovo() {
        List<ModuloItemDTO> itens = progressoService.listarModulosParaUsuario("aluno_novo");
        assertEquals(3, itens.size());

        ModuloItemDTO m1 = itens.get(0);
        assertTrue(m1.isDesbloqueado());
        assertFalse(m1.isConcluido());
        assertNull(m1.getMotivoBloqueio());

        ModuloItemDTO m2 = itens.get(1);
        assertFalse(m2.isDesbloqueado());
        assertFalse(m2.isConcluido());
        assertEquals("Conclua o Mundo 01 para desbloquear", m2.getMotivoBloqueio());

        ModuloItemDTO m3 = itens.get(2);
        assertFalse(m3.isDesbloqueado());
        assertEquals("Em Breve", m3.getMotivoBloqueio());
    }

    @Test
    @DisplayName("Listagem com Módulo 1 concluído deve liberar o Módulo 2")
    public void testListarModulosComModulo1Concluido() {
        String usuario = "aluno_completo";
        stubRepository.dados.put(usuario + ":1", new ProgressoUsuario(1, usuario, 1, 200, 3, true, true, true, 1, 6));

        List<ModuloItemDTO> itens = progressoService.listarModulosParaUsuario(usuario);
        ModuloItemDTO m2 = itens.get(1);

        assertTrue(m2.isDesbloqueado());
        assertNull(m2.getMotivoBloqueio());
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
    @DisplayName("Não deve permitir acessar etapa de módulo bloqueado")
    public void testNaoPermiteAcessarEtapaDeModuloBloqueado() {
        assertFalse(progressoService.podeAcessarEtapa("aluno_novo", 2, 1));
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
        progressoService.registrarAvancoEtapa("aluno_quase_la", 1, 5, 5);

        int etapaMaxima = progressoService.obterEtapaMaximaDesbloqueada("aluno_quase_la", 1);
        assertEquals(6, etapaMaxima);

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

        ResumoJogadorDTO resumo = progressoService.obterResumoJogador("aluno_resumo");
        assertNotNull(resumo);
        assertEquals(200, resumo.getXpTotal());
        assertEquals(4, resumo.getFasesConcluidas());
        assertEquals(1, resumo.getMundosConquistados());
    }

    private static class StubModuloRepository extends ModuloRepository {
        @Override
        public List<Modulo> findAll() {
            return List.of(
                    new Modulo(1, 1, "Introdução à Lógica", "Descrição", 5, true, 1),
                    new Modulo(2, 2, "Variáveis", "Descrição", 5, true, 2),
                    new Modulo(3, 3, "Operadores", "Descrição", 0, false, 3)
            );
        }
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
        public ResumoJogadorDTO obterResumoJogador(String usuarioId) {
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
            return new ResumoJogadorDTO(xp, fases, mundos);
        }
    }
}
