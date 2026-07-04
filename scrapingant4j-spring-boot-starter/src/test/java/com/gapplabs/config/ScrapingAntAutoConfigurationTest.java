package com.gapplabs.config;

import com.gapplabs.ScrapingAntClient;
import com.gapplabs.ScrapingAntClientOptions;
import com.google.gson.Gson;
import feign.Feign;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

class ScrapingAntAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestApplication.class);

    @Test
    void testScrapingAntClientBeanExists() {
        contextRunner.withPropertyValues("scrapingant.api-key=test-api-key")
                .run(context -> {
                    assertThat(context).hasSingleBean(ScrapingAntClient.class);
                    assertThat(context).hasSingleBean(ScrapingAntProperties.class);
                    assertThat(context).hasSingleBean(ScrapingAntClientOptions.class);
                });
    }

    @Test
    void testConfigurationPropertiesAreLoaded() {
        contextRunner.withPropertyValues(
                "scrapingant.api-key=custom-key",
                "scrapingant.endpoint=https://internal.example.com/",
                "scrapingant.api-version=/v9"
        ).run(context -> {
            ScrapingAntProperties properties = context.getBean(ScrapingAntProperties.class);
            ScrapingAntClientOptions options = context.getBean(ScrapingAntClientOptions.class);
            assertThat(properties.getApiKey()).isEqualTo("custom-key");
            assertThat(properties.getEndpoint()).isEqualTo("https://internal.example.com/");
            assertThat(properties.getApiVersion()).isEqualTo("/v9");
            assertThat(options.getBaseUrl()).isEqualTo("https://internal.example.com/v9");
        });
    }

    @Test
    void testCustomRuntimeComponentsAreAppliedToClientOptions() {
        contextRunner
                .withPropertyValues("scrapingant.api-key=custom-key")
                .withBean(Gson.class, Gson::new)
                .withBean(Feign.Builder.class, Feign::builder)
                .run(context -> {
                    ScrapingAntClientOptions options = context.getBean(ScrapingAntClientOptions.class);
                    assertThat(options.getGson()).isSameAs(context.getBean(Gson.class));
                    assertThat(options.getFeignBuilder()).isSameAs(context.getBean(Feign.Builder.class));
                });
    }

    @Test
    void testAutoConfigurationIsDiscoveredFromMetadata() {
        contextRunner.withPropertyValues("scrapingant.api-key=test-api-key")
                .run(context -> {
                    assertThat(context).hasSingleBean(ScrapingAntClient.class);
                    assertThat(context).hasSingleBean(ScrapingAntClientOptions.class);
                });
    }

    @Test
    void testAutoConfigurationDoesNotCreateBeansWithoutApiKey() {
        contextRunner.run(context -> {
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
                .run(context -> {
                    assertThat(context).hasSingleBean(ScrapingAntClient.class);
                    assertThat(context).doesNotHaveBean(ScrapingAntClientOptions.class);
                });
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class CustomClientApplication {

        @Bean
        ScrapingAntClient scrapingAntClient() {
            return new ScrapingAntClient("custom");
        }
    }
}
