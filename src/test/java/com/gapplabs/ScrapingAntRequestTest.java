package com.gapplabs;

import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
import com.gapplabs.dto.ScrapingAntRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScrapingAntRequestTest {

    @Test
    void testBuilderThrowsExceptionWhenUrlIsNull() {
        assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url(null)
                .build());
    }

    @Test
    void testBuilderThrowsExceptionWhenProxyTypeIsNull() {
        assertThrows(NullPointerException.class, () -> ScrapingAntRequest.builder()
                .url("https://example.com")
                .proxyType(null)
                .build());
    }

    @Test
    void testToQueryMap() {
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(false)
                .proxyType(ProxyType.STANDARD)
                .proxyCountry(ProxyCountry.USA)
                .returnPageSource(false)
                .build();

        Map<String, Object> queryMap = request.toQueryMap();

        assertEquals("https://example.com", queryMap.get("url"));
        assertEquals(false, queryMap.get("browser"));
        assertNull(queryMap.get("proxy_type"));
        assertEquals("us", queryMap.get("proxy_country"));
    }

    @Test
    void testJsSnippetEncoding() {
        String jsSnippet = "console.log('hello');";
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(true)
                .proxyType(ProxyType.STANDARD)
                .proxyCountry(ProxyCountry.ALL_COUNTRIES)
                .returnPageSource(false)
                .jsSnippet(jsSnippet)
                .build();

        Map<String, Object> queryMap = request.toQueryMap();
        
        // Base64 for "console.log('hello');" is "Y29uc29sZS5sb2coJ2hlbGxvJyk7"
        assertEquals("Y29uc29sZS5sb2coJ2hlbGxvJyk7", queryMap.get("js_snippet"));
    }

    @Test
    void testInconsistentParametersThrowsException() {
        // browser=false but returnPageSource=true should throw exception
        assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(false)
                .proxyType(ProxyType.STANDARD)
                .proxyCountry(ProxyCountry.ALL_COUNTRIES)
                .returnPageSource(true)
                .build());
    }
}
