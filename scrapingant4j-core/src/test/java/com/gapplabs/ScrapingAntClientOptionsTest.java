package com.gapplabs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class ScrapingAntClientOptionsTest {
  @Test
  void usesDocumentedDefaultsWithoutExposingTheApiKey() {
    ScrapingAntClientOptions options = ScrapingAntClientOptions.builder().apiKey("secret").build();
    assertEquals(Duration.ofSeconds(5), options.getConnectTimeout());
    assertEquals(Duration.ofSeconds(60), options.getReadTimeout());
    org.junit.jupiter.api.Assertions.assertFalse(options.toString().contains("secret"));
    org.junit.jupiter.api.Assertions.assertFalse(
        ScrapingAntClientOptions.builder().apiKey("secret").toString().contains("secret"));
    assertThrows(
        NoSuchMethodException.class, () -> ScrapingAntClientOptions.class.getMethod("getApiKey"));
    assertThrows(
        NoSuchMethodException.class, () -> ScrapingAntClientOptions.class.getMethod("getBaseUrl"));
    assertThrows(
        NoSuchMethodException.class,
        () ->
            ScrapingAntClientOptions.ScrapingAntClientOptionsBuilder.class.getMethod(
                "baseUrl", String.class));
  }

  @Test
  void validatesApiKey() {
    assertEquals(
        "apiKey is required and cannot be blank",
        assertThrows(IllegalArgumentException.class, ScrapingAntClientOptions.builder()::build)
            .getMessage());
  }

  @Test
  void validatesTimeoutSettings() {

    ScrapingAntClientOptions.ScrapingAntClientOptionsBuilder scrapingAntClientOptionsBuilder =
        ScrapingAntClientOptions.builder().apiKey("key").connectTimeout(Duration.ZERO);
    assertThrows(IllegalArgumentException.class, scrapingAntClientOptionsBuilder::build);

  }
}
