package com.brandguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "review_records")
public class ReviewRecord {
    @Id
    private String id;
    @Column(name = "submission_id", nullable = false)
    private String submissionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;
    @Column(nullable = false, length = 40)
    private String action;
    @Column(nullable = false, length = 120)
    private String reviewer;
    @Column(length = 2000)
    private String comment;
    @Column(nullable = false)
    private Instant createdAt;

    protected ReviewRecord() {}
    public ReviewRecord(String id, String submissionId, String action, String reviewer, String comment) {
        this.id = id; this.submissionId = submissionId; this.action = action; this.reviewer = reviewer; this.comment = comment;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public String getId() { return id; }
    public String getSubmissionId() { return submissionId; }
    public String getAction() { return action; }
    public String getReviewer() { return reviewer; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
