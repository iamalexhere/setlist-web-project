package com.tubes.setlist.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tubes.setlist.auth.service.AuthService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
        @RequestParam String username,
        @RequestParam String email,
        @RequestParam String password,
        @RequestParam String confirmPassword,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (!password.equals(confirmPassword)) {
                throw new RuntimeException("Passwords do not match");
            }
            
            authService.register(username, email, password);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login with your email.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @PostMapping("/login")
    public String login(
        @RequestParam String email,
        @RequestParam String password,
        RedirectAttributes redirectAttributes
    ) {
        try {
            authService.login(email, password);
            return "redirect:/";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        authService.logout();
        return "redirect:/auth/login";
    }
}
