package com.gapplabs;


import java.time.Duration;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

/** Immutable configuration for {@link ScrapingAntClient}. */
@Getter
public final class ScrapingAntClientOptions {

  public static final String DEFAULT_BASE_URL = "https://api.scrapingant.com/v2";

  @Getter(AccessLevel.NONE)
  private final String apiKey;
  
  private final Duration connectTimeout;
  private final Duration readTimeout;

  @Builder
  private ScrapingAntClientOptions(
      String apiKey,
      Duration connectTimeout,
      Duration readTimeout) {
    this.apiKey = requireApiKey(apiKey);
    this.connectTimeout = requireTimeout(connectTimeout, "connectTimeout");
    this.readTimeout = requireTimeout(readTimeout, "readTimeout");
  }

  String apiKey() {
    return apiKey;
  }

  /** Partial Lombok builder for defaults and secret-safe logging. */
  public static class ScrapingAntClientOptionsBuilder {
    
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(60);

    @Override
    public String toString() {
      return "ScrapingAntClientOptionsBuilder[apiKey=<redacted>]";
    }
  }

  private static String requireApiKey(String apiKey) {
    if (apiKey == null || apiKey.isBlank()) {
      throw new IllegalArgumentException("apiKey is required and cannot be blank");
    }
    return apiKey.trim();
  }

  private static Duration requireTimeout(Duration timeout, String name) {
    Objects.requireNonNull(timeout, name);
    if (timeout.isZero()
        || timeout.isNegative()
        || timeout.compareTo(Duration.ofMillis(Integer.MAX_VALUE)) > 0
        || timeout.toMillis() < 1) {
      throw new IllegalArgumentException(
          name + " must be between 1 ms and " + Integer.MAX_VALUE + " ms");
    }
    return timeout;
  }

}
