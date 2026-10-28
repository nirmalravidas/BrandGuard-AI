package com.brandguard.repository;

import com.brandguard.domain.BrandRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRuleRepository extends JpaRepository<BrandRule, String> {
    List<BrandRule> findByBrandIdAndEnabledTrue(String brandId);
    List<BrandRule> findByBrandId(String brandId);
}
