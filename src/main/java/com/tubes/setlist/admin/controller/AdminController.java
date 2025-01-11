package com.tubes.setlist.admin.controller;

import com.tubes.setlist.admin.repository.AdminRepository;
import com.tubes.setlist.admin.model.UserManagement;
import com.tubes.setlist.admin.model.Statistics;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private final AdminRepository adminRepository;
    
    public AdminController(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
    private boolean checkAdminAccess(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equals("Admin");
    }
    
    private void addUserAttributes(HttpSession session, Model model) {
        model.addAttribute("username", session.getAttribute("username"));
        model.addAttribute("email", session.getAttribute("email"));
    }
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        // Add statistics to the dashboard
        model.addAttribute("userStats", adminRepository.getUserStatistics());
        model.addAttribute("contentStats", adminRepository.getContentStatistics());
        model.addAttribute("recentActivity", adminRepository.getActivityLog(
            LocalDateTime.now().minusDays(7), LocalDateTime.now()));
        
        return "admin/dashboard";
    }
    
    @GetMapping("/members")
    public String members(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        // Add user statistics
        model.addAttribute("userStats", adminRepository.getUserStatistics());
        
        // Add user list
        List<UserManagement> users = adminRepository.getAllUsers();
        model.addAttribute("users", users);
        
        return "admin/members";
    }
    
    @PostMapping("/members/{userId}/block")
    @ResponseBody
    public ResponseEntity<?> blockUser(@PathVariable Long userId, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        adminRepository.blockUser(userId);
        adminRepository.logAdminAction(
            (Long) session.getAttribute("userId"),
            "Blocked user with ID: " + userId
        );
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/members/{userId}/unblock")
    @ResponseBody
    public ResponseEntity<?> unblockUser(@PathVariable Long userId, HttpSession session) {
        if (!checkAdminAccess(session)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        adminRepository.unblockUser(userId);
        adminRepository.logAdminAction(
            (Long) session.getAttribute("userId"),
            "Unblocked user with ID: " + userId
        );
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/content/pending")
    public String pendingApprovals(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        model.addAttribute("pendingContent", adminRepository.getPendingApprovals());
        return "admin/pending-approvals";
    }
    
    @PostMapping("/content/{contentType}/{contentId}/approve")
    @ResponseBody
    public ResponseEntity<?> approveContent(
            @PathVariable String contentType,
            @PathVariable Long contentId,
            HttpSession session) {
        if (!checkAdminAccess(session)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        adminRepository.approveContent(contentId, contentType);
        adminRepository.logAdminAction(
            (Long) session.getAttribute("userId"),
            "Approved " + contentType + " with ID: " + contentId
        );
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/content/{contentType}/{contentId}/reject")
    @ResponseBody
    public ResponseEntity<?> rejectContent(
            @PathVariable String contentType,
            @PathVariable Long contentId,
            @RequestParam String reason,
            HttpSession session) {
        if (!checkAdminAccess(session)) {
            return ResponseEntity.status(403).body("Access denied");
        }
        
        adminRepository.rejectContent(contentId, contentType);
        adminRepository.logAdminAction(
            (Long) session.getAttribute("userId"),
            "Rejected " + contentType + " with ID: " + contentId + ". Reason: " + reason
        );
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        // Add statistics for reports
        model.addAttribute("stats", adminRepository.getStatistics());
        
        return "admin/reports";
    }
    
    @GetMapping("/reports/download")
    public ResponseEntity<byte[]> downloadReport(HttpSession session) {
        if (!checkAdminAccess(session)) {
            return ResponseEntity.status(403).build();
        }
        
        byte[] report = adminRepository.generateReport();
        
        return ResponseEntity.ok()
            .header("Content-Type", "application/vnd.ms-excel")
            .header("Content-Disposition", "attachment; filename=\"setlist-report.xlsx\"")
            .body(report);
    }
    
    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpSession session,
            Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();
        
        model.addAttribute("userStats", adminRepository.getUserStatistics());
        model.addAttribute("contentStats", adminRepository.getContentStatistics());
        model.addAttribute("activityLog", adminRepository.getActivityLog(start, end));
        model.addAttribute("adminActions", adminRepository.getAdminActionLog(start, end));
        
        return "admin/reports";
    }
}
