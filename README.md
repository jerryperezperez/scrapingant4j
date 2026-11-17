[![Quality gate status](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=coverage)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Maintainability issues](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=software_quality_maintainability_issues)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j) [![Security issues](https://sonarcloud.io/api/project_badges/measure?project=jerryperezperez_scrapingant4j&metric=software_quality_security_issues)](https://sonarcloud.io/summary/new_code?id=jerryperezperez_scrapingant4j)

# scrapingant4j

`scrapingant4j` is now a multi-module Maven project with separate runtime and Spring Boot artifacts.

Modules:

- `com.gapplabs:scrapingant4j-core`
- `com.gapplabs:scrapingant4j-spring-boot-starter`

Supported ScrapingAnt endpoints:

- `general`
- `extended`
- `markdown`
- `extract`

## Migration

- Plain Java consumers should depend on `scrapingant4j-core`
- Spring Boot consumers should depend on `scrapingant4j-spring-boot-starter`
- The repository root is now a parent/aggregator POM and no longer produces a runtime JAR

## Plain Java

```xml
<dependency>
    <groupId>com.gapplabs</groupId>
    <artifactId>scrapingant4j-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

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

## Spring Boot

```xml
<dependency>
    <groupId>com.gapplabs</groupId>
    <artifactId>scrapingant4j-spring-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

```properties
scrapingant.api-key=your-api-key
```

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

- Only explicitly supplied request options are serialized
- If optional fields are omitted, ScrapingAnt API defaults are preserved
- Browser-only options are rejected when `browser=false`
- `timeout` must be between `5` and `60` seconds when provided
