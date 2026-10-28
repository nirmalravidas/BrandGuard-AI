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
@Table(name = "content_revisions")
public class ContentRevision {
    @Id
    private String id;
    @Column(name = "submission_id", nullable = false)
    private String submissionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    private Submission submission;
    @Column(nullable = false, length = 12000)
    private String content;
    @Column(nullable = false, length = 40)
    private String action;
    @Column(nullable = false)
    private Instant createdAt;

    protected ContentRevision() {}
    public ContentRevision(String id, String submissionId, String content, String action) {
        this.id = id; this.submissionId = submissionId; this.content = content; this.action = action;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public String getId() { return id; }
    public String getSubmissionId() { return submissionId; }
    public String getContent() { return content; }
    public String getAction() { return action; }
    public Instant getCreatedAt() { return createdAt; }
}
