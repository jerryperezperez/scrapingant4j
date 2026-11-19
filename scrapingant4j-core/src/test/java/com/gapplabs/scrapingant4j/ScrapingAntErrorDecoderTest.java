package com.gapplabs.scrapingant4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gapplabs.scrapingant4j.exceptions.ScrapingAntErrorType;
import com.gapplabs.scrapingant4j.exceptions.ScrapingAntException;
import com.gapplabs.scrapingant4j.exceptions.ScrapingAntHttpException;
import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ScrapingAntErrorDecoderTest {

  private final ScrapingAntErrorDecoder decoder = new ScrapingAntErrorDecoder();

  private Response buildResponse(int status, Map<String, Collection<String>> headers, String body) {
    byte[] bytes = null;
    if (body != null) {
      bytes = body.getBytes(StandardCharsets.UTF_8);
    }
    Request request =
        Request.create(
            Request.HttpMethod.GET, "http://localhost", Collections.emptyMap(), null, null, null);
    return Response.builder()
        .status(status)
        .reason("Reason")
        .headers(headers != null ? headers : Collections.emptyMap())
        .request(request)
        .body(bytes)
        .build();
  }

  @ParameterizedTest
  @MethodSource("errorStatusProvider")
  void testSpecificErrorCodes(
      int status,
      Class<? extends ScrapingAntHttpException> expectedExceptionClass,
      String expectedDefaultMessage) {
    Response resp = buildResponse(status, null, null);
    Exception ex = decoder.decode("m", resp);

    assertInstanceOf(expectedExceptionClass, ex);
    ScrapingAntHttpException sae = (ScrapingAntHttpException) ex;
    assertEquals(status, sae.getStatusCode());
    assertEquals(expectedDefaultMessage, sae.getMessage());
  }

  @Test
  void decode400UsesDetailWhenPresent() {
    Response resp = buildResponse(400, null, "{\"detail\":\"Bad request detail\"}");

    Exception ex = decoder.decode("m", resp);

    assertInstanceOf(ScrapingAntException.class, ex);
    assertEquals("Bad request detail", ex.getMessage());
  }

  @Test
  void decodeExposesDetailAndClassifiesInvalidApiKey() {
    Response resp = buildResponse(401, null, "{\"detail\":\"Invalid API key\"}");

    ScrapingAntHttpException exception =
        assertInstanceOf(ScrapingAntHttpException.class, decoder.decode("m", resp));

    assertEquals("Invalid API key", exception.getDetail());
    assertEquals(ScrapingAntErrorType.INVALID_API_KEY, exception.getErrorType());
    assertFalse(exception.isRetryable());
  }

  @Test
  void decodeClassifiesCreditsAndTimeoutDetails() {
    ScrapingAntHttpException credits =
        (ScrapingAntHttpException)
            decoder.decode("m", buildResponse(400, null, "{\"detail\":\"No credits left\"}"));
    ScrapingAntHttpException timeout =
        (ScrapingAntHttpException)
            decoder.decode("m", buildResponse(400, null, "{\"detail\":\"Target timeout\"}"));

    assertEquals(ScrapingAntErrorType.INSUFFICIENT_CREDITS, credits.getErrorType());
    assertEquals(ScrapingAntErrorType.TARGET_TIMEOUT, timeout.getErrorType());
    assertTrue(timeout.isRetryable());
  }

  @Test
  void decodeWithoutDetailHasNullDetailAndUnknownType() {
    ScrapingAntHttpException exception =
        (ScrapingAntHttpException) decoder.decode("m", buildResponse(400, null, null));

    assertNull(exception.getDetail());
    assertEquals(ScrapingAntErrorType.UNKNOWN, exception.getErrorType());
    assertFalse(exception.isRetryable());
  }

  @Test
  void decodeServerErrorIsRetryable() {
    ScrapingAntHttpException exception =
        (ScrapingAntHttpException) decoder.decode("m", buildResponse(500, null, null));

    assertEquals(ScrapingAntErrorType.SERVER_ERROR, exception.getErrorType());
    assertTrue(exception.isRetryable());
  }

  @ParameterizedTest
  @MethodSource("fallbackMessageCases")
  void decodeWithoutUsableDetailUsesFallbackMessage(
      int status, String body, String expectedMessage) {
    Response resp = buildResponse(status, null, body);

    Exception ex = decoder.decode("m", resp);

    assertInstanceOf(ScrapingAntException.class, ex);
    assertEquals(status, ((ScrapingAntHttpException) ex).getStatusCode());
    assertEquals(expectedMessage, ex.getMessage());
  }

  @Test
  void decode429ReadsRetryAfterHeader() {
    Response resp =
        buildResponse(429, Map.of("Retry-After", List.of("120")), "{\"detail\":\"Slow down\"}");

    Exception ex = decoder.decode("m", resp);
    assertInstanceOf(ScrapingAntHttpException.class, ex);
    ScrapingAntHttpException rateLimitException = (ScrapingAntHttpException) ex;
    assertEquals("Slow down", rateLimitException.getMessage());
    assertEquals("Slow down", rateLimitException.getDetail());
    assertEquals(ScrapingAntErrorType.RATE_LIMITED, rateLimitException.getErrorType());
    assertTrue(rateLimitException.isRetryable());
    assertEquals(java.time.Duration.ofSeconds(120), rateLimitException.getRetryAfter());
  }

  @Test
  void decode429HandlesMissingRetryAfterHeader() {
    Response resp = buildResponse(429, null, null);
    ScrapingAntHttpException ex = (ScrapingAntHttpException) decoder.decode("m", resp);
    assertEquals(null, ex.getRetryAfter());
  }

  @Test
  void decode429HandlesMalformedRetryAfterHeader() {
    Response resp = buildResponse(429, Map.of("Retry-After", List.of("abc")), null);
    ScrapingAntHttpException ex = (ScrapingAntHttpException) decoder.decode("m", resp);
    assertEquals(null, ex.getRetryAfter());
  }

  @Test
  void decode429ParsesRfc1123RetryAfterHeader() {
    String retryAt =
        ZonedDateTime.now(ZoneOffset.UTC)
            .plusSeconds(30)
            .format(DateTimeFormatter.RFC_1123_DATE_TIME);
    Response resp = buildResponse(429, Map.of("Retry-After", List.of(retryAt)), null);

    ScrapingAntHttpException exception = (ScrapingAntHttpException) decoder.decode("m", resp);

    assertTrue(exception.getRetryAfter().compareTo(Duration.ofSeconds(28)) >= 0);
    assertTrue(exception.getRetryAfter().compareTo(Duration.ofSeconds(30)) <= 0);
  }

  @Test
  void decode429IgnoresNegativeRetryAfterSeconds() {
    Response resp = buildResponse(429, Map.of("Retry-After", List.of("-1")), null);

    ScrapingAntHttpException exception = (ScrapingAntHttpException) decoder.decode("m", resp);

    assertEquals(null, exception.getRetryAfter());
  }

  @Test
  void only429ResponsesExposeRetryAfterMetadata() {
    Response resp = buildResponse(500, Map.of("Retry-After", List.of("120")), null);

    Exception exception = decoder.decode("m", resp);

    assertEquals(ScrapingAntHttpException.class, exception.getClass());
    assertEquals(null, ((ScrapingAntHttpException) exception).getRetryAfter());
  }

  @Test
  void decodeUnknownStatusWithDetailReturnsDetail() {
    Response resp = buildResponse(418, null, "{\"detail\":\"I am a teapot\"}");
    Exception ex = decoder.decode("m", resp);
    assertEquals("I am a teapot", ex.getMessage());
  }

  private static Stream<Arguments> fallbackMessageCases() {
    return Stream.of(
        Arguments.of(418, null, "HTTP 418"),
        Arguments.of(418, "{\"detail\":\"\"}", "HTTP 418"),
        Arguments.of(418, "{\"detail\":null}", "HTTP 418"),
        Arguments.of(418, "{\"foo\":\"bar\"}", "HTTP 418"),
        Arguments.of(400, "{ not json", "HTTP 400"));
  }

  private static Stream<Arguments> errorStatusProvider() {
    return Stream.of(
        Arguments.of(400, ScrapingAntHttpException.class, "HTTP 400"),
        Arguments.of(403, ScrapingAntHttpException.class, "HTTP 403"),
        Arguments.of(404, ScrapingAntHttpException.class, "HTTP 404"),
        Arguments.of(405, ScrapingAntHttpException.class, "HTTP 405"),
        Arguments.of(409, ScrapingAntHttpException.class, "HTTP 409"),
        Arguments.of(422, ScrapingAntHttpException.class, "HTTP 422"),
        Arguments.of(423, ScrapingAntHttpException.class, "HTTP 423"),
        Arguments.of(429, ScrapingAntHttpException.class, "HTTP 429"),
        Arguments.of(500, ScrapingAntHttpException.class, "HTTP 500"));
  }
}
