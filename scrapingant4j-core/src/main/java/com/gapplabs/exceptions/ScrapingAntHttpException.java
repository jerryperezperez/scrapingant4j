package com.gapplabs.exceptions;

import java.time.Duration;
import lombok.Getter;

/** An unsuccessful HTTP response from the ScrapingAnt API. */
@Getter
public class ScrapingAntHttpException extends ScrapingAntException {
  private final int statusCode;
  private final Duration retryAfter;

  /** Creates an exception for an HTTP response. */
  public ScrapingAntHttpException(int statusCode, String message) {
    this(statusCode, message, null);
  }

  /** Creates an HTTP failure with optional Retry-After metadata. */
  public ScrapingAntHttpException(int statusCode, String message, Duration retryAfter) {
    super(message);
    this.statusCode = statusCode;
    this.retryAfter = retryAfter;
  }
}
