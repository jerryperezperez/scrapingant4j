package com.gapplabs;

import feign.Feign;
import lombok.Builder;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * Immutable configuration for {@link ScrapingAntClient}.
 */
@Getter
public final class ScrapingAntClientOptions {

    public static final String BASE_URL = "https://api.scrapingant.com/v2";
    private final String apiKey;
    private final Consumer<Feign.Builder> feignCustomizer;

    @Builder
    private ScrapingAntClientOptions(String apiKey, Consumer<Feign.Builder> feignCustomizer) {
        this.apiKey = requireApiKey(apiKey);
        this.feignCustomizer = feignCustomizer;
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
