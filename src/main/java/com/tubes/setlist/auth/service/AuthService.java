package com.tubes.setlist.auth.service;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import lombok.Data;

@Service
public class AuthService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HttpSession session;

    public void register(String username, String email, String password) {
        // Check if username already exists
        String checkUsernameSql = "SELECT COUNT(*) FROM users WHERE username = ? AND is_deleted = false";
        int usernameCount = jdbcTemplate.queryForObject(checkUsernameSql, Integer.class, username);
        if (usernameCount > 0) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email already exists
        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email = ? AND is_deleted = false";
        int emailCount = jdbcTemplate.queryForObject(checkEmailSql, Integer.class, email);
        if (emailCount > 0) {
            throw new RuntimeException("Email already exists");
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Insert new user with MEMBER role
        String sql = "INSERT INTO users (username, email, hashed_password, id_role) VALUES (?, ?, ?, 1)";
        jdbcTemplate.update(sql, username, email, hashedPassword);
    }

    @Data
    private static class User {
        private Integer idUser;
        private String username;
        private String email;
        private String hashedPassword;
        private String roleName;
    }

    public String login(String email, String password) {
        try {
            String sql = "SELECT u.id_user, u.username, u.email, u.hashed_password, r.role_name " +
                        "FROM users u " +
                        "JOIN roles r ON u.id_role = r.id_role " +
                        "WHERE u.email = ? AND u.is_deleted = false";
            
            User user = jdbcTemplate.queryForObject(sql, 
                (rs, rowNum) -> {
                    User u = new User();
                    u.setIdUser(rs.getInt("id_user"));
                    u.setUsername(rs.getString("username"));
                    u.setEmail(rs.getString("email"));
                    u.setHashedPassword(rs.getString("hashed_password"));
                    u.setRoleName(rs.getString("role_name"));
                    return u;
                }, 
                email
            );

            if (user != null && BCrypt.checkpw(password, user.getHashedPassword())) {
                session.setAttribute("userId", user.getIdUser());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("email", user.getEmail());
                session.setAttribute("role", user.getRoleName());
                
                // Return redirect URL based on role
                return getRedirectUrlByRole(user.getRoleName());
            } else {
                throw new RuntimeException("Invalid password");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Email not found");
        }
    }

    private String getRedirectUrlByRole(String role) {
        switch (role) {
            case "Admin":
                return "redirect:/admin/dashboard";
            case "Member":
                return "redirect:/member/dashboard";
            default:
                return "redirect:/auth/login";
        }
    }

    public void logout() {
        session.invalidate();
    }
}
