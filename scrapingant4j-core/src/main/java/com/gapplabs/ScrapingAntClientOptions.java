package com.gapplabs;

import feign.Feign;
import lombok.Builder;
import lombok.Getter;

/**
 * Immutable configuration for {@link ScrapingAntClient}.
 */
@Getter
public final class ScrapingAntClientOptions {

    public static final String BASE_URL = "https://api.scrapingant.com/v2";
    private final String apiKey;
    private final Feign.Builder feignBuilder;

    @Builder
    private ScrapingAntClientOptions(String apiKey, Feign.Builder feignBuilder) {
        this.apiKey = requireApiKey(apiKey);
        this.feignBuilder = feignBuilder != null ? feignBuilder : Feign.builder();
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    private static String requireApiKey(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalArgumentException("apiKey is required and cannot be blank");
        }
        return apiKey.trim();
    }
}
