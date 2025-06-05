package hexlet.code.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DataBase {
    private static HikariDataSource dataSource;
    private static final String DEFAULT_JDBC_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1;";
    private static final String SCHEMA_FILE = "schema.sql";

    public static DataSource getDataSource() throws SQLException, IOException {
        if (dataSource == null) {
            HikariConfig config = new HikariConfig();
            String jdbcUrl = System.getenv().getOrDefault("JDBC_DATABASE_URL", DEFAULT_JDBC_URL);
            config.setJdbcUrl(jdbcUrl);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(30000);

            dataSource = new HikariDataSource(config);
        }
        return dataSource;
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
        }
    }

    public static void runMigrations(DataSource ds) throws SQLException, IOException {
        String sql = readResourceFile(SCHEMA_FILE);
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private static String readResourceFile(String fileName) throws IOException {
        try (InputStream inputStream = DataBase.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
