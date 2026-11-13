package com.codequest.dto;

import java.util.List;

public class QuizQuestionDTO {
    private int id;
    private String categoria;
    private String nivel;
    private String enunciado;
    private List<OptionDTO> alternativas;

    private String correta;
    private String explicacao;

    public QuizQuestionDTO() {}

    public QuizQuestionDTO(int id, String categoria, String nivel, String enunciado, String correta, String explicacao, List<OptionDTO> alternativas) {
        this.id = id;
        this.categoria = categoria;
        this.nivel = nivel;
        this.enunciado = enunciado;
        this.correta = correta;
        this.explicacao = explicacao;
        this.alternativas = alternativas;
    }

    public int getId() { return id; }
    public String getCategoria() { return categoria; }
    public String getNivel() { return nivel; }
    public String getEnunciado() { return enunciado; }
    public String getCorreta() { return correta; }
    public String getExplicacao() { return explicacao; }
    public List<OptionDTO> getAlternativas() { return alternativas; }

    public static class OptionDTO {
        private String key; // "A", "B", "C" ou "D" original
        private String text;

        public OptionDTO() {}

        public OptionDTO(String key, String text) {
            this.key = key;
            this.text = text;
        }

        public String getKey() { return key; }
        public String getText() { return text; }
    }
}
