package edu.asu.cse360.hw4.staff;

/**
 * Enumeration defining staff role hierarchy levels for role-based access control.
 * Each role has an associated security level that determines access permissions
 * to various system features and administrative functions.
 * 
 * @author Jose Mendoza  
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */
public enum StaffRole {
    /**
     * Teaching Assistant - Basic staff privileges (Level 1)
     */
    TEACHING_ASSISTANT(1, "Teaching Assistant", "Basic grading and student support functions"),
    
    /**
     * Instructor - Standard teaching privileges (Level 2) 
     */
    INSTRUCTOR(2, "Instructor", "Course management, grading, and content creation"),
    
    /**
     * Senior Instructor - Advanced teaching privileges (Level 3)
     */
    SENIOR_INSTRUCTOR(3, "Senior Instructor", "Advanced course management and staff supervision"),
    
    /**
     * Administrator - Full system access (Level 4)
     */
    ADMINISTRATOR(4, "Administrator", "Complete system administration and user management");
    
    private final int level;
    private final String displayName;
    private final String description;
    
    /**
     * Creates a staff role with the specified security level and metadata.
     * 
     * @param level Security clearance level (higher = more privileges)
     * @param displayName Human-readable name for the role
     * @param description Description of role responsibilities
     */
    StaffRole(int level, String displayName, String description) {
        this.level = level;
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the security level for this role.
     * 
     * @return Security level (1-4, higher = more access)
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Gets the human-readable display name for this role.
     * 
     * @return Display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of this role's responsibilities.
     * 
     * @return Role description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this role has sufficient privileges for the specified requirement.
     * 
     * @param requiredRole Minimum role required
     * @return true if this role meets or exceeds the requirement
     */
    public boolean hasAccess(StaffRole requiredRole) {
        return this.level >= requiredRole.level;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}