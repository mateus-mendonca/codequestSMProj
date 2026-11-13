package com.codequest.service;

import com.codequest.model.ProgressoUsuario;
import com.codequest.model.Questao;
import com.codequest.dto.QuizQuestionDTO;
import com.codequest.dto.QuizResultDTO;
import com.codequest.repository.ProgressoRepository;
import com.codequest.repository.QuestaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class QuizServiceTest {

    private QuizService quizService;
    private StubQuestaoRepository stubQuestaoRepository;
    private StubProgressoRepository stubProgressoRepository;

    @BeforeEach
    public void setUp() {
        stubQuestaoRepository = new StubQuestaoRepository();
        stubProgressoRepository = new StubProgressoRepository();
        quizService = new QuizService(stubQuestaoRepository, stubProgressoRepository);
    }

    @Test
    @DisplayName("Deve gerar exatamente 10 questões para o quiz com alternativas embaralhadas")
    public void testGerarQuizParaModulo() {
        List<QuizQuestionDTO> questoes = quizService.gerarQuizParaModulo(1);

        assertEquals(10, questoes.size());
        for (QuizQuestionDTO q : questoes) {
            assertNotNull(q.getEnunciado());
            assertNotNull(q.getCategoria());
            assertEquals(4, q.getAlternativas().size());
        }
    }

    @Test
    @DisplayName("Deve calcular pontuação e recompensas de XP com nota máxima e sem perder vidas")
    public void testProcessarResultadoSucessoTotal() {
        Map<Integer, String> respostas = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            respostas.put(i, "B");
        }

        QuizResultDTO resultado = quizService.processarResultado(1, "aluno_demo", respostas, 3);

        assertEquals(10, resultado.getPontuacao());
        assertEquals(10, resultado.getTotalQuestoes());
        assertTrue(resultado.isNotaMaxima());
        assertTrue(resultado.isSemPerderVidas());
        assertEquals(3, resultado.getEstrelas());
        // XP: 100 (etapas) + 100 (conclusao) + 50 (nota maxima) + 30 (sem perder vidas) = 280 XP
        assertEquals(280, resultado.getXpGanha());
        assertNotNull(stubProgressoRepository.ultimoSalvo);
    }

    @Test
    @DisplayName("Deve calcular estrelas e XP corretamente para desempenho parcial")
    public void testProcessarResultadoDesempenhoParcial() {
        Map<Integer, String> respostas = new HashMap<>();
        for (int i = 1; i <= 7; i++) {
            respostas.put(i, "B"); // correto
        }
        for (int i = 8; i <= 10; i++) {
            respostas.put(i, "A"); // errado
        }

        QuizResultDTO resultado = quizService.processarResultado(1, "aluno_demo", respostas, 1);

        assertEquals(7, resultado.getPontuacao());
        assertFalse(resultado.isNotaMaxima());
        assertFalse(resultado.isSemPerderVidas());
        assertTrue(resultado.isPassou());
        assertEquals(2, resultado.getEstrelas());
        // XP: 100 (etapas) + 100 (conclusao) = 200 XP
        assertEquals(200, resultado.getXpGanha());
    }

    @Test
    @DisplayName("Não deve conceder XP de conclusão de módulo se o aluno reprovar no quiz")
    public void testProcessarResultadoDerrota() {
        Map<Integer, String> respostas = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            respostas.put(i, "B"); // 3 acertos
        }
        for (int i = 4; i <= 10; i++) {
            respostas.put(i, "A"); // 7 erros
        }

        QuizResultDTO resultado = quizService.processarResultado(1, "aluno_demo", respostas, 0); // 0 vidas

        assertEquals(3, resultado.getPontuacao());
        assertFalse(resultado.isPassou());
        assertEquals(0, resultado.getEstrelas());
        // XP: apenas 100 (etapas) e NÃO 200 (sem bônus de conclusão de módulo)
        assertEquals(100, resultado.getXpGanha());
    }

    private static class StubQuestaoRepository extends QuestaoRepository {
        @Override
        public List<Questao> findByModulo(int moduloId) {
            List<Questao> questoes = new ArrayList<>();
            String[] categorias = {"Lógica de Programação", "Algoritmos", "Fluxogramas", "Pseudocódigo", "Entrada e Saída"};

            int idCounter = 1;
            for (String cat : categorias) {
                for (int i = 1; i <= 4; i++) {
                    questoes.add(new Questao(
                            idCounter,
                            moduloId,
                            cat,
                            "Fácil",
                            "Enunciado " + idCounter,
                            "Alt A",
                            "Alt B",
                            "Alt C",
                            "Alt D",
                            "B",
                            "Explicação " + idCounter,
                            idCounter
                    ));
                    idCounter++;
                }
            }
            return questoes;
        }
    }

    private static class StubProgressoRepository extends ProgressoRepository {
        public ProgressoUsuario ultimoSalvo;

        @Override
        public Optional<ProgressoUsuario> findByUsuarioEModulo(String usuarioId, int moduloId) {
            return Optional.ofNullable(ultimoSalvo);
        }

        @Override
        public ProgressoUsuario saveOrUpdate(ProgressoUsuario progresso) {
            this.ultimoSalvo = progresso;
            return progresso;
        }
    }
}
