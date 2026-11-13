package com.codequest.config;

import com.codequest.database.SchemaInitializer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.SQLException;

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

        SchemaInitializer.initialize();
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
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
