package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Additional supporting model classes for Question Review Workflow system.
 * These classes provide data structures for question management, review processes,
 * and workflow analytics used throughout the staff services.
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */

// Question-related classes
class Question {
    private final String id;
    private String title;
    private String content;
    private final QuestionType type;
    private final DifficultyLevel difficulty;
    private QuestionStatus status;
    private Set<QuestionCategory> categories;
    private Set<String> tags;
    private int version;
    private LocalDateTime lastModified;
    private String approvedBy;
    private LocalDateTime approvalTime;
    
    public Question(String id, String title, String content, QuestionType type, DifficultyLevel difficulty) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.status = QuestionStatus.DRAFT;
        this.categories = new HashSet<>();
        this.tags = new HashSet<>();
        this.version = 1;
        this.lastModified = LocalDateTime.now();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public QuestionType getType() { return type; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public QuestionStatus getStatus() { return status; }
    public void setStatus(QuestionStatus status) { this.status = status; }
    public Set<QuestionCategory> getCategories() { return categories; }
    public void setCategories(Set<QuestionCategory> categories) { this.categories = categories; }
    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public int getVersion() { return version; }
    public void incrementVersion() { this.version++; }
    public LocalDateTime getLastModified() { return lastModified; }
    public void updateLastModified() { this.lastModified = LocalDateTime.now(); }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public LocalDateTime getApprovalTime() { return approvalTime; }
    public void setApprovalTime(LocalDateTime approvalTime) { this.approvalTime = approvalTime; }
}

// Enums for question system
enum QuestionType {
    MULTIPLE_CHOICE, ESSAY, SHORT_ANSWER, TRUE_FALSE, CODING
}

enum DifficultyLevel {
    EASY, MEDIUM, HARD
}

enum QuestionStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, REVISION_REQUIRED, PUBLISHED
}

enum QuestionCategory {
    PROGRAMMING, THEORY, DESIGN_PATTERNS, ALGORITHMS, DATA_STRUCTURES, SOFTWARE_ENGINEERING
}

enum ReviewStatus {
    PENDING, UNDER_REVIEW, APPROVED, REJECTED, CHANGES_REQUESTED, ESCALATED
}

enum DecisionType {
    APPROVE, REJECT, REQUEST_CHANGES, ESCALATE
}

enum ReviewComplexity {
    LOW, MEDIUM, HIGH
}

// Review workflow classes
class ReviewRequest {
    private final String reviewId;
    private final Question question;
    private final SubmissionMetadata metadata;
    private ReviewStatus status;
    private final LocalDateTime submissionTime;
    private final String submittedBy;
    private String reviewedBy;
    private LocalDateTime reviewTime;
    private String feedback;
    
    public ReviewRequest(String reviewId, Question question, SubmissionMetadata metadata,
                        ReviewStatus status, LocalDateTime submissionTime, String submittedBy) {
        this.reviewId = reviewId;
        this.question = question;
        this.metadata = metadata;
        this.status = status;
        this.submissionTime = submissionTime;
        this.submittedBy = submittedBy;
    }
    
    // Getters and setters
    public String getReviewId() { return reviewId; }
    public Question getQuestion() { return question; }
    public SubmissionMetadata getMetadata() { return metadata; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public LocalDateTime getSubmissionTime() { return submissionTime; }
    public String getSubmittedBy() { return submittedBy; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }
    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}

class SubmissionMetadata {
    private final String courseId;
    private final String submitterNotes;
    private final List<String> attachments;
    
    public SubmissionMetadata(String courseId, String submitterNotes, List<String> attachments) {
        this.courseId = courseId;
        this.submitterNotes = submitterNotes;
        this.attachments = attachments;
    }
    
    public String getCourseId() { return courseId; }
    public String getSubmitterNotes() { return submitterNotes; }
    public List<String> getAttachments() { return attachments; }
}

class ReviewDecision {
    private final DecisionType decisionType;
    private final String reason;
    private final Map<String, Object> additionalData;
    
    public ReviewDecision(DecisionType decisionType, String reason) {
        this.decisionType = decisionType;
        this.reason = reason;
        this.additionalData = new HashMap<>();
    }
    
    public DecisionType getDecisionType() { return decisionType; }
    public String getReason() { return reason; }
}

class ReviewHistory {
    private final String reviewId;
    private final String reviewer;
    private final DecisionType decision;
    private final String feedback;
    private final LocalDateTime timestamp;
    private final int questionVersion;
    
