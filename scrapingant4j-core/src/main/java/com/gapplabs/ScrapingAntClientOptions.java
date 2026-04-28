package com.gapplabs;

import com.google.gson.Gson;
import feign.Feign;

/**
 * Immutable configuration for {@link ScrapingAntClient}.
 */
public final class ScrapingAntClientOptions {

    public static final String DEFAULT_ENDPOINT = "https://api.scrapingant.com";
    public static final String DEFAULT_API_VERSION = "v2";

    private final String apiKey;
    private final String endpoint;
    private final String apiVersion;
    private final Gson gson;
    private final Feign.Builder feignBuilder;

    private ScrapingAntClientOptions(Builder builder) {
        this.apiKey = requireText(builder.apiKey, "apiKey");
        this.endpoint = normalizeEndpoint(builder.endpoint);
        this.apiVersion = normalizeApiVersion(builder.apiVersion);
        this.gson = builder.gson != null ? builder.gson : new Gson();
        this.feignBuilder = builder.feignBuilder != null ? builder.feignBuilder : Feign.builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Gson getGson() {
        return gson;
    }

    public Feign.Builder getFeignBuilder() {
        return feignBuilder;
    }

    public String getBaseUrl() {
        return endpoint + "/" + apiVersion;
    }

    public static final class Builder {
        private String apiKey;
        private String endpoint = DEFAULT_ENDPOINT;
        private String apiVersion = DEFAULT_API_VERSION;
        private Gson gson;
        private Feign.Builder feignBuilder;

        private Builder() {
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder gson(Gson gson) {
            this.gson = gson;
            return this;
        }

        public Builder feignBuilder(Feign.Builder feignBuilder) {
            this.feignBuilder = feignBuilder;
            return this;
        }

        public ScrapingAntClientOptions build() {
            return new ScrapingAntClientOptions(this);
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("'" + fieldName + "' is required and cannot be blank");
        }
        return value.trim();
    }

    private static String normalizeEndpoint(String endpoint) {
        String normalized = endpoint == null || endpoint.isBlank()
                ? DEFAULT_ENDPOINT
                : endpoint.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String normalizeApiVersion(String apiVersion) {
        String normalized = apiVersion == null || apiVersion.isBlank()
                ? DEFAULT_API_VERSION
                : apiVersion.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("'apiVersion' cannot be blank");
        }
        return normalized;
    }
}
