package com.gapplabs.scrapingant4j.exceptions;

import java.time.Duration;
import lombok.Getter;

/** An unsuccessful HTTP response from the ScrapingAnt API. */
@Getter
public class ScrapingAntHttpException extends ScrapingAntException {
  private final int statusCode;
  private final Duration retryAfter;
  private final String detail;
  private final ScrapingAntErrorType errorType;

  /** Creates an exception for an HTTP response. */
  public ScrapingAntHttpException(int statusCode, String message) {
    this(statusCode, message, null);
  }

  /** Creates an HTTP failure with optional Retry-After metadata. */
  public ScrapingAntHttpException(int statusCode, String message, Duration retryAfter) {
    this(statusCode, message, retryAfter, null);
  }

  public ScrapingAntHttpException(
      int statusCode, String message, Duration retryAfter, String detail) {
    super(message);
    this.statusCode = statusCode;
    this.retryAfter = retryAfter;
    this.detail = detail;
    this.errorType = ScrapingAntErrorType.from(statusCode, detail == null ? message : detail);
  }

  public boolean isRetryable() {
    return errorType.isRetryable();
  }
}
