package com.gapplabs;

import feign.Feign;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
                ScrapingAntClientOptions.builder()::build
        );

        assertEquals("apiKey is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderRejectsNullApiKeyAtBuild() {
        
        ScrapingAntClientOptions.ScrapingAntClientOptionsBuilder optionsBuilder = ScrapingAntClientOptions.builder()
                .apiKey(null);
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, optionsBuilder::build
        );

        assertEquals("apiKey is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderRejectsBlankApiKeyAtBuild() {
        
        ScrapingAntClientOptions.ScrapingAntClientOptionsBuilder optionsBuilder = ScrapingAntClientOptions.builder()
                .apiKey("   ");
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, optionsBuilder::build
        );

        assertEquals("apiKey is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderHasNoFeignCustomizerByDefault() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .build();

        assertNull(options.getFeignCustomizer());
    }

    @Test
    void builderStoresFeignCustomizerWhenProvided() {
        Consumer<Feign.Builder> feignCustomizer = builder -> {};

        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .feignCustomizer(feignCustomizer)
                .build();

        assertSame(feignCustomizer, options.getFeignCustomizer());
    }
}
