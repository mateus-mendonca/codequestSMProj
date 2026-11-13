package com.codequest.database.seed;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class Modulo3Seed {
        public static void populate(Connection conn) throws SQLException {
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

}
