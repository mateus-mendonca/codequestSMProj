package com.codequest.config;

import com.codequest.repository.EtapaRepository;
import com.codequest.repository.ModuloRepository;
import com.codequest.repository.ProgressoRepository;
import com.codequest.repository.QuestaoRepository;
import com.codequest.service.EtapaService;
import com.codequest.service.ModuloService;
import com.codequest.service.ProgressoService;
import com.codequest.service.QuestaoService;
import com.codequest.service.QuizService;

/** Dependencies shared by the HTTP controllers. */
public record ApplicationServices(ModuloService modulos, EtapaService etapas,
                                  ProgressoService progresso, QuestaoService questoes, QuizService quiz) {
    public static ApplicationServices create() {
        ModuloRepository modulos = new ModuloRepository();
        ProgressoRepository progresso = new ProgressoRepository();
        QuestaoRepository questoes = new QuestaoRepository();
        return new ApplicationServices(new ModuloService(modulos), new EtapaService(new EtapaRepository()),
                new ProgressoService(progresso, modulos), new QuestaoService(questoes),
                new QuizService(questoes, progresso));
    }
}
