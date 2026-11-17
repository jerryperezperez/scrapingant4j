package com.gapplabs.config;

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
     * API key for ScrapingAnt.
     */
    private String apiKey;

}
