package com.gapplabs;

import com.gapplabs.constants.ExtractionType;
import com.gapplabs.dto.ScrapingAntRequest;
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;
import com.gapplabs.exceptions.ScrapingAntNetworkException;
import feign.Feign;
import feign.Request;
import feign.RetryableException;
import feign.Retryer;
import feign.slf4j.Slf4jLogger;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Thin SDK client for ScrapingAnt.
 */
public record ScrapingAntClient(ScrapingAntApi api, ScrapingAntClientOptions options) {
  
  public ScrapingAntClient(String apiKey) {
    this(ScrapingAntClientOptions.builder().apiKey(apiKey).build());
  }
  
  public ScrapingAntClient(ScrapingAntClientOptions options) {
    this(options, buildApi(options));
  }
  
  ScrapingAntClient(ScrapingAntClientOptions options, ScrapingAntApi api) {
    this(Objects.requireNonNull(api, "api"), Objects.requireNonNull(options, "options"));
  }
  
  /**
   * Sends a general scrape request and returns its response body unchanged.
   */
  public String executeGeneral(ScrapingAntRequest request) {
    return call(() -> api.scrape(request.toQueryMap(), ExtractionType.GENERAL.getValue()));
  }
  
  /**
   * Sends a Markdown scrape request and parses the JSON response.
   */
  public MarkdownResponse executeMarkdown(ScrapingAntRequest request) {
    String response = call(() -> api.scrape(request.toQueryMap(), ExtractionType.MARKDOWN.getValue()));
    return ScrapingAntResponseConverter.toMarkdown(response);
  }
  
  /**
   * Sends an extended scrape request and parses its structured response.
   */
  public ExtendedResponse executeExtended(ScrapingAntRequest request) {
    String response = call(() -> api.scrape(request.toQueryMap(), ExtractionType.JSON.getValue()));
    return ScrapingAntResponseConverter.toExtended(response);
  }
  
  /**
   * Runs AI extraction and returns a dynamic map for the requested properties.
   */
  public Map<String, Object> executeExtract(ScrapingAntRequest request, String extractProperties) {
    String normalizedProperties = normalizeExtractProperties(extractProperties);
    Map<String, Object> query = new HashMap<>(request.toQueryMap());
    query.put("extract_properties", normalizedProperties);
    String response = call(() -> api.scrape(query, ExtractionType.AI_EXTRACTED.getValue()));
    return ScrapingAntResponseConverter.toExtract(response);
  }
  
  /**
   * Runs AI extraction using a list of requested properties.
   */
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
    
    return Stream.of(raw.split(",")).map(String::trim).map(token -> token.replaceAll("\\s+", " ")).map(ScrapingAntClient::requireNonBlankToken).collect(Collectors.joining(", "));
  }
  
  /**
   * Executes an extraction and deserializes it into the requested Java type.
   */
  public <T> T executeExtract(ScrapingAntRequest request, String extractProperties, Class<T> resultType) {
    return executeExtract(request, extractProperties, (Type) resultType);
  }
  
  /**
   * Supports parameterized extraction result types using Gson's {@link Type} tokens.
   */
  public <T> T executeExtract(ScrapingAntRequest request, String extractProperties, Type resultType) {
    Objects.requireNonNull(resultType, "resultType");
    String normalizedProperties = normalizeExtractProperties(extractProperties);
    Map<String, Object> query = new HashMap<>(request.toQueryMap());
    query.put("extract_properties", normalizedProperties);
    String response = call(() -> api.scrape(query, ExtractionType.AI_EXTRACTED.getValue()));
    return ScrapingAntResponseConverter.toExtract(response, resultType);
  }
  
  private <T> T call(Supplier<T> request) {
    try {
      return request.get();
    } catch (RetryableException exception) {
      throw new ScrapingAntNetworkException("ScrapingAnt request failed before receiving a response", exception);
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
    Feign.Builder builder = Feign.builder().retryer(Retryer.NEVER_RETRY).options(new Request.Options(options.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS, options.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS, true));
    builder.logger(new Slf4jLogger(ScrapingAntClient.class)).requestInterceptor(new ScrapingAntAuthInterceptor(options.apiKey())).errorDecoder(new ScrapingAntErrorDecoder());
    return builder.target(ScrapingAntApi.class, baseUrl);
  }
}
