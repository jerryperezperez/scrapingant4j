package com.gapplabs.config;

import com.gapplabs.ScrapingAntClient;
import com.gapplabs.ScrapingAntClientOptions;
import com.google.gson.Gson;
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
            ObjectProvider<Feign.Builder> feignBuilderProvider,
            ObjectProvider<Gson> gsonProvider
    ) {
        ScrapingAntClientOptions.Builder builder = ScrapingAntClientOptions.builder()
                .apiKey(properties.getApiKey())
                .endpoint(properties.getEndpoint())
                .apiVersion(properties.getApiVersion());

        Feign.Builder feignBuilder = feignBuilderProvider.getIfAvailable();
        if (feignBuilder != null) {
            builder.feignBuilder(feignBuilder);
        }

        Gson gson = gsonProvider.getIfAvailable();
        if (gson != null) {
            builder.gson(gson);
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
