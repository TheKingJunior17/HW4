package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;

/**
 * Represents staff member credentials including authentication information,
 * role assignments, and multi-factor authentication state.
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */
public class StaffCredential {
    private final String username;
    private final String passwordHash;
    private final byte[] salt;
    private final StaffRole role;
    private final String email;
    private final LocalDateTime createdDate;
    private String currentMfaCode;
    private LocalDateTime mfaGeneratedTime;
    private boolean isActive;
    
    /**
     * Creates new staff credentials with the specified authentication details.
     * 
     * @param username Unique username for the staff member
     * @param passwordHash Hashed password for authentication
     * @param salt Salt used in password hashing
     * @param role Staff role assignment
     * @param email Email address for the staff member
     * @param createdDate Date when credentials were created
     */
    public StaffCredential(String username, String passwordHash, byte[] salt, 
                          StaffRole role, String email, LocalDateTime createdDate) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.email = email;
        this.createdDate = createdDate;
        this.isActive = true;
    }
    
    // Getters and setters
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public byte[] getSalt() { return salt; }
    public StaffRole getRole() { return role; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public String getCurrentMfaCode() { return currentMfaCode; }
    public LocalDateTime getMfaGeneratedTime() { return mfaGeneratedTime; }
    public boolean isActive() { return isActive; }
    
    public void setCurrentMfaCode(String currentMfaCode) { this.currentMfaCode = currentMfaCode; }
    public void setMfaGeneratedTime(LocalDateTime mfaGeneratedTime) { this.mfaGeneratedTime = mfaGeneratedTime; }
    public void setActive(boolean active) { isActive = active; }
    
    @Override
    public String toString() {
        return String.format("StaffCredential{username='%s', role=%s, email='%s', active=%s}", 
                           username, role, email, isActive);
    }
}