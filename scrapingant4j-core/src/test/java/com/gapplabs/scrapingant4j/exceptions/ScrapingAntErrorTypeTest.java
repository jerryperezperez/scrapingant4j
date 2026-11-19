package com.gapplabs.scrapingant4j.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScrapingAntErrorTypeTest {

  @ParameterizedTest
  @CsvSource({
    "401, INVALID_API_KEY, false",
    "402, INSUFFICIENT_CREDITS, false",
    "408, TARGET_TIMEOUT, true",
    "429, RATE_LIMITED, true",
    "500, SERVER_ERROR, true",
    "400, UNKNOWN, false"
  })
  void statusCodeDeterminesErrorTypeWithoutDetail(
      int statusCode, ScrapingAntErrorType expectedType, boolean expectedRetryable) {
    ScrapingAntErrorType type = ScrapingAntErrorType.from(statusCode, null);

    assertEquals(expectedType, type);
    assertEquals(expectedRetryable, type.isRetryable());
  }

  @ParameterizedTest
  @MethodSource("detailCases")
  void detailDeterminesErrorType(String detail, ScrapingAntErrorType expectedType) {
    assertEquals(expectedType, ScrapingAntErrorType.from(400, detail));
  }

  @ParameterizedTest
  @MethodSource("retryableCases")
  void retryableFlagMatchesErrorType(ScrapingAntErrorType type, boolean expectedRetryable) {
    assertEquals(expectedRetryable, type.isRetryable());
  }

  private static Stream<Arguments> detailCases() {
    return Stream.of(
        Arguments.of("Invalid API key", ScrapingAntErrorType.INVALID_API_KEY),
        Arguments.of("Authentication failed", ScrapingAntErrorType.INVALID_API_KEY),
        Arguments.of("You have no credits left", ScrapingAntErrorType.INSUFFICIENT_CREDITS),
        Arguments.of("Target timeout", ScrapingAntErrorType.TARGET_TIMEOUT),
        Arguments.of("Too many requests", ScrapingAntErrorType.RATE_LIMITED),
        Arguments.of("Slow down", ScrapingAntErrorType.RATE_LIMITED));
  }

  private static Stream<Arguments> retryableCases() {
    return Stream.of(
        Arguments.of(ScrapingAntErrorType.INVALID_API_KEY, false),
        Arguments.of(ScrapingAntErrorType.INSUFFICIENT_CREDITS, false),
        Arguments.of(ScrapingAntErrorType.TARGET_TIMEOUT, true),
        Arguments.of(ScrapingAntErrorType.RATE_LIMITED, true),
        Arguments.of(ScrapingAntErrorType.SERVER_ERROR, true),
        Arguments.of(ScrapingAntErrorType.UNKNOWN, false));
  }
}
