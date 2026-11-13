package com.codequest.dto;

import java.util.HashMap;
import java.util.Map;

public class QuizSubmission {
    public Map<Integer, String> respostas = new HashMap<>();
    public int vidasRestantes = 3;
    public String usuarioId = "aluno_demo";
}
