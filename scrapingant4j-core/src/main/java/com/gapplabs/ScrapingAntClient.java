package com.gapplabs;

import com.gapplabs.constants.ExtractionType;
import com.gapplabs.dto.ExtractRequestOptions;
import com.gapplabs.dto.ScrapingAntRequest;
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import feign.Feign;
import feign.slf4j.Slf4jLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Thin SDK client for ScrapingAnt.
 */
public class ScrapingAntClient {

    private final ScrapingAntApi api;
    private final ScrapingAntClientOptions options;
    private final Map<ExtractionType, Function<String, Object>> converterMap = new HashMap<>();

    public ScrapingAntClient(String apiKey) {
        this(ScrapingAntClientOptions.builder().apiKey(apiKey).build());
    }

    public ScrapingAntClient(ScrapingAntClientOptions options) {
        this(options, buildApi(options));
    }

    ScrapingAntClient(ScrapingAntClientOptions options, ScrapingAntApi api) {
        this.options = Objects.requireNonNull(options, "options");
        this.api = Objects.requireNonNull(api, "api");

        Gson gson = options.getGson();
        converterMap.put(ExtractionType.GENERAL, raw -> raw);
        converterMap.put(ExtractionType.MARKDOWN, raw -> gson.fromJson(raw, MarkdownResponse.class));
        converterMap.put(ExtractionType.AI_EXTRACTED, raw -> gson.fromJson(raw, new TypeToken<Map<String, Object>>() {
        }.getType()));
        converterMap.put(ExtractionType.JSON, raw -> gson.fromJson(raw, ExtendedResponse.class));
    }

    public String executeGeneral(ScrapingAntRequest request) {
        return api.scrape(request.toQueryMap(), ExtractionType.GENERAL.getValue());
    }

    public MarkdownResponse executeMarkdown(ScrapingAntRequest request) {
        String response = api.scrape(request.toQueryMap(), ExtractionType.MARKDOWN.getValue());
        return (MarkdownResponse) convertResponse(response, ExtractionType.MARKDOWN);
    }

    public ExtendedResponse executeExtended(ScrapingAntRequest request) {
        String response = api.scrape(request.toQueryMap(), ExtractionType.JSON.getValue());
        return (ExtendedResponse) convertResponse(response, ExtractionType.JSON);
    }

    @Deprecated(forRemoval = false)
    @SuppressWarnings("unchecked")
    public Map<String, Object> executeExtract(ScrapingAntRequest request) {
        String response = api.scrape(request.toQueryMap(), ExtractionType.AI_EXTRACTED.getValue());
        return (Map<String, Object>) convertResponse(response, ExtractionType.AI_EXTRACTED);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> executeExtract(ScrapingAntRequest request, ExtractRequestOptions options) {
        Map<String, Object> query = new HashMap<>(request.toQueryMap());
        query.putAll(options.toQueryMap());
        String response = api.scrape(query, ExtractionType.AI_EXTRACTED.getValue());
        return (Map<String, Object>) convertResponse(response, ExtractionType.AI_EXTRACTED);
    }

    public Map<String, Object> executeExtract(ScrapingAntRequest request, String extractProperties) {
        return executeExtract(request, ExtractRequestOptions.fromString(extractProperties));
    }

    public Map<String, Object> executeExtract(ScrapingAntRequest request, List<String> extractProperties) {
        return executeExtract(request, ExtractRequestOptions.fromList(extractProperties));
    }

    public ScrapingAntClientOptions getOptions() {
        return options;
    }

    private Object convertResponse(String response, ExtractionType extractionType) {
        Function<String, Object> converter = converterMap.get(extractionType);
        if (converter == null) {
            throw new IllegalArgumentException("No converter for: " + extractionType);
        }
        try {
            return converter.apply(response);
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Failed to deserialize response for: " + extractionType, e);
        }
    }

    private static ScrapingAntApi buildApi(ScrapingAntClientOptions options) {
        Feign.Builder builder = options.getFeignBuilder()
                .logger(new Slf4jLogger(ScrapingAntClient.class))
                .requestInterceptor(new ScrapingAntAuthInterceptor(options.getApiKey()))
                .errorDecoder(new ScrapingAntErrorDecoder());
        return builder.target(ScrapingAntApi.class, options.getBaseUrl());
    }
}
