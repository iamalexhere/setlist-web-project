package com.tubes.setlist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Arrays;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("")
    public String dashboard(Model model) {
        // TODO: Add dashboard statistics
        model.addAttribute("totalMembers", 150);
        model.addAttribute("totalSetlists", 324);
        model.addAttribute("pendingSetlists", 12);
        model.addAttribute("pendingCorrections", 8);
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String members(Model model) {
        // TODO: Add member management
        return "admin/members";
    }

    @GetMapping("/members/{id}")
    public String memberDetail(@PathVariable Long id, Model model) {
        // TODO: Add member detail view
        return "admin/member-detail";
    }

    @PostMapping("/members/{id}/status")
    public String updateMemberStatus(@PathVariable Long id, @RequestParam String status) {
        // TODO: Update member status (active/suspended)
        return "redirect:/admin/members";
    }

    @GetMapping("/setlists")
    public String setlists(Model model) {
        // TODO: Add setlist moderation
        return "admin/setlists";
    }

    @GetMapping("/setlists/{id}")
    public String setlistDetail(@PathVariable Long id, Model model) {
        // TODO: Add setlist detail view
        return "admin/setlist-detail";
    }

    @PostMapping("/setlists/{id}/approve")
    public String approveSetlist(@PathVariable Long id) {
        // TODO: Approve setlist
        return "redirect:/admin/setlists";
    }

    @PostMapping("/setlists/{id}/reject")
    public String rejectSetlist(@PathVariable Long id, @RequestParam String reason) {
        // TODO: Reject setlist with reason
        return "redirect:/admin/setlists";
    }

    @GetMapping("/corrections")
    public String corrections(Model model) {
        // TODO: Add correction moderation
        return "admin/corrections";
    }

    @PostMapping("/corrections/{id}/approve")
    public String approveCorrection(@PathVariable Long id) {
        // TODO: Approve correction
        return "redirect:/admin/corrections";
    }

    @PostMapping("/corrections/{id}/reject")
    public String rejectCorrection(@PathVariable Long id, @RequestParam String reason) {
        // TODO: Reject correction with reason
        return "redirect:/admin/corrections";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        // Mock data for timeline
        List<Map<String, Object>> timelineData = new ArrayList<>();
        LocalDate now = LocalDate.now();
        Random random = new Random();
        
        for (int i = 30; i >= 0; i--) {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("date", now.minusDays(i).toString());
            data.put("value", random.nextInt(20) + 1);
            timelineData.add(data);
        }
        model.addAttribute("timelineData", timelineData);

        // Mock data for top artists
        List<Map<String, Object>> artistData = Arrays.asList(
            Map.of("name", "Taylor Swift", "count", 150),
            Map.of("name", "Ed Sheeran", "count", 120),
            Map.of("name", "Coldplay", "count", 100),
            Map.of("name", "The Weeknd", "count", 80),
            Map.of("name", "BTS", "count", 75)
        );
        model.addAttribute("artistData", artistData);

        // Mock data for popular venues
        List<Map<String, Object>> venueData = Arrays.asList(
            Map.of("name", "Madison Square Garden", "count", 50),
            Map.of("name", "O2 Arena", "count", 45),
            Map.of("name", "Wembley Stadium", "count", 40),
            Map.of("name", "AccorHotels Arena", "count", 35),
            Map.of("name", "Tokyo Dome", "count", 30)
        );
        model.addAttribute("venueData", venueData);

        // Mock data for member growth
        List<Map<String, Object>> memberGrowthData = new ArrayList<>();
        int totalMembers = 1000;
        for (int i = 12; i >= 0; i--) {
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("date", now.minusMonths(i).toString());
            totalMembers += random.nextInt(100) + 50;
            data.put("count", totalMembers);
            memberGrowthData.add(data);
        }
        model.addAttribute("memberGrowthData", memberGrowthData);

        // Mock data for contribution distribution
        List<Map<String, Object>> contributionData = Arrays.asList(
            Map.of("type", "Setlists", "count", 500),
            Map.of("type", "Corrections", "count", 300),
            Map.of("type", "Comments", "count", 200)
        );
        model.addAttribute("contributionData", contributionData);

        // Mock data for correction success rate
        Map<String, Integer> correctionRateData = Map.of(
            "approved", 850,
            "rejected", 150
        );
        model.addAttribute("correctionRateData", correctionRateData);

        return "admin/reports";
    }
}
