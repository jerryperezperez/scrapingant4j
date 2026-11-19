package com.gapplabs.scrapingant4j.exceptions;

import java.util.Locale;

public enum ScrapingAntErrorType {
  INVALID_API_KEY(false),
  INSUFFICIENT_CREDITS(false),
  TARGET_TIMEOUT(true),
  RATE_LIMITED(true),
  SERVER_ERROR(true),
  UNKNOWN(false);

  private final boolean retryable;

  ScrapingAntErrorType(boolean retryable) {
    this.retryable = retryable;
  }

  public boolean isRetryable() {
    return retryable;
  }

  static ScrapingAntErrorType from(int statusCode, String detail) {
    String text = detail == null ? "" : detail.toLowerCase(Locale.ROOT);

    if (containsAny(text, "api key", "apikey", "unauthorized", "invalid token", "authentication")) {
      return INVALID_API_KEY;
    }
    if (containsAny(text, "credit", "balance", "insufficient funds")) {
      return INSUFFICIENT_CREDITS;
    }
    if (containsAny(text, "timeout", "timed out", "time out")) {
      return TARGET_TIMEOUT;
    }
    if (containsAny(text, "rate limit", "too many request", "slow down")) {
      return RATE_LIMITED;
    }

    return switch (statusCode) {
      case 401, 403 -> INVALID_API_KEY;
      case 402 -> INSUFFICIENT_CREDITS;
      case 408, 504 -> TARGET_TIMEOUT;
      case 429 -> RATE_LIMITED;
      default -> statusCode >= 500 ? SERVER_ERROR : UNKNOWN;
    };
  }

  private static boolean containsAny(String text, String... needles) {
    for (String needle : needles) {
      if (text.contains(needle)) {
        return true;
      }
    }
    return false;
  }
}
