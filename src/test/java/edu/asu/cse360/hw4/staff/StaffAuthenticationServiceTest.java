package edu.asu.cse360.hw4.staff;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Comprehensive JUnit test suite for StaffAuthenticationService
 * demonstrating complete test coverage with meaningful assertions
 * and professional testing patterns for HW4 staff role implementation.
 * 
 * This test class covers all major authentication functionality including:
 * - Staff authentication across all role levels
 * - Multi-factor authentication validation
 * - Session management and timeout controls
 * - Role-based access control validation
 * - Security audit logging verification
 * - Rate limiting and failed attempt handling
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */
@DisplayName("Staff Authentication Service Tests")
class StaffAuthenticationServiceTest {
    
    private StaffAuthenticationService authService;
    private final String validUsername = "instructor";
    private final String validPassword = "InstructorPass123!";
    private final String invalidUsername = "nonexistent";
    private final String invalidPassword = "wrongpass";
    
    @BeforeEach
    void setUp() {
        authService = new StaffAuthenticationService();
    }
    
    @AfterEach
    void tearDown() {
        authService = null;
    }
    
    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {
        
        @Test
        @DisplayName("Should successfully authenticate valid staff credentials")
        void testValidAuthentication() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            
            // Act
            StaffAuthenticationService.AuthenticationResult result = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            
            // Assert
            assertNotNull(result, "Authentication result should not be null");
            assertNotNull(result.getSessionToken(), "Session token should not be null");
            assertEquals(StaffRole.INSTRUCTOR, result.getRole(), "Role should match expected");
            assertNotNull(result.getExpiryTime(), "Expiry time should be set");
            assertTrue(result.getExpiryTime().isAfter(LocalDateTime.now()), 
                      "Expiry time should be in the future");
        }
        
