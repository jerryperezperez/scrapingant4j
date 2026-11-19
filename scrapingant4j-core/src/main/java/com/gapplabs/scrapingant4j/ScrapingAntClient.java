package com.gapplabs.scrapingant4j;

import com.gapplabs.scrapingant4j.constants.ExtractionType;
import com.gapplabs.scrapingant4j.dto.ScrapingAntRequest;
import com.gapplabs.scrapingant4j.dto.responses.ExtendedResponse;
import com.gapplabs.scrapingant4j.dto.responses.MarkdownResponse;
import com.gapplabs.scrapingant4j.exceptions.ScrapingAntNetworkException;
import feign.Feign;
import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/** Thin SDK client for ScrapingAnt. */
public class ScrapingAntClient {
  private final ScrapingAntApi api;
  @Getter private final ScrapingAntClientOptions options;

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

  /** Sends a general scrape request and returns its response body unchanged. */
  public String executeGeneral(ScrapingAntRequest request) {
    Objects.requireNonNull(request, "request");
    return call(() -> api.scrape(request.toQueryMap(), ExtractionType.GENERAL.getValue()));
  }

  /** Sends a Markdown scrape request and parses the JSON response. */
  public MarkdownResponse executeMarkdown(ScrapingAntRequest request) {
    Objects.requireNonNull(request, "request");
    String response =
        call(() -> api.scrape(request.toQueryMap(), ExtractionType.MARKDOWN.getValue()));
    return ScrapingAntResponseConverter.toMarkdown(response);
  }

  /** Sends an extended scrape request and parses its structured response. */
  public ExtendedResponse executeExtended(ScrapingAntRequest request) {
    Objects.requireNonNull(request, "request");
    String response = call(() -> api.scrape(request.toQueryMap(), ExtractionType.JSON.getValue()));
    return ScrapingAntResponseConverter.toExtended(response);
  }

  /** Runs AI extraction and returns a dynamic map for the requested properties. */
  public Map<String, Object> executeExtract(ScrapingAntRequest request, String extractProperties) {
    Map<String, Object> query = extractionQuery(request, extractProperties);
    String response = call(() -> api.scrape(query, ExtractionType.AI_EXTRACTED.getValue()));
    return ScrapingAntResponseConverter.toExtract(response);
  }

  /** Runs AI extraction using a list of requested properties. */
  public Map<String, Object> executeExtract(
      ScrapingAntRequest request, List<String> extractProperties) {
    Objects.requireNonNull(request, "request");
    if (extractProperties == null || extractProperties.isEmpty()) {
      throw new IllegalArgumentException(
          "'extractProperties' list is required and cannot be null or empty");
    }
    return executeExtract(request, String.join(", ", extractProperties));
  }

  /** Executes an extraction and deserializes it into the requested Java type. */
  public <T> T executeExtract(
      ScrapingAntRequest request, String extractProperties, Class<T> resultType) {
    Objects.requireNonNull(resultType, "resultType");
    return executeExtract(request, extractProperties, (Type) resultType);
  }

  /** Supports parameterized extraction result types using Gson's {@link Type} tokens. */
  public <T> T executeExtract(
      ScrapingAntRequest request, String extractProperties, Type resultType) {
    Objects.requireNonNull(resultType, "resultType");
    Map<String, Object> query = extractionQuery(request, extractProperties);
    String response = call(() -> api.scrape(query, ExtractionType.AI_EXTRACTED.getValue()));
    return ScrapingAntResponseConverter.toExtract(response, resultType);
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

  private static Map<String, Object> extractionQuery(
      ScrapingAntRequest request, String extractProperties) {
    Objects.requireNonNull(request, "request");
    Map<String, Object> query = new HashMap<>(request.toQueryMap());
    query.put("extract_properties", normalizeExtractProperties(extractProperties));
    return query;
  }

  private <T> T call(Supplier<T> request) {
    try {
      return request.get();
    } catch (RetryableException exception) {
      throw new ScrapingAntNetworkException(
          "ScrapingAnt request failed before receiving a response", exception);
    }
  }

  private static String requireNonBlankToken(String token) {
    if (token.isBlank()) {
      throw new IllegalArgumentException("'extractProperties' contains an empty token");
    }
    return token;
  }

  private static ScrapingAntApi buildApi(ScrapingAntClientOptions options) {
    return buildApi(options, ScrapingAntClientOptions.DEFAULT_BASE_URL);
  }

  static ScrapingAntApi buildApi(ScrapingAntClientOptions options, String baseUrl) {
    Feign.Builder builder =
        Feign.builder()
            .retryer(Retryer.NEVER_RETRY)
            .options(
                new Request.Options(
                    options.getConnectTimeout().toMillis(),
                    TimeUnit.MILLISECONDS,
                    options.getReadTimeout().toMillis(),
                    TimeUnit.MILLISECONDS,
                    true));
    builder
        .requestInterceptor(new ScrapingAntAuthInterceptor(options.apiKey()))
        .errorDecoder(new ScrapingAntErrorDecoder());
    return builder.target(ScrapingAntApi.class, baseUrl);
  }
}
