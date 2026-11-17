[![Quality gate](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=coverage)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)

# scrapingant4j

A lightweight Java client for the [ScrapingAnt](https://scrapingant.com) web scraping API. It handles request building, validation and response parsing, so you don't have to deal with raw HTTP calls.

> Unofficial client, not affiliated with ScrapingAnt.

## Features

- Four endpoints: `general` (raw HTML), `markdown`, `extended` (HTML, headers, XHRs, iframes) and `extract` (AI data extraction)
- Typed request builder with validation of invalid option combinations
- Typed responses for `markdown` and `extended`
- Spring Boot starter with auto-configuration

## Requirements

- Java 21 or later

## Installation

> **Status:** not yet published to Maven Central. Until the first release, build locally with `mvn install` and depend on the version you installed (`1.0.0-SNAPSHOT` currently).

| Use case | Artifact |
|---|---|
| Plain Java | `com.gapplabs:scrapingant4j-core` |
| Spring Boot | `com.gapplabs:scrapingant4j-spring-boot-starter` |

``` xml
<dependency>
    <groupId>com.gapplabs</groupId>
    <artifactId>scrapingant4j-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick start (plain Java)

``` java
import com.gapplabs.ScrapingAntClient;
import com.gapplabs.dto.ScrapingAntRequest;

ScrapingAntClient client = new ScrapingAntClient("your-api-key");

ScrapingAntRequest request = ScrapingAntRequest.builder()
        .url("https://example.com")
        .build();

String html = client.executeGeneral(request);
```

Load the API key from an environment variable or a secret store. Never hardcode or commit it.

### Other endpoints

These examples use the `client` and `request` from the quick start.

``` java
import com.gapplabs.dto.responses.ExtendedResponse;
import com.gapplabs.dto.responses.MarkdownResponse;

import java.util.List;
import java.util.Map;

MarkdownResponse md = client.executeMarkdown(request);
ExtendedResponse extended = client.executeExtended(request);

Map<String, Object> data =
        client.executeExtract(request, List.of("title", "price"));
```

### Request options

Build a request with browser-specific options like this:

``` java
import com.gapplabs.constants.BlockResource;
import com.gapplabs.constants.ProxyCountry;
import com.gapplabs.constants.ProxyType;
import com.gapplabs.dto.ScrapingAntRequest;

import java.util.List;

ScrapingAntRequest request = ScrapingAntRequest.builder()
        .url("https://example.com")
        .browser(true)
        .proxyType(ProxyType.RESIDENTIAL)
        .proxyCountry(ProxyCountry.GERMANY)
        .waitForSelector(".product")
        .blockResources(List.of(BlockResource.IMAGE, BlockResource.FONT))
        .timeout(30)
        .build();
```

| Option | Notes |
|---|---|
| `url` | Required |
| `browser` | Defaults to the API's default when omitted |
| `proxyType`, `proxyCountry` | Proxy selection |
| `cookies` | Sent as provided |
| `jsSnippet` | Base64-encoded for you; only allowed when `browser=true` |
| `waitForSelector`, `cssSelector` | Only allowed when `browser=true` |
| `returnPageSource` | `true` requires `browser=true`; `false` is allowed when `browser=false` |
| `blockResources` | A non-empty list requires `browser=true`; an empty list is allowed when `browser=false` |
| `timeout` | Only allowed when `browser=true`; must be 5 to 60 seconds |

Only options you set explicitly are sent, so ScrapingAnt's own defaults apply to everything else.

## Error handling

HTTP errors from the API throw `ScrapingAntException`, which carries the status code and the API's `detail` message when provided. If there is no usable `detail`, the message falls back to `HTTP <status>`.

The example uses the `client` and `request` from the quick start.

``` java
import com.gapplabs.exceptions.ScrapingAntException;

try {
    client.executeGeneral(request);
} catch (ScrapingAntException e) {
    int status = e.getStatusCode();
    // handle, e.g. 429 (rate limit) or 4xx/5xx
}
```

Invalid requests throw `IllegalArgumentException` before any network call is made.

## Spring Boot

``` xml
<dependency>
    <groupId>com.gapplabs</groupId>
    <artifactId>scrapingant4j-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```properties
scrapingant.api-key=${SCRAPINGANT_API_KEY}
```

A `ScrapingAntClient` bean is created when `scrapingant.api-key` is set. Define your own `ScrapingAntClient` bean to override it.

``` java
import com.gapplabs.ScrapingAntClient;
import org.springframework.stereotype.Service;

@Service
public class ScrapingService {
    private final ScrapingAntClient client;

    public ScrapingService(ScrapingAntClient client) {
        this.client = client;
    }
}
```

## Building

``` bash
mvn -B -ntp clean verify
```

## Contributing

Issues and pull requests are welcome. See `AGENTS.md` for repository conventions.

## License

GPL-3.0. See [LICENSE](LICENSE).
