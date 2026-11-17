package com.gapplabs;

import com.gapplabs.constants.ExtractionType;
import com.gapplabs.dto.ScrapingAntRequest;
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;
import feign.Feign;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Thin SDK client for ScrapingAnt.
 */
public class ScrapingAntClient {

    private final ScrapingAntApi api;
    @Getter
    private final ScrapingAntClientOptions options;

    public ScrapingAntClient(String apiKey) {
        this(ScrapingAntClientOptions.builder().apiKey(apiKey).build());
    }

    public ScrapingAntClient(ScrapingAntClientOptions options) {
        this(options, buildApi(options));
    }

    ScrapingAntClient(ScrapingAntClientOptions options, ScrapingAntApi api) {
        this.options = Objects.requireNonNull(options, "options");
        this.api = Objects.requireNonNull(api, "api");
    }

    public String executeGeneral(ScrapingAntRequest request) {
        return api.scrape(request.toQueryMap(), ExtractionType.GENERAL.getValue());
    }

    public MarkdownResponse executeMarkdown(ScrapingAntRequest request) {
        String response = api.scrape(request.toQueryMap(), ExtractionType.MARKDOWN.getValue());
        return ScrapingAntResponseConverter.toMarkdown(response);
    }

    public ExtendedResponse executeExtended(ScrapingAntRequest request) {
        String response = api.scrape(request.toQueryMap(), ExtractionType.JSON.getValue());
        return ScrapingAntResponseConverter.toExtended(response);
    }
    

    public Map<String, Object> executeExtract(ScrapingAntRequest request, String extractProperties) {
        String normalizedProperties = normalizeExtractProperties(extractProperties);
        Map<String, Object> query = new HashMap<>(request.toQueryMap());
        query.put("extract_properties", normalizedProperties);
        String response = api.scrape(query, ExtractionType.AI_EXTRACTED.getValue());
        return ScrapingAntResponseConverter.toExtract(response);
    }

    public Map<String, Object> executeExtract(ScrapingAntRequest request, List<String> extractProperties) {
        if (extractProperties == null || extractProperties.isEmpty()) {
            throw new IllegalArgumentException("'extractProperties' list is required and cannot be null or empty");
        }
        return executeExtract(request, String.join(", ", extractProperties));
    }

    private static String normalizeExtractProperties(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("'extractProperties' is required and cannot be blank");
        }

        return Stream.of(raw.split(","))
                .map(String::trim)
                .map(token -> token.replaceAll("\\s+", " "))
                .map(ScrapingAntClient::requireNonBlankToken)
                .collect(Collectors.joining(", "));
    }

    private static String requireNonBlankToken(String token) {
        if (token.isBlank()) {
            throw new IllegalArgumentException("'extractProperties' contains an empty token");
        }
        return token;
    }

    private static ScrapingAntApi buildApi(ScrapingAntClientOptions options) {
        Feign.Builder builder = Feign.builder();
        Consumer<Feign.Builder> feignCustomizer = options.getFeignCustomizer();
        if (feignCustomizer != null) {
            feignCustomizer.accept(builder);
        }
        builder.logger(new Slf4jLogger(ScrapingAntClient.class))
                .requestInterceptor(new ScrapingAntAuthInterceptor(options.getApiKey()))
                .errorDecoder(new ScrapingAntErrorDecoder());
        return builder.target(ScrapingAntApi.class, options.getBaseUrl());
    }
}
