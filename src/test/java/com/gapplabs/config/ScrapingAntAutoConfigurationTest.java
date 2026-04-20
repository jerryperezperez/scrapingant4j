package com.gapplabs.config;

import com.gapplabs.ScrapingAntClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapingAntAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ScrapingAntAutoConfiguration.class));

    @Test
    void testScrapingAntClientBeanExists() {
        contextRunner.withPropertyValues("scrapingant.api-key=test-api-key")
                .run(context -> {
                    assertThat(context).hasSingleBean(ScrapingAntClient.class);
                    assertThat(context).hasSingleBean(ScrapingAntProperties.class);
                });
    }

    @Test
    void testConfigurationPropertiesAreLoaded() {
        contextRunner.withPropertyValues(
                "scrapingant.api-key=custom-key",
                "scrapingant.timeout=45"
        ).run(context -> {
            ScrapingAntProperties properties = context.getBean(ScrapingAntProperties.class);
            assertThat(properties.getApiKey()).isEqualTo("custom-key");
            assertThat(properties.getTimeout()).isEqualTo(45);
        });
    }
}
