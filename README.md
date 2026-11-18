# scrapingant4j

[![Quality gate](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=coverage)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)

An unofficial Java SDK for the [ScrapingAnt API](https://scrapingant.com). The SDK builds and validates requests, sends API-key-authenticated HTTP requests, and maps JSON responses.

## Project status

The project is not yet published to Maven Central. The current development version is `1.0.0-SNAPSHOT`; install it locally with `mvn install`. See [CHANGELOG.md](CHANGELOG.md) for changes and [COMPATIBILITY.md](COMPATIBILITY.md) for the supported runtime matrix. The SDK is not affiliated with ScrapingAnt.

## Modules and requirements

| Module | Purpose |
|---|---|
| `scrapingant4j-core` | Plain Java client. Does not depend on Spring. |
| `scrapingant4j-spring-boot-starter` | Spring Boot configuration and auto-configuration for the core client. |

Java 21 or later and Maven 3.9 or later are required to build. Compatibility details are in [COMPATIBILITY.md](COMPATIBILITY.md).

## Install locally

Until the first stable Central release, build and install both modules from a checkout:

```shell
mvn -B -ntp clean install
```

Add one of these dependencies using the version installed locally:

```xml
<dependency>
    <groupId>com.gapplabs</groupId>
    <artifactId>scrapingant4j-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

For Spring Boot, use `scrapingant4j-spring-boot-starter` as the artifact ID.

## Quick start: plain Java

```java
import com.gapplabs.ScrapingAntClient;
import com.gapplabs.dto.ScrapingAntRequest;

String apiKey = System.getenv("SCRAPINGANT_API_KEY");
ScrapingAntClient client = new ScrapingAntClient(apiKey);
ScrapingAntRequest request = ScrapingAntRequest.builder()
        .url("https://example.com")
        .build();

String html = client.executeGeneral(request);
```

The client also provides `executeMarkdown`, `executeExtended`, and `executeExtract`. Extract responses can be returned as `Map<String, Object>` or deserialized using a `Class<T>` or Gson `Type` token.

Request options are sent only when set, preserving the API's defaults. URLs must be absolute HTTP or HTTPS URLs with a host. Browser-only fields such as `jsSnippet`, `cssSelector`, and `waitForSelector` require `browser(true)`.

## Spring Boot example

Add the starter dependency, then configure the key through an environment variable:

```properties
scrapingant.api-key=${SCRAPINGANT_API_KEY}
```

The starter creates a `ScrapingAntClient` bean when `scrapingant.api-key` is configured. A minimal runnable app can use a `CommandLineRunner`:

```java
import com.gapplabs.ScrapingAntClient;
import com.gapplabs.dto.ScrapingAntRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }

    @Bean
    CommandLineRunner scrapeExample(ScrapingAntClient client) {
        return args -> {
            var request = ScrapingAntRequest.builder().url("https://example.com").build();
            System.out.println(client.executeGeneral(request));
        };
    }
}
```

### Spring configuration

| Property | Type | Default | Validation / meaning |
|---|---|---|---|
| `scrapingant.api-key` | String | none | Required to auto-configure the client; keep it in an environment variable or secret store. |
| `scrapingant.connect-timeout` | Duration | `5s` | Positive duration up to the Feign millisecond limit. |
| `scrapingant.read-timeout` | Duration | `60s` | Positive duration up to the Feign millisecond limit. |

Example overrides:

```properties
scrapingant.connect-timeout=3s
scrapingant.read-timeout=20s
```

These settings are also available through `ScrapingAntClientOptions.builder()` in plain Java.

## HTTP behavior and errors

Connection and read timeouts default to 5 and 60 seconds. The SDK does not automatically retry failed requests. HTTP errors expose their status code and, for rate limits, a parsed `Retry-After` duration when provided. Callers decide whether to issue another request, taking their own billing and application semantics into account.

All SDK failures derive from `ScrapingAntException`:

| Exception | Meaning | Metadata |
|---|---|---|
| `ScrapingAntHttpException` | Non-success HTTP response | `getStatusCode()`, optional `getRetryAfter()` duration |
| `ScrapingAntNetworkException` | Transport failure without a usable response | Original cause |
| `ScrapingAntResponseException` | Empty, JSON null, or malformed typed response | Original cause for parse failures |

`Retry-After` accepts seconds or an RFC 1123 date. Invalid or missing values produce `null`. `executeGeneral` returns the response body as-is, including an empty body. Typed JSON operations reject empty bodies and JSON `null` with `ScrapingAntResponseException`.

```java
try {
    client.executeGeneral(request);
} catch (ScrapingAntHttpException e) {
    if (e.getStatusCode() == 429 && e.getRetryAfter() != null) {
        // Decide at the application boundary whether and when to retry.
    }
} catch (ScrapingAntNetworkException | ScrapingAntResponseException e) {
    // Handle transport and response failures separately.
}
```

The API key and request cookies/JavaScript are excluded from generated string representations. Avoid logging raw request query maps or raw response bodies, which may contain user data.

## Build and test

```shell
mvn -B -ntp clean verify
```

CI tests Java 21 and 25. Core integration tests use a local JDK HTTP server and the actual Feign client; no external service or API key is needed.

## Releases and license

Versions follow semantic versioning after the first stable release. Release notes are maintained in [CHANGELOG.md](CHANGELOG.md). Maven Central staging is prepared through the manual [release workflow](.github/workflows/release.yml) and opt-in `central-publish` Maven profile. It requires Central Portal credentials and GPG signing; staged artifacts still require review and publication in Central Portal. See [RELEASING.md](RELEASING.md).

The repository currently uses GPL-3.0. The license choice has not been changed; review it before adopting or distributing the SDK. See [LICENSE](LICENSE).
