package com.gapplabs;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
