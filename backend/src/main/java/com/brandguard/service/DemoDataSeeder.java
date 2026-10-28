package com.brandguard.service;

import com.brandguard.domain.Brand;
import com.brandguard.domain.BrandRule;
import com.brandguard.domain.Product;
import com.brandguard.repository.BrandRepository;
import com.brandguard.repository.BrandRuleRepository;
import com.brandguard.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataSeeder implements CommandLineRunner {
    public static final String BRAND_ID = "novafit";
    private final BrandRepository brands;
    private final ProductRepository products;
    private final BrandRuleRepository rules;

    public DemoDataSeeder(BrandRepository brands, ProductRepository products, BrandRuleRepository rules) {
        this.brands = brands;
        this.products = products;
        this.rules = rules;
    }

    @Override
    public void run(String... args) {
        brands.findById(BRAND_ID).orElseGet(() -> brands.save(new Brand(BRAND_ID, "NovaFit",
                "A fictional wellness brand for this BrandGuard AI demonstration.",
                "Friendly, informative, realistic, and trustworthy.", true)));
        if (products.findByBrandId(BRAND_ID).isEmpty()) {
            products.save(new Product("novafit-smart-band", BRAND_ID, "NovaFit Smart Band",
                    "A smart band for activity tracking and healthier daily habits.", new BigDecimal("1999.00")));
        }
        saveRuleIfMissing(new BrandRule("novafit-promo-policy", BRAND_ID, "PROMOTIONAL_CLAIM",
                "(?i)\\b(?:guarantee(?:d|s)?|100\\s*%\\s*results|risk[- ]free)\\b",
                "Performance guarantees require supporting evidence and approval.", "HIGH", true));
        saveRuleIfMissing(new BrandRule("novafit-disclaimer-demo", BRAND_ID, "REQUIRED_DISCLAIMER",
                "Results may vary.", "Use the required disclaimer on promotional material when configured.", "MEDIUM", false));
        saveRuleIfMissing(new BrandRule("novafit-prohibited-demo", BRAND_ID, "PROHIBITED_TERM",
                "miracle cure", "This prohibited terminology is not approved by the brand.", "HIGH", true));
        saveRuleIfMissing(new BrandRule("novafit-approved-terms", BRAND_ID, "APPROVED_TERM",
                "fitness tracker=>activity tracker", "Prefer the approved phrase “activity tracker” over “fitness tracker”.", "MEDIUM", true));
    }

    private void saveRuleIfMissing(BrandRule rule) {
        if (!rules.existsById(rule.getId())) rules.save(rule);
    }
}
