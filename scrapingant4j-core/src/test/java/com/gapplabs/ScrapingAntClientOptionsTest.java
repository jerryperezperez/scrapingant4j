package com.gapplabs;

import feign.Feign;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrapingAntClientOptionsTest {

    @Test
    void builderTrimsApiKeyAndUsesFixedBaseUrl() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("  test-api-key  ")
                .build();

        assertEquals("test-api-key", options.getApiKey());
        assertEquals(ScrapingAntClientOptions.BASE_URL, options.getBaseUrl());
    }

    @Test
    void builderRejectsMissingApiKeyAtBuild() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ScrapingAntClientOptions.builder().build()
        );

        assertEquals("apiKey is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderRejectsNullApiKeyAtBuild() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ScrapingAntClientOptions.builder().apiKey(null).build()
        );

        assertEquals("apiKey is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderRejectsBlankApiKeyAtBuild() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ScrapingAntClientOptions.builder().apiKey("   ").build()
        );

        assertEquals("apiKey is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderUsesCustomFeignBuilderWhenProvided() {
        Feign.Builder feignBuilder = Feign.builder();

        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .feignBuilder(feignBuilder)
                .build();

        assertSame(feignBuilder, options.getFeignBuilder());
    }
}
