package hexlet.code.util;

import hexlet.code.repository.BaseRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Collectors;

public final class DataBase {
    private static final String SCHEMA_FILE = "schema.sql";

    private DataBase() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void runMigrations() throws SQLException, IOException {
        String sql = readResourceFile(SCHEMA_FILE);
        try (Connection conn = BaseRepository.dataSource.getConnection();
            var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static String readResourceFile(String fileName) throws IOException {
        try (InputStream inputStream = DataBase.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void cleanBase() throws SQLException {
        try (var conn = BaseRepository.dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM url_checks");
            stmt.execute("DELETE FROM urls");
        }
    }
}
