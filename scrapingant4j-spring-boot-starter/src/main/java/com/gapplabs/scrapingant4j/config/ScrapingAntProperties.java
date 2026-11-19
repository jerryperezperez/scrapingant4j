package com.gapplabs.scrapingant4j.config;

import java.time.Duration;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the ScrapingAnt SDK when used inside a Spring application. Prefix:
 * scrapingant
 */
@Data
@ConfigurationProperties(prefix = "scrapingant")
public class ScrapingAntProperties {

  /** API key for ScrapingAnt. */
  @Getter(AccessLevel.NONE)
  @ToString.Exclude
  private String apiKey;

  /** Maximum time to establish a connection. */
  private Duration connectTimeout = Duration.ofSeconds(5);

  /** Maximum time to wait for response data. */
  private Duration readTimeout = Duration.ofSeconds(120);

  String apiKey() {
    return apiKey;
  }
}
