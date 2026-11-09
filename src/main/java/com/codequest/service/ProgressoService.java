package com.codequest.service;

import com.codequest.model.ProgressoUsuario;
import com.codequest.model.Modulo;
import com.codequest.repository.ProgressoRepository;

import java.util.List;

public class ProgressoService {

    private final ProgressoRepository repository;

    public ProgressoService() {
        this(new ProgressoRepository());
    }

    public ProgressoService(ProgressoRepository repository) {
        this.repository = repository;
    }

    public int obterEtapaMaximaDesbloqueada(String usuarioId, int moduloId) {
        return repository.findByUsuarioEModulo(usuarioId, moduloId)
                .map(ProgressoUsuario::getEtapaMaximaDesbloqueada)
                .orElse(1);
    }

    public boolean podeAcessarEtapa(String usuarioId, int moduloId, int etapaNumero) {
        if (etapaNumero <= 1) return true;
        int etapaMaxima = obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
        return etapaNumero <= etapaMaxima;
    }

    public boolean podeAcessarQuiz(String usuarioId, int moduloId, int totalEtapas) {
        if (totalEtapas <= 0) return true;
        int etapaMaxima = obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
        // O quiz é liberado quando o aluno alcançou/concluiu a última etapa (etapaMaxima > totalEtapas)
        // ou já concluiu o módulo
        boolean moduloConcluido = repository.findByUsuarioEModulo(usuarioId, moduloId)
                .map(ProgressoUsuario::isConcluido)
                .orElse(false);

        return moduloConcluido || etapaMaxima > totalEtapas;
    }

    public boolean moduloConcluido(String usuarioId, int moduloId) {
        return repository.findByUsuarioEModulo(usuarioId, moduloId)
                .map(ProgressoUsuario::isConcluido)
                .orElse(false);
    }

    /**
     * Um jogador novo acessa somente o primeiro módulo publicado. Cada módulo
     * seguinte exige a conclusão do módulo publicado imediatamente anterior.
     */
    public boolean podeAcessarModulo(String usuarioId, int moduloId, List<Modulo> modulos) {
        boolean caminhoLiberado = true;

        for (Modulo modulo : modulos) {
            boolean acessivel = caminhoLiberado && modulo.isDisponivel();
            if (modulo.getId() != null && modulo.getId() == moduloId) {
                return acessivel;
            }
            caminhoLiberado = acessivel && moduloConcluido(usuarioId, modulo.getId());
        }

        return false;
    }

    public void aplicarDisponibilidadeIndividual(String usuarioId, List<Modulo> modulos) {
        boolean caminhoLiberado = true;

        for (Modulo modulo : modulos) {
            boolean acessivel = caminhoLiberado && modulo.isDisponivel();
            modulo.setDisponivel(acessivel);
            caminhoLiberado = acessivel && moduloConcluido(usuarioId, modulo.getId());
        }
    }

    public void registrarAvancoEtapa(String usuarioId, int moduloId, int etapaAtual, int totalEtapas) {
        // Ao avançar na etapa N, liberamos pelo menos até N + 1
        int novaEtapaLiberada = Math.min(etapaAtual + 1, totalEtapas + 1);
        repository.atualizarEtapaMaxima(usuarioId, moduloId, novaEtapaLiberada);
    }

    public com.codequest.model.ResumoJogadorDTO obterResumoJogador(String usuarioId) {
        return repository.obterResumoJogador(usuarioId);
    }
}