    public ReviewHistory(String reviewId, String reviewer, DecisionType decision,
                        String feedback, LocalDateTime timestamp, int questionVersion) {
        this.reviewId = reviewId;
        this.reviewer = reviewer;
        this.decision = decision;
        this.feedback = feedback;
        this.timestamp = timestamp;
        this.questionVersion = questionVersion;
    }
    
    public String getReviewId() { return reviewId; }
    public String getReviewer() { return reviewer; }
    public DecisionType getDecision() { return decision; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public int getQuestionVersion() { return questionVersion; }
}

// Validation classes
class ContentValidator {
    public ValidationResult validateQuestion(Question question) {
        List<String> errors = new ArrayList<>();
        
        if (question.getTitle() == null || question.getTitle().trim().isEmpty()) {
            errors.add("Question title is required");
        }
        
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            errors.add("Question content is required");
        }
        
        if (question.getContent() != null && question.getContent().length() > 5000) {
            errors.add("Question content exceeds maximum length");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}

class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    
    public ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = errors;
    }
    
    public boolean isValid() { return valid; }
    public List<String> getErrors() { return errors; }
}

// Workflow engine
class WorkflowEngine {
    public void routeReviewRequest(ReviewRequest request, StaffRole requiredLevel) {
        // Route to appropriate reviewer based on required level
        System.out.println("Routing review " + request.getReviewId() + " to " + requiredLevel);
    }
    
    public void escalateReview(ReviewRequest request, StaffRole escalationLevel) {
        // Escalate review to higher level
        System.out.println("Escalating review " + request.getReviewId() + " to " + escalationLevel);
    }
}

// Result classes
class ReviewSubmissionResult {
    private final String reviewId;
    private final ReviewStatus status;
    private final StaffRole requiredReviewerLevel;
    
    public ReviewSubmissionResult(String reviewId, ReviewStatus status, StaffRole requiredReviewerLevel) {
        this.reviewId = reviewId;
        this.status = status;
        this.requiredReviewerLevel = requiredReviewerLevel;
    }
    
    public String getReviewId() { return reviewId; }
    public ReviewStatus getStatus() { return status; }
    public StaffRole getRequiredReviewerLevel() { return requiredReviewerLevel; }
}

class ReviewProcessingResult {
    private final String reviewId;
    private final ReviewStatus newStatus;
    private final List<String> nextSteps;
    
    public ReviewProcessingResult(String reviewId, ReviewStatus newStatus, List<String> nextSteps) {
        this.reviewId = reviewId;
        this.newStatus = newStatus;
        this.nextSteps = nextSteps;
    }
    
    public String getReviewId() { return reviewId; }
    public ReviewStatus getNewStatus() { return newStatus; }
    public List<String> getNextSteps() { return nextSteps; }
}

class ReviewStatusReport {
    private final ReviewRequest reviewRequest;
    private final List<ReviewHistory> history;
    private final ReviewMetrics metrics;
    private final List<ReviewTimelineEntry> timeline;
    
    public ReviewStatusReport(ReviewRequest reviewRequest, List<ReviewHistory> history,
                             ReviewMetrics metrics, List<ReviewTimelineEntry> timeline) {
        this.reviewRequest = reviewRequest;
        this.history = history;
        this.metrics = metrics;
        this.timeline = timeline;
    }
    
    public ReviewRequest getReviewRequest() { return reviewRequest; }
    public List<ReviewHistory> getHistory() { return history; }
}

class ReviewMetrics {
    private final double averageReviewTime;
    private final int totalReviews;
    private final ReviewComplexity complexity;
    
    public ReviewMetrics(double averageReviewTime, int totalReviews, ReviewComplexity complexity) {
        this.averageReviewTime = averageReviewTime;
        this.totalReviews = totalReviews;
        this.complexity = complexity;
    }
    
    public double getAverageReviewTime() { return averageReviewTime; }
    public int getTotalReviews() { return totalReviews; }
    public ReviewComplexity getComplexity() { return complexity; }
}

class ReviewTimelineEntry {
    private final String event;
    private final LocalDateTime timestamp;
    
    public ReviewTimelineEntry(String event, LocalDateTime timestamp) {
        this.event = event;
        this.timestamp = timestamp;
    }
    
