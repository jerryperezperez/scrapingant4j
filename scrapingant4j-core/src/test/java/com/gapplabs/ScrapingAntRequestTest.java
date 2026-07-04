package com.gapplabs;

import com.gapplabs.constants.BlockResource;
import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
import com.gapplabs.dto.ScrapingAntRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testBuilderAllowsBrowserOnlyValuesWhenBrowserIsTrue() {
        String jsSnippet = "console.log('hello');";
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(true)
                .returnPageSource(true)
                .proxyType(ProxyType.RESIDENTIAL)
                .proxyCountry(ProxyCountry.USA)
                .cookies("session=abc")
                .jsSnippet(jsSnippet)
                .waitForSelector(".card")
                .cssSelector(".result")
                .blockResources(List.of(BlockResource.IMAGE, BlockResource.SCRIPT))
                .timeout(10)
                .build();

        Map<String, Object> queryMap = request.toQueryMap();

        assertEquals("https://example.com", queryMap.get("url"));
        assertEquals(true, queryMap.get("browser"));
        assertEquals(true, queryMap.get("return_page_source"));
        assertEquals("residential", queryMap.get("proxy_type"));
        assertEquals("us", queryMap.get("proxy_country"));
        assertEquals("session=abc", queryMap.get("cookies"));
        assertEquals("console.log('hello');", jsSnippet);
        assertEquals(Base64.getEncoder().encodeToString(jsSnippet.getBytes(StandardCharsets.UTF_8)), queryMap.get("js_snippet"));
        assertEquals(".card", queryMap.get("wait_for_selector"));
        assertEquals(".result", queryMap.get("css_selector"));
        assertEquals(List.of("image", "script"), queryMap.get("block_resource"));
        assertEquals(10, queryMap.get("timeout"));
    }

    @Test
    void testToQueryMapOmitsEmptyOptionalStrings() {
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(true)
                .jsSnippet("")
                .cssSelector("")
                .blockResources(List.of())
                .build();

        Map<String, Object> queryMap = request.toQueryMap();

        assertFalse(queryMap.containsKey("js_snippet"));
        assertFalse(queryMap.containsKey("css_selector"));
        assertFalse(queryMap.containsKey("block_resource"));
        assertTrue(queryMap.containsKey("browser"));
    }

    @Test
    void testBuilderAcceptsBoundaryTimeout() {
        ScrapingAntRequest request = ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(true)
                .timeout(5)
                .build();

        assertEquals(5, request.toQueryMap().get("timeout"));
    }

    @ParameterizedTest
    @MethodSource("browserFalseValidationCases")
    void testBuilderRejectsBrowserOnlyOptionsWhenBrowserIsFalse(
            UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder> customizer,
            String expectedMessage
    ) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> customizer.apply(
                ScrapingAntRequest.builder()
                        .url("https://example.com")
                        .browser(false)
        ).build());

        assertEquals(expectedMessage, exception.getMessage());
    }

    private static Stream<Arguments> browserFalseValidationCases() {
        return Stream.of(
                Arguments.of(
                        (UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder>) builder -> builder.returnPageSource(true),
                        "returnPageSource can only be true when browser is true"
                ),
                Arguments.of(
                        (UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder>) builder -> builder.jsSnippet("console.log('x');"),
                        "jsSnippet can only be set when browser is true"
                ),
                Arguments.of(
                        (UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder>) builder -> builder.cssSelector(".card"),
                        "cssSelector can only be set when browser is true"
                ),
                Arguments.of(
                        (UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder>) builder -> builder.waitForSelector(".card"),
                        "waitForSelector can only be set when browser is true"
                ),
                Arguments.of(
                        (UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder>) builder -> builder.timeout(10),
                        "timeout can only be set when browser is true"
                ),
                Arguments.of(
                        (UnaryOperator<ScrapingAntRequest.ScrapingAntRequestBuilder>) builder -> builder.blockResources(List.of(BlockResource.IMAGE)),
                        "blockResources can only be set when browser is true"
                )
        );
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
    void testBuilderRejectsTimeoutOutsideAllowedRange() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ScrapingAntRequest.builder()
                .url("https://example.com")
                .browser(true)
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
