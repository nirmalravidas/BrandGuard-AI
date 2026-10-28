package com.brandguard.api;

import com.brandguard.api.ApiModels.*;
import com.brandguard.service.BrandGuardService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BrandGuardController {
    private final BrandGuardService service;

    public BrandGuardController(BrandGuardService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP"); }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("aiConfigured", service.aiConfigured(), "analysisMode", service.aiConfigured() ? "AI_ASSISTED" : "RULES_ONLY");
    }

    @GetMapping("/dashboard")
    public DashboardView dashboard() { return service.dashboard(); }

    @GetMapping("/brands")
    public java.util.List<BrandView> brands() { return service.allBrands(); }

    @GetMapping("/brands/{id}")
    public BrandView brand(@PathVariable String id) { return service.brand(id); }

    @PutMapping("/brands/{id}")
    public BrandView updateBrand(@PathVariable String id, @Valid @RequestBody BrandUpdate input) {
        return service.updateBrand(id, input);
    }

    @GetMapping("/brands/{id}/products")
    public java.util.List<ProductView> products(@PathVariable String id) { return service.products(id); }

    @PutMapping("/products/{id}")
    public ProductView updateProduct(@PathVariable String id, @Valid @RequestBody ProductUpdate input) {
        return service.updateProduct(id, input);
    }

    @GetMapping("/brands/{id}/guidelines")
    public java.util.List<RuleView> guidelines(@PathVariable String id) { return service.guidelines(id); }

    @PutMapping("/guidelines/{id}")
    public RuleView updateGuideline(@PathVariable String id, @Valid @RequestBody RuleUpdate input) {
        return service.updateRule(id, input);
    }

    @PostMapping("/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionView create(@Valid @RequestBody SubmissionCreate input) { return service.create(input); }

    @GetMapping("/submissions")
    public java.util.List<SubmissionView> submissions() { return service.allSubmissions(); }

    @GetMapping("/submissions/{id}")
    public SubmissionView submission(@PathVariable String id) { return service.submission(id); }

    @GetMapping("/submissions/{id}/findings")
    public java.util.List<FindingView> findings(@PathVariable String id) { return service.submission(id).findings(); }

    @GetMapping("/submissions/{id}/history")
    public Map<String, Object> history(@PathVariable String id) {
        SubmissionView view = service.submission(id);
        return Map.of("revisions", view.revisions(), "reviews", view.reviews());
    }

    @PostMapping("/submissions/{id}/analyze")
    public AnalysisResult analyze(@PathVariable String id) { return service.analyze(id); }

    @PutMapping("/submissions/{id}/content")
    public SubmissionView updateContent(@PathVariable String id, @Valid @RequestBody ContentUpdate input) {
        return service.updateContent(id, input);
    }

    @PostMapping("/submissions/{id}/submit-for-review")
    public SubmissionView submitForReview(@PathVariable String id) { return service.submitForReview(id); }

    @PostMapping("/submissions/{id}/approve")
    public SubmissionView approve(@PathVariable String id, @Valid @RequestBody ReviewRequest input) {
        return service.approve(id, input);
    }

    @PostMapping("/submissions/{id}/request-changes")
    public SubmissionView requestChanges(@PathVariable String id, @Valid @RequestBody ReviewRequest input) {
        return service.requestChanges(id, input);
    }
}