    public String getEvent() { return event; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

// Categorization and version control classes
class CategorizationResult {
    private final String questionId;
    private final List<QuestionCategory> categories;
    private final List<String> tags;
    private final LocalDateTime timestamp;
    
    public CategorizationResult(String questionId, List<QuestionCategory> categories,
                               List<String> tags, LocalDateTime timestamp) {
        this.questionId = questionId;
        this.categories = categories;
        this.tags = tags;
        this.timestamp = timestamp;
    }
    
    public String getQuestionId() { return questionId; }
    public List<QuestionCategory> getCategories() { return categories; }
}

class QuestionModifications {
    private final String newTitle;
    private final String newContent;
    private final Set<QuestionCategory> newCategories;
    private final Set<String> newTags;
    
    public QuestionModifications(String newTitle, String newContent) {
        this.newTitle = newTitle;
        this.newContent = newContent;
        this.newCategories = new HashSet<>();
        this.newTags = new HashSet<>();
    }
    
    public String getNewTitle() { return newTitle; }
    public String getNewContent() { return newContent; }
}

class VersionControlResult {
    private final String questionId;
    private final int newVersion;
    private final QuestionVersion previousVersion;
    
    public VersionControlResult(String questionId, int newVersion, QuestionVersion previousVersion) {
        this.questionId = questionId;
        this.newVersion = newVersion;
        this.previousVersion = previousVersion;
    }
    
    public String getQuestionId() { return questionId; }
    public int getNewVersion() { return newVersion; }
}

class QuestionVersion {
    private final int versionNumber;
    private final String content;
    private final LocalDateTime createdTime;
    
    public QuestionVersion(int versionNumber, String content, LocalDateTime createdTime) {
        this.versionNumber = versionNumber;
        this.content = content;
        this.createdTime = createdTime;
    }
    
    public int getVersionNumber() { return versionNumber; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedTime() { return createdTime; }
}

class VersionHistory {
    private final String questionId;
    private final int fromVersion;
    private final int toVersion;
    private final String modifiedBy;
    private final String changeReason;
    private final LocalDateTime timestamp;
    private final QuestionModifications modifications;
    
    public VersionHistory(String questionId, int fromVersion, int toVersion, String modifiedBy,
                         String changeReason, LocalDateTime timestamp, QuestionModifications modifications) {
        this.questionId = questionId;
        this.fromVersion = fromVersion;
        this.toVersion = toVersion;
        this.modifiedBy = modifiedBy;
        this.changeReason = changeReason;
        this.timestamp = timestamp;
        this.modifications = modifications;
    }
    
    public String getQuestionId() { return questionId; }
    public int getFromVersion() { return fromVersion; }
    public int getToVersion() { return toVersion; }
}

// Analytics and reporting classes
class ReportParameters {
    private final TimeRange timeRange;
    private final Set<ReviewStatus> statusFilter;
    private final Set<String> reviewerFilter;
    
    public ReportParameters(TimeRange timeRange) {
        this.timeRange = timeRange;
        this.statusFilter = new HashSet<>();
        this.reviewerFilter = new HashSet<>();
    }
    
    public TimeRange getTimeRange() { return timeRange; }
}

class WorkflowAnalyticsReport {
    private final Map<ReviewStatus, Integer> statusCounts;
    private final Map<String, Double> reviewerMetrics;
    private final List<WorkflowBottleneck> bottlenecks;
    private final Map<QuestionCategory, Integer> categoryDistribution;
    private final double averageReviewTime;
    private final LocalDateTime generatedTime;
    
    public WorkflowAnalyticsReport(Map<ReviewStatus, Integer> statusCounts,
                                  Map<String, Double> reviewerMetrics,
                                  List<WorkflowBottleneck> bottlenecks,
                                  Map<QuestionCategory, Integer> categoryDistribution,
                                  double averageReviewTime,
                                  LocalDateTime generatedTime) {
        this.statusCounts = statusCounts;
        this.reviewerMetrics = reviewerMetrics;
        this.bottlenecks = bottlenecks;
        this.categoryDistribution = categoryDistribution;
        this.averageReviewTime = averageReviewTime;
        this.generatedTime = generatedTime;
    }
    
    public Map<ReviewStatus, Integer> getStatusCounts() { return statusCounts; }
    public Map<String, Double> getReviewerMetrics() { return reviewerMetrics; }
}

class WorkflowBottleneck {
    private final String stage;
    private final double averageDelayHours;
    private final String description;
    
    public WorkflowBottleneck(String stage, double averageDelayHours, String description) {
        this.stage = stage;
        this.averageDelayHours = averageDelayHours;
        this.description = description;
    }
    
    public String getStage() { return stage; }
    public double getAverageDelayHours() { return averageDelayHours; }
    public String getDescription() { return description; }
}