package com.gapplabs.config;

import com.gapplabs.ScrapingAntClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


@Configuration
@EnableConfigurationProperties(ScrapingAntProperties.class)
public class ScrapingAntAutoConfiguration {
    
    @Bean
    public ScrapingAntClient scrapingAntClient(ScrapingAntProperties properties) {
        return new ScrapingAntClient(properties.getApiKey());
    }
}
