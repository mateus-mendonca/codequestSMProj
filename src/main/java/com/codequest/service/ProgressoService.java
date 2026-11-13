package com.codequest.service;

import com.codequest.model.Modulo;
import com.codequest.dto.ModuloItemDTO;
import com.codequest.model.ProgressoUsuario;
import com.codequest.dto.ResumoJogadorDTO;
import com.codequest.repository.ModuloRepository;
import com.codequest.repository.ProgressoRepository;

import java.util.ArrayList;
import java.util.List;

public class ProgressoService {

    private final ProgressoRepository repository;
    private final ModuloRepository moduloRepository;

    public ProgressoService() {
        this(new ProgressoRepository(), new ModuloRepository());
    }

    public ProgressoService(ProgressoRepository repository) {
        this(repository, new ModuloRepository());
    }

    public ProgressoService(ProgressoRepository repository, ModuloRepository moduloRepository) {
        this.repository = repository;
        this.moduloRepository = moduloRepository;
    }

    public boolean podeAcessarModulo(String usuarioId, int moduloId) {
        List<Modulo> modulos = moduloRepository.findAll();
        for (int i = 0; i < modulos.size(); i++) {
            Modulo m = modulos.get(i);
            if (m.getId() == moduloId) {
                return isModuloDesbloqueado(usuarioId, m, i, modulos);
            }
        }
        return false;
    }

    public List<ModuloItemDTO> listarModulosParaUsuario(String usuarioId) {
        List<Modulo> modulos = moduloRepository.findAll();
        List<ModuloItemDTO> itens = new ArrayList<>();

        for (int i = 0; i < modulos.size(); i++) {
            Modulo m = modulos.get(i);
            boolean concluido = isModuloConcluido(usuarioId, m.getId());
            boolean desbloqueado = isModuloDesbloqueado(usuarioId, m, i, modulos);
            String motivo = obterMotivoBloqueio(m, i, modulos, desbloqueado);

            itens.add(new ModuloItemDTO(m, desbloqueado, concluido, motivo));
        }
        return itens;
    }

    public int obterEtapaMaximaDesbloqueada(String usuarioId, int moduloId) {
        return repository.findByUsuarioEModulo(usuarioId, moduloId)
                .map(ProgressoUsuario::getEtapaMaximaDesbloqueada)
                .orElse(1);
    }

    public boolean podeAcessarEtapa(String usuarioId, int moduloId, int etapaNumero) {
        if (!podeAcessarModulo(usuarioId, moduloId)) {
            return false;
        }
        if (etapaNumero <= 1) {
            return true;
        }
        int etapaMaxima = obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
        return etapaNumero <= etapaMaxima;
    }

    public boolean podeAcessarQuiz(String usuarioId, int moduloId, int totalEtapas) {
        if (!podeAcessarModulo(usuarioId, moduloId)) {
            return false;
        }
        if (totalEtapas <= 0) {
            return true;
        }
        int etapaMaxima = obterEtapaMaximaDesbloqueada(usuarioId, moduloId);
        boolean moduloConcluido = isModuloConcluido(usuarioId, moduloId);

        return moduloConcluido || etapaMaxima > totalEtapas;
    }

    public void registrarAvancoEtapa(String usuarioId, int moduloId, int etapaAtual, int totalEtapas) {
        int novaEtapaLiberada = Math.min(etapaAtual + 1, totalEtapas + 1);
        repository.atualizarEtapaMaxima(usuarioId, moduloId, novaEtapaLiberada);
    }

    public ResumoJogadorDTO obterResumoJogador(String usuarioId) {
        return repository.obterResumoJogador(usuarioId);
    }

    private boolean isModuloDesbloqueado(String usuarioId, Modulo modulo, int indice, List<Modulo> todos) {
        if (!modulo.isDisponivel()) {
            return false;
        }
        if (indice == 0) {
            return true;
        }
        Modulo anterior = todos.get(indice - 1);
        return isModuloConcluido(usuarioId, anterior.getId());
    }

    private boolean isModuloConcluido(String usuarioId, int moduloId) {
        return repository.findByUsuarioEModulo(usuarioId, moduloId)
                .map(ProgressoUsuario::isConcluido)
                .orElse(false);
    }

    private String obterMotivoBloqueio(Modulo modulo, int indice, List<Modulo> todos, boolean desbloqueado) {
        if (desbloqueado) {
            return null;
        }
        if (!modulo.isDisponivel()) {
            return "Em Breve";
        }
        if (indice > 0) {
            Modulo anterior = todos.get(indice - 1);
            return "Conclua o Mundo " + String.format("%02d", anterior.getNumero()) + " para desbloquear";
        }
        return "Bloqueado";
    }
}
