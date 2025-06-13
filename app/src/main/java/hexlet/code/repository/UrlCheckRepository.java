package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UrlCheckRepository extends BaseRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlCheckRepository.class);

    public static void save(UrlCheck urlCheck) throws SQLException {
        String sql = "INSERT INTO url_checks (url_id, status_code, title, h1,"
                + " description, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, urlCheck.getUrlId());
            stmt.setInt(2, urlCheck.getStatusCode());
            stmt.setString(3, urlCheck.getTitle());
            stmt.setString(4, urlCheck.getH1());
            stmt.setString(5, urlCheck.getDescription());
            var createdAt = LocalDateTime.now();
            stmt.setTimestamp(6, Timestamp.valueOf(createdAt));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating check failed, no rows affected.");
            }

            var generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                urlCheck.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating check failed, no ID obtained.");
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to save UrlCheck for URL ID: {}", urlCheck.getUrlId(), e);
            throw e;
        }
    }

    public static List<UrlCheck> getChecksByUrlId(Long urlId) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY Id DESC";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            try (var resultSet = stmt.executeQuery()) {
                var checks = new ArrayList<UrlCheck>();
                while (resultSet.next()) {
                    checks.add(mapRowToUrlCheck(resultSet));
                }
                return checks;
            }
        }
    }

    public static Optional<UrlCheck> getLastCheckByUrlId(Long urlId) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ? "
                + "ORDER BY created_at DESC FETCH FIRST 1 ROW ONLY";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            try (var resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToUrlCheck(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to get last check for URL ID: {}", urlId, e);
            throw e;
        }
    }

    private static UrlCheck mapRowToUrlCheck(ResultSet resultSet) throws SQLException {
        UrlCheck check = new UrlCheck(
                resultSet.getInt("status_code"),
                resultSet.getString("title"),
                resultSet.getString("h1"),
                resultSet.getString("description"),
                resultSet.getLong("url_id")
        );

        check.setId(resultSet.getLong("id"));

        Timestamp timestamp = resultSet.getTimestamp("created_at");
        if (timestamp != null) {
            check.setCreatedAt(timestamp.toLocalDateTime());
        }

        return check;
    }
}
