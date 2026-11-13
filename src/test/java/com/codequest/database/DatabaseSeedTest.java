package com.codequest.database;

import com.codequest.database.seed.Modulo1Seed;
import com.codequest.database.seed.Modulo2Seed;
import com.codequest.database.seed.Modulo3Seed;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseSeedTest {
    @Test
    void firstWorldRetainsFiveLessonsAndTwentyQuestions() throws SQLException {
        RecordingDatabase database = new RecordingDatabase(false);
        Modulo1Seed.populate(database.connection());
        assertEquals(5, database.insertions("etapas"));
        assertEquals(20, database.insertions("questoes"));
    }

    @Test
    void secondWorldRetainsFiveLessonsAndTwentyQuestions() throws SQLException {
        RecordingDatabase database = new RecordingDatabase(false);
        Modulo2Seed.populate(database.connection());
        assertEquals(5, database.insertions("etapas"));
        assertEquals(20, database.insertions("questoes"));
    }

    @Test
    void existingWorldsDoNotDuplicateLessonsOrQuestions() throws SQLException {
        RecordingDatabase database = new RecordingDatabase(true);
        Modulo1Seed.populate(database.connection());
        Modulo2Seed.populate(database.connection());
        Modulo3Seed.populate(database.connection());
        assertEquals(0, database.insertions("etapas"));
        assertEquals(0, database.insertions("questoes"));
    }

    private static class RecordingDatabase {
        private final boolean populated;
        private final List<String> insertions = new ArrayList<>();
        RecordingDatabase(boolean populated) { this.populated = populated; }

        int insertions(String table) {
            return (int) insertions.stream().filter(sql -> sql.strip().startsWith("INSERT INTO " + table)).count();
        }

        Connection connection() {
            return proxy(Connection.class, (method, args) -> switch (method) {
                case "createStatement" -> statement("");
                case "prepareStatement" -> statement((String) args[0]);
                default -> null;
            });
        }

        private PreparedStatement statement(String sql) {
            return proxy(PreparedStatement.class, (method, args) -> switch (method) {
                case "executeQuery" -> result(args == null || args.length == 0 ? sql : (String) args[0]);
                case "executeUpdate" -> { insertions.add(sql); yield 1; }
                case "addBatch" -> { insertions.add(sql); yield null; }
                case "executeBatch" -> new int[0];
                case "execute" -> { insertions.add((String) args[0]); yield false; }
                default -> null;
            });
        }

        private ResultSet result(String sql) {
            boolean[] consumed = {false};
            boolean available = populated || sql.strip().startsWith("INSERT") || sql.contains("COUNT(*)");
            return proxy(ResultSet.class, (method, args) -> switch (method) {
                case "next" -> { boolean next = available && !consumed[0]; consumed[0] = true; yield next; }
                case "getInt" -> sql.contains("COUNT(*)") ? (populated ? 20 : 0) : 1;
                default -> null;
            });
        }

        private interface Invocation { Object call(String method, Object[] args); }
        private <T> T proxy(Class<T> type, Invocation invocation) {
            return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                    (proxy, method, args) -> invocation.call(method.getName(), args)));
        }
    }
}
