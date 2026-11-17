package com.gapplabs.config;

import com.gapplabs.ScrapingAntClient;
import com.gapplabs.ScrapingAntClientOptions;
import feign.Feign;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(ScrapingAntProperties.class)
public class ScrapingAntAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean({ScrapingAntClientOptions.class, ScrapingAntClient.class})
    @ConditionalOnProperty(prefix = "scrapingant", name = "api-key")
    public ScrapingAntClientOptions scrapingAntClientOptions(
            ScrapingAntProperties properties,
            ObjectProvider<Feign.Builder> feignBuilderProvider
    ) {
        ScrapingAntClientOptions.ScrapingAntClientOptionsBuilder builder = ScrapingAntClientOptions.builder()
                .apiKey(properties.getApiKey());

        Feign.Builder feignBuilder = feignBuilderProvider.getIfAvailable();
        if (feignBuilder != null) {
            builder.feignBuilder(feignBuilder);
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "scrapingant", name = "api-key")
    public ScrapingAntClient scrapingAntClient(ScrapingAntClientOptions options) {
        return new ScrapingAntClient(options);
    }
}
