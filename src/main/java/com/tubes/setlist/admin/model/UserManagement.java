package com.tubes.setlist.admin.model;

import java.time.LocalDateTime;

public class UserManagement {
    private Long idUser;
    private Long idRole;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private boolean isDeleted;

    public UserManagement(Long idUser, Long idRole, String username, String email, 
                         LocalDateTime createdAt, boolean isDeleted) {
        this.idUser = idUser;
        this.idRole = idRole;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
    }

    // Getters and setters
    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }
    
    public Long getIdRole() { return idRole; }
    public void setIdRole(Long idRole) { this.idRole = idRole; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}
