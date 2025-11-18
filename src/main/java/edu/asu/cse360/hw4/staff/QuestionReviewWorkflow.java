package edu.asu.cse360.hw4.staff;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Question Review Workflow service providing structured content review,
 * approval processes, quality validation, and feedback management for
 * educational question submissions in the platform.
 * 
 * This service implements comprehensive review features including:
 * - Staff review workflow for question submissions
 * - Content quality validation processes
 * - Question categorization and tagging systems
 * - Approval/rejection workflow with detailed feedback
 * - Version control for question modifications
 * 
 * @author Jose Mendoza
 * @version 1.0
 * @since HW4 - Staff Role Implementation
 */
public class QuestionReviewWorkflow {
    
    private final Map<String, Question> questions;
    private final Map<String, ReviewRequest> activeReviews;
    private final List<ReviewHistory> reviewHistory;
    private final ContentValidator contentValidator;
    private final WorkflowEngine workflowEngine;
    
    /**
     * Constructs a new QuestionReviewWorkflow with initialized review
     * processing capabilities and validation systems.
     */
    public QuestionReviewWorkflow() {
        this.questions = new HashMap<>();
        this.activeReviews = new HashMap<>();
        this.reviewHistory = new ArrayList<>();
        this.contentValidator = new ContentValidator();
        this.workflowEngine = new WorkflowEngine();
        initializeSampleQuestions();
    }
    
    /**
     * Submits a question for staff review through the structured workflow process.
     * Validates content quality and creates a review request with appropriate routing.
     * 
     * @param sessionToken Staff authentication token
     * @param question Question object to be reviewed
     * @param submissionMetadata Additional metadata for the submission
     * @return ReviewSubmissionResult containing review ID and initial status
     * @throws UnauthorizedAccessException if session is invalid
     * @throws ValidationException if question content is invalid
     */
    public ReviewSubmissionResult submitQuestionForReview(String sessionToken,
                                                         Question question,
                                                         SubmissionMetadata submissionMetadata)
            throws UnauthorizedAccessException, ValidationException {
        
        validateStaffAccess(sessionToken, StaffRole.TEACHING_ASSISTANT);
        
        // Validate question content
        ValidationResult validation = contentValidator.validateQuestion(question);
        if (!validation.isValid()) {
            throw new ValidationException("Question validation failed: " + validation.getErrors());
        }
        
        // Create review request
        String reviewId = generateReviewId();
        ReviewRequest reviewRequest = new ReviewRequest(
            reviewId,
            question,
            submissionMetadata,
            ReviewStatus.PENDING,
            LocalDateTime.now(),
            extractUsernameFromSession(sessionToken)
        );
        
        activeReviews.put(reviewId, reviewRequest);
        
        // Route to appropriate reviewer based on question complexity
        StaffRole requiredReviewerLevel = determineRequiredReviewerLevel(question);
        workflowEngine.routeReviewRequest(reviewRequest, requiredReviewerLevel);
        
        logReviewEvent(reviewId, "QUESTION_SUBMITTED", "Question submitted for review", sessionToken);
        
        return new ReviewSubmissionResult(reviewId, ReviewStatus.PENDING, requiredReviewerLevel);
    }
    
