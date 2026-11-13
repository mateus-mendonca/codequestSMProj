package com.codequest.dto;

import java.util.List;

public class QuizResultDTO {
    private int pontuacao; // número de acertos (ex: 8/10)
    private int totalQuestoes; // 10
    private int xpGanha;
    private int estrelas;
    private int vidasRestantes;
    private boolean notaMaxima;
    private boolean semPerderVidas;
    private boolean passou;
    private List<QuestionFeedbackDTO> detalheQuestoes;

    public QuizResultDTO() {}

    public QuizResultDTO(int pontuacao, int totalQuestoes, int xpGanha, int estrelas,
                         int vidasRestantes, boolean notaMaxima, boolean semPerderVidas, boolean passou,
                         List<QuestionFeedbackDTO> detalheQuestoes) {
        this.pontuacao = pontuacao;
        this.totalQuestoes = totalQuestoes;
        this.xpGanha = xpGanha;
        this.estrelas = estrelas;
        this.vidasRestantes = vidasRestantes;
        this.notaMaxima = notaMaxima;
        this.semPerderVidas = semPerderVidas;
        this.passou = passou;
        this.detalheQuestoes = detalheQuestoes;
    }

    public int getPontuacao() { return pontuacao; }
    public int getTotalQuestoes() { return totalQuestoes; }
    public int getXpGanha() { return xpGanha; }
    public int getEstrelas() { return estrelas; }
    public int getVidasRestantes() { return vidasRestantes; }
    public boolean isNotaMaxima() { return notaMaxima; }
    public boolean isSemPerderVidas() { return semPerderVidas; }
    public boolean isPassou() { return passou; }
    public List<QuestionFeedbackDTO> getDetalheQuestoes() { return detalheQuestoes; }

    public static class QuestionFeedbackDTO {
        private int id;
        private boolean correta;
        private String respostaEscolhida;
        private String respostaCorreta;
        private String explicacao;

        public QuestionFeedbackDTO() {}

        public QuestionFeedbackDTO(int id, boolean correta, String respostaEscolhida, String respostaCorreta, String explicacao) {
            this.id = id;
            this.correta = correta;
            this.respostaEscolhida = respostaEscolhida;
            this.respostaCorreta = respostaCorreta;
            this.explicacao = explicacao;
        }

        public int getId() { return id; }
        public boolean isCorreta() { return correta; }
        public String getRespostaEscolhida() { return respostaEscolhida; }
        public String getRespostaCorreta() { return respostaCorreta; }
        public String getExplicacao() { return explicacao; }
    }
}
