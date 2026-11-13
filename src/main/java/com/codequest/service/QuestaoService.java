package com.codequest.service;

import com.codequest.model.Questao;
import com.codequest.repository.QuestaoRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public class QuestaoService {

    private static final Set<String> LETRAS_VALIDAS = Set.of("A", "B", "C", "D");

    private final QuestaoRepository repository;

    public QuestaoService() { this(new QuestaoRepository()); }

    public QuestaoService(QuestaoRepository repository) { this.repository = repository; }

    public List<Questao> listarPorModulo(int moduloId) {
        return repository.findByModulo(moduloId);
    }

    public Questao buscar(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Questão " + id + " não encontrada"));
    }

    public Questao criar(Questao questao) {
        validar(questao);
        return repository.create(questao);
    }

    public void atualizar(int id, Questao questao) {
        buscar(id);
        validar(questao);
        questao.setId(id);
        repository.update(questao);
    }

    public void excluir(int id) {
        buscar(id);
        repository.delete(id);
    }

    private void validar(Questao questao) {
        if (questao.getEnunciado() == null || questao.getEnunciado().isBlank()) {
            throw new IllegalArgumentException("O enunciado da questão é obrigatório");
        }
        if (questao.getAlternativaA() == null || questao.getAlternativaB() == null
                || questao.getAlternativaC() == null || questao.getAlternativaD() == null) {
            throw new IllegalArgumentException("As 4 alternativas são obrigatórias");
        }
        if (questao.getCorreta() == null || !LETRAS_VALIDAS.contains(questao.getCorreta().toUpperCase())) {
            throw new IllegalArgumentException("A alternativa correta deve ser A, B, C ou D");
        }
        questao.setCorreta(questao.getCorreta().toUpperCase());
    }
}
