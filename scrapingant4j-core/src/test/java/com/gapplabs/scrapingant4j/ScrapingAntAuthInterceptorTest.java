package com.gapplabs.scrapingant4j;

import static org.junit.jupiter.api.Assertions.assertTrue;

import feign.RequestTemplate;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScrapingAntAuthInterceptorTest {

  @Test
  void testApplyAddsApiKeyHeader() {
    String apiKey = "test-api-key";
    ScrapingAntAuthInterceptor interceptor = new ScrapingAntAuthInterceptor(apiKey);
    RequestTemplate template = new RequestTemplate();

    interceptor.apply(template);

    Map<String, Collection<String>> headers = template.headers();
    assertTrue(headers.containsKey("x-api-key"));
    assertTrue(headers.get("x-api-key").contains(apiKey));
  }
}
