package io.github.jerryperezperez.scrapingant4j;

import feign.RequestInterceptor;
import feign.RequestTemplate;

final class ScrapingAntAuthInterceptor implements RequestInterceptor {
  static final String API_KEY_HEADER = "x-api-key";
  private final String apiKey;

  ScrapingAntAuthInterceptor(String apiKey) {
    this.apiKey = apiKey;
  }

  @Override
  public void apply(RequestTemplate template) {
    template.header(API_KEY_HEADER, apiKey);
  }
}
