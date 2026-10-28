package com.brandguard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.brandguard.api.ApiModels.ReviewRequest;
import com.brandguard.api.ApiModels.SubmissionCreate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:brandguard;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "brandguard.ai.api-key="
})
class SubmissionWorkflowTest {
    @Autowired
    private BrandGuardService service;

    @Test
    void contentCanBeAnalyzedAndApprovedWithPersistedReviewHistory() {
        var created = service.create(new SubmissionCreate("Workflow test", "SOCIAL_POST",
                "Discover the NovaFit Smart Band for ₹1,999."));
        var analysis = service.analyze(created.id());
        assertThat(analysis.mode()).isEqualTo("RULES_ONLY");
        assertThat(service.submission(created.id()).status()).isEqualTo("ANALYZED");

        service.submitForReview(created.id());
        var approved = service.approve(created.id(), new ReviewRequest("Reviewer", "Approved for demo."));
        assertThat(approved.status()).isEqualTo("APPROVED");
        assertThat(approved.reviews()).hasSize(1);
        assertThat(approved.reviews().getFirst().comment()).isEqualTo("Approved for demo.");
    }

    @Test
    void approvalCannotSkipThePendingReviewState() {
        var created = service.create(new SubmissionCreate("Invalid transition", "ADVERTISEMENT", "Draft copy."));
        assertThatThrownBy(() -> service.approve(created.id(), new ReviewRequest("Reviewer", "Nope")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void demoScenarioFindsBrandMismatchesAndSuggestsApprovedFacts() {
        var created = service.create(new SubmissionCreate("Demo scenario", "ADVERTISEMENT",
                "Get guaranteed fitness results with our new Nova Smart Watch for just ₹1,499!"));

        var analysis = service.analyze(created.id());

        assertThat(analysis.findings()).extracting(finding -> finding.category())
                .contains("PRODUCT_NAME", "PRICE_MISMATCH", "UNSUPPORTED_CLAIM");
        assertThat(analysis.correctedContent())
                .isEqualTo("Discover the NovaFit Smart Band for ₹1,999. Track your activity and build healthier daily habits.");
    }
}
