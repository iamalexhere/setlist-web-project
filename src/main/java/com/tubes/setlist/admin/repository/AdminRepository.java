package com.tubes.setlist.admin.repository;

import com.tubes.setlist.admin.model.UserManagement;
import com.tubes.setlist.member.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AdminRepository {
    // User Management
    List<UserManagement> getAllUsers();
    UserManagement getUserById(Long userId);
    void blockUser(Long userId);
    void unblockUser(Long userId);
    void deleteUser(Long userId);
    
    // Reports
    Map<String, Long> getUserStatistics();
    Map<String, Long> getContentStatistics();
    List<Map<String, Object>> getActivityLog(LocalDateTime startDate, LocalDateTime endDate);
    
    // Content Management
    void approveContent(Long contentId, String contentType);
    void rejectContent(Long contentId, String contentType);
    List<Map<String, Object>> getPendingApprovals();
    
    // Audit
    void logAdminAction(Long adminId, String action);
    List<Map<String, Object>> getAdminActionLog(LocalDateTime startDate, LocalDateTime endDate);
}
