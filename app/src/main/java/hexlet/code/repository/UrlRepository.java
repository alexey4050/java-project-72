package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UrlRepository extends BaseRepository {
    public static void save(Url url) throws SQLException {
        String sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";
        try (var conn = getDataSource().getConnection();
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
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }
}
