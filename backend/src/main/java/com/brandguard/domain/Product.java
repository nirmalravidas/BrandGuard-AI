package com.brandguard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private String id;
    @Column(name = "brand_id", nullable = false)
    private String brandId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", insertable = false, updatable = false)
    private Brand brand;
    @NotBlank
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, length = 2000)
    private String description;
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    protected Product() {}

    public Product(String id, String brandId, String name, String description, BigDecimal price) {
        this.id = id;
        this.brandId = brandId;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getId() { return id; }
    public String getBrandId() { return brandId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public void update(String name, String description, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }
}
