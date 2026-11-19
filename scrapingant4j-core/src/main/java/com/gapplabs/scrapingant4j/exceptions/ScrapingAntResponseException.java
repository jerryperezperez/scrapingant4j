package com.gapplabs.scrapingant4j.exceptions;

/** A successful HTTP response that is empty, null, or cannot be decoded as expected. */
public final class ScrapingAntResponseException extends ScrapingAntException {
  /** Creates a response failure without an underlying parsing exception. */
  public ScrapingAntResponseException(String message) {
    super(message);
  }

  /** Creates a response parsing failure and preserves the original cause. */
  public ScrapingAntResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
