package com.brandguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "brands")
public class Brand {
    @Id
    private String id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 1000)
    private String preferredTone;

    @Column(nullable = false)
    private boolean fictional;

    protected Brand() {}

    public Brand(String id, String name, String description, String preferredTone, boolean fictional) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.preferredTone = preferredTone;
        this.fictional = fictional;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPreferredTone() { return preferredTone; }
    public boolean isFictional() { return fictional; }
    public void update(String name, String description, String preferredTone) {
        this.name = name;
        this.description = description;
        this.preferredTone = preferredTone;
    }
}
