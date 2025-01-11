package com.tubes.setlist.admin.repository;

import com.tubes.setlist.admin.model.Statistics;
import com.tubes.setlist.admin.model.UserManagement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            SELECT 'artist' as content_type, id_artist as content_id, artist_name as content_name, created_at 
            FROM artists WHERE is_deleted = false
            UNION ALL
            SELECT 'event' as content_type, id_event as content_id, event_name as content_name, created_at 
            FROM events WHERE is_deleted = false
            UNION ALL
            SELECT 'setlist' as content_type, id_setlist as content_id, setlist_name as content_name, created_at 
            FROM setlists WHERE is_deleted = false
            ORDER BY created_at DESC
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
    public Statistics getStatistics() {
        Statistics stats = new Statistics();
        
        // Get total counts
        stats.setTotalUsers(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE deleted = false", Integer.class));
        
        stats.setTotalArtists(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artists WHERE deleted = false", Integer.class));
        
        stats.setTotalSetlists(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM setlists WHERE deleted = false", Integer.class));
        
        // Calculate growth (comparing with last month)
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        
        int lastMonthUsers = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE created_at < ? AND deleted = false",
            Integer.class, lastMonth);
        
        int lastMonthArtists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM artists WHERE created_at < ? AND deleted = false",
            Integer.class, lastMonth);
        
        int lastMonthSetlists = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM setlists WHERE created_at < ? AND deleted = false",
            Integer.class, lastMonth);
        
        // Calculate growth percentages
        stats.setUserGrowth(calculateGrowth(lastMonthUsers, stats.getTotalUsers()));
        stats.setArtistGrowth(calculateGrowth(lastMonthArtists, stats.getTotalArtists()));
        stats.setSetlistGrowth(calculateGrowth(lastMonthSetlists, stats.getTotalSetlists()));
        
        return stats;
    }
    
    private double calculateGrowth(int oldValue, int newValue) {
        if (oldValue == 0) return 100.0; // If starting from zero, growth is 100%
        return ((double)(newValue - oldValue) / oldValue) * 100.0;
    }
    
    @Override
    public byte[] generateReport() {
        // TODO: Implement report generation using Apache POI or similar library
        // For now, return empty byte array
        return new byte[0];
    }
}
