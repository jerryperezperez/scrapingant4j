package com.gapplabs.scrapingant4j.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.gapplabs.scrapingant4j.ScrapingAntClient;
import com.gapplabs.scrapingant4j.ScrapingAntClientOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

class ScrapingAntAutoConfigurationTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(TestApplication.class);

  @Test
  void testScrapingAntClientBeanExists() {
    contextRunner
        .withPropertyValues("scrapingant.api-key=test-api-key")
        .run(
            context -> {
              assertThat(context).hasSingleBean(ScrapingAntClient.class);
              assertThat(context).hasSingleBean(ScrapingAntProperties.class);
              assertThat(context).hasSingleBean(ScrapingAntClientOptions.class);
            });
  }

  @Test
  void testDefaultTimeoutsMatchPlainJavaClient() {
    contextRunner
        .withPropertyValues("scrapingant.api-key=test-api-key")
        .run(
            context -> {
              ScrapingAntClientOptions options = context.getBean(ScrapingAntClientOptions.class);
              assertThat(options.getConnectTimeout()).isEqualTo(java.time.Duration.ofSeconds(5));
              assertThat(options.getReadTimeout()).isEqualTo(java.time.Duration.ofSeconds(120));
            });
  }

  @Test
  void testConfigurationPropertiesAreLoaded() {
    contextRunner
        .withPropertyValues(
            "scrapingant.api-key=custom-key",
            "scrapingant.connect-timeout=2s",
            "scrapingant.read-timeout=4s")
        .run(
            context -> {
              ScrapingAntProperties properties = context.getBean(ScrapingAntProperties.class);
              ScrapingAntClientOptions options = context.getBean(ScrapingAntClientOptions.class);
              assertThat(properties.apiKey()).isEqualTo("custom-key");
              assertThat(options.getConnectTimeout()).isEqualTo(java.time.Duration.ofSeconds(2));
              assertThat(options.getReadTimeout()).isEqualTo(java.time.Duration.ofSeconds(4));
              assertThat(properties.toString()).doesNotContain("custom-key");
            });
  }

  @Test
  void testAutoConfigurationIsDiscoveredFromMetadata() {
    contextRunner
        .withPropertyValues("scrapingant.api-key=test-api-key")
        .run(
            context -> {
              assertThat(context).hasSingleBean(ScrapingAntClient.class);
              assertThat(context).hasSingleBean(ScrapingAntClientOptions.class);
            });
  }

  @Test
  void testAutoConfigurationDoesNotCreateBeansWithoutApiKey() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(ScrapingAntProperties.class);
          assertThat(context).doesNotHaveBean(ScrapingAntClient.class);
          assertThat(context).doesNotHaveBean(ScrapingAntClientOptions.class);
        });
  }

  @Test
  void testClientBeanBacksOffWhenUserProvidesCustomBean() {
    new ApplicationContextRunner()
        .withUserConfiguration(CustomClientApplication.class)
        .withPropertyValues("scrapingant.api-key=test-api-key")
        .run(
            context -> {
              assertThat(context).hasSingleBean(ScrapingAntClient.class);
              assertThat(context).doesNotHaveBean(ScrapingAntClientOptions.class);
            });
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class TestApplication {}

  @SpringBootConfiguration
  @EnableAutoConfiguration
  static class CustomClientApplication {

    @Bean
    ScrapingAntClient scrapingAntClient() {
      return new ScrapingAntClient("custom");
    }
  }
}
