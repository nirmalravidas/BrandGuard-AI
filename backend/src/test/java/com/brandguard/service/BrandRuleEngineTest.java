package com.brandguard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.brandguard.domain.BrandRule;
import com.brandguard.domain.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrandRuleEngineTest {
    private BrandRuleEngine engine;
    private Product product;
    private List<BrandRule> rules;

    @BeforeEach
    void setUp() {
        engine = new BrandRuleEngine();
        product = new Product("band-id", "brand-id", "NovaFit Smart Band", "Activity tracking", new BigDecimal("1999.00"));
        rules = List.of(
                new BrandRule("prohibited-id", "brand-id", "PROHIBITED_TERM", "miracle cure",
                        "Not approved.", "HIGH", true),
                new BrandRule("disclaimer-id", "brand-id", "REQUIRED_DISCLAIMER", "Results may vary.",
                        "Disclaimer required.", "MEDIUM", true));
    }

    @Test
    void correctContentIsNotFlagged() {
        assertThat(engine.analyze("Discover the NovaFit Smart Band for ₹1,999. Results may vary.",
                List.of(product), rules)).isEmpty();
    }

    @Test
    void detectsIncorrectProductName() {
        assertThat(engine.analyze("Try our Nova Smart Watch today.", List.of(product), List.of()))
                .extracting(FindingDraft::category).contains("PRODUCT_NAME");
    }

    @Test
    void detectsPriceDiscrepancy() {
        assertThat(engine.analyze("NovaFit Smart Band for just ₹1,499.", List.of(product), List.of()))
                .extracting(FindingDraft::category).contains("PRICE_MISMATCH");
    }

    @Test
    void detectsProhibitedTerminology() {
        assertThat(engine.analyze("The miracle cure is here.", List.of(), rules))
                .extracting(FindingDraft::category).contains("PROHIBITED_TERM");
    }

    @Test
    void detectsMissingRequiredDisclaimer() {
        assertThat(engine.analyze("Discover the product.", List.of(), rules))
                .extracting(FindingDraft::category).contains("MISSING_DISCLAIMER");
    }

    @Test
    void replacesDeprecatedTerminologyWithApprovedTerm() {
        var termRule = new BrandRule("term-id", "brand-id", "APPROVED_TERM",
                "fitness tracker=>activity tracker", "Use the approved term.", "MEDIUM", true);
        assertThat(engine.analyze("Meet our fitness tracker.", List.of(), List.of(termRule)))
                .extracting(FindingDraft::suggestion).contains("activity tracker");
    }
}
