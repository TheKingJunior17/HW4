package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;

/**
 * Represents a security audit log entry for tracking staff authentication
 * and system access events for compliance and security monitoring.
 * 
 * @author Jose Mendoza
 * @version 1.0 
 * @since HW4 - Staff Role Implementation
 */
public class AuditLogEntry {
    private final LocalDateTime timestamp;
    private final String username;
    private final String action;
    private final String details;
    private final String clientInfo;
    
    /**
     * Creates a new audit log entry with the specified event information.
     * 
     * @param timestamp When the event occurred
     * @param username Username associated with the event
     * @param action Type of action performed
     * @param details Additional details about the event
     * @param clientInfo Client information (IP, user agent, etc.)
     */
    public AuditLogEntry(LocalDateTime timestamp, String username, String action, 
                        String details, String clientInfo) {
        this.timestamp = timestamp;
        this.username = username;
        this.action = action;
        this.details = details;
        this.clientInfo = clientInfo;
    }
    
    // Getters
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public String getClientInfo() { return clientInfo; }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s (%s)", 
                           timestamp, username, action, details, clientInfo);
    }
}