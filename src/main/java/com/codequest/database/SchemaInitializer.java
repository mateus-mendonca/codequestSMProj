package com.codequest.database;

import com.codequest.config.Database;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.codequest.database.seed.Modulo1Seed;
import com.codequest.database.seed.Modulo2Seed;
import com.codequest.database.seed.Modulo3Seed;

public final class SchemaInitializer {
        public static void initialize() {
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

                try (Connection conn = Database.getConnection();
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

                        Modulo1Seed.populate(conn);
                        Modulo2Seed.populate(conn);
                        Modulo3Seed.populate(conn);
                } catch (SQLException e) {
                        throw new RuntimeException("Falha ao rodar as migrations do banco", e);
                }
        }

}
