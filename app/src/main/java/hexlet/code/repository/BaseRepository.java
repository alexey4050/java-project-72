package hexlet.code.repository;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;

public class BaseRepository {
    private static HikariDataSource dataSource;

    public static void setDataSource(HikariDataSource dataSource) {
        BaseRepository.dataSource = dataSource;
    }

    public static HikariDataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource;
    }
}
