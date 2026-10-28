package com.brandguard.service;

public record FindingDraft(String category, String severity, String source, String excerpt, String explanation,
                           String expectedValue, String evidenceSourceId, String suggestion, boolean needsVerification) {}