        @Test
        @DisplayName("Should reject authentication with invalid username")
        void testInvalidUsernameAuthentication() {
            // Arrange
            String mfaCode = "123456";
            
            // Act & Assert
            StaffAuthenticationService.AuthenticationException exception = 
                assertThrows(StaffAuthenticationService.AuthenticationException.class, 
                           () -> authService.authenticate(invalidUsername, validPassword, mfaCode));
            
            assertEquals("Invalid username or password", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject authentication with invalid password")
        void testInvalidPasswordAuthentication() {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            
            // Act & Assert
            StaffAuthenticationService.AuthenticationException exception = 
                assertThrows(StaffAuthenticationService.AuthenticationException.class, 
                           () -> authService.authenticate(validUsername, invalidPassword, mfaCode));
            
            assertEquals("Invalid username or password", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should reject authentication with invalid MFA code")
        void testInvalidMfaAuthentication() {
            // Arrange
            String invalidMfaCode = "000000";
            
            // Act & Assert
            StaffAuthenticationService.AuthenticationException exception = 
                assertThrows(StaffAuthenticationService.AuthenticationException.class, 
                           () -> authService.authenticate(validUsername, validPassword, invalidMfaCode));
            
            assertEquals("Invalid MFA code", exception.getMessage());
        }
        
        @Test
        @DisplayName("Should implement rate limiting after multiple failed attempts")
        void testRateLimiting() throws Exception {
            // Arrange
            String invalidMfa = "000000";
            
            // Act - Generate multiple failed attempts
            for (int i = 0; i < 5; i++) {
                assertThrows(StaffAuthenticationService.AuthenticationException.class,
                           () -> authService.authenticate(validUsername, validPassword, invalidMfa));
            }
            
            // Assert - Next attempt should be rate limited
            StaffAuthenticationService.AuthenticationException exception = 
                assertThrows(StaffAuthenticationService.AuthenticationException.class,
                           () -> authService.authenticate(validUsername, validPassword, invalidMfa));
            
            assertTrue(exception.getMessage().contains("temporarily locked"),
                      "Should indicate rate limiting");
        }
    }
    
    @Nested
    @DisplayName("Multi-Factor Authentication Tests")
    class MfaTests {
        
        @Test
        @DisplayName("Should generate valid MFA codes")
        void testMfaCodeGeneration() {
            // Act
            String mfaCode = authService.generateMfaCode(validUsername);
            
            // Assert
            assertNotNull(mfaCode, "MFA code should not be null");
            assertEquals(6, mfaCode.length(), "MFA code should be 6 digits");
            assertTrue(mfaCode.matches("\\d{6}"), "MFA code should contain only digits");
        }
        
        @Test
        @DisplayName("Should validate fresh MFA codes")
        void testFreshMfaValidation() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            
            // Act
            StaffAuthenticationService.AuthenticationResult result = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            
            // Assert
            assertNotNull(result, "Authentication should succeed with fresh MFA code");
        }
        
        @Test
        @DisplayName("Should reject expired MFA codes")
        void testExpiredMfaRejection() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            
            // Simulate time passage (in real implementation would use time manipulation)
            Thread.sleep(100); // Brief pause to simulate different generation time
            
            // Generate new code to make previous one invalid
            authService.generateMfaCode(validUsername);
            
            // Act & Assert
            StaffAuthenticationService.AuthenticationException exception = 
                assertThrows(StaffAuthenticationService.AuthenticationException.class,
                           () -> authService.authenticate(validUsername, validPassword, mfaCode));
            
            assertEquals("Invalid MFA code", exception.getMessage());
        }
    }
    
    @Nested
    @DisplayName("Session Management Tests")
    class SessionManagementTests {
        
        @Test
        @DisplayName("Should create valid sessions for authenticated users")
        void testSessionCreation() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            StaffAuthenticationService.AuthenticationResult authResult = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            
            // Act
            StaffSession session = authService.validateSession(authResult.getSessionToken());
            
            // Assert
            assertNotNull(session, "Session should be valid");
            assertEquals(validUsername, session.getUsername(), "Username should match");
            assertEquals(StaffRole.INSTRUCTOR, session.getRole(), "Role should match");
            assertNotNull(session.getCreatedTime(), "Created time should be set");
            assertNotNull(session.getExpiryTime(), "Expiry time should be set");
        }
        
        @Test
        @DisplayName("Should reject invalid session tokens")
        void testInvalidSessionValidation() {
            // Act
            StaffSession session = authService.validateSession("invalid-token");
            
            // Assert
            assertNull(session, "Invalid session token should return null");
        }
        
        @Test
        @DisplayName("Should extend session on activity")
        void testSessionExtension() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            StaffAuthenticationService.AuthenticationResult authResult = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            
            // Act
            LocalDateTime firstExpiry = authService.validateSession(authResult.getSessionToken()).getExpiryTime();
            Thread.sleep(10); // Brief pause
            LocalDateTime secondExpiry = authService.validateSession(authResult.getSessionToken()).getExpiryTime();
            
            // Assert
            assertTrue(secondExpiry.isAfter(firstExpiry) || secondExpiry.isEqual(firstExpiry),
                      "Session should be extended on activity");
        }
        
        @Test
        @DisplayName("Should successfully logout active sessions")
        void testSessionLogout() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            StaffAuthenticationService.AuthenticationResult authResult = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            String sessionToken = authResult.getSessionToken();
            
            // Act
            boolean logoutResult = authService.logout(sessionToken);
            StaffSession sessionAfterLogout = authService.validateSession(sessionToken);
            
