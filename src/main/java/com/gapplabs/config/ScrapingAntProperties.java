package com.gapplabs.config;

import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the ScrapingAnt SDK when used inside a Spring application.
 * Prefix: scrapingant
 */
@Data
@ConfigurationProperties(prefix = "scrapingant")
public class ScrapingAntProperties {

    /**
     * API key for ScrapingAnt (required).
     * Can be provided via environment variable SCRAPINGANT_API_KEY.
     */
    private String apiKey;

    /**
     * Base endpoint for the ScrapingAnt API. Default matches the SDK's built-in value.
     */
    private String endpoint = "https://api.scrapingant.com";

    /**
     * API version. Keep default synchronized with client if changed.
     */
    private String apiVersion = "v2";

    /**
     * Default proxy type used by the SDK when not supplied in requests.
     */
    private ProxyType proxyType = ProxyType.STANDARD;

    /**
     * Default proxy country used by the SDK when not supplied in requests.
     */
    private ProxyCountry proxyCountry = ProxyCountry.ALL_COUNTRIES;

    /**
     * Whether to default to browser execution when not supplied in the request.
     */
    private Boolean browserDefault = false;

    /**
     * Default timeout in seconds for requests when not supplied.
     */
    private Integer timeout = 30;
}
