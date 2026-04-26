# scrapingant4j

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=bugs)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=coverage)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)

`scrapingant4j` is a lightweight Java SDK for the ScrapingAnt API with support for both plain Java and Spring Boot applications.

Supported ScrapingAnt endpoints:

- `general`
- `extended`
- `markdown`
- `extract`

## Plain Java

```java
import com.gapplabs.ScrapingAntClient;
import com.gapplabs.ScrapingAntClientOptions;
import com.gapplabs.dto.ScrapingAntRequest;

ScrapingAntClient client = new ScrapingAntClient(
        ScrapingAntClientOptions.builder()
                .apiKey("your-api-key")
                .build()
);

ScrapingAntRequest request = ScrapingAntRequest.builder()
        .url("https://example.com")
        .build();

String html = client.executeGeneral(request);
```

You can also override the API host or version:

```java
ScrapingAntClient client = new ScrapingAntClient(
        ScrapingAntClientOptions.builder()
                .apiKey("your-api-key")
                .endpoint("https://api.scrapingant.com")
                .apiVersion("v2")
                .build()
);
```

## Spring Boot

Add your API key:

```properties
scrapingant.api-key=your-api-key
```

Optional overrides:

```properties
scrapingant.endpoint=https://api.scrapingant.com
scrapingant.api-version=v2
```

Then inject the client:

```java
import com.gapplabs.ScrapingAntClient;
import org.springframework.stereotype.Service;

@Service
public class ScrapingService {

    private final ScrapingAntClient scrapingAntClient;

    public ScrapingService(ScrapingAntClient scrapingAntClient) {
        this.scrapingAntClient = scrapingAntClient;
    }
}
```

## Request behavior

- Only explicitly supplied request options are serialized.
- If you omit optional fields, ScrapingAnt API defaults are preserved.
- Browser-only options are rejected when `browser=false`.
- `timeout` must be between `5` and `60` seconds when provided.
