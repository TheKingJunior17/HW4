package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;

/**
 * Represents an active staff session with security and tracking information.
 * This class manages session state, expiration, and activity monitoring for
 * authenticated staff members in the educational platform.
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */
public class StaffSession {
    private final String sessionToken;
    private final String username;
    private final StaffRole role;
    private final LocalDateTime createdTime;
    private LocalDateTime lastActivity;
    private LocalDateTime expiryTime;
    
    /**
     * Creates a new staff session with the specified parameters.
     * 
     * @param sessionToken Unique token identifying this session
     * @param username Username of the authenticated staff member
     * @param role Role of the authenticated staff member
     * @param createdTime Time when session was created
     * @param expiryTime Initial expiration time for the session
     */
    public StaffSession(String sessionToken, String username, StaffRole role, 
                       LocalDateTime createdTime, LocalDateTime expiryTime) {
        this.sessionToken = sessionToken;
        this.username = username;
        this.role = role;
        this.createdTime = createdTime;
        this.lastActivity = createdTime;
        this.expiryTime = expiryTime;
    }
    
    /**
     * Updates the last activity timestamp to current time.
     */
    public void updateLastActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getSessionToken() { return sessionToken; }
    public String getUsername() { return username; }
    public StaffRole getRole() { return role; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
    public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
    
    @Override
    public String toString() {
        return String.format("StaffSession{username='%s', role=%s, created=%s, expires=%s}", 
                           username, role, createdTime, expiryTime);
    }
}