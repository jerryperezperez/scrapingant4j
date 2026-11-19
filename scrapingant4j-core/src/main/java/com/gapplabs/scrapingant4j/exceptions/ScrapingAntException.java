package com.gapplabs.scrapingant4j.exceptions;

/** Base exception for failures while calling the ScrapingAnt API. */
public class ScrapingAntException extends RuntimeException {
  /** Creates an SDK exception with a safe diagnostic message. */
  public ScrapingAntException(String message) {
    super(message);
  }

  /** Creates an SDK exception and preserves the underlying cause. */
  public ScrapingAntException(String message, Throwable cause) {
    super(message, cause);
  }
}
