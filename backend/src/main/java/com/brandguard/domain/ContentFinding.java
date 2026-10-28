package com.brandguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_findings")
public class ContentFinding {
    @Id
    private String id;
    @Column(name = "submission_id", nullable = false)
    private String submissionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;
    @Column(nullable = false)
    private String category;
    @Column(nullable = false)
    private String severity;
    @Column(nullable = false, length = 40)
    private String source;
    @Column(nullable = false, length = 1000)
    private String excerpt;
    @Column(nullable = false, length = 2000)
    private String explanation;
    @Column(length = 1000)
    private String expectedValue;
    @Column(length = 100)
    private String evidenceSourceId;
    @Column(length = 1000)
    private String suggestion;
    @Column(nullable = false)
    private boolean needsVerification;

    protected ContentFinding() {}

    public ContentFinding(String id, String submissionId, String category, String severity, String source,
                          String excerpt, String explanation, String expectedValue, String evidenceSourceId,
                          String suggestion, boolean needsVerification) {
        this.id = id;
        this.submissionId = submissionId;
        this.category = category;
        this.severity = severity;
        this.source = source;
        this.excerpt = excerpt;
        this.explanation = explanation;
        this.expectedValue = expectedValue;
        this.evidenceSourceId = evidenceSourceId;
        this.suggestion = suggestion;
        this.needsVerification = needsVerification;
    }

    public String getId() { return id; }
    public String getSubmissionId() { return submissionId; }
    public String getCategory() { return category; }
    public String getSeverity() { return severity; }
    public String getSource() { return source; }
    public String getExcerpt() { return excerpt; }
    public String getExplanation() { return explanation; }
    public String getExpectedValue() { return expectedValue; }
    public String getEvidenceSourceId() { return evidenceSourceId; }
    public String getSuggestion() { return suggestion; }
    public boolean isNeedsVerification() { return needsVerification; }
}
