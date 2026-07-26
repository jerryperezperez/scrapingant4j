package com.gapplabs;

import com.google.gson.Gson;
import feign.Feign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrapingAntClientOptionsTest {

    @Test
    void builderNormalizesAndExposesConfiguredValues() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("  test-api-key  ")
                .endpoint("https://custom.scrapingant.com///")
                .apiVersion("/v3")
                .build();

        assertEquals("test-api-key", options.getApiKey());
        assertEquals("https://custom.scrapingant.com", options.getEndpoint());
        assertEquals("v3", options.getApiVersion());
        assertEquals("https://custom.scrapingant.com/v3", options.getBaseUrl());
        assertNotNull(options.getGson());
        assertNotNull(options.getFeignBuilder());
    }

    @Test
    void builderUsesDefaultsWhenOptionalValuesAreOmitted() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .build();

        assertEquals(ScrapingAntClientOptions.DEFAULT_ENDPOINT, options.getEndpoint());
        assertEquals(ScrapingAntClientOptions.DEFAULT_API_VERSION, options.getApiVersion());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void builderFallsBackToDefaultEndpointWhenBlank(String endpoint) {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .endpoint(endpoint)
                .build();

        assertEquals(ScrapingAntClientOptions.DEFAULT_ENDPOINT, options.getEndpoint());
    }

    @Test
    void builderFallsBackToDefaultEndpointWhenNull() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .endpoint(null)
                .build();

        assertEquals(ScrapingAntClientOptions.DEFAULT_ENDPOINT, options.getEndpoint());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void builderFallsBackToDefaultApiVersionWhenBlank(String apiVersion) {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .apiVersion(apiVersion)
                .build();

        assertEquals(ScrapingAntClientOptions.DEFAULT_API_VERSION, options.getApiVersion());
    }

    @Test
    void builderFallsBackToDefaultApiVersionWhenNull() {
        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .apiVersion(null)
                .build();

        assertEquals(ScrapingAntClientOptions.DEFAULT_API_VERSION, options.getApiVersion());
    }

    @Test
    void builderRejectsNullApiKey() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ScrapingAntClientOptions.builder().apiKey(null).build()
        );

        assertEquals("'apiKey' is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderRejectsBlankApiKey() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ScrapingAntClientOptions.builder().apiKey("   ").build()
        );

        assertEquals("'apiKey' is required and cannot be blank", exception.getMessage());
    }

    @Test
    void builderRejectsApiVersionThatNormalizesToBlank() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                ScrapingAntClientOptions.builder()
                        .apiKey("test-api-key")
                        .apiVersion("///")
                        .build()
        );

        assertEquals("'apiVersion' cannot be blank", exception.getMessage());
    }

    @Test
    void builderUsesCustomRuntimeDependenciesWhenProvided() {
        Gson gson = new Gson();
        Feign.Builder feignBuilder = Feign.builder();

        ScrapingAntClientOptions options = ScrapingAntClientOptions.builder()
                .apiKey("test-api-key")
                .gson(gson)
                .feignBuilder(feignBuilder)
                .build();

        assertSame(gson, options.getGson());
        assertSame(feignBuilder, options.getFeignBuilder());
    }
}
