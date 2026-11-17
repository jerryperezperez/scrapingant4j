package com.gapplabs.dto;

import com.gapplabs.constants.BlockResource;
import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
import com.gapplabs.constants.ScrapingAntRequestConstants;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@ToString
public class ScrapingAntRequest {

    private final String url;
    private final Boolean browser;
    private final Boolean returnPageSource;
    private final ProxyType proxyType;
    private final ProxyCountry proxyCountry;
    private final String cookies;
    private final String jsSnippet;
    private final String waitForSelector;
    private final String cssSelector;
    private final List<BlockResource> blockResources;
    private final Integer timeout;

    @SuppressWarnings("unused")
    public static class ScrapingAntRequestBuilder {
        public ScrapingAntRequest build() {
            validateUrl();
            validateBrowserOnlyOptions();
            validateTimeout();

            return new ScrapingAntRequest(
                    url,
                    browser,
                    returnPageSource,
                    proxyType,
                    proxyCountry,
                    cookies,
                    jsSnippet,
                    waitForSelector,
                    cssSelector,
                    blockResources,
                    timeout
            );
        }

        private void validateUrl() {
            if (this.url == null || this.url.isBlank()) {
                throw new IllegalArgumentException("'url' is required and cannot be null nor empty");
            }
        }

        private void validateBrowserOnlyOptions() {
            if (!Boolean.FALSE.equals(this.browser)) {
                return;
            }

            rejectWhenBrowserDisabled(Boolean.TRUE.equals(this.returnPageSource),
                    "returnPageSource can only be true when browser is true");
            rejectWhenBrowserDisabled(this.jsSnippet != null,
                    "jsSnippet can only be set when browser is true");
            rejectWhenBrowserDisabled(this.cssSelector != null,
                    "cssSelector can only be set when browser is true");
            rejectWhenBrowserDisabled(this.waitForSelector != null,
                    "waitForSelector can only be set when browser is true");
            rejectWhenBrowserDisabled(this.timeout != null,
                    "timeout can only be set when browser is true");
            rejectWhenBrowserDisabled(this.blockResources != null && !this.blockResources.isEmpty(),
                    "blockResources can only be set when browser is true");
        }

        private void rejectWhenBrowserDisabled(boolean optionIsSet, String message) {
            if (optionIsSet) {
                throw new IllegalArgumentException(message);
            }
        }

        private void validateTimeout() {
            if (this.timeout != null && (this.timeout < 5 || this.timeout > 60)) {
                throw new IllegalArgumentException("'timeout' must be between 5 and 60 seconds");
            }
        }
    }

    /**
     * Builds the query parameters for OpenFeign.
     * Only explicitly supplied values are serialized so the API can apply its defaults.
     */
    public Map<String, Object> toQueryMap() {
        Map<String, Object> params = new HashMap<>();
        params.put(ScrapingAntRequestConstants.URL, this.url);
        putIfNotNull(params, ScrapingAntRequestConstants.BROWSER, browser);
        putIfNotNull(params, ScrapingAntRequestConstants.TIMEOUT, timeout);
        putIfNotNull(params, ScrapingAntRequestConstants.RETURN_PAGE_SOURCE, returnPageSource);
        putIfNotNull(params, ScrapingAntRequestConstants.COOKIES, cookies);
        putIfNotNull(params, ScrapingAntRequestConstants.PROXY_TYPE, enumValue(proxyType));
        putIfNotNull(params, ScrapingAntRequestConstants.PROXY_COUNTRY, enumValue(proxyCountry));
        putIfNotNull(params, ScrapingAntRequestConstants.WAIT_FOR_SELECTOR, waitForSelector);
        putIfNotEmpty(params, ScrapingAntRequestConstants.JS_SNIPPET, encodeJsSnippet(jsSnippet));
        putIfNotEmpty(params, ScrapingAntRequestConstants.CSS_SELECTOR, cssSelector);
        putBlockResources(params, blockResources);
        return params;
    }

    private static void putIfNotNull(Map<String, Object> params, String key, Object value) {
        if (value != null) {
            params.put(key, value);
        }
    }

    private static void putIfNotEmpty(Map<String, Object> params, String key, String value) {
        if (value != null && !value.isEmpty()) {
            params.put(key, value);
        }
    }

    private static String enumValue(ProxyType proxyType) {
        return proxyType != null ? proxyType.getValue() : null;
    }

    private static String enumValue(ProxyCountry proxyCountry) {
        return proxyCountry != null ? proxyCountry.getValue() : null;
    }

    private static String encodeJsSnippet(String jsSnippet) {
        if (jsSnippet == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(jsSnippet.getBytes(StandardCharsets.UTF_8));
    }

    private static void putBlockResources(Map<String, Object> params, List<BlockResource> blockResources) {
        if (blockResources == null || blockResources.isEmpty()) {
            return;
        }

        params.put(ScrapingAntRequestConstants.BLOCK_RESOURCE,
                blockResources.stream().map(BlockResource::getValue).toList());
    }
}
