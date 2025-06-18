package hexlet.code.repository;

import hexlet.code.model.UrlCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class UrlCheckRepository extends BaseRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlCheckRepository.class);

    public static void save(UrlCheck urlCheck) throws SQLException {
        LOGGER.info("Saving UrlCheck: {}", urlCheck);
        String sql = "INSERT INTO url_checks ( status_code, title,"
                + " description, h1, url_id, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, urlCheck.getStatusCode());
            stmt.setString(2, urlCheck.getTitle());
            stmt.setString(3, urlCheck.getH1());
            stmt.setString(4, urlCheck.getDescription());
            stmt.setLong(5, urlCheck.getUrlId());
            var createdAt = LocalDateTime.now();
            stmt.setTimestamp(6, Timestamp.valueOf(createdAt));
            stmt.executeUpdate();
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
        LOGGER.info("Getting checks for URL ID: {}", urlId);
        String sql = "SELECT * FROM url_checks WHERE url_id = ? ORDER BY created_at DESC";
        var checks = new ArrayList<UrlCheck>();
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, urlId);
            var resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var statusCode = resultSet.getInt("status_code");
                var title = resultSet.getString("title");
                var h1 = resultSet.getString("h1");
                var description = resultSet.getString("description");
                var createAt = resultSet.getTimestamp("created_at").toLocalDateTime();
                var dataToSave = new UrlCheck(statusCode, title, h1, description, urlId, createAt);
                dataToSave.setId(id);
                checks.add(dataToSave);
            }
            return checks;
        }
    }

    public static Map<Long, UrlCheck> getLastChecksForAllUrls() throws SQLException {
        LOGGER.info("Getting last checks for all URLs");
        String sql = "SELECT DISTINCT ON (url_id) * FROM url_checks ORDER BY url_id, created_at DESC";
        var lastChecks = new HashMap<Long, UrlCheck>();
        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                var listOfUrls = new UrlCheck();
                listOfUrls.setId(resultSet.getLong("id"));
                listOfUrls.setStatusCode(resultSet.getInt("status_code"));
                listOfUrls.setTitle(resultSet.getString("title"));
                listOfUrls.setH1(resultSet.getString("h1"));
                listOfUrls.setDescription(resultSet.getString("description"));
                listOfUrls.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                var urlId = resultSet.getLong("url_id");
                listOfUrls.setUrlId(urlId);
                lastChecks.put(urlId, listOfUrls);
            }
            return lastChecks;
        } catch (SQLException e) {
            LOGGER.error("Failed to get last checks for all URLs", e);
            throw e;
        }
    }
}
