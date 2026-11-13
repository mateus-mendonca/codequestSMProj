package com.codequest.service;

import com.codequest.model.ProgressoUsuario;
import com.codequest.model.Questao;
import com.codequest.dto.QuizQuestionDTO;
import com.codequest.dto.QuizResultDTO;
import com.codequest.repository.ProgressoRepository;
import com.codequest.repository.QuestaoRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuizService {

    private final QuestaoRepository questaoRepository;
    private final ProgressoRepository progressoRepository;

    public QuizService() {
        this.questaoRepository = new QuestaoRepository();
        this.progressoRepository = new ProgressoRepository();
    }

    public QuizService(QuestaoRepository questaoRepository, ProgressoRepository progressoRepository) {
        this.questaoRepository = questaoRepository;
        this.progressoRepository = progressoRepository;
    }

    public List<QuizQuestionDTO> gerarQuizParaModulo(int moduloId) {
        List<Questao> todas = questaoRepository.findByModulo(moduloId);

        // Agrupar por categoria
        Map<String, List<Questao>> porCategoria = todas.stream()
                .collect(Collectors.groupingBy(q -> normalizeCategoria(q.getCategoria())));

        List<Questao> selecionadas = new ArrayList<>();

        // Sortear 2 questões de cada uma das 5 categorias principais
        String[] categoriasPrincipais = {
                "Lógica de Programação",
                "Algoritmos",
                "Fluxogramas",
                "Pseudocódigo",
                "Entrada e Saída"
        };

        for (String cat : categoriasPrincipais) {
            List<Questao> daCategoria = porCategoria.getOrDefault(cat, new ArrayList<>());
            Collections.shuffle(daCategoria);
            selecionadas.addAll(daCategoria.stream().limit(2).toList());
        }

        // Se por algum motivo não deu 10, completa com o restante aleatório
        if (selecionadas.size() < 10) {
            List<Questao> restantes = new ArrayList<>(todas);
            restantes.removeAll(selecionadas);
            Collections.shuffle(restantes);
            int faltam = 10 - selecionadas.size();
            selecionadas.addAll(restantes.stream().limit(faltam).toList());
        }

        Collections.shuffle(selecionadas);

        // Converter para DTO e embaralhar alternativas
        return selecionadas.stream().map(q -> {
            List<QuizQuestionDTO.OptionDTO> opcoes = new ArrayList<>();
            opcoes.add(new QuizQuestionDTO.OptionDTO("A", q.getAlternativaA()));
            opcoes.add(new QuizQuestionDTO.OptionDTO("B", q.getAlternativaB()));
            opcoes.add(new QuizQuestionDTO.OptionDTO("C", q.getAlternativaC()));
            opcoes.add(new QuizQuestionDTO.OptionDTO("D", q.getAlternativaD()));

            Collections.shuffle(opcoes);

            return new QuizQuestionDTO(
                    q.getId(),
                    q.getCategoria(),
                    q.getNivel(),
                    q.getEnunciado(),
                    q.getCorreta(),
                    q.getExplicacao(),
                    opcoes
            );
        }).toList();
    }

    public QuizResultDTO processarResultado(int moduloId, String usuarioId, Map<Integer, String> respostas, int vidasRestantes) {
        List<Questao> questoesModulo = questaoRepository.findByModulo(moduloId);
        Map<Integer, Questao> mapaQuestoes = questoesModulo.stream()
                .collect(Collectors.toMap(Questao::getId, q -> q));

        int acertos = 0;
        List<QuizResultDTO.QuestionFeedbackDTO> feedbacks = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : respostas.entrySet()) {
            Integer qId = entry.getKey();
            String resEscolhida = entry.getValue();

            Questao q = mapaQuestoes.get(qId);
            if (q != null) {
                boolean correta = q.getCorreta().equalsIgnoreCase(resEscolhida);
                if (correta) {
                    acertos++;
                }
                feedbacks.add(new QuizResultDTO.QuestionFeedbackDTO(
                        qId,
                        correta,
                        resEscolhida,
                        q.getCorreta(),
                        q.getExplicacao()
                ));
            }
        }

        int total = respostas.size() > 0 ? respostas.size() : 10;
        boolean passou = (vidasRestantes > 0 && acertos >= 6);
        boolean notaMaxima = passou && (acertos == total);
        boolean semPerderVidas = passou && (vidasRestantes == 3);

        // Recompensas XP:
        // 📘 +20 XP por etapa concluída (Módulo 1 tem 5 etapas = 100 XP)
        // 🏆 +100 XP por concluir o módulo (APENAS SE PASSOUR NO QUIZ)
        // 💎 +50 XP por obter nota máxima
        // ⚡ +30 XP por finalizar sem perder vidas
        int xpTotal = 100; // 5 etapas * 20

        int estrelas = 0;

        if (passou) {
            xpTotal += 100; // Conclusão do módulo
            if (notaMaxima) {
                xpTotal += 50;
            }
            if (semPerderVidas) {
                xpTotal += 30;
            }

            estrelas = 1;
            if (acertos >= 9) {
                estrelas = 3;
            } else if (acertos >= 6) {
                estrelas = 2;
            }
        }

        // Persistir progresso no banco de dados
        ProgressoUsuario p = new ProgressoUsuario(
                null,
                usuarioId,
                moduloId,
                xpTotal,
                estrelas,
                passou,
                notaMaxima,
                semPerderVidas,
                1
        );
        progressoRepository.saveOrUpdate(p);

        return new QuizResultDTO(
                acertos,
                total,
                xpTotal,
                estrelas,
                vidasRestantes,
                notaMaxima,
                semPerderVidas,
                passou,
                feedbacks
        );
    }

    private String normalizeCategoria(String cat) {
        if (cat == null) return "Lógica de Programação";
        if (cat.toLowerCase().contains("lógica")) return "Lógica de Programação";
        if (cat.toLowerCase().contains("algoritmo")) return "Algoritmos";
        if (cat.toLowerCase().contains("fluxograma")) return "Fluxogramas";
        if (cat.toLowerCase().contains("pseudocódigo")) return "Pseudocódigo";
        if (cat.toLowerCase().contains("entrada") || cat.toLowerCase().contains("saída") || cat.toLowerCase().contains("interpretação") || cat.toLowerCase().contains("organização") || cat.toLowerCase().contains("revisão")) {
            return "Entrada e Saída";
        }
        return "Lógica de Programação";
    }
}
