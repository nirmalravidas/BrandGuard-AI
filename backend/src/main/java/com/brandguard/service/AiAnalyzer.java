package com.brandguard.service;

import java.util.Set;
import java.util.List;

public interface AiAnalyzer {
    boolean isConfigured();
    AiResult analyze(String content, String referenceContext, Set<String> validEvidenceIds);

    record AiResult(boolean available, String summary, String correctedContent, List<FindingDraft> findings) {
        static AiResult unavailable() { return new AiResult(false, null, null, List.of()); }
        static AiResult failed() { return unavailable(); }
    }
}
