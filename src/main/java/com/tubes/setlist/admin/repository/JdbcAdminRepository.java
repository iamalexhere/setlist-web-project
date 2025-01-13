package com.tubes.setlist.admin.repository;

import com.tubes.setlist.admin.model.UserManagement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.LinkedHashMap;

@Repository
public class JdbcAdminRepository implements AdminRepository {
    private final JdbcTemplate jdbcTemplate;

    public JdbcAdminRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private final RowMapper<UserManagement> userRowMapper = (rs, rowNum) ->
        new UserManagement(
            rs.getLong("id_user"),
            rs.getLong("id_role"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getBoolean("is_deleted")
        );

    @Override
    public List<UserManagement> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public UserManagement getUserById(Long userId) {
        String sql = "SELECT * FROM users WHERE id_user = ?";
        return jdbcTemplate.queryForObject(sql, userRowMapper, userId);
    }

    @Override
    public void blockUser(Long userId) {
        String sql = "UPDATE users SET is_deleted = true WHERE id_user = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void unblockUser(Long userId) {
        String sql = "UPDATE users SET is_deleted = false WHERE id_user = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public void deleteUser(Long userId) {
        String sql = "UPDATE users SET is_deleted = true WHERE id_user = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        String totalUsersSql = "SELECT COUNT(*) FROM users WHERE is_deleted = false";
        String newUsersThisMonthSql = "SELECT COUNT(*) FROM users WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE)";
        
        stats.put("totalUsers", jdbcTemplate.queryForObject(totalUsersSql, Long.class));
        stats.put("newUsersThisMonth", jdbcTemplate.queryForObject(newUsersThisMonthSql, Long.class));
        
        return stats;
    }

    @Override
    public Map<String, Long> getContentStatistics() {
        Map<String, Long> stats = new HashMap<>();
        
        String artistsSql = "SELECT COUNT(*) FROM artists WHERE is_deleted = false";
        String eventsSql = "SELECT COUNT(*) FROM events WHERE is_deleted = false";
        String setlistsSql = "SELECT COUNT(*) FROM setlists WHERE is_deleted = false";
        
        stats.put("totalArtists", jdbcTemplate.queryForObject(artistsSql, Long.class));
        stats.put("totalEvents", jdbcTemplate.queryForObject(eventsSql, Long.class));
        stats.put("totalSetlists", jdbcTemplate.queryForObject(setlistsSql, Long.class));
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getActivityLog(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT u.username, c.comment_text as action, c.comment_date as action_date 
            FROM comments c 
            JOIN users u ON c.id_user = u.id_user 
            WHERE c.comment_date BETWEEN ? AND ? 
            ORDER BY c.comment_date DESC
        """;
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    @Override
    public void approveContent(Long contentId, String contentType) {
        // Since we don't have an approval system in the schema, we'll just verify the content exists
        String sql = switch(contentType.toLowerCase()) {
            case "artist" -> "SELECT COUNT(*) FROM artists WHERE id_artist = ? AND is_deleted = false";
            case "event" -> "SELECT COUNT(*) FROM events WHERE id_event = ? AND is_deleted = false";
            case "setlist" -> "SELECT COUNT(*) FROM setlists WHERE id_setlist = ? AND is_deleted = false";
            default -> throw new IllegalArgumentException("Invalid content type: " + contentType);
        };
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, contentId);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("Content not found or already deleted");
        }
    }

    @Override
    public void rejectContent(Long contentId, String contentType) {
        String sql = switch(contentType.toLowerCase()) {
            case "artist" -> "UPDATE artists SET is_deleted = true WHERE id_artist = ?";
            case "event" -> "UPDATE events SET is_deleted = true WHERE id_event = ?";
            case "setlist" -> "UPDATE setlists SET is_deleted = true WHERE id_setlist = ?";
            default -> throw new IllegalArgumentException("Invalid content type: " + contentType);
        };
        jdbcTemplate.update(sql, contentId);
    }

    @Override
    public List<Map<String, Object>> getPendingApprovals() {
        String sql = """
            SELECT 
                'setlist' as content_type, 
                s.id_setlist as content_id, 
                s.setlist_name as content_name,
                e.edit_description,
                u.username as submitted_by,
                e.date_added::timestamp as submitted_at,
                e.status
            FROM setlists s
            JOIN edits e ON s.id_setlist = e.id_setlist
            JOIN users u ON e.id_user = u.id_user
            WHERE s.is_deleted = false
            ORDER BY e.date_added DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public void logAdminAction(Long adminId, String action) {
        String sql = "INSERT INTO comments (id_setlist, id_user, comment_text) VALUES (1, ?, ?)";
        jdbcTemplate.update(sql, adminId, action);
    }

    @Override
    public List<Map<String, Object>> getAdminActionLog(LocalDateTime startDate, LocalDateTime endDate) {
        String sql = """
            SELECT u.username, c.comment_text as action, c.comment_date as action_date 
            FROM comments c 
            JOIN users u ON c.id_user = u.id_user 
            WHERE u.id_role = (SELECT id_role FROM roles WHERE role_name = 'Admin')
            AND c.comment_date BETWEEN ? AND ? 
            ORDER BY c.comment_date DESC
        """;
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    @Override
    public Map<String, Integer> getSetlistCountByArtist() {
        try {
            String sql = """
                SELECT a.artist_name, COUNT(s.id_setlist) as setlist_count 
                FROM artists a 
                LEFT JOIN setlists s ON a.id_artist = s.id_artist 
                WHERE a.is_deleted = false 
                GROUP BY a.artist_name, a.id_artist
                HAVING COUNT(s.id_setlist) > 0
                ORDER BY setlist_count DESC 
                LIMIT 10
                """;
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            Map<String, Integer> result = new LinkedHashMap<>();
            
            for (Map<String, Object> row : rows) {
                String artistName = (String) row.get("artist_name");
                Integer count = ((Number) row.get("setlist_count")).intValue();
                result.put(artistName, count);
            }
            
            System.out.println("Setlist count query result: " + result);
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in getSetlistCountByArtist: " + e.getMessage());
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Integer> getEventCountByVenue() {
        try {
            String sql = """
                SELECT v.venue_name, COUNT(e.id_event) as event_count 
                FROM venues v 
                LEFT JOIN events e ON v.id_venue = e.id_venue 
                GROUP BY v.venue_name, v.id_venue
                HAVING COUNT(e.id_event) > 0
                ORDER BY event_count DESC 
                LIMIT 10
                """;
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            Map<String, Integer> result = new LinkedHashMap<>();
            
            for (Map<String, Object> row : rows) {
                String venueName = (String) row.get("venue_name");
                Integer count = ((Number) row.get("event_count")).intValue();
                result.put(venueName, count);
            }
            
            System.out.println("Event count query result: " + result);
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in getEventCountByVenue: " + e.getMessage());
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Integer> getSongCountByArtist() {
        try {
            String sql = """
                SELECT a.artist_name, COUNT(s.id_song) as song_count 
                FROM artists a 
                LEFT JOIN songs s ON a.id_artist = s.id_artist
                WHERE a.is_deleted = false 
                GROUP BY a.artist_name, a.id_artist
                HAVING COUNT(s.id_song) > 0
                ORDER BY song_count DESC 
                LIMIT 10
                """;
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            Map<String, Integer> result = new LinkedHashMap<>();
            
            for (Map<String, Object> row : rows) {
                String artistName = (String) row.get("artist_name");
                Integer count = ((Number) row.get("song_count")).intValue();
                result.put(artistName, count);
            }
            
            System.out.println("Song count query result: " + result);
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in getSongCountByArtist: " + e.getMessage());
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Integer> getMonthlySetlistCount() {
        try {
            String sql = """
                SELECT TO_CHAR(created_at, 'YYYY-MM') as month, 
                       COUNT(*) as setlist_count 
                FROM setlists 
                WHERE is_deleted = false 
                  AND created_at >= NOW() - INTERVAL '12 months'
                GROUP BY month 
                ORDER BY month DESC
                """;
            
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            Map<String, Integer> result = new LinkedHashMap<>();
            
            for (Map<String, Object> row : rows) {
                String month = (String) row.get("month");
                Integer count = ((Number) row.get("setlist_count")).intValue();
                result.put(month, count);
            }
            
            System.out.println("Monthly setlist count result: " + result);
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in getMonthlySetlistCount: " + e.getMessage());
            e.printStackTrace();
            return new LinkedHashMap<>();
        }
    }
}
