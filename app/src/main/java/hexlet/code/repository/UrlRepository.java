package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, url.getName());
            var createdAt = LocalDateTime.now();
            stmt.setTimestamp(2, Timestamp.valueOf(createdAt));
            stmt.executeUpdate();

            var generateKeys = stmt.getGeneratedKeys();
            if (generateKeys.next()) {
                url.setId(generateKeys.getLong(1));
                url.setCreatedAt(createdAt);
            } else {
                throw new SQLException("Creating url failed, no ID obtained.");
            }
        }
    }

    public static Optional<Url> findByName(String name) throws SQLException {
        String sql = "SELECT * FROM urls WHERE name = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                var createAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                var id = resultSet.getLong("id");
                var url = new Url(id, name, createAt);
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

//    private static Url mapRowToUrl(ResultSet resultSet) throws SQLException {
//        Url url = new Url(
//                resultSet.getString("name")
//        );
//        url.setId(resultSet.getLong("id"));
//        url.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
//        return url;
//    }

    public static Optional<Url> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM urls WHERE id = ?";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                var createAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                var name = resultSet.getString("name");
                var url = new Url(id, name, createAt);
                return Optional.of(url);
            }
            return Optional.empty();
        }
    }

    public static List<Url> getEntities() throws SQLException {
        var sql = "SELECT * FROM urls ORDER BY created_at ASC";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var result = new ArrayList<Url>();
            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                var url = new Url(name, createdAt);
                url.setId(id);
                url.setName(name);
                result.add(url);
            }
            return result;
        }
    }
}

