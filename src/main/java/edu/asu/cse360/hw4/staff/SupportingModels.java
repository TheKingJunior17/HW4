package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Collection of supporting model classes for the HW4 Staff Role Implementation.
 * These classes provide data structures for student analytics, performance tracking,
 * and various dashboard components used throughout the staff services.
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */

/**
 * Represents a student profile with basic information and enrollment details.
 */
class StudentProfile {
    private final String studentId;
    private final String name;
    private final String email;
    private final String major;
    
    public StudentProfile(String studentId, String name, String email, String major) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.major = major;
    }
    
    public String getStudentId() { return studentId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMajor() { return major; }
}

/**
 * Represents a student performance record with score and timestamp information.
 */
class StudentPerformanceRecord {
    private final String studentId;
    private final String courseId;
    private final double score;
    private final LocalDateTime timestamp;
    
    public StudentPerformanceRecord(String studentId, String courseId, double score, LocalDateTime timestamp) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
        this.timestamp = timestamp;
    }
    
    public String getStudentId() { return studentId; }
    public String getCourseId() { return courseId; }
    public double getScore() { return score; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

/**
 * Represents a learning outcome with associated metadata.
 */
class LearningOutcome {
    private final String id;
    private final String title;
    private final String description;
    
    public LearningOutcome(String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }
    
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}

/**
 * Time range utility class for filtering analytics data.
 */
class TimeRange {
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    
    public TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public boolean contains(LocalDateTime timestamp) {
        return !timestamp.isBefore(startTime) && !timestamp.isAfter(endTime);
    }
    
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}

/**
 * Enumeration for progress trends in student performance.
 */
enum ProgressTrend {
    IMPROVING, STABLE, DECLINING
}

/**
 * Enumeration for student risk levels.
 */
enum RiskLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

/**
 * Enumeration for different types of analytics analysis.
 */
enum AnalysisType {
    PERFORMANCE_OVER_TIME, COMPARATIVE_COHORT, LEARNING_OUTCOME_PROGRESSION, ENGAGEMENT_PATTERNS
}

/**
 * Enumeration for dashboard widget types.
 */
enum WidgetType {
    PERFORMANCE_SUMMARY, TREND_CHART, AT_RISK_LIST, LEARNING_OUTCOMES
}

/**
 * Enumeration for export formats.
 */
enum ExportFormat {
    CSV, PDF, JSON, EXCEL
}

/**
 * Analytics engine for processing student performance data.
 */
class AnalyticsEngine {
    public StudentRiskProfile assessStudentRisk(StudentProfile student, 
                                               List<StudentPerformanceRecord> history, 
                                               RiskAssessmentCriteria criteria) {
        // Simplified risk assessment
        double averageScore = history.stream().mapToDouble(StudentPerformanceRecord::getScore).average().orElse(0.0);
        RiskLevel riskLevel = averageScore < 60 ? RiskLevel.HIGH : 
                             averageScore < 70 ? RiskLevel.MEDIUM : RiskLevel.LOW;
        
        return new StudentRiskProfile(student, riskLevel, averageScore, "Risk assessment based on performance");
    }
    
    public ProgressTrend calculateTrend(List<StudentPerformanceRecord> history) {
        if (history.size() < 2) return ProgressTrend.STABLE;
        
        double firstScore = history.get(0).getScore();
        double lastScore = history.get(history.size() - 1).getScore();
        
        if (lastScore > firstScore + 5) return ProgressTrend.IMPROVING;
        if (lastScore < firstScore - 5) return ProgressTrend.DECLINING;
        return ProgressTrend.STABLE;
    }
}

/**
 * Report classes for various analytics outputs.
 */
class StudentProgressReport {
    private final Map<String, Double> averageScores;
    private final Map<String, ProgressTrend> trends;
    private final List<String> atRiskStudents;
    private final Map<String, LearningOutcomeProgress> outcomeProgress;
    private final LocalDateTime generatedTime;
    private final TimeRange timeRange;
    private final String courseId;
    
    public StudentProgressReport(Map<String, Double> averageScores, Map<String, ProgressTrend> trends,
                               List<String> atRiskStudents, Map<String, LearningOutcomeProgress> outcomeProgress,
                               LocalDateTime generatedTime, TimeRange timeRange, String courseId) {
        this.averageScores = averageScores;
        this.trends = trends;
        this.atRiskStudents = atRiskStudents;
        this.outcomeProgress = outcomeProgress;
        this.generatedTime = generatedTime;
        this.timeRange = timeRange;
        this.courseId = courseId;
    }
    
    // Getters
    public Map<String, Double> getAverageScores() { return averageScores; }
    public Map<String, ProgressTrend> getTrends() { return trends; }
    public List<String> getAtRiskStudents() { return atRiskStudents; }
}

class AtRiskStudentReport {
    private final List<StudentRiskProfile> riskProfiles;
    private final RiskAssessmentCriteria criteria;
    private final LocalDateTime generatedTime;
    
    public AtRiskStudentReport(List<StudentRiskProfile> riskProfiles, RiskAssessmentCriteria criteria, LocalDateTime generatedTime) {
        this.riskProfiles = riskProfiles;
        this.criteria = criteria;
        this.generatedTime = generatedTime;
    }
    
    public List<StudentRiskProfile> getRiskProfiles() { return riskProfiles; }
}

class StudentRiskProfile {
    private final StudentProfile student;
    private final RiskLevel riskLevel;
    private final double averageScore;
    private final String riskFactors;
    
    public StudentRiskProfile(StudentProfile student, RiskLevel riskLevel, double averageScore, String riskFactors) {
        this.student = student;
        this.riskLevel = riskLevel;
        this.averageScore = averageScore;
        this.riskFactors = riskFactors;
    }
    
    public RiskLevel getRiskLevel() { return riskLevel; }
    public StudentProfile getStudent() { return student; }
}

class LearningOutcomeProgress {
    private final LearningOutcome outcome;
    private final double progressPercentage;
    
    public LearningOutcomeProgress(LearningOutcome outcome, double progressPercentage) {
        this.outcome = outcome;
        this.progressPercentage = progressPercentage;
    }
    
    public LearningOutcome getOutcome() { return outcome; }
    public double getProgressPercentage() { return progressPercentage; }
}

class TrendAnalysisReport {
    private final String analysisType;
    private final List<StudentPerformanceRecord> data;
    private final LocalDateTime generatedTime;
    
    public TrendAnalysisReport(String analysisType, List<StudentPerformanceRecord> data, LocalDateTime generatedTime) {
        this.analysisType = analysisType;
        this.data = data;
        this.generatedTime = generatedTime;
    }
    
    public String getAnalysisType() { return analysisType; }
}

/**
 * Configuration and parameter classes.
 */
class RiskAssessmentCriteria {
    private final double minimumScoreThreshold;
    private final int consecutiveFailuresThreshold;
    
    public RiskAssessmentCriteria(double minimumScoreThreshold, int consecutiveFailuresThreshold) {
        this.minimumScoreThreshold = minimumScoreThreshold;
        this.consecutiveFailuresThreshold = consecutiveFailuresThreshold;
    }
    
    public double getMinimumScoreThreshold() { return minimumScoreThreshold; }
}

class AnalysisParameters {
    private final String courseId;
    private final TimeRange timeRange;
    private final Map<String, Object> additionalParams;
    
    public AnalysisParameters(String courseId, TimeRange timeRange) {
        this.courseId = courseId;
        this.timeRange = timeRange;
        this.additionalParams = new HashMap<>();
    }
    
    public String getCourseId() { return courseId; }
    public TimeRange getTimeRange() { return timeRange; }
}

class DashboardConfiguration {
    private final List<WidgetConfiguration> widgets;
    private final String title;
    
    public DashboardConfiguration(String title) {
        this.title = title;
        this.widgets = new ArrayList<>();
    }
    
    public List<WidgetConfiguration> getWidgets() { return widgets; }
    public void addWidget(WidgetConfiguration widget) { widgets.add(widget); }
}

class WidgetConfiguration {
    private final WidgetType type;
    private final String title;
    private final Map<String, Object> parameters;
    
    public WidgetConfiguration(WidgetType type, String title) {
        this.type = type;
        this.title = title;
        this.parameters = new HashMap<>();
    }
    
    public WidgetType getType() { return type; }
    public String getTitle() { return title; }
}

/**
 * Dashboard and widget classes.
 */
class InteractiveDashboard {
    private final DashboardConfiguration config;
    private final List<DashboardWidget> widgets;
    
    public InteractiveDashboard(DashboardConfiguration config) {
        this.config = config;
        this.widgets = new ArrayList<>();
    }
    
    public void addWidget(DashboardWidget widget) {
        widgets.add(widget);
    }
    
    public List<DashboardWidget> getWidgets() { return widgets; }
}

class DashboardWidget {
    private final WidgetType type;
    private final String title;
    private final Object data;
    
    public DashboardWidget(WidgetType type, String title, Object data) {
        this.type = type;
        this.title = title;
        this.data = data;
    }
    
    public WidgetType getType() { return type; }
    public String getTitle() { return title; }
    public Object getData() { return data; }
}

/**
 * Export configuration and result classes.
 */
class ExportConfiguration {
    private final ExportFormat format;
    private final String courseId;
    private final TimeRange timeRange;
    
    public ExportConfiguration(ExportFormat format, String courseId, TimeRange timeRange) {
        this.format = format;
        this.courseId = courseId;
        this.timeRange = timeRange;
    }
    
    public ExportFormat getFormat() { return format; }
    public String getCourseId() { return courseId; }
    public TimeRange getTimeRange() { return timeRange; }
}

class ExportResult {
    private final String message;
    private final String filename;
    private final int recordCount;
    
    public ExportResult(String message, String filename, int recordCount) {
        this.message = message;
        this.filename = filename;
        this.recordCount = recordCount;
    }
    
    public String getMessage() { return message; }
    public String getFilename() { return filename; }
    public int getRecordCount() { return recordCount; }
}