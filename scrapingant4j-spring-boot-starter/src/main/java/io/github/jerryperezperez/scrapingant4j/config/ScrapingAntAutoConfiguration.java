package io.github.jerryperezperez.scrapingant4j.config;

import io.github.jerryperezperez.scrapingant4j.ScrapingAntClient;
import io.github.jerryperezperez.scrapingant4j.ScrapingAntClientOptions;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/** Auto-configuration for ScrapingAnt client beans. */
@AutoConfiguration
@EnableConfigurationProperties(ScrapingAntProperties.class)
public class ScrapingAntAutoConfiguration {

  /** Creates client options from configuration properties. */
  @Bean
  @ConditionalOnMissingBean({ScrapingAntClientOptions.class, ScrapingAntClient.class})
  @ConditionalOnProperty(prefix = "scrapingant", name = "api-key")
  public ScrapingAntClientOptions scrapingAntClientOptions(ScrapingAntProperties properties) {
    return ScrapingAntClientOptions.builder()
        .apiKey(properties.apiKey())
        .connectTimeout(properties.getConnectTimeout())
        .readTimeout(properties.getReadTimeout())
        .build();
  }

  /** Creates the client from configured options. */
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(prefix = "scrapingant", name = "api-key")
  public ScrapingAntClient scrapingAntClient(ScrapingAntClientOptions options) {
    return new ScrapingAntClient(options);
  }
}
