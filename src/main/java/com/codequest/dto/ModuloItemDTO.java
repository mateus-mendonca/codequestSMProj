package com.codequest.dto;

import com.codequest.model.Modulo;

public class ModuloItemDTO {

    private final Modulo modulo;
    private final boolean desbloqueado;
    private final boolean concluido;
    private final String motivoBloqueio;

    public ModuloItemDTO(Modulo modulo, boolean desbloqueado, boolean concluido, String motivoBloqueio) {
        this.modulo = modulo;
        this.desbloqueado = desbloqueado;
        this.concluido = concluido;
        this.motivoBloqueio = motivoBloqueio;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public boolean isDesbloqueado() {
        return desbloqueado;
    }

    public boolean isConcluido() {
        return concluido;
    }

    public String getMotivoBloqueio() {
        return motivoBloqueio;
    }
}
