package com.brandguard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "brandguard.ai")
public record AiProperties(String apiKey, String endpoint, String model) {
    public boolean configured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
