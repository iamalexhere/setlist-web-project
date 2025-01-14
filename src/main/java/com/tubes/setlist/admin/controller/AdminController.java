package com.tubes.setlist.admin.controller;

import com.tubes.setlist.admin.model.Correction;
import com.tubes.setlist.admin.model.CorrectionStats;
import com.tubes.setlist.admin.model.UserManagement;
import com.tubes.setlist.admin.repository.AdminRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        
        List<Map<String, Object>> pendingContent = adminRepository.getPendingApprovals();
        
        // Convert to Correction objects
        List<Correction> corrections = pendingContent.stream()
            .map(content -> {
                Object dateObj = content.get("submitted_at");
                LocalDateTime submittedAt;
                if (dateObj instanceof java.sql.Timestamp) {
                    submittedAt = ((java.sql.Timestamp) dateObj).toLocalDateTime();
                } else if (dateObj instanceof java.sql.Date) {
                    submittedAt = ((java.sql.Date) dateObj).toLocalDate().atStartOfDay();
                } else {
                    submittedAt = LocalDateTime.now(); // fallback
                }
                
                return new Correction(
                    ((Number) content.get("content_id")).longValue(),
                    (String) content.get("content_type"),
                    (String) content.get("content_name"),
                    (String) content.get("status"),
                    (String) content.get("submitted_by"),
                    submittedAt
                );
            })
            .collect(Collectors.toList());
        
        model.addAttribute("corrections", corrections);
        
        // Add correction stats
        CorrectionStats stats = new CorrectionStats(
            (long) corrections.size(),
            corrections.stream()
                .filter(c -> "PENDING".equals(c.getStatus()))
                .count(),
            corrections.stream()
                .filter(c -> "APPROVED".equals(c.getStatus()))
                .filter(c -> c.getSubmittedAt().toLocalDate().equals(LocalDate.now()))
                .count()
        );
        model.addAttribute("correctionStats", stats);
        
        return "admin/corrections";
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
    public String showReports(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);

        try {
            // Get all report data
            Map<String, Integer> setlistsByArtist = adminRepository.getSetlistCountByArtist();
            Map<String, Integer> eventsByVenue = adminRepository.getEventCountByVenue();
            Map<String, Integer> songsByArtist = adminRepository.getSongCountByArtist();
            Map<String, Integer> monthlySetlists = adminRepository.getMonthlySetlistCount();

            // Debug logs
            System.out.println("Setlists by Artist: " + setlistsByArtist);
            System.out.println("Events by Venue: " + eventsByVenue);
            System.out.println("Songs by Artist: " + songsByArtist);
            System.out.println("Monthly Setlists: " + monthlySetlists);

            // Add to model
            model.addAttribute("setlistsByArtist", setlistsByArtist.isEmpty() ? null : setlistsByArtist);
            model.addAttribute("eventsByVenue", eventsByVenue.isEmpty() ? null : eventsByVenue);
            model.addAttribute("songsByArtist", songsByArtist.isEmpty() ? null : songsByArtist);
            model.addAttribute("monthlySetlists", monthlySetlists.isEmpty() ? null : monthlySetlists);

        } catch (Exception e) {
            System.err.println("Error in showReports: " + e.getMessage());
            e.printStackTrace();
            // Add empty data to model
            model.addAttribute("setlistsByArtist", null);
            model.addAttribute("eventsByVenue", null);
            model.addAttribute("songsByArtist", null);
            model.addAttribute("monthlySetlists", null);
        }

        return "admin/reports";
    }
}
