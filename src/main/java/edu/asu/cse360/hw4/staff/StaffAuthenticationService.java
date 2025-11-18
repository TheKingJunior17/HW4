package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Enhanced Staff Authentication Service providing multi-factor authentication,
 * session management, role-based access control, and comprehensive audit logging
 * for administrative staff access to the educational platform.
 * 
 * This service implements enterprise-grade security features including:
 * - Multi-factor authentication (MFA) with time-based tokens
 * - Role-based security clearance levels
 * - Session timeout and management controls  
 * - Comprehensive audit trail logging
 * - Rate limiting for failed authentication attempts
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */
public class StaffAuthenticationService {
    
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int MFA_CODE_LENGTH = 6;
    private static final int MFA_VALIDITY_MINUTES = 5;
    
    private final Map<String, StaffSession> activeSessions;
    private final Map<String, Integer> failedAttempts;
    private final List<AuditLogEntry> auditLog;
    private final Map<String, StaffCredential> staffCredentials;
    private final SecureRandom secureRandom;
    
    /**
     * Constructs a new StaffAuthenticationService with initialized security components.
     * Sets up concurrent data structures for thread-safe operations and initializes
     * the secure random number generator for MFA token generation.
     */
    public StaffAuthenticationService() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.failedAttempts = new ConcurrentHashMap<>();
        this.auditLog = Collections.synchronizedList(new ArrayList<>());
        this.staffCredentials = new ConcurrentHashMap<>();
        this.secureRandom = new SecureRandom();
        initializeDefaultStaff();
    }
    
    /**
     * Authenticates staff member with enhanced security validation including
     * rate limiting, password verification, and MFA token validation.
     * 
     * @param username The staff member's username
     * @param password The staff member's password
     * @param mfaCode The multi-factor authentication code
     * @return AuthenticationResult containing session token and status
     * @throws AuthenticationException if authentication fails
     */
    public AuthenticationResult authenticate(String username, String password, String mfaCode) 
            throws AuthenticationException {
        
        logAuditEvent(username, "AUTHENTICATION_ATTEMPT", "User attempted authentication");
        
        // Check rate limiting
        if (isRateLimited(username)) {
            logAuditEvent(username, "AUTHENTICATION_BLOCKED", "Rate limited due to failed attempts");
            throw new AuthenticationException("Account temporarily locked due to failed attempts");
        }
        
        // Validate credentials
        StaffCredential credential = staffCredentials.get(username);
        if (credential == null || !verifyPassword(password, credential.getPasswordHash(), credential.getSalt())) {
            recordFailedAttempt(username);
            logAuditEvent(username, "AUTHENTICATION_FAILED", "Invalid credentials provided");
            throw new AuthenticationException("Invalid username or password");
        }
        
        // Validate MFA code
        if (!validateMfaCode(username, mfaCode)) {
            recordFailedAttempt(username);
            logAuditEvent(username, "MFA_VALIDATION_FAILED", "Invalid MFA code provided");
            throw new AuthenticationException("Invalid MFA code");
        }
        
        // Create session
        String sessionToken = generateSessionToken();
        StaffSession session = new StaffSession(
            sessionToken, 
            username, 
            credential.getRole(),
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES)
        );
        
        activeSessions.put(sessionToken, session);
        failedAttempts.remove(username); // Reset failed attempts on successful login
        
        logAuditEvent(username, "AUTHENTICATION_SUCCESS", 
            String.format("Successfully authenticated with role: %s", credential.getRole()));
        
        return new AuthenticationResult(sessionToken, credential.getRole(), session.getExpiryTime());
    }
    
    /**
     * Validates an active session token and checks for expiration.
     * 
     * @param sessionToken The session token to validate
     * @return StaffSession if valid and not expired, null otherwise
     */
    public StaffSession validateSession(String sessionToken) {
        StaffSession session = activeSessions.get(sessionToken);
        
        if (session == null) {
            return null;
        }
        
        if (LocalDateTime.now().isAfter(session.getExpiryTime())) {
            activeSessions.remove(sessionToken);
            logAuditEvent(session.getUsername(), "SESSION_EXPIRED", "Session automatically expired");
            return null;
        }
        
        // Extend session on activity
        session.updateLastActivity();
        session.setExpiryTime(LocalDateTime.now().plusMinutes(SESSION_TIMEOUT_MINUTES));
        
        return session;
    }
    
    /**
     * Performs role-based access control validation for protected resources.
     * 
     * @param sessionToken The session token to check
     * @param requiredRole The minimum role required for access
     * @return true if access is granted, false otherwise
     */
    public boolean validateAccess(String sessionToken, StaffRole requiredRole) {
        StaffSession session = validateSession(sessionToken);
        
        if (session == null) {
            return false;
        }
        
        boolean hasAccess = session.getRole().getLevel() >= requiredRole.getLevel();
        
        logAuditEvent(session.getUsername(), "ACCESS_CHECK", 
            String.format("Access %s for resource requiring %s", 
                hasAccess ? "GRANTED" : "DENIED", requiredRole));
        
        return hasAccess;
    }
    
    /**
     * Logs out a staff member by invalidating their session.
     * 
     * @param sessionToken The session token to invalidate
     * @return true if logout successful, false if session not found
     */
    public boolean logout(String sessionToken) {
        StaffSession session = activeSessions.remove(sessionToken);
        
        if (session != null) {
            logAuditEvent(session.getUsername(), "LOGOUT", "User logged out");
            return true;
        }
        
        return false;
    }
    
    /**
     * Generates a new MFA code for the specified user.
     * 
     * @param username The username to generate MFA code for
     * @return Generated MFA code
     */
    public String generateMfaCode(String username) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < MFA_CODE_LENGTH; i++) {
            code.append(secureRandom.nextInt(10));
        }
        
        String mfaCode = code.toString();
        StaffCredential credential = staffCredentials.get(username);
        if (credential != null) {
            credential.setCurrentMfaCode(mfaCode);
            credential.setMfaGeneratedTime(LocalDateTime.now());
        }
        
        logAuditEvent(username, "MFA_CODE_GENERATED", "New MFA code generated");
        return mfaCode;
    }
    
    /**
     * Retrieves audit log entries for security monitoring and compliance.
     * 
     * @param username Optional username filter (null for all entries)
     * @param fromTime Optional start time filter
     * @param toTime Optional end time filter
     * @return List of matching audit log entries
     */
    public List<AuditLogEntry> getAuditLog(String username, LocalDateTime fromTime, LocalDateTime toTime) {
        return auditLog.stream()
            .filter(entry -> username == null || entry.getUsername().equals(username))
            .filter(entry -> fromTime == null || entry.getTimestamp().isAfter(fromTime))
            .filter(entry -> toTime == null || entry.getTimestamp().isBefore(toTime))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .toList();
    }
    
    /**
     * Registers a new staff member with the specified credentials and role.
     * 
     * @param username The username for the new staff member
     * @param password The password for the new staff member
     * @param role The role to assign to the new staff member
     * @param email The email address for the staff member
     * @return true if registration successful, false if username already exists
     */
    public boolean registerStaff(String username, String password, StaffRole role, String email) {
        if (staffCredentials.containsKey(username)) {
            return false;
        }
        
        byte[] salt = generateSalt();
        String passwordHash = hashPassword(password, salt);
        
        StaffCredential credential = new StaffCredential(
            username, passwordHash, salt, role, email, LocalDateTime.now()
        );
        
        staffCredentials.put(username, credential);
        logAuditEvent(username, "STAFF_REGISTERED", 
            String.format("New staff member registered with role: %s", role));
        
        return true;
    }
    
    // Private helper methods
    
    private void initializeDefaultStaff() {
        // Create default admin staff member
        registerStaff("admin", "AdminPass123!", StaffRole.ADMINISTRATOR, "admin@asu.edu");
        registerStaff("instructor", "InstructorPass123!", StaffRole.INSTRUCTOR, "instructor@asu.edu");
        registerStaff("assistant", "AssistantPass123!", StaffRole.TEACHING_ASSISTANT, "assistant@asu.edu");
    }
    
    private boolean isRateLimited(String username) {
        Integer attempts = failedAttempts.get(username);
        return attempts != null && attempts >= MAX_FAILED_ATTEMPTS;
    }
    
    private void recordFailedAttempt(String username) {
        failedAttempts.merge(username, 1, Integer::sum);
    }
    
    private boolean verifyPassword(String password, String hash, byte[] salt) {
        String computedHash = hashPassword(password, salt);
        return MessageDigest.isEqual(hash.getBytes(), computedHash.getBytes());
    }
    
    private boolean validateMfaCode(String username, String providedCode) {
        StaffCredential credential = staffCredentials.get(username);
        if (credential == null || credential.getCurrentMfaCode() == null) {
            return false;
        }
        
        // Check if MFA code is still valid (not expired)
        if (credential.getMfaGeneratedTime().plus(MFA_VALIDITY_MINUTES, ChronoUnit.MINUTES)
                .isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return credential.getCurrentMfaCode().equals(providedCode);
    }
    
    private String generateSessionToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
    
    private String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return salt;
    }
    
    private void logAuditEvent(String username, String action, String details) {
        AuditLogEntry entry = new AuditLogEntry(
            LocalDateTime.now(), username, action, details, getClientInfo()
        );
        auditLog.add(entry);
    }
    
    private String getClientInfo() {
        // In a real application, this would capture client IP, user agent, etc.
        return "Local Application";
    }
    
    // Inner classes and enums
    
    /**
     * Represents the result of an authentication attempt.
     */
    public static class AuthenticationResult {
        private final String sessionToken;
        private final StaffRole role;
        private final LocalDateTime expiryTime;
        
        public AuthenticationResult(String sessionToken, StaffRole role, LocalDateTime expiryTime) {
            this.sessionToken = sessionToken;
            this.role = role;
            this.expiryTime = expiryTime;
        }
        
        public String getSessionToken() { return sessionToken; }
        public StaffRole getRole() { return role; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }
    
    /**
     * Custom exception for authentication failures.
     */
    public static class AuthenticationException extends Exception {
        public AuthenticationException(String message) {
            super(message);
        }
        
        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}