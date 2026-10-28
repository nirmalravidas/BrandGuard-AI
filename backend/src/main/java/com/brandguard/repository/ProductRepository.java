package com.brandguard.repository;

import com.brandguard.domain.Product;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByBrandId(String brandId);
}
