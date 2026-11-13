package com.codequest.dto;

public class ResumoJogadorDTO {
    private int xpTotal;
    private int fasesConcluidas;
    private int mundosConquistados;

    public ResumoJogadorDTO() {}

    public ResumoJogadorDTO(int xpTotal, int fasesConcluidas, int mundosConquistados) {
        this.xpTotal = xpTotal;
        this.fasesConcluidas = fasesConcluidas;
        this.mundosConquistados = mundosConquistados;
    }

    public int getXpTotal() {
        return xpTotal;
    }

    public void setXpTotal(int xpTotal) {
        this.xpTotal = xpTotal;
    }

    public int getFasesConcluidas() {
        return fasesConcluidas;
    }

    public void setFasesConcluidas(int fasesConcluidas) {
        this.fasesConcluidas = fasesConcluidas;
    }

    public int getMundosConquistados() {
        return mundosConquistados;
    }

    public void setMundosConquistados(int mundosConquistados) {
        this.mundosConquistados = mundosConquistados;
    }
}
