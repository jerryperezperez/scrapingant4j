package com.gapplabs.dto;

import com.gapplabs.constants.BlockResource;
import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
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
            if (this.url == null || this.url.isBlank()) {
                throw new IllegalArgumentException("'url' is required and cannot be null nor empty");
            }
            if (Boolean.FALSE.equals(this.browser) && Boolean.TRUE.equals(this.returnPageSource)) {
                throw new IllegalArgumentException("returnPageSource can only be true when browser is true");
            }
            if (Boolean.FALSE.equals(this.browser) && this.jsSnippet != null) {
                throw new IllegalArgumentException("jsSnippet can only be set when browser is true");
            }
            if (Boolean.FALSE.equals(this.browser) && this.cssSelector != null) {
                throw new IllegalArgumentException("cssSelector can only be set when browser is true");
            }
            if (Boolean.FALSE.equals(this.browser) && this.waitForSelector != null) {
                throw new IllegalArgumentException("waitForSelector can only be set when browser is true");
            }
            if (Boolean.FALSE.equals(this.browser) && this.timeout != null) {
                throw new IllegalArgumentException("timeout can only be set when browser is true");
            }
            if (Boolean.FALSE.equals(this.browser) && this.blockResources != null && !this.blockResources.isEmpty()) {
                throw new IllegalArgumentException("blockResources can only be set when browser is true");
            }
            if (this.timeout != null && (this.timeout < 5 || this.timeout > 60)) {
                throw new IllegalArgumentException("'timeout' must be between 5 and 60 seconds");
            }
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
    }

    /**
     * Builds the query parameters for OpenFeign.
     * Only explicitly supplied values are serialized so the API can apply its defaults.
     */
    public Map<String, Object> toQueryMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("url", this.url);

        if (browser != null) {
            params.put("browser", browser);
        }
        if (timeout != null) {
            params.put("timeout", timeout);
        }
        if (returnPageSource != null) {
            params.put("return_page_source", returnPageSource);
        }
        if (cookies != null) {
            params.put("cookies", cookies);
        }
        if (proxyType != null) {
            params.put("proxy_type", proxyType.getValue());
        }
        if (proxyCountry != null) {
            params.put("proxy_country", proxyCountry.getValue());
        }
        if (waitForSelector != null) {
            params.put("wait_for_selector", waitForSelector);
        }
        if (jsSnippet != null && !jsSnippet.isEmpty()) {
            params.put("js_snippet", Base64.getEncoder().encodeToString(jsSnippet.getBytes(StandardCharsets.UTF_8)));
        }
        if (cssSelector != null && !cssSelector.isEmpty()) {
            params.put("css_selector", cssSelector);
        }
        if (blockResources != null && !blockResources.isEmpty()) {
            params.put("block_resource", blockResources.stream().map(BlockResource::getValue).toList());
        }

        return params;
    }
}