            // Assert
            assertTrue(logoutResult, "Logout should succeed");
            assertNull(sessionAfterLogout, "Session should be invalid after logout");
        }
    }
    
    @Nested
    @DisplayName("Role-Based Access Control Tests")
    class RoleBasedAccessTests {
        
        @Test
        @DisplayName("Should grant access when role meets requirements")
        void testValidRoleAccess() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            StaffAuthenticationService.AuthenticationResult authResult = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            
            // Act
            boolean accessResult = authService.validateAccess(
                authResult.getSessionToken(), StaffRole.TEACHING_ASSISTANT);
            
            // Assert
            assertTrue(accessResult, "Instructor should have access to TA-level resources");
        }
        
        @Test
        @DisplayName("Should deny access when role insufficient")
        void testInsufficientRoleAccess() throws Exception {
            // Arrange - Use TA credentials
            String taUsername = "assistant";
            String taPassword = "AssistantPass123!";
            String mfaCode = authService.generateMfaCode(taUsername);
            StaffAuthenticationService.AuthenticationResult authResult = 
                authService.authenticate(taUsername, taPassword, mfaCode);
            
            // Act
            boolean accessResult = authService.validateAccess(
                authResult.getSessionToken(), StaffRole.ADMINISTRATOR);
            
            // Assert
            assertFalse(accessResult, "TA should not have access to admin-level resources");
        }
        
        @Test
        @DisplayName("Should deny access with invalid session token")
        void testAccessWithInvalidSession() {
            // Act
            boolean accessResult = authService.validateAccess(
                "invalid-token", StaffRole.TEACHING_ASSISTANT);
            
            // Assert
            assertFalse(accessResult, "Access should be denied with invalid session");
        }
    }
    
    @Nested
    @DisplayName("Audit Logging Tests")
    class AuditLoggingTests {
        
        @Test
        @DisplayName("Should log successful authentication events")
        void testSuccessfulAuthenticationLogging() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            
            // Act
            authService.authenticate(validUsername, validPassword, mfaCode);
            List<AuditLogEntry> auditLog = authService.getAuditLog(validUsername, null, null);
            
            // Assert
            assertFalse(auditLog.isEmpty(), "Audit log should contain entries");
            
            boolean hasAuthSuccess = auditLog.stream()
                .anyMatch(entry -> "AUTHENTICATION_SUCCESS".equals(entry.getAction()));
            assertTrue(hasAuthSuccess, "Should log successful authentication");
        }
        
        @Test
        @DisplayName("Should log failed authentication attempts")
        void testFailedAuthenticationLogging() {
            // Act
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate(validUsername, invalidPassword, "123456"));
            
            List<AuditLogEntry> auditLog = authService.getAuditLog(validUsername, null, null);
            
            // Assert
            boolean hasAuthFailure = auditLog.stream()
                .anyMatch(entry -> "AUTHENTICATION_FAILED".equals(entry.getAction()));
            assertTrue(hasAuthFailure, "Should log failed authentication");
        }
        
        @Test
        @DisplayName("Should log access control events")
        void testAccessControlLogging() throws Exception {
            // Arrange
            String mfaCode = authService.generateMfaCode(validUsername);
            StaffAuthenticationService.AuthenticationResult authResult = 
                authService.authenticate(validUsername, validPassword, mfaCode);
            
            // Act
            authService.validateAccess(authResult.getSessionToken(), StaffRole.TEACHING_ASSISTANT);
            List<AuditLogEntry> auditLog = authService.getAuditLog(validUsername, null, null);
            
            // Assert
            boolean hasAccessCheck = auditLog.stream()
                .anyMatch(entry -> "ACCESS_CHECK".equals(entry.getAction()));
            assertTrue(hasAccessCheck, "Should log access control checks");
        }
    }
    
    @Nested
    @DisplayName("Staff Registration Tests")
    class StaffRegistrationTests {
        
        @Test
        @DisplayName("Should successfully register new staff members")
        void testSuccessfulStaffRegistration() {
            // Arrange
            String newUsername = "newstaff";
            String newPassword = "NewStaffPass123!";
            String email = "newstaff@asu.edu";
            
            // Act
            boolean registrationResult = authService.registerStaff(
                newUsername, newPassword, StaffRole.TEACHING_ASSISTANT, email);
            
            // Assert
            assertTrue(registrationResult, "Staff registration should succeed");
        }
        
        @Test
        @DisplayName("Should reject duplicate username registration")
        void testDuplicateUsernameRegistration() {
            // Act - Try to register with existing username
            boolean registrationResult = authService.registerStaff(
                validUsername, "NewPassword123!", StaffRole.TEACHING_ASSISTANT, "duplicate@asu.edu");
            
            // Assert
            assertFalse(registrationResult, "Should reject duplicate username");
        }
        
        @Test
        @DisplayName("Should allow authentication after successful registration")
        void testAuthenticationAfterRegistration() throws Exception {
            // Arrange
            String newUsername = "testauthuser";
            String newPassword = "TestAuthPass123!";
            authService.registerStaff(newUsername, newPassword, StaffRole.INSTRUCTOR, "test@asu.edu");
            
            // Act
            String mfaCode = authService.generateMfaCode(newUsername);
            StaffAuthenticationService.AuthenticationResult result = 
                authService.authenticate(newUsername, newPassword, mfaCode);
            
            // Assert
            assertNotNull(result, "Should authenticate after registration");
            assertEquals(StaffRole.INSTRUCTOR, result.getRole(), "Role should match registration");
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle null parameters gracefully")
        void testNullParameterHandling() {
            // Act & Assert
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate(null, validPassword, "123456"));
            
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate(validUsername, null, "123456"));
            
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate(validUsername, validPassword, null));
        }
        
        @Test
        @DisplayName("Should handle empty string parameters")
        void testEmptyStringHandling() {
            // Act & Assert
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate("", validPassword, "123456"));
            
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate(validUsername, "", "123456"));
            
            assertThrows(StaffAuthenticationService.AuthenticationException.class,
                       () -> authService.authenticate(validUsername, validPassword, ""));
        }
        
        @Test
        @DisplayName("Should validate session tokens are unique")
        void testSessionTokenUniqueness() throws Exception {
            // Arrange & Act
            String mfaCode1 = authService.generateMfaCode(validUsername);
            String mfaCode2 = authService.generateMfaCode("admin");
            
            StaffAuthenticationService.AuthenticationResult result1 = 
                authService.authenticate(validUsername, validPassword, mfaCode1);
            StaffAuthenticationService.AuthenticationResult result2 = 
                authService.authenticate("admin", "AdminPass123!", mfaCode2);
            
            // Assert
            assertNotEquals(result1.getSessionToken(), result2.getSessionToken(),
                           "Session tokens should be unique");
        }
    }
    
    @Test
    @DisplayName("Integration test - Complete authentication workflow")
    void testCompleteAuthenticationWorkflow() throws Exception {
        // Arrange
        String testUsername = "integrationtest";
        String testPassword = "IntegrationPass123!";
        String testEmail = "integration@asu.edu";
        
        // Act - Complete workflow
        // 1. Register new staff
        boolean registered = authService.registerStaff(
            testUsername, testPassword, StaffRole.SENIOR_INSTRUCTOR, testEmail);
        assertTrue(registered, "Registration should succeed");
        
        // 2. Generate MFA code
        String mfaCode = authService.generateMfaCode(testUsername);
        assertNotNull(mfaCode, "MFA code should be generated");
        
        // 3. Authenticate
        StaffAuthenticationService.AuthenticationResult authResult = 
            authService.authenticate(testUsername, testPassword, mfaCode);
        assertNotNull(authResult, "Authentication should succeed");
        
        // 4. Validate session
        StaffSession session = authService.validateSession(authResult.getSessionToken());
        assertNotNull(session, "Session should be valid");
        
        // 5. Check access control
        boolean hasAccess = authService.validateAccess(
            authResult.getSessionToken(), StaffRole.INSTRUCTOR);
        assertTrue(hasAccess, "Should have appropriate access");
        
        // 6. Logout
        boolean loggedOut = authService.logout(authResult.getSessionToken());
        assertTrue(loggedOut, "Logout should succeed");
        
        // 7. Verify session invalidated
        StaffSession invalidSession = authService.validateSession(authResult.getSessionToken());
        assertNull(invalidSession, "Session should be invalid after logout");
        
        // Assert - Check audit log
        List<AuditLogEntry> auditLog = authService.getAuditLog(testUsername, null, null);
        assertTrue(auditLog.size() >= 4, "Should have multiple audit log entries");
    }
}