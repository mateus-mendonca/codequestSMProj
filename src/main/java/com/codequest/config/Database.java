package com.codequest.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Configuração central de acesso ao banco de dados PostgreSQL.
 * Lê as credenciais de variáveis de ambiente (.env ou sistema)
 * e executa as migrations (CREATE TABLE IF NOT EXISTS) na inicialização.
 */
public class Database {

        private static HikariDataSource dataSource;
        private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        public static void init() {
                String host = env("DB_HOST", "localhost");
                String port = env("DB_PORT", "5432");
                String name = env("DB_NAME", "codequest");
                String user = env("DB_USER", "postgres");
                String pass = env("DB_PASSWORD", "postgres");

                HikariConfig config = new HikariConfig();
                config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + name);
                config.setUsername(user);
                config.setPassword(pass);
                config.setMaximumPoolSize(10);
                config.setDriverClassName("org.postgresql.Driver");

                dataSource = new HikariDataSource(config);

                migrate();
        }

        public static Connection getConnection() throws SQLException {
                return dataSource.getConnection();
        }

        private static void migrate() {
                String createModulos = """
                                CREATE TABLE IF NOT EXISTS modulos (
                                    id SERIAL PRIMARY KEY,
                                    numero INT NOT NULL,
                                    titulo VARCHAR(255) NOT NULL,
                                    descricao TEXT,
                                    topicos INT NOT NULL DEFAULT 0,
                                    disponivel BOOLEAN NOT NULL DEFAULT FALSE,
                                    ordem INT NOT NULL DEFAULT 0
                                )
                                """;

                String createQuestoes = """
                                CREATE TABLE IF NOT EXISTS questoes (
                                    id SERIAL PRIMARY KEY,
                                    modulo_id INT NOT NULL REFERENCES modulos(id) ON DELETE CASCADE,
                                    categoria VARCHAR(100) DEFAULT 'Geral',
                                    nivel VARCHAR(50) DEFAULT 'Fácil',
                                    enunciado TEXT NOT NULL,
                                    alternativa_a VARCHAR(255) NOT NULL,
                                    alternativa_b VARCHAR(255) NOT NULL,
                                    alternativa_c VARCHAR(255) NOT NULL,
                                    alternativa_d VARCHAR(255) NOT NULL,
                                    correta CHAR(1) NOT NULL,
                                    explicacao TEXT,
                                    ordem INT NOT NULL DEFAULT 0
                                )
                                """;

                String createEtapas = """
                                CREATE TABLE IF NOT EXISTS etapas (
                                    id SERIAL PRIMARY KEY,
                                    modulo_id INT NOT NULL REFERENCES modulos(id) ON DELETE CASCADE,
                                    numero INT NOT NULL,
                                    titulo VARCHAR(255) NOT NULL,
                                    objetivo TEXT,
                                    explicacao TEXT,
                                    exemplo TEXT,
                                    ordem INT NOT NULL DEFAULT 0
                                )
                                """;

                String createProgresso = """
                                CREATE TABLE IF NOT EXISTS progresso_usuario (
                                    id SERIAL PRIMARY KEY,
                                    usuario_id VARCHAR(100) NOT NULL DEFAULT 'aluno_demo',
                                    modulo_id INT NOT NULL REFERENCES modulos(id) ON DELETE CASCADE,
                                    xp_total INT NOT NULL DEFAULT 0,
                                    estrelas INT NOT NULL DEFAULT 0,
                                    concluido BOOLEAN NOT NULL DEFAULT FALSE,
                                    nota_maxima BOOLEAN NOT NULL DEFAULT FALSE,
                                    sem_perder_vidas BOOLEAN NOT NULL DEFAULT FALSE,
                                    tentativas INT NOT NULL DEFAULT 0,
                                    CONSTRAINT unq_user_modulo UNIQUE(usuario_id, modulo_id)
                                )
                                """;

                try (Connection conn = getConnection();
                                Statement stmt = conn.createStatement()) {
                        stmt.execute(createModulos);
                        stmt.execute(createQuestoes);
                        stmt.execute(createEtapas);
                        stmt.execute(createProgresso);

                        // Garantir colunas em bancos existentes
                        stmt.execute("ALTER TABLE questoes ADD COLUMN IF NOT EXISTS categoria VARCHAR(100) DEFAULT 'Geral';");
                        stmt.execute("ALTER TABLE questoes ADD COLUMN IF NOT EXISTS nivel VARCHAR(50) DEFAULT 'Fácil';");
                        stmt.execute("ALTER TABLE questoes ADD COLUMN IF NOT EXISTS explicacao TEXT;");
                        stmt.execute("ALTER TABLE progresso_usuario ADD COLUMN IF NOT EXISTS etapa_maxima_desbloqueada INT NOT NULL DEFAULT 1;");

                        seedModulo1(conn);
                        seedModulo2(conn);
                        seedModulo3(conn);
                } catch (SQLException e) {
                        throw new RuntimeException("Falha ao rodar as migrations do banco", e);
                }
        }

        private static void seedModulo1(Connection conn) throws SQLException {
                String checkSql = "SELECT id FROM modulos WHERE numero = 1";
                try (Statement stmt = conn.createStatement();
                                var rs = stmt.executeQuery(checkSql)) {
                        if (rs.next()) {
                                return; // Módulo 1 já existe
                        }
                }

                String insertModuloSql = """
                                INSERT INTO modulos (numero, titulo, descricao, topicos, disponivel, ordem)
                                VALUES (1, 'Introdução à Lógica de Programação', 'Aprenda a organizar uma solução em pequenas etapas e entenda o pensamento computacional.', 5, TRUE, 1)
                                RETURNING id
                                """;

                int moduloId;
                try (Statement stmt = conn.createStatement();
                                var rs = stmt.executeQuery(insertModuloSql)) {
                        if (rs.next()) {
                                moduloId = rs.getInt(1);
                        } else {
                                return;
                        }
                }

                String insertEtapaSql = """
                                INSERT INTO etapas (modulo_id, numero, titulo, objetivo, explicacao, exemplo, ordem)
                                VALUES (?, ?, ?, ?, ?, ?, ?)
                                """;

                try (var stmt = conn.prepareStatement(insertEtapaSql)) {
                        // Etapa 1
                        stmt.setInt(1, moduloId);
                        stmt.setInt(2, 1);
                        stmt.setString(3, "O que é Lógica de Programação?");
                        stmt.setString(4,
                                        "Mostrar que programar é ensinar o computador a resolver problemas por meio de instruções claras e organizadas.");
                        stmt.setString(5,
                                        "Computadores são super-rápidos, mas eles só fazem aquilo que mandamos. Lógica de programação é a arte de dar instruções tão claras que até a máquina entenda e acerte o caminho.");
                        stmt.setString(6,
                                        "Fazer um bolo seguindo a ordem certa dos ingredientes e do modo de preparo.");
                        stmt.setInt(7, 1);
                        stmt.executeUpdate();

                        // Etapa 2
                        stmt.setInt(1, moduloId);
                        stmt.setInt(2, 2);
                        stmt.setString(3, "O que é um Algoritmo?");
                        stmt.setString(4,
                                        "Explicar que um algoritmo é um guia com instruções claras e organizadas.");
                        stmt.setString(5,
                                        "Pense num algoritmo como o guia de uma missão ou uma receita de bolo: você não pode assar a massa antes de quebrar os ovos. A ordem das ações muda tudo!");
                        stmt.setString(6,
                                        "Algoritmo para escovar os dentes:\n1. Pegar a escova e a pasta\n2. Colocar a pasta na escova\n3. Escovar os dentes\n4. Enxaguar a boca\n5. Guardar a escova");
                        stmt.setInt(7, 2);
                        stmt.executeUpdate();

                        // Etapa 3
                        stmt.setInt(1, moduloId);
                        stmt.setInt(2, 3);
                        stmt.setString(3, "Fluxogramas");
                        stmt.setString(4,
                                        "Apresentar uma forma visual e intuitiva de desenhar algoritmos antes de programar.");
                        stmt.setString(5,
                                        "Um fluxograma usa formas geométricas conectadas por setas para ilustrar o caminho das decisões e ações do programa.\n\n• Início / Fim: formato oval\n• Ação / Processamento: retângulo\n• Decisão / Condição: losango\n• Setas: indicam o fluxo de execução");
                        stmt.setString(6,
                                        "Fluxograma simples de decisão:\n[Início] -> [Está chovendo?] \n  Se SIM -> [Levar guarda-chuva] -> [Fim]\n  Se NÃO -> [Sair normalmente] -> [Fim]");
                        stmt.setInt(7, 3);
                        stmt.executeUpdate();

                        // Etapa 4
                        stmt.setInt(1, moduloId);
                        stmt.setInt(2, 4);
                        stmt.setString(3, "Pseudocódigo");
                        stmt.setString(4,
                                        "Apresentar uma forma de escrever algoritmos usando linguagem natural.");
                        stmt.setString(5,
                                        "O pseudocódigo é uma forma simples de representar um algoritmo, usando palavras comuns do dia a dia. Sem se preocupar com sintaxes difíceis e linguagens de programação reais");
                        stmt.setString(6,
                                        "algoritmo\ninicio\n    escreva(\"Olá, mundo!\")\nfim");
                        stmt.setInt(7, 4);
                        stmt.executeUpdate();

                        // Etapa 5
                        stmt.setInt(1, moduloId);
                        stmt.setInt(2, 5);
                        stmt.setString(3, "Entrada, Processamento e Saída");
                        stmt.setString(4,
                                        "Mostrar a estrutura básica de funcionamento da grande maioria dos programas.");
                        stmt.setString(5,
                                        "O ciclo vital de qualquer app ou jogo: você aperta um botão (Entrada), o jogo calcula o dano (Processamento) e o monstro perde vida na tela (Saída).");
                        stmt.setString(6,
                                        "Calculadora:\n• Entrada: digitar 5 e 3\n• Processamento: somar 5 + 3 = 8\n• Saída: exibir 8 na tela");
                        stmt.setInt(7, 5);
                        stmt.executeUpdate();
                }

                String insertSql = """
                                INSERT INTO questoes (modulo_id, categoria, nivel, enunciado, alternativa_a, alternativa_b, alternativa_c, alternativa_d, correta, explicacao, ordem)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """;

                // Questões com categorias e justificativas pedagógicas detalhadas
                Object[][] questoesData = {
                                // Q1
                                { moduloId, "Lógica de Programação", "Fácil",
                                                "O que é lógica de programação?",
                                                "Uma linguagem de computador específica.",
                                                "A forma de organizar pensamentos para resolver um problema através de instruções.",
                                                "Um programa para navegar na internet.",
                                                "Um componente físico do computador.",
                                                "B",
                                                "Lógica de programação é a base do raciocínio estruturado que permite criar algoritmos antes de codificar.",
                                                1 },
                                // Q2
                                { moduloId, "Algoritmos", "Fácil",
                                                "O que melhor define um algoritmo?",
                                                "Um erro no código do programa.",
                                                "Uma sequência ordenada e finita de passos para resolver um problema.",
                                                "Um tipo de computador super rápido.",
                                                "Um arquivo de texto sem formatação.",
                                                "B",
                                                "Algoritmos são sequências lógicas, finitas e com passos bem determinados para atingir um objetivo.",
                                                2 },
                                // Q3
                                { moduloId, "Algoritmos", "Fácil",
                                                "Qual das opções abaixo representa um algoritmo do dia a dia?",
                                                "Uma receita de bolo.", "Um teclado de computador.",
                                                "Uma foto de família.",
                                                "Uma cadeira de escritório.",
                                                "A",
                                                "Uma receita possui ingredientes (entradas), modo de preparo (processamento/passos) e o bolo pronto (saída).",
                                                3 },
                                // Q4
                                { moduloId, "Algoritmos", "Médio",
                                                "Por que a ordem das instruções em um algoritmo é importante?",
                                                "Para deixar o texto mais bonito.",
                                                "Porque alterar a ordem pode gerar resultados incorretos ou impedir a execução.",
                                                "A ordem não importa na programação.",
                                                "Apenas para economizar memória do computador.",
                                                "B",
                                                "Computadores executam comandos sequencialmente; trocar passos lógicos altera ou quebra o resultado.",
                                                4 },
                                // Q5
                                { moduloId, "Fluxogramas", "Fácil",
                                                "Para que serve um fluxograma?",
                                                "Para medir a temperatura do computador.",
                                                "Para representar visualmente as etapas de um algoritmo por meio de figuras geométricas.",
                                                "Para tocar músicas no sistema.",
                                                "Para substituir completamente o código final.",
                                                "B",
                                                "Fluxogramas facilitam a visualização e comunicação das etapas e decisões de um algoritmo.",
                                                5 },
                                // Q6
                                { moduloId, "Fluxogramas", "Fácil",
                                                "Qual símbolo representa uma decisão em um fluxograma?",
                                                "Retângulo", "Losango", "Círculo", "Paralelogramo",
                                                "B",
                                                "O losango é o símbolo padrão para tomada de decisões e desvios condicionais.",
                                                6 },
                                // Q7
                                { moduloId, "Fluxogramas", "Fácil",
                                                "Qual símbolo normalmente representa início e fim?",
                                                "Losango", "Oval", "Retângulo", "Seta",
                                                "B",
                                                "O símbolo oval (terminador) indica os pontos de início e término de um fluxograma.",
                                                7 },
                                // Q8
                                { moduloId, "Fluxogramas", "Médio",
                                                "As setas em um fluxograma indicam:",
                                                "O tamanho do algoritmo.", "O fluxo de execução.", "Os comentários.",
                                                "A velocidade do programa.",
                                                "B",
                                                "As setas conectam os símbolos mostrando o caminho e a ordem de execução do programa.",
                                                8 },
                                // Q9
                                { moduloId, "Pseudocódigo", "Fácil",
                                                "Qual comando é utilizado para exibir uma mensagem na tela?",
                                                "leia()", "escreva()", "inicio()", "algoritmo()",
                                                "B",
                                                "O comando escreva() envia dados/textos para a saída padrão (tela).",
                                                9 },
                                // Q10
                                { moduloId, "Pseudocódigo", "Fácil",
                                                "Qual será a saída do comando escreva(\"Olá Mundo\")?",
                                                "Olá", "Olá Mundo", "Mundo", "Nada",
                                                "B",
                                                "O comando imprime exatamente o conteúdo delimitado pelas aspas.",
                                                10 },
                                // Q11
                                { moduloId, "Pseudocódigo", "Médio",
                                                "Complete corretamente:\nalgoritmo\n_______\nescreva(\"CodeQuest\")\nfim",
                                                "fim", "inicio", "algoritmo", "leia",
                                                "B",
                                                "A palavra-chave inicio marca o começo do bloco de instruções do algoritmo.",
                                                11 },
                                // Q12
                                { moduloId, "Pseudocódigo", "Médio",
                                                "Qual será a saída?\nescreva(\"Programação\")\nescreva(\"Legal\")",
                                                "Programação Legal", "Programação\nLegal", "Legal\nProgramação", "Nada",
                                                "B",
                                                "Cada instrução escreva exibe o texto na saída.",
                                                12 },
                                // Q13
                                { moduloId, "Entrada e Saída", "Fácil",
                                                "Qual opção representa corretamente o fluxo de um programa?",
                                                "Saída -> Entrada -> Processamento",
                                                "Entrada -> Processamento -> Saída",
                                                "Processamento -> Entrada -> Saída",
                                                "Saída -> Processamento -> Entrada",
                                                "B",
                                                "O ciclo de dados fundamental é receber dados (Entrada), manipulá-los (Processamento) e exibir resultados (Saída).",
                                                13 },
                                // Q14
                                { moduloId, "Entrada e Saída", "Médio",
                                                "Ao utilizar uma calculadora: Você digita dois números. Esses números representam:",
                                                "Saída", "Entrada", "Processamento", "Resultado",
                                                "B",
                                                "Os dados fornecidos pelo usuário para o sistema realizar uma ação constituem a Entrada.",
                                                14 },
                                // Q15
                                { moduloId, "Entrada e Saída", "Médio",
                                                "Depois que um programa realiza todos os cálculos, o resultado exibido é chamado de:",
                                                "Entrada", "Processamento", "Saída", "Algoritmo",
                                                "C",
                                                "A Saída é a apresentação do resultado final do processamento de dados.",
                                                15 },
                                // Q16
                                { moduloId, "Pseudocódigo", "Médio",
                                                "Qual será a saída?\nalgoritmo\ninicio\nescreva(\"Olá\")\nescreva(\"CodeQuest\")\nfim",
                                                "OláCodeQuest", "Olá\nCodeQuest", "CodeQuest\nOlá", "Nada",
                                                "B",
                                                "As duas mensagens são escritas em sequência na saída.",
                                                16 },
                                // Q17
                                { moduloId, "Lógica de Programação", "Difícil",
                                                "Qual alternativa descreve melhor o papel da lógica de programação?",
                                                "Decorar comandos de uma linguagem.",
                                                "Aprender apenas a usar computadores.",
                                                "Organizar o raciocínio para resolver problemas antes da implementação.",
                                                "Criar interfaces gráficas.",
                                                "C",
                                                "Lógica de programação desenvolve a habilidade de estruturar soluções antes da codificação.",
                                                17 },
                                // Q18
                                { moduloId, "Algoritmos", "Difícil",
                                                "Qual destas atividades NÃO pode ser descrita por um algoritmo?",
                                                "Preparar um bolo.", "Escovar os dentes.",
                                                "Escolher aleatoriamente um número sem qualquer critério.",
                                                "Calcular uma média.",
                                                "C",
                                                "Sem critérios ou passos determinados, uma ação totalmente caótica/sem lógica não forma um algoritmo.",
                                                18 },
                                // Q19
                                { moduloId, "Entrada e Saída", "Difícil",
                                                "Qual sequência está correta quanto ao processamento de dados?",
                                                "Saída -> Processamento -> Entrada",
                                                "Entrada -> Saída -> Processamento",
                                                "Entrada -> Processamento -> Saída",
                                                "Processamento -> Saída -> Entrada",
                                                "C",
                                                "A ordem cronológica dos dados é sempre Entrada -> Processamento -> Saída.",
                                                19 },
                                // Q20
                                { moduloId, "Pseudocódigo", "Difícil",
                                                "Qual afirmação está correta?",
                                                "Fluxogramas e pseudocódigo servem para representar algoritmos.",
                                                "Fluxogramas substituem totalmente os algoritmos.",
                                                "Pseudocódigo é uma linguagem de programação compilada.",
                                                "Todo algoritmo precisa ser escrito em Java.",
                                                "A",
                                                "Tanto gráficos (fluxogramas) quanto textuais (pseudocódigo) são formas válidas de representação de algoritmos.",
                                                20 }
                };

                try (var stmt = conn.prepareStatement(insertSql)) {
                        for (Object[] q : questoesData) {
                                stmt.setInt(1, (Integer) q[0]);
                                stmt.setString(2, (String) q[1]);
                                stmt.setString(3, (String) q[2]);
                                stmt.setString(4, (String) q[3]);
                                stmt.setString(5, (String) q[4]);
                                stmt.setString(6, (String) q[5]);
                                stmt.setString(7, (String) q[6]);
                                stmt.setString(8, (String) q[7]);
                                stmt.setString(9, (String) q[8]);
                                stmt.setString(10, (String) q[9]);
                                stmt.setInt(11, (Integer) q[10]);
                                stmt.addBatch();
                        }
                        stmt.executeBatch();
                }
        }

        private static void seedModulo2(Connection conn) throws SQLException {
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

        private static void seedModulo3(Connection conn) throws SQLException {
                String checkSql = "SELECT id FROM modulos WHERE numero = 3";
                try (Statement stmt = conn.createStatement();
                                var rs = stmt.executeQuery(checkSql)) {
                        if (rs.next()) {
                                return; // Módulo 3 já existe na base de dados
                        }
                }

                String insertModuloSql = """
                                INSERT INTO modulos (numero, titulo, descricao, topicos, disponivel, ordem)
                                VALUES (3, 'Operadores (Em Desenvolvimento)', 'Este mundo está em desenvolvimento e logo estará disponível para você.', 0, FALSE, 3)
                                """;

                try (Statement stmt = conn.createStatement()) {
                        stmt.execute(insertModuloSql);
                }
        }

        public static String env(String key, String defaultValue) {
                String value = null;
                try {
                        value = dotenv.get(key);
                } catch (Exception ignored) {
                }
                if (value == null || value.isBlank()) {
                        value = System.getenv(key);
                }
                if (value == null || value.isBlank()) {
                        value = System.getProperty(key);
                }
                return (value == null || value.isBlank()) ? defaultValue : value.trim();
        }
}
