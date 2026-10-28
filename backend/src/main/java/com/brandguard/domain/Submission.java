package com.brandguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "content_submissions")
public class Submission {
    @Id
    private String id;
    @Column(name = "brand_id", nullable = false)
    private String brandId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brand brand;
    @Column(nullable = false, length = 160)
    private String title;
    @Column(nullable = false, length = 40)
    private String contentType;
    @Column(nullable = false, length = 12000)
    private String originalContent;
    @Column(nullable = false, length = 12000)
    private String currentContent;
    @Column(nullable = false, length = 40)
    private String status;
    @Column(length = 2000)
    private String analysisSummary;
    @Column(length = 40)
    private String analysisMode;
    @Column(length = 12000)
    private String suggestedContent;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected Submission() {}

    public Submission(String id, String brandId, String title, String contentType, String content) {
        this.id = id;
        this.brandId = brandId;
        this.title = title;
        this.contentType = contentType;
        this.originalContent = content;
        this.currentContent = content;
        this.status = "DRAFT";
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; }
    public String getBrandId() { return brandId; }
    public String getTitle() { return title; }
    public String getContentType() { return contentType; }
    public String getOriginalContent() { return originalContent; }
    public String getCurrentContent() { return currentContent; }
    public String getStatus() { return status; }
    public String getAnalysisSummary() { return analysisSummary; }
    public String getAnalysisMode() { return analysisMode; }
    public String getSuggestedContent() { return suggestedContent; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setCurrentContent(String content) { currentContent = content; }
    public void setStatus(String status) { this.status = status; }
    public void setAnalysisSummary(String summary) { analysisSummary = summary; }
    public void setAnalysisMode(String mode) { analysisMode = mode; }
    public void setSuggestedContent(String content) { suggestedContent = content; }
}
