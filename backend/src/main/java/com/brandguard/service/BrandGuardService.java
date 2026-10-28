package com.brandguard.service;

import com.brandguard.api.ApiModels;
import com.brandguard.api.ApiModels.*;
import com.brandguard.domain.*;
import com.brandguard.repository.*;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BrandGuardService {
    private final BrandRepository brands;
    private final ProductRepository products;
    private final BrandRuleRepository rules;
    private final SubmissionRepository submissions;
    private final ContentFindingRepository findings;
    private final ContentRevisionRepository revisions;
    private final ReviewRecordRepository reviews;
    private final BrandRuleEngine ruleEngine;
    private final AiAnalyzer ai;

    public BrandGuardService(BrandRepository brands, ProductRepository products, BrandRuleRepository rules,
                             SubmissionRepository submissions, ContentFindingRepository findings,
                             ContentRevisionRepository revisions, ReviewRecordRepository reviews,
                             BrandRuleEngine ruleEngine, AiAnalyzer ai) {
        this.brands = brands; this.products = products; this.rules = rules; this.submissions = submissions;
        this.findings = findings; this.revisions = revisions; this.reviews = reviews;
        this.ruleEngine = ruleEngine; this.ai = ai;
    }

    public List<BrandView> allBrands() {
        return brands.findAll().stream().map(this::toBrandView).toList();
    }

    public boolean aiConfigured() { return ai.isConfigured(); }

    public BrandView brand(String id) { return toBrandView(getBrand(id)); }

    @Transactional
    public BrandView updateBrand(String id, BrandUpdate input) {
        Brand brand = getBrand(id);
        brand.update(input.name(), input.description(), input.preferredTone());
        return toBrandView(brand);
    }

    public List<ProductView> products(String brandId) {
        getBrand(brandId);
        return products.findByBrandId(brandId).stream().map(this::toProductView).toList();
    }

    @Transactional
    public ProductView updateProduct(String id, ProductUpdate input) {
        Product product = products.findById(id).orElseThrow(() -> new EntityNotFoundException("Product not found"));
        BigDecimal price = parsePrice(input.price());
        product.update(input.name(), input.description(), price);
        return toProductView(product);
    }

    public List<RuleView> guidelines(String brandId) {
        getBrand(brandId);
        return rules.findByBrandId(brandId).stream().map(this::toRuleView).toList();
    }

    @Transactional
    public RuleView updateRule(String id, RuleUpdate input) {
        BrandRule rule = rules.findById(id).orElseThrow(() -> new EntityNotFoundException("Guideline not found"));
        if (!Set.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(input.severity())) {
            throw new IllegalArgumentException("Severity must be LOW, MEDIUM, HIGH, or CRITICAL");
        }
        if ("PROMOTIONAL_CLAIM".equals(rule.getKind())) {
            try {
                Pattern.compile(input.value(), Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Promotional claim rules must be a valid regular expression");
            }
        }
        if ("APPROVED_TERM".equals(rule.getKind())) {
            String[] terms = input.value().split("=>", 2);
            if (terms.length != 2 || terms[0].isBlank() || terms[1].isBlank()) {
                throw new IllegalArgumentException("Approved terminology must use the format deprecated term=>approved term");
            }
        }
        rule.update(input.value(), input.explanation(), input.severity(), input.enabled());
        return toRuleView(rule);
    }

    @Transactional
    public SubmissionView create(SubmissionCreate input) {
        if (!Set.of("ADVERTISEMENT", "SOCIAL_POST", "PROMOTIONAL_EMAIL").contains(input.contentType())) {
            throw new IllegalArgumentException("Content type must be ADVERTISEMENT, SOCIAL_POST, or PROMOTIONAL_EMAIL");
        }
        Submission submission = submissions.save(new Submission(id(), DemoDataSeeder.BRAND_ID,
                input.title().trim(), input.contentType(), input.content().trim()));
        revisions.save(new ContentRevision(id(), submission.getId(), submission.getCurrentContent(), "CREATED"));
        return detail(submission);
    }

    public List<SubmissionView> allSubmissions() {
        return submissions.findAllByOrderByUpdatedAtDesc().stream().map(this::detail).toList();
    }

    public SubmissionView submission(String id) { return detail(getSubmission(id)); }

    @Transactional
    public SubmissionView updateContent(String id, ContentUpdate input) {
        Submission submission = getSubmission(id);
        requireStatus(submission, Set.of("DRAFT", "ANALYZED", "CHANGES_REQUESTED"),
                "Only drafts, analyzed content, or requested changes can be edited");
        revisions.save(new ContentRevision(id(), id, submission.getCurrentContent(), "BEFORE_EDIT"));
        submission.setCurrentContent(input.content().trim());
        submission.setStatus("DRAFT");
        submission.setAnalysisSummary(null);
        submission.setAnalysisMode(null);
        submission.setSuggestedContent(null);
        findings.deleteBySubmissionId(id);
        return detail(submission);
    }

    @Transactional
    public AnalysisResult analyze(String id) {
        Submission submission = getSubmission(id);
        requireStatus(submission, Set.of("DRAFT", "ANALYZED", "CHANGES_REQUESTED"),
                "This submission cannot be analyzed in its current status");
        Brand brand = getBrand(submission.getBrandId());
        List<Product> brandProducts = products.findByBrandId(brand.getId());
        List<BrandRule> brandRules = rules.findByBrandIdAndEnabledTrue(brand.getId());
        List<FindingDraft> combined = new ArrayList<>(ruleEngine.analyze(submission.getCurrentContent(), brandProducts, brandRules));
        String context = referenceContext(brand, brandProducts, brandRules);
        Set<String> validEvidence = java.util.stream.Stream.concat(
                brandProducts.stream().map(Product::getId), brandRules.stream().map(BrandRule::getId))
                .collect(Collectors.toSet());
        AiAnalyzer.AiResult aiResult = ai.analyze(submission.getCurrentContent(), context, validEvidence);
        combined.addAll(aiResult.findings());
        findings.deleteBySubmissionId(id);
        combined.forEach(draft -> findings.save(new ContentFinding(id(), id, draft.category(), draft.severity(),
                draft.source(), trim(draft.excerpt(), 1000), trim(draft.explanation(), 2000),
                draft.expectedValue(), draft.evidenceSourceId(), draft.suggestion(), draft.needsVerification())));

        String mode = aiResult.available() ? "AI_ASSISTED" : "RULES_ONLY";
        String summary = aiResult.available() ? aiResult.summary()
                : "Deterministic rules completed. AI analysis is not configured or unavailable; human review is still required.";
        submission.setAnalysisSummary(summary);
        submission.setAnalysisMode(mode);
        submission.setSuggestedContent(aiResult.available() ? aiResult.correctedContent()
                : fallbackCorrection(submission.getCurrentContent(), combined, brandProducts));
        submission.setStatus("ANALYZED");
        return new AnalysisResult(summary, mode, submission.getSuggestedContent(), viewFindings(id));
    }

    @Transactional
    public SubmissionView submitForReview(String id) {
        Submission submission = getSubmission(id);
        requireStatus(submission, Set.of("ANALYZED"), "Analyze the content before submitting it for review");
        submission.setStatus("PENDING_APPROVAL");
        revisions.save(new ContentRevision(id(), id, submission.getCurrentContent(), "SUBMITTED_FOR_REVIEW"));
        return detail(submission);
    }

    @Transactional
    public SubmissionView approve(String id, ReviewRequest input) {
        Submission submission = getSubmission(id);
        requireStatus(submission, Set.of("PENDING_APPROVAL"), "Only pending submissions can be approved");
        submission.setStatus("APPROVED");
        reviews.save(new ReviewRecord(id(), id, "APPROVED", reviewer(input), input.comment()));
        return detail(submission);
    }

    @Transactional
    public SubmissionView requestChanges(String id, ReviewRequest input) {
        Submission submission = getSubmission(id);
        requireStatus(submission, Set.of("PENDING_APPROVAL"), "Only pending submissions can be returned for changes");
        if (input.comment() == null || input.comment().isBlank()) {
            throw new IllegalArgumentException("A comment is required when requesting changes");
        }
        submission.setStatus("CHANGES_REQUESTED");
        reviews.save(new ReviewRecord(id(), id, "CHANGES_REQUESTED", reviewer(input), input.comment().trim()));
        return detail(submission);
    }

    public DashboardView dashboard() {
        List<SubmissionView> recent = submissions.findAllByOrderByUpdatedAtDesc().stream().limit(5).map(this::detail).toList();
        return new DashboardView(submissions.count(), submissions.countByStatus("PENDING_APPROVAL"),
                submissions.countByStatus("APPROVED"), recent);
    }

    public SubmissionView detail(Submission submission) {
        String mode = submission.getAnalysisMode() == null ? "NOT_ANALYZED" : submission.getAnalysisMode();
        return new SubmissionView(submission.getId(), submission.getBrandId(), submission.getTitle(),
                submission.getContentType(), submission.getOriginalContent(), submission.getCurrentContent(),
                submission.getStatus(), submission.getAnalysisSummary(), submission.getCreatedAt(), submission.getUpdatedAt(),
                viewFindings(submission.getId()),
                revisions.findBySubmissionIdOrderByCreatedAtAsc(submission.getId()).stream()
                        .map(r -> new RevisionView(r.getId(), r.getContent(), r.getAction(), r.getCreatedAt())).toList(),
                reviews.findBySubmissionIdOrderByCreatedAtAsc(submission.getId()).stream()
                        .map(r -> new ReviewView(r.getId(), r.getAction(), r.getReviewer(), r.getComment(), r.getCreatedAt())).toList(),
                mode, submission.getSuggestedContent());
    }

    private List<FindingView> viewFindings(String submissionId) {
        return findings.findBySubmissionId(submissionId).stream().map(f -> new FindingView(f.getId(),
                f.getCategory(), f.getSeverity(), f.getSource(), f.getExcerpt(), f.getExplanation(),
                f.getExpectedValue(), f.getEvidenceSourceId(), evidenceReference(f.getEvidenceSourceId()),
                f.getSuggestion(), f.isNeedsVerification())).toList();
    }

    private String evidenceReference(String id) {
        if (id == null) return null;
        return products.findById(id).map(p -> p.getName() + " — " + formatPrice(p.getPrice()))
                .orElseGet(() -> rules.findById(id).map(BrandRule::getExplanation).orElse(null));
    }

    private String referenceContext(Brand brand, List<Product> brandProducts, List<BrandRule> brandRules) {
        StringBuilder context = new StringBuilder("Brand: ").append(brand.getName()).append("\nDescription: ")
                .append(brand.getDescription()).append("\nPreferred tone: ").append(brand.getPreferredTone());
        brandProducts.forEach(p -> context.append("\nProduct [").append(p.getId()).append("]: ").append(p.getName())
                .append(", price ").append(formatPrice(p.getPrice())).append(", ").append(p.getDescription()));
        brandRules.forEach(r -> context.append("\nGuideline [").append(r.getId()).append("]: ")
                .append(r.getExplanation()).append(" (").append(r.getValue()).append(")"));
        return context.toString();
    }

    private String fallbackCorrection(String content, List<FindingDraft> drafts, List<Product> brandProducts) {
        boolean hasUnsupportedClaim = drafts.stream().anyMatch(f -> "UNSUPPORTED_CLAIM".equals(f.category()));
        boolean hasProductOrPriceIssue = drafts.stream().anyMatch(f -> "PRODUCT_NAME".equals(f.category())
                || "PRICE_MISMATCH".equals(f.category()));
        if (hasUnsupportedClaim && hasProductOrPriceIssue && !brandProducts.isEmpty()) {
            Product product = brandProducts.getFirst();
            return "Discover the " + product.getName() + " for " + formatPrice(product.getPrice())
                    + ". Track your activity and build healthier daily habits.";
        }
        String corrected = content;
        for (FindingDraft finding : drafts) {
            if (finding.suggestion() == null || finding.excerpt() == null) continue;
            if ("PRODUCT_NAME".equals(finding.category())) corrected = corrected.replace(finding.excerpt(), finding.suggestion());
            if ("PRICE_MISMATCH".equals(finding.category())) corrected = corrected.replace(finding.excerpt(), finding.suggestion());
        }
        for (FindingDraft finding : drafts) {
            if ("UNSUPPORTED_CLAIM".equals(finding.category()) && finding.excerpt() != null) {
                corrected = corrected.replace(finding.excerpt(), "Track your activity and build healthier daily habits");
            }
        }
        return corrected;
    }

    private Brand getBrand(String id) {
        return brands.findById(id).orElseThrow(() -> new EntityNotFoundException("Brand not found"));
    }
    private Submission getSubmission(String id) {
        return submissions.findById(id).orElseThrow(() -> new EntityNotFoundException("Submission not found"));
    }
    private void requireStatus(Submission submission, Set<String> allowed, String message) {
        if (!allowed.contains(submission.getStatus())) throw new IllegalStateException(message);
    }
    private BrandView toBrandView(Brand brand) {
        return new BrandView(brand.getId(), brand.getName(), brand.getDescription(), brand.getPreferredTone(), brand.isFictional());
    }
    private ProductView toProductView(Product product) {
        return new ProductView(product.getId(), product.getBrandId(), product.getName(), product.getDescription(), formatPrice(product.getPrice()));
    }
    private RuleView toRuleView(BrandRule rule) {
        return new RuleView(rule.getId(), rule.getKind(), rule.getValue(), rule.getExplanation(), rule.getSeverity(), rule.isEnabled());
    }
    private BigDecimal parsePrice(String price) {
        try {
            BigDecimal parsed = new BigDecimal(price.replace("₹", "").replace(",", "").trim());
            if (parsed.signum() < 0) throw new NumberFormatException();
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a non-negative number, for example 1999.00");
        }
    }
    private String formatPrice(BigDecimal price) {
        return "₹" + NumberFormat.getNumberInstance(new Locale("en", "IN")).format(price);
    }
    private String reviewer(ReviewRequest input) {
        return input.reviewer() == null || input.reviewer().isBlank() ? "Demo reviewer" : input.reviewer().trim();
    }
    private String trim(String value, int max) {
        return value == null ? null : value.substring(0, Math.min(value.length(), max));
    }
    private String id() { return UUID.randomUUID().toString(); }
}
