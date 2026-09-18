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
                    "Mostrar que programar é ensinar o computador a resolver problemas por meio de passos lógicos.");
            stmt.setString(5,
                    "Antes de escrever código em qualquer linguagem, precisamos organizar as ideias e entender o caminho para chegar ao resultado esperado. Lógica de programação é a forma como estruturamos esse raciocínio.");
            stmt.setString(6,
                    "Fazer um bolo seguindo a ordem certa dos ingredientes e do modo de preparo.");
            stmt.setInt(7, 1);
            stmt.executeUpdate();

            // Etapa 2
            stmt.setInt(1, moduloId);
            stmt.setInt(2, 2);
            stmt.setString(3, "O que é um Algoritmo?");
            stmt.setString(4,
                    "Explicar que um algoritmo é uma sequência finita de passos bem definidos.");
            stmt.setString(5,
                    "Um algoritmo precisa ser claro, ter começo, meio e fim, e seguir uma ordem que faça sentido. Se invertermos os passos, o resultado pode sair totalmente errado!");
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
                    "Apresentar uma forma textual de escrever algoritmos usando português estruturado.");
            stmt.setString(5,
                    "O pseudocódigo é uma ponte entre a nossa língua e o código real de um computador. Usamos comandos como 'escreva' para exibir algo na tela e 'leia' para receber dados.");
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
                    "Qualquer sistema segue esse ciclo:\n1. Entrada: dados que o usuário ou sistema fornece\n2. Processamento: cálculos, verificações ou transformações\n3. Saída: apresentação do resultado final");
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
                        "Uma receita de bolo.", "Um teclado de computador.", "Uma foto de família.",
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
                        "Saída -> Entrada -> Processamento", "Entrada -> Processamento -> Saída",
                        "Processamento -> Entrada -> Saída", "Saída -> Processamento -> Entrada",
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
                        "Decorar comandos de uma linguagem.", "Aprender apenas a usar computadores.",
                        "Organizar o raciocínio para resolver problemas antes da implementação.",
                        "Criar interfaces gráficas.",
                        "C",
                        "Lógica de programação desenvolve a habilidade de estruturar soluções antes da codificação.",
                        17 },
                // Q18
                { moduloId, "Algoritmos", "Difícil",
                        "Qual destas atividades NÃO pode ser descrita por um algoritmo?",
                        "Preparar um bolo.", "Escovar os dentes.",
                        "Escolher aleatoriamente um número sem qualquer critério.", "Calcular uma média.",
                        "C",
                        "Sem critérios ou passos determinados, uma ação totalmente caótica/sem lógica não forma um algoritmo.",
                        18 },
                // Q19
                { moduloId, "Entrada e Saída", "Difícil",
                        "Qual sequência está correta quanto ao processamento de dados?",
                        "Saída -> Processamento -> Entrada", "Entrada -> Saída -> Processamento",
                        "Entrada -> Processamento -> Saída", "Processamento -> Saída -> Entrada",
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
        String checkSql = "SELECT id FROM modulos WHERE numero = 2";
        try (Statement stmt = conn.createStatement();
                var rs = stmt.executeQuery(checkSql)) {
            if (rs.next()) {
                return; // Módulo 2 já existe
            }
        }

        String insertModuloSql = """
                INSERT INTO modulos (numero, titulo, descricao, topicos, disponivel, ordem)
                VALUES (2, 'Módulo 2 (Em Desenvolvimento)', 'Este módulo está em desenvolvimento e logo estará disponível para você.', 0, FALSE, 2)
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
