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

    public void register(String username, String password) {
        // Check if username already exists
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ? AND is_deleted = false";
        int count = jdbcTemplate.queryForObject(checkSql, Integer.class, username);
        if (count > 0) {
            throw new RuntimeException("Username already exists");
        }

        // Hash password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Insert new user with MEMBER role (assuming role_id 2 is for MEMBER)
        String sql = "INSERT INTO users (username, hashed_password, id_role) VALUES (?, ?, 2)";
        jdbcTemplate.update(sql, username, hashedPassword);
    }

    @Data
    private static class User {
        private Integer idUser;
        private String username;
        private String hashedPassword;
        private String roleName;
    }

    public void login(String username, String password) {
        try {
            String sql = "SELECT u.id_user, u.username, u.hashed_password, r.role_name " +
                        "FROM users u " +
                        "JOIN roles r ON u.id_role = r.id_role " +
                        "WHERE u.username = ? AND u.is_deleted = false";
            
            User user = jdbcTemplate.queryForObject(sql, 
                (rs, rowNum) -> {
                    User u = new User();
                    u.setIdUser(rs.getInt("id_user"));
                    u.setUsername(rs.getString("username"));
                    u.setHashedPassword(rs.getString("hashed_password"));
                    u.setRoleName(rs.getString("role_name"));
                    return u;
                }, 
                username
            );

            if (user != null && BCrypt.checkpw(password, user.getHashedPassword())) {
                session.setAttribute("userId", user.getIdUser());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("role", user.getRoleName());
            } else {
                throw new RuntimeException("Invalid password");
            }
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("User not found");
        }
    }

    public void logout() {
        session.invalidate();
    }
}
