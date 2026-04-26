package com.gapplabs;

import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.dto.ScrapingAntRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScrapingAntRequestTest {

    @Test
    void testBuilderThrowsExceptionWhenUrlIsNull() {
        assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url(null)
                .build());
    }

    @Test
    void testBuilderAllowsProxyTypeToBeOmitted() {
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .proxyType(null)
                .build();

        assertNull(request.getProxyType());
    }

    @Test
    void testToQueryMap() {
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(false)
                .proxyCountry(ProxyCountry.USA)
                .returnPageSource(false)
                .build();

        Map<String, Object> queryMap = request.toQueryMap();

        assertEquals("https://example.com", queryMap.get("url"));
        assertEquals(false, queryMap.get("browser"));
        assertEquals("us", queryMap.get("proxy_country"));
        assertEquals(false, queryMap.get("return_page_source"));
    }

    @Test
    void testJsSnippetEncoding() {
        String jsSnippet = "console.log('hello');";
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(true)
                .jsSnippet(jsSnippet)
                .build();

        Map<String, Object> queryMap = request.toQueryMap();
        assertEquals("Y29uc29sZS5sb2coJ2hlbGxvJyk7", queryMap.get("js_snippet"));
    }

    @Test
    void testInconsistentParametersThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(false)
                .returnPageSource(true)
                .build());
    }

    @Test
    void testBuilderRejectsWaitForSelectorWhenBrowserIsFalse() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(false)
                .waitForSelector(".card")
                .build());

        assertEquals("waitForSelector can only be set when browser is true", ex.getMessage());
    }

    @Test
    void testBuilderRejectsTimeoutOutsideAllowedRange() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url("https://example.com")
                .timeout(61)
                .build());

        assertEquals("'timeout' must be between 5 and 60 seconds", ex.getMessage());
    }

    @Test
    void testToQueryMapPreservesApiDefaultsWhenOptionIsOmitted() {
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .build();

        Map<String, Object> queryMap = request.toQueryMap();

        assertEquals(1, queryMap.size());
        assertFalse(queryMap.containsKey("browser"));
        assertFalse(queryMap.containsKey("proxy_type"));
        assertFalse(queryMap.containsKey("return_page_source"));
    }
}
