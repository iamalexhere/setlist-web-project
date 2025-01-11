package com.tubes.setlist.admin.repository;

import com.tubes.setlist.admin.model.UserManagement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Sql({"/schema.sql", "/mockup_new.sql"})
class JdbcAdminRepositoryTest {

    @Autowired
    private DataSource dataSource;

    private JdbcAdminRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcAdminRepository(dataSource);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        List<UserManagement> users = repository.getAllUsers();
        assertFalse(users.isEmpty());
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        UserManagement user = repository.getUserById(1L);
        assertNotNull(user);
        assertEquals(1L, user.getIdUser());
    }

    @Test
    void blockUser_ShouldMarkUserAsDeleted() {
        repository.blockUser(1L);
        UserManagement user = repository.getUserById(1L);
        assertTrue(user.isDeleted());
    }

    @Test
    void unblockUser_ShouldMarkUserAsNotDeleted() {
        repository.blockUser(1L);
        repository.unblockUser(1L);
        UserManagement user = repository.getUserById(1L);
        assertFalse(user.isDeleted());
    }

    @Test
    void getUserStatistics_ShouldReturnValidStats() {
        Map<String, Long> stats = repository.getUserStatistics();
        assertNotNull(stats.get("totalUsers"));
        assertNotNull(stats.get("newUsersThisMonth"));
    }

    @Test
    void getContentStatistics_ShouldReturnValidStats() {
        Map<String, Long> stats = repository.getContentStatistics();
        assertNotNull(stats.get("totalArtists"));
        assertNotNull(stats.get("totalEvents"));
        assertNotNull(stats.get("totalSetlists"));
    }

    @Test
    void approveContent_WithValidArtist_ShouldVerifyContent() {
        // Since we don't have approval column, we just verify the content exists
        assertDoesNotThrow(() -> repository.approveContent(1L, "artist"));
    }

    @Test
    void rejectContent_WithValidEvent_ShouldMarkAsDeleted() {
        repository.rejectContent(1L, "event");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Boolean isDeleted = jdbcTemplate.queryForObject(
            "SELECT is_deleted FROM events WHERE id_event = ?",
            Boolean.class,
            1L
        );
        assertTrue(isDeleted);
    }

    @Test
    void getPendingApprovals_ShouldReturnContent() {
        List<Map<String, Object>> pendingContent = repository.getPendingApprovals();
        assertNotNull(pendingContent);
    }

    @Test
    void logAdminAction_ShouldCreateNewComment() {
        Long adminId = 1L;
        String action = "TEST_ACTION";
        
        repository.logAdminAction(adminId, action);
        
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comments WHERE id_user = ? AND comment_text = ?",
            Integer.class,
            adminId,
            action
        );
        assertEquals(1, count);
    }

    @Test
    void getAdminActionLog_ShouldReturnActionsBetweenDates() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<Map<String, Object>> actionLog = repository.getAdminActionLog(startDate, endDate);
        assertNotNull(actionLog);
    }
}
