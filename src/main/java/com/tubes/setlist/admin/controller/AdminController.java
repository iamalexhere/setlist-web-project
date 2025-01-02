package com.tubes.setlist.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
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
        return "admin/dashboard";
    }
    
    @GetMapping("/members")
    public String members(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "admin/members";
    }
    
    @GetMapping("/setlists")
    public String setlists(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "admin/setlists";
    }
    
    @GetMapping("/corrections")
    public String corrections(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "admin/corrections";
    }
    
    @GetMapping("/reports")
    public String reports(HttpSession session, Model model) {
        if (!checkAdminAccess(session)) {
            return "redirect:/auth/login";
        }
        addUserAttributes(session, model);
        return "admin/reports";
    }
}
