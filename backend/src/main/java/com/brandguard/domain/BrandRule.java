package com.brandguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "brand_rules")
public class BrandRule {
    @Id
    private String id;
    @Column(name = "brand_id", nullable = false)
    private String brandId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brand brand;
    @Column(nullable = false)
    private String kind;
    @Column(name = "rule_value", nullable = false, length = 1000)
    private String value;
    @Column(nullable = false, length = 2000)
    private String explanation;
    @Column(nullable = false)
    private String severity;
    @Column(nullable = false)
    private boolean enabled;

    protected BrandRule() {}

    public BrandRule(String id, String brandId, String kind, String value, String explanation, String severity, boolean enabled) {
        this.id = id;
        this.brandId = brandId;
        this.kind = kind;
        this.value = value;
        this.explanation = explanation;
        this.severity = severity;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public String getBrandId() { return brandId; }
    public String getKind() { return kind; }
    public String getValue() { return value; }
    public String getExplanation() { return explanation; }
    public String getSeverity() { return severity; }
    public boolean isEnabled() { return enabled; }
    public void update(String value, String explanation, String severity, boolean enabled) {
        this.value = value;
        this.explanation = explanation;
        this.severity = severity;
        this.enabled = enabled;
    }
}
