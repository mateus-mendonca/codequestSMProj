package com.codequest.service;

import com.codequest.model.ProgressoUsuario;
import com.codequest.repository.ProgressoRepository;

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

    public void registrarAvancoEtapa(String usuarioId, int moduloId, int etapaAtual, int totalEtapas) {
        // Ao avançar na etapa N, liberamos pelo menos até N + 1
        int novaEtapaLiberada = Math.min(etapaAtual + 1, totalEtapas + 1);
        repository.atualizarEtapaMaxima(usuarioId, moduloId, novaEtapaLiberada);
    }

    public com.codequest.model.ResumoJogadorDTO obterResumoJogador(String usuarioId) {
        return repository.obterResumoJogador(usuarioId);
    }
}
