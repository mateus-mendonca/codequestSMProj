package com.codequest.database.seed;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Modulo2Seed {
        public static void populate(Connection conn) throws SQLException {
                int moduloId = -1;

                String checkSql = "SELECT id FROM modulos WHERE numero = 2";
                try (Statement stmt = conn.createStatement();
                                var rs = stmt.executeQuery(checkSql)) {
                        if (rs.next()) {
                                moduloId = rs.getInt("id");
                                String updateModuloSql = """
                                                UPDATE modulos
                                                SET titulo = 'Variáveis',
                                                descricao = 'Vamos explorar o conceito fundamental de variáveis: como guardar informações na memória do computador e manipular esses dados',
                                                topicos = 5,
                                                disponivel = TRUE,
                                                ordem = 2
                                                WHERE id = ?
                                                """;
                                try (var updateStmt = conn.prepareStatement(updateModuloSql)) {
                                        updateStmt.setInt(1, moduloId);
                                        updateStmt.executeUpdate();
                                }
                        }
                }

                if (moduloId == -1) {
                        String insertModuloSql = """
                                        INSERT INTO modulos (numero, titulo, descricao, topicos, disponivel, ordem)
                                        VALUES (2, 'Variáveis', 'Vamos explorar o conceito fundamental de variáveis: como guardar informações na memória do computador e manipular esses dados.', 5, TRUE, 2)
                                        RETURNING id;
                                        """;
                        try (Statement stmt = conn.createStatement();
                                        var rs = stmt.executeQuery(insertModuloSql)) {
                                if (rs.next()) {
                                        moduloId = rs.getInt("id");
                                } else {
                                        return;
                                }
                        }
                }

                // 2. Garante a inserção das 5 Etapas do Mundo 2
                String checkEtapasSql = "SELECT COUNT(*) FROM etapas WHERE modulo_id = ?";
                try (var stmt = conn.prepareStatement(checkEtapasSql)) {
                        stmt.setInt(1, moduloId);
                        var rs = stmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                                String insertEtapaSql = """
                                                INSERT INTO etapas (modulo_id, numero, titulo, objetivo, explicacao, exemplo, ordem)
                                                VALUES (?, ?, ?, ?, ?, ?, ?)
                                                """;
                                try (var etapaStmt = conn.prepareStatement(insertEtapaSql)) {
                                        // Etapa 1
                                        etapaStmt.setInt(1, moduloId);
                                        etapaStmt.setInt(2, 1);
                                        etapaStmt.setString(3, "Conhecendo as Variáveis");
                                        etapaStmt.setString(4,
                                                        "Apresentar o conceito de variável de maneira intuitiva como um espaço de armazenamento com nome.");
                                        etapaStmt.setString(5,
                                                        "Uma variável é um espaço utilizado pelo programa para armazenar uma informação na memória, funcionando como uma caixa identificada por um nome. Os valores armazenados podem ser alterados ou substituídos ao longo da execução do programa.");
                                        etapaStmt.setString(6,
                                                        "// Criando e alterando variáveis:\nvida <- 100\nvida <- 80\nvida <- 50\n// O valor final de vida será 50.");
                                        etapaStmt.setInt(7, 1);
                                        etapaStmt.executeUpdate();

                                        // Etapa 2
                                        etapaStmt.setInt(1, moduloId);
                                        etapaStmt.setInt(2, 2);
                                        etapaStmt.setString(3, "Tipos de Dados");
                                        etapaStmt.setString(4,
                                                        "Compreender as diferentes naturezas de informações que podem ser guardadas em variáveis.");
                                        etapaStmt.setString(5,
                                                        "As variáveis armazenam diferentes tipos de dados essenciais:\n• Inteiro: números sem casas decimais (ex: 21, -5)\n• Real: números com casas decimais (ex: 1.75, 19.90)\n• Texto: palavras e frases entre aspas (ex: \"Mateus\", \"Olá\")\n• Booleano: valores lógicos (verdadeiro ou falso).");
                                        etapaStmt.setString(6,
                                                        "idade <- 21          // Inteiro\naltura <- 1.75       // Real\nnome <- \"Arkan\"      // Texto\njogadorVivo <- verdadeiro // Booleano");
                                        etapaStmt.setInt(7, 2);
                                        etapaStmt.executeUpdate();

                                        // Etapa 3
                                        etapaStmt.setInt(1, moduloId);
                                        etapaStmt.setInt(2, 3);
                                        etapaStmt.setString(3, "Atribuição e Operações");
                                        etapaStmt.setString(4,
                                                        "Aprender a modificar o valor de uma variável aplicando operações aritméticas.");
                                        etapaStmt.setString(5,
                                                        "O comando de atribuição (<-) calcula a expressão à direita e armazena o resultado na variável à esquerda. Em programação, 'x <- x + 3' significa: pegue o valor atual de x, adicione 3 e armazene o novo total em x.");
                                        etapaStmt.setString(6,
                                                        "a <- 10\nb <- 5\nsoma <- a + b\n// soma = 15\n\npontos <- 100\npontos <- pontos + 50\n// pontos = 150");
                                        etapaStmt.setInt(7, 3);
                                        etapaStmt.executeUpdate();

                                        // Etapa 4
                                        etapaStmt.setInt(1, moduloId);
                                        etapaStmt.setInt(2, 4);
                                        etapaStmt.setString(3, "Entrada de Dados");
                                        etapaStmt.setString(4,
                                                        "Permitir que o usuário forneça dados externos para serem manipulados pelo algoritmo.");
                                        etapaStmt.setString(5,
                                                        "O comando leia() pausa a execução e aguarda o usuário digitar um dado. O valor digitado é então atribuído imediatamente à variável informada entre parênteses, conectando o fluxo de Entrada com o Processamento.");
                                        etapaStmt.setString(6,
                                                        "algoritmo \"Cadastro\"\ninicio\n    leia(nome)\n    escreva(nome)\nfim");
                                        etapaStmt.setInt(7, 4);
                                        etapaStmt.executeUpdate();

                                        // Etapa 5
                                        etapaStmt.setInt(1, moduloId);
                                        etapaStmt.setInt(2, 5);
                                        etapaStmt.setString(3, "Colocando Tudo em Prática");
                                        etapaStmt.setString(4,
                                                        "Integrar variáveis, tipos, atribuição, operações aritméticas e entrada/saída em algoritmos completos.");
                                        etapaStmt.setString(5,
                                                        "Nesta etapa combinamos todos os conceitos para resolver problemas do mundo real: capturar dados via leia(), aplicar cálculos aritméticos armazenando em variáveis e exibir a resposta final com escreva().");
                                        etapaStmt.setString(6,
                                                        "algoritmo \"Compra\"\ninicio\n    leia(preco)\n    leia(quantidade)\n    total <- preco * quantidade\n    escreva(total)\nfim");
                                        etapaStmt.setInt(7, 5);
                                        etapaStmt.executeUpdate();
                                }
                        }
                }

                // 3. Garante a inserção das 20 Questões (4 por categoria)
                String checkQuestoesSql = "SELECT COUNT(*) FROM questoes WHERE modulo_id = ?";
                try (var stmt = conn.prepareStatement(checkQuestoesSql)) {
                        stmt.setInt(1, moduloId);
                        var rs = stmt.executeQuery();
                        if (rs.next() && rs.getInt(1) == 0) {
                                String insertSql = """
                                                INSERT INTO questoes (modulo_id, categoria, nivel, enunciado, alternativa_a, alternativa_b, alternativa_c, alternativa_d, correta, explicacao, ordem)
                                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                                """;

                                Object[][] questoesMundo2 = {
                                                // Categoria 1: Conceito de Variável (4 questões)
                                                { moduloId, "Conceito de Variável", "Fácil",
                                                                "O que é uma variável na programação?",
                                                                "Um comando que desliga o computador.",
                                                                "Um espaço reservado na memória para armazenar dados identificados por um nome.",
                                                                "Uma tela que exibe erros no código.",
                                                                "Um cabo de conexão de rede.",
                                                                "B",
                                                                "Variáveis funcionam como contêineres ou caixas na memória que guardam dados durante a execução do programa.",
                                                                1 },

                                                { moduloId, "Conceito de Variável", "Fácil",
                                                                "O que acontece com o valor anterior quando uma variável recebe uma nova atribuição?",
                                                                "O valor antigo é preservado e somado ao novo.",
                                                                "O valor anterior é substituído pelo novo valor.",
                                                                "O programa gera um erro de repetição.",
                                                                "A variável é destruída da memória.",
                                                                "B",
                                                                "Uma variável comum mantém apenas um valor por vez; qualquer nova atribuição sobrepõe o conteúdo anterior.",
                                                                2 },

                                                { moduloId, "Conceito de Variável", "Médio",
                                                                "Considere o trecho:\nmoedas <- 100\nmoedas <- 250\nQual o valor final da variável moedas?",
                                                                "100", "250", "350", "0",
                                                                "B",
                                                                "A segunda instrução sobrescreveu o valor 100 pelo número 250.",
                                                                3 },

                                                { moduloId, "Conceito de Variável", "Médio",
                                                                "Por que os jogos digitais utilizam variáveis constantemente?",
                                                                "Para evitar que o jogador precise usar o teclado.",
                                                                "Para armazenar e atualizar informações que mudam durante o jogo, como vida, moedas e pontuação.",
                                                                "Para impedir o jogo de fechar sozinho.",
                                                                "Apenas para tocar efeitos sonoros.",
                                                                "B",
                                                                "Jogos dependem de dados dinâmicos que sofrem atualizações constantes em tempo de execução.",
                                                                4 },

                                                // Categoria 2: Tipos de Dados (4 questões)
                                                { moduloId, "Tipos de Dados", "Fácil",
                                                                "Qual tipo de dado é mais adequado para armazenar a quantidade de vidas de um personagem?",
                                                                "Real", "Inteiro", "Texto", "Booleano",
                                                                "B",
                                                                "Vidas são contadas em números inteiros sem casas decimais (1, 2, 3...).",
                                                                5 },

                                                { moduloId, "Tipos de Dados", "Fácil",
                                                                "Qual tipo de dado deve ser utilizado para armazenar o nome de um jogador?",
                                                                "Inteiro", "Real", "Texto", "Booleano",
                                                                "C",
                                                                "Nomes e palavras são sequências de caracteres, pertencentes ao tipo Texto.",
                                                                6 },

                                                { moduloId, "Tipos de Dados", "Médio",
                                                                "Qual dos seguintes valores pertence ao tipo de dado Real?",
                                                                "\"25\"", "1.75", "verdadeiro", "100",
                                                                "B",
                                                                "O número 1.75 possui casas decimais, caracterizando o tipo Real (ponto flutuante).",
                                                                7 },

                                                { moduloId, "Tipos de Dados", "Médio",
                                                                "Uma variável que armazena se a porta de uma fase está aberta ou fechada (verdadeiro/falso) é do tipo:",
                                                                "Booleano", "Inteiro", "Texto", "Real",
                                                                "A",
                                                                "O tipo Booleano (ou lógico) aceita apenas dois estados possíveis: verdadeiro ou falso.",
                                                                8 },

                                                // Categoria 3: Atribuição (4 questões)
                                                { moduloId, "Atribuição", "Fácil",
                                                                "O que o símbolo '<-' representa na maioria dos pseudocódigos?",
                                                                "Uma comparação de igualdade matemática.",
                                                                "Uma operação de atribuição de valor a uma variável.",
                                                                "Uma mensagem que deve ser exibida na tela.",
                                                                "Uma divisão entre dois números.",
                                                                "B",
                                                                "A seta '<-' indica que a variável à esquerda recebe o resultado da expressão à direita.",
                                                                9 },

                                                { moduloId, "Atribuição", "Médio",
                                                                "Dado o código:\nx <- 5\nx <- x + 3\nQual o valor final de x?",
                                                                "5", "3", "8", "53",
                                                                "C",
                                                                "A expressão calcula 5 + 3 = 8 e grava o novo valor 8 de volta na variável x.",
                                                                10 },

                                                { moduloId, "Atribuição", "Médio",
                                                                "Analise o algoritmo:\npontos <- 100\npontos <- pontos + 50\npontos <- pontos - 20\nO valor final de pontos será:",
                                                                "150", "130", "100", "80",
                                                                "B", "100 + 50 = 150; depois 150 - 20 = 130.", 11 },

                                                { moduloId, "Atribuição", "Difícil",
                                                                "Se executarmos:\na <- 10\nb <- 20\na <- b\nb <- a\nQuais serão os valores finais de a e b respectivamente?",
                                                                "a = 20 e b = 10", "a = 20 e b = 20", "a = 10 e b = 10",
                                                                "a = 10 e b = 20",
                                                                "B",
                                                                "Quando 'a <- b' roda, 'a' passa a valer 20. Na linha seguinte 'b <- a', 'b' recebe o valor atual de 'a' (que já é 20).",
                                                                12 },

                                                // Categoria 4: Entrada de Dados (4 questões)
                                                { moduloId, "Entrada de Dados", "Fácil",
                                                                "Qual é a função do comando leia() em um pseudocódigo?",
                                                                "Ler um texto em voz alta pelo computador.",
                                                                "Capturar um dado fornecido pelo usuário e armazená-lo em uma variável.",
                                                                "Imprimir uma mensagem na tela.",
                                                                "Apagar os dados da memória.",
                                                                "B",
                                                                "O comando leia() realiza a entrada de dados vinda do teclado/usuário.",
                                                                13 },

                                                { moduloId, "Entrada de Dados", "Fácil",
                                                                "Complete o comando para receber o ano atual do usuário:\n_______(anoAtual)",
                                                                "escreva", "leia", "imprima", "inicio",
                                                                "B",
                                                                "O comando leia(anoAtual) recebe o valor digitado e guarda na variável anoAtual.",
                                                                14 },

                                                { moduloId, "Entrada de Dados", "Médio",
                                                                "Em relação ao fluxo de dados, o comando leia() se enquadra em qual etapa?",
                                                                "Saída", "Processamento", "Entrada", "Compilação",
                                                                "C",
                                                                "O leia() é o responsável direto pela Entrada de dados no programa.",
                                                                15 },

                                                { moduloId, "Entrada de Dados", "Difícil",
                                                                "No trecho:\nleia(idade)\nidade <- idade + 1\nescreva(idade)\nSe o usuário digitar 20, o que será impresso?",
                                                                "20", "21", "201", "idade",
                                                                "B",
                                                                "A entrada recebe 20, a atribuição calcula 20 + 1 = 21 e escreva exibe 21.",
                                                                16 },

                                                // Categoria 5: Interpretação de Algoritmos (4 questões)
                                                { moduloId, "Interpretação de Algoritmos", "Médio",
                                                                "Analise o algoritmo:\ninicio\n    a <- 4\n    b <- 2\n    res <- a * b + 2\n    escreva(res)\nfim\nQual valor será impresso?",
                                                                "10", "16", "8", "12",
                                                                "A",
                                                                "A multiplicação tem precedência: 4 * 2 = 8, depois 8 + 2 = 10.",
                                                                17 },

                                                { moduloId, "Interpretação de Algoritmos", "Médio",
                                                                "Para um programa de compra onde:\npreco <- 15\nquantidade <- 3\ntotal <- preco * quantidade\nescreva(total)\nQual será a saída?",
                                                                "18", "45", "153", "30",
                                                                "B",
                                                                "O total é obtido multiplicando 15 por 3, resultando em 45.",
                                                                18 },

                                                { moduloId, "Interpretação de Algoritmos", "Difícil",
                                                                "Considere:\nx <- 10\ny <- 5\nx <- x - y\ny <- x + y\nAo final, x e y valem, respectivamente:",
                                                                "x = 5 e y = 10", "x = 10 e y = 5", "x = 5 e y = 5",
                                                                "x = 0 e y = 15",
                                                                "A",
                                                                "Primeiro: x = 10 - 5 = 5. Em seguida: y = 5 + 5 = 10.",
                                                                19 },

                                                { moduloId, "Interpretação de Algoritmos", "Difícil",
                                                                "Qual das opções descreve corretamente a sequência necessária para calcular o dobro de um número digitado?",
                                                                "escreva(n) -> leia(n) -> dobro <- n * 2",
                                                                "leia(n) -> dobro <- n * 2 -> escreva(dobro)",
                                                                "dobro <- n * 2 -> leia(n) -> escreva(dobro)",
                                                                "leia(dobro) -> escreva(n * 2)",
                                                                "B",
                                                                "Primeiro lê-se a entrada n, depois calcula-se o processamento 'dobro <- n * 2' e por fim exibe-se a saída escreva(dobro).",
                                                                20 }
                                };

                                try (var questaoStmt = conn.prepareStatement(insertSql)) {
                                        for (Object[] q : questoesMundo2) {
                                                questaoStmt.setInt(1, (Integer) q[0]);
                                                questaoStmt.setString(2, (String) q[1]);
                                                questaoStmt.setString(3, (String) q[2]);
                                                questaoStmt.setString(4, (String) q[3]);
                                                questaoStmt.setString(5, (String) q[4]);
                                                questaoStmt.setString(6, (String) q[5]);
                                                questaoStmt.setString(7, (String) q[6]);
                                                questaoStmt.setString(8, (String) q[7]);
                                                questaoStmt.setString(9, (String) q[8]);
                                                questaoStmt.setString(10, (String) q[9]);
                                                questaoStmt.setInt(11, (Integer) q[10]);
                                                questaoStmt.addBatch();
                                        }
                                        questaoStmt.executeBatch();
                                }
                        }
                }
        }

}
