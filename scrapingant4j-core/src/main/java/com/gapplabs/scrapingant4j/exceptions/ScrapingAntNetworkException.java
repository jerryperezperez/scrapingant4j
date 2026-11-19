package com.gapplabs.scrapingant4j.exceptions;

/** A transport-level failure before a usable HTTP response was received. */
public final class ScrapingAntNetworkException extends ScrapingAntException {
  /** Creates a network failure while preserving the transport exception as its cause. */
  public ScrapingAntNetworkException(String message, Throwable cause) {
    super(message, cause);
  }
}
