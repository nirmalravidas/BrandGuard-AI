package com.brandguard.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

public final class ApiModels {
    private ApiModels() {}

    public record BrandView(String id, String name, String description, String preferredTone, boolean fictional) {}
    public record BrandUpdate(@NotBlank @Size(max = 120) String name,
                              @NotBlank @Size(max = 2000) String description,
                              @NotBlank @Size(max = 1000) String preferredTone) {}
    public record ProductView(String id, String brandId, String name, String description, String price) {}
    public record ProductUpdate(@NotBlank @Size(max = 120) String name,
                                @NotBlank @Size(max = 2000) String description,
                                @NotBlank String price) {}
    public record RuleView(String id, String kind, String value, String explanation, String severity, boolean enabled) {}
    public record RuleUpdate(@NotBlank @Size(max = 1000) String value,
                             @NotBlank @Size(max = 2000) String explanation,
                             @NotBlank String severity, boolean enabled) {}
    public record SubmissionCreate(@NotBlank @Size(max = 160) String title,
                                   @NotBlank String contentType,
                                   @NotBlank @Size(max = 12000) String content) {}
    public record ContentUpdate(@NotBlank @Size(max = 12000) String content) {}
    public record ReviewRequest(@Size(max = 120) String reviewer, @Size(max = 2000) String comment) {}
    public record FindingView(String id, String category, String severity, String source, String excerpt,
                              String explanation, String expectedValue, String evidenceSourceId,
                              String evidenceReference, String suggestion, boolean needsVerification) {}
    public record RevisionView(String id, String content, String action, Instant createdAt) {}
    public record ReviewView(String id, String action, String reviewer, String comment, Instant createdAt) {}
    public record SubmissionView(String id, String brandId, String title, String contentType, String originalContent,
                                 String currentContent, String status, String analysisSummary, Instant createdAt,
                                 Instant updatedAt, List<FindingView> findings, List<RevisionView> revisions,
                                 List<ReviewView> reviews, String analysisMode, String correctedContent) {}
    public record AnalysisResult(String summary, String mode, String correctedContent, List<FindingView> findings) {}
    public record DashboardView(long submissions, long pendingReviews, long approved, List<SubmissionView> recent) {}
    public record ErrorView(String message, Instant timestamp) {}
}