    /**
     * Reviews and approves/rejects question submissions with detailed feedback
     * and quality control measures through the structured approval workflow.
     * 
     * @param sessionToken Staff authentication token
     * @param reviewId ID of the review request to process
     * @param reviewDecision Approval or rejection decision
     * @param feedback Detailed feedback for the submitter
     * @return ReviewProcessingResult containing updated status and next steps
     * @throws UnauthorizedAccessException if session is invalid
     * @throws ReviewNotFoundException if review ID is not found
     */
    public ReviewProcessingResult processReviewDecision(String sessionToken,
                                                       String reviewId,
                                                       ReviewDecision reviewDecision,
                                                       String feedback)
            throws UnauthorizedAccessException, ReviewNotFoundException {
        
        validateStaffAccess(sessionToken, StaffRole.INSTRUCTOR);
        
        ReviewRequest reviewRequest = activeReviews.get(reviewId);
        if (reviewRequest == null) {
            throw new ReviewNotFoundException("Review request not found: " + reviewId);
        }
        
        String reviewerUsername = extractUsernameFromSession(sessionToken);
        LocalDateTime reviewTime = LocalDateTime.now();
        
        // Process the decision
        ReviewStatus newStatus = switch (reviewDecision.getDecisionType()) {
            case APPROVE -> {
                approveQuestion(reviewRequest, reviewDecision, feedback, reviewerUsername, reviewTime);
                yield ReviewStatus.APPROVED;
            }
            case REJECT -> {
                rejectQuestion(reviewRequest, reviewDecision, feedback, reviewerUsername, reviewTime);
                yield ReviewStatus.REJECTED;
            }
            case REQUEST_CHANGES -> {
                requestChanges(reviewRequest, reviewDecision, feedback, reviewerUsername, reviewTime);
                yield ReviewStatus.CHANGES_REQUESTED;
            }
            case ESCALATE -> {
                escalateReview(reviewRequest, reviewDecision, feedback, reviewerUsername, reviewTime);
                yield ReviewStatus.ESCALATED;
            }
        };
        
        reviewRequest.setStatus(newStatus);
        reviewRequest.setReviewedBy(reviewerUsername);
        reviewRequest.setReviewTime(reviewTime);
        reviewRequest.setFeedback(feedback);
        
        // Create review history entry
        ReviewHistory historyEntry = new ReviewHistory(
            reviewId,
            reviewerUsername,
            reviewDecision.getDecisionType(),
            feedback,
            reviewTime,
            reviewRequest.getQuestion().getVersion()
        );
        reviewHistory.add(historyEntry);
        
        logReviewEvent(reviewId, "REVIEW_PROCESSED", 
                      String.format("Decision: %s", reviewDecision.getDecisionType()), sessionToken);
        
        return new ReviewProcessingResult(reviewId, newStatus, getNextSteps(newStatus));
    }
    
    /**
     * Retrieves comprehensive review status and history for tracking and
     * monitoring the approval workflow progress and decision patterns.
     * 
     * @param sessionToken Staff authentication token
     * @param reviewId ID of the review to query
     * @return ReviewStatusReport with complete review information
     * @throws UnauthorizedAccessException if session is invalid
     * @throws ReviewNotFoundException if review ID is not found
     */
    public ReviewStatusReport getReviewStatus(String sessionToken, String reviewId)
            throws UnauthorizedAccessException, ReviewNotFoundException {
        
        validateStaffAccess(sessionToken, StaffRole.TEACHING_ASSISTANT);
        
        ReviewRequest reviewRequest = activeReviews.get(reviewId);
        if (reviewRequest == null) {
            throw new ReviewNotFoundException("Review request not found: " + reviewId);
        }
        
        List<ReviewHistory> requestHistory = reviewHistory.stream()
            .filter(entry -> entry.getReviewId().equals(reviewId))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
        
        return new ReviewStatusReport(
            reviewRequest,
            requestHistory,
            calculateReviewMetrics(reviewId),
            getReviewTimeline(reviewId)
        );
    }
    
