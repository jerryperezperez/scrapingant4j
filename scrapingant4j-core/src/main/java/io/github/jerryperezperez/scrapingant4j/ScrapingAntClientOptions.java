package io.github.jerryperezperez.scrapingant4j;

import java.time.Duration;
import lombok.Builder;
import lombok.Getter;

/** Immutable configuration for {@link ScrapingAntClient}. */
public final class ScrapingAntClientOptions {

  public static final String DEFAULT_BASE_URL = "https://api.scrapingant.com/v2";

  private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(120);
  private static final Duration MIN_TIMEOUT = Duration.ofMillis(1);
  private static final Duration MAX_TIMEOUT = Duration.ofMillis(Integer.MAX_VALUE);

  private final String apiKey;
  @Getter private final Duration connectTimeout;
  @Getter private final Duration readTimeout;

  @Builder
  private ScrapingAntClientOptions(String apiKey, Duration connectTimeout, Duration readTimeout) {
    this.apiKey = requireApiKey(apiKey);
    this.connectTimeout =
        requireTimeout(
            connectTimeout == null ? DEFAULT_CONNECT_TIMEOUT : connectTimeout, "connectTimeout");
    this.readTimeout =
        requireTimeout(readTimeout == null ? DEFAULT_READ_TIMEOUT : readTimeout, "readTimeout");
  }

  String apiKey() {
    return apiKey;
  }

  /** Lombok generates the setters and build(); this only hides the key from toString. */
  public static class ScrapingAntClientOptionsBuilder {
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
    if (timeout.compareTo(MIN_TIMEOUT) < 0 || timeout.compareTo(MAX_TIMEOUT) > 0) {
      throw new IllegalArgumentException(
          name + " must be between 1 ms and " + Integer.MAX_VALUE + " ms");
    }
    return timeout;
  }
}
