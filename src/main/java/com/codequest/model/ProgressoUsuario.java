package com.codequest.model;

public class ProgressoUsuario {
    private Integer id;
    private String usuarioId;
    private int moduloId;
    private int xpTotal;
    private int estrelas;
    private boolean concluido;
    private boolean notaMaxima;
    private boolean semPerderVidas;
    private int tentativas;
    private int etapaMaximaDesbloqueada = 1;

    public ProgressoUsuario() {}

    public ProgressoUsuario(Integer id, String usuarioId, int moduloId, int xpTotal, int estrelas,
                            boolean concluido, boolean notaMaxima, boolean semPerderVidas, int tentativas) {
        this(id, usuarioId, moduloId, xpTotal, estrelas, concluido, notaMaxima, semPerderVidas, tentativas, 1);
    }

    public ProgressoUsuario(Integer id, String usuarioId, int moduloId, int xpTotal, int estrelas,
                            boolean concluido, boolean notaMaxima, boolean semPerderVidas, int tentativas, int etapaMaximaDesbloqueada) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.moduloId = moduloId;
        this.xpTotal = xpTotal;
        this.estrelas = estrelas;
        this.concluido = concluido;
        this.notaMaxima = notaMaxima;
        this.semPerderVidas = semPerderVidas;
        this.tentativas = tentativas;
        this.etapaMaximaDesbloqueada = Math.max(1, etapaMaximaDesbloqueada);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public int getModuloId() { return moduloId; }
    public void setModuloId(int moduloId) { this.moduloId = moduloId; }

    public int getXpTotal() { return xpTotal; }
    public void setXpTotal(int xpTotal) { this.xpTotal = xpTotal; }

    public int getEstrelas() { return estrelas; }
    public void setEstrelas(int estrelas) { this.estrelas = estrelas; }

    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }

    public boolean isNotaMaxima() { return notaMaxima; }
    public void setNotaMaxima(boolean notaMaxima) { this.notaMaxima = notaMaxima; }

    public boolean isSemPerderVidas() { return semPerderVidas; }
    public void setSemPerderVidas(boolean semPerderVidas) { this.semPerderVidas = semPerderVidas; }

    public int getTentativas() { return tentativas; }
    public void setTentativas(int tentativas) { this.tentativas = tentativas; }

    public int getEtapaMaximaDesbloqueada() { return etapaMaximaDesbloqueada; }
    public void setEtapaMaximaDesbloqueada(int etapaMaximaDesbloqueada) {
        this.etapaMaximaDesbloqueada = Math.max(1, etapaMaximaDesbloqueada);
    }
}