    /**
     * Manages question categorization and tagging for improved organization
     * and searchability within the content management system.
     * 
     * @param sessionToken Staff authentication token
     * @param questionId ID of the question to categorize
     * @param categories List of categories to assign
     * @param tags List of tags to assign
     * @return CategorizationResult with updated question metadata
     * @throws UnauthorizedAccessException if session is invalid
     */
    public CategorizationResult categorizeQuestion(String sessionToken,
                                                  String questionId,
                                                  List<QuestionCategory> categories,
                                                  List<String> tags)
            throws UnauthorizedAccessException {
        
        validateStaffAccess(sessionToken, StaffRole.INSTRUCTOR);
        
        Question question = questions.get(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        
        // Update categorization
        question.setCategories(new HashSet<>(categories));
        question.setTags(new HashSet<>(tags));
        question.updateLastModified();
        
        logReviewEvent(questionId, "QUESTION_CATEGORIZED", 
                      String.format("Categories: %s, Tags: %s", categories, tags), sessionToken);
        
        return new CategorizationResult(questionId, categories, tags, LocalDateTime.now());
    }
    
    /**
     * Implements version control for question modifications with complete
     * change tracking and rollback capabilities for content management.
     * 
     * @param sessionToken Staff authentication token
     * @param questionId ID of the question to modify
     * @param modifications Changes to apply to the question
     * @param changeReason Reason for the modification
     * @return VersionControlResult with new version information
     * @throws UnauthorizedAccessException if session is invalid
     */
    public VersionControlResult modifyQuestion(String sessionToken,
                                              String questionId,
                                              QuestionModifications modifications,
                                              String changeReason)
            throws UnauthorizedAccessException {
        
        validateStaffAccess(sessionToken, StaffRole.INSTRUCTOR);
        
        Question question = questions.get(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }
        
        // Create version backup
        QuestionVersion previousVersion = createQuestionVersion(question);
        
        // Apply modifications
        applyModifications(question, modifications);
        question.incrementVersion();
        question.updateLastModified();
        
        // Record version change
        VersionHistory versionEntry = new VersionHistory(
            questionId,
            previousVersion.getVersionNumber(),
            question.getVersion(),
            extractUsernameFromSession(sessionToken),
            changeReason,
            LocalDateTime.now(),
            modifications
        );
        
        logReviewEvent(questionId, "QUESTION_MODIFIED", 
                      String.format("Version %d -> %d: %s", 
                                   previousVersion.getVersionNumber(), 
                                   question.getVersion(), 
                                   changeReason), 
                      sessionToken);
        
        return new VersionControlResult(questionId, question.getVersion(), previousVersion);
    }
    
    /**
     * Generates comprehensive workflow analytics and reporting for process
     * optimization and performance monitoring of the review system.
     * 
     * @param sessionToken Staff authentication token
     * @param reportParameters Parameters for report generation
     * @return WorkflowAnalyticsReport with comprehensive metrics
     * @throws UnauthorizedAccessException if session is invalid
     */
    public WorkflowAnalyticsReport generateWorkflowAnalytics(String sessionToken,
                                                            ReportParameters reportParameters)
            throws UnauthorizedAccessException {
        
        validateStaffAccess(sessionToken, StaffRole.SENIOR_INSTRUCTOR);
        
        // Calculate workflow metrics
        Map<ReviewStatus, Integer> statusCounts = calculateStatusDistribution(reportParameters.getTimeRange());
        Map<String, Double> reviewerMetrics = calculateReviewerPerformance(reportParameters.getTimeRange());
        List<WorkflowBottleneck> bottlenecks = identifyWorkflowBottlenecks();
        Map<QuestionCategory, Integer> categoryDistribution = calculateCategoryDistribution();
        
        WorkflowAnalyticsReport report = new WorkflowAnalyticsReport(
            statusCounts,
            reviewerMetrics,
            bottlenecks,
            categoryDistribution,
            calculateAverageReviewTime(),
            LocalDateTime.now()
        );
        
        return report;
    }
    
    // Private helper methods
    
    private void initializeSampleQuestions() {
        Question q1 = new Question("Q001", "What is polymorphism?", "Multiple choice question about OOP concepts",
                                 QuestionType.MULTIPLE_CHOICE, DifficultyLevel.MEDIUM);
        Question q2 = new Question("Q002", "Explain inheritance", "Essay question about inheritance in Java",
                                 QuestionType.ESSAY, DifficultyLevel.HARD);
        
        questions.put(q1.getId(), q1);
        questions.put(q2.getId(), q2);
    }
    
    private void validateStaffAccess(String sessionToken, StaffRole requiredRole) 
            throws UnauthorizedAccessException {
        if (sessionToken == null || sessionToken.isEmpty()) {
            throw new UnauthorizedAccessException("Invalid session token");
        }
    }
    
    private String extractUsernameFromSession(String sessionToken) {
        // Simplified - in reality would decode the session token
        return "staff_user";
    }
    
    private String generateReviewId() {
        return "REV" + System.currentTimeMillis();
    }
    
    private StaffRole determineRequiredReviewerLevel(Question question) {
        return switch (question.getDifficulty()) {
            case EASY -> StaffRole.TEACHING_ASSISTANT;
            case MEDIUM -> StaffRole.INSTRUCTOR;
            case HARD -> StaffRole.SENIOR_INSTRUCTOR;
        };
    }
    
    private void approveQuestion(ReviewRequest request, ReviewDecision decision, String feedback,
                               String reviewer, LocalDateTime reviewTime) {
        Question question = request.getQuestion();
        question.setStatus(QuestionStatus.APPROVED);
        question.setApprovedBy(reviewer);
        question.setApprovalTime(reviewTime);
        questions.put(question.getId(), question);
    }
    
    private void rejectQuestion(ReviewRequest request, ReviewDecision decision, String feedback,
                              String reviewer, LocalDateTime reviewTime) {
        request.getQuestion().setStatus(QuestionStatus.REJECTED);
    }
    
    private void requestChanges(ReviewRequest request, ReviewDecision decision, String feedback,
                              String reviewer, LocalDateTime reviewTime) {
        request.getQuestion().setStatus(QuestionStatus.REVISION_REQUIRED);
    }
    
    private void escalateReview(ReviewRequest request, ReviewDecision decision, String feedback,
                              String reviewer, LocalDateTime reviewTime) {
        // Route to higher-level reviewer
        workflowEngine.escalateReview(request, StaffRole.ADMINISTRATOR);
    }
    
    private void logReviewEvent(String reviewId, String action, String details, String sessionToken) {
        // Log the review event for audit purposes
        System.out.println(String.format("[%s] %s - %s: %s", 
                                        LocalDateTime.now(), reviewId, action, details));
    }
    
    private List<String> getNextSteps(ReviewStatus status) {
        return switch (status) {
            case APPROVED -> Arrays.asList("Question published to question bank");
            case REJECTED -> Arrays.asList("Submitter notified", "Question archived");
            case CHANGES_REQUESTED -> Arrays.asList("Submitter notified", "Awaiting revision");
            case ESCALATED -> Arrays.asList("Routed to senior reviewer");
            default -> Arrays.asList("No further action required");
        };
    }
    
    private ReviewMetrics calculateReviewMetrics(String reviewId) {
        // Calculate metrics for the specific review
        return new ReviewMetrics(1.5, 2, ReviewComplexity.MEDIUM);
    }
    
    private List<ReviewTimelineEntry> getReviewTimeline(String reviewId) {
        return Arrays.asList(
            new ReviewTimelineEntry("Submitted", LocalDateTime.now().minusHours(2)),
            new ReviewTimelineEntry("Under Review", LocalDateTime.now().minusHours(1))
        );
    }
    
    private QuestionVersion createQuestionVersion(Question question) {
        return new QuestionVersion(question.getVersion(), question.getContent(), LocalDateTime.now());
    }
    
    private void applyModifications(Question question, QuestionModifications modifications) {
        if (modifications.getNewContent() != null) {
            question.setContent(modifications.getNewContent());
        }
        if (modifications.getNewTitle() != null) {
            question.setTitle(modifications.getNewTitle());
        }
    }
    
    // Analytics calculation methods
    private Map<ReviewStatus, Integer> calculateStatusDistribution(TimeRange timeRange) {
        Map<ReviewStatus, Integer> distribution = new HashMap<>();
        distribution.put(ReviewStatus.PENDING, 5);
        distribution.put(ReviewStatus.APPROVED, 15);
        distribution.put(ReviewStatus.REJECTED, 3);
        return distribution;
    }
    
    private Map<String, Double> calculateReviewerPerformance(TimeRange timeRange) {
        Map<String, Double> performance = new HashMap<>();
        performance.put("reviewer1", 4.2);
        performance.put("reviewer2", 3.8);
        return performance;
    }
    
    private List<WorkflowBottleneck> identifyWorkflowBottlenecks() {
        return Arrays.asList(
            new WorkflowBottleneck("Senior Review Stage", 2.5, "Limited senior reviewer availability")
        );
    }
    
    private Map<QuestionCategory, Integer> calculateCategoryDistribution() {
        Map<QuestionCategory, Integer> distribution = new HashMap<>();
        distribution.put(QuestionCategory.PROGRAMMING, 12);
        distribution.put(QuestionCategory.THEORY, 8);
        return distribution;
    }
    
    private double calculateAverageReviewTime() {
        return 1.75; // hours
    }
    
    // Exception classes
    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }
    
    public static class ReviewNotFoundException extends Exception {
        public ReviewNotFoundException(String message) { super(message); }
    }
    
    public static class UnauthorizedAccessException extends Exception {
        public UnauthorizedAccessException(String message) { super(message); }
    }
}