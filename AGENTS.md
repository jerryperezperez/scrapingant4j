# AGENTS.md

Java 17 Maven multi-module SDK for the ScrapingAnt API. Core uses Feign and Gson. The parent POM manages shared dependency versions; the Spring Boot starter imports the Spring Boot BOM.

## Modules

- `scrapingant4j-core`: plain Java client (request/response models, Feign API access, client options). It must not have Spring on its compile or runtime classpath; Maven Enforcer fails the build if it does.
- `scrapingant4j-spring-boot-starter`: Spring Boot integration (properties, auto-configuration, starter tests). Depends on core. Never move Spring code into core.

## Compatibility rules

Preserve public behavior unless the task explicitly changes the API:
endpoint names, query parameter names, omitted-option behavior (only explicitly set options are sent), response JSON mappings, and validation messages.

## Security rules

- Never hardcode, log, or commit API keys. This includes scratch files such as a `Main.java` with a real key.
- Do not mutate a caller-supplied `Feign.Builder`. Configure what the client needs without changing a builder that other clients may share.
- Keep secrets (API key, cookies) out of `toString()`, logs and exception messages.
- Do not commit `target/`, `.idea/` or other generated output.

## Code conventions

- Prefer not to expose Feign types in new public API.
- Every new request option needs validation, a `toQueryMap()` mapping, and tests.
- Use the existing Lombok, Gson and Google Java Format conventions; do not add parallel tooling.
- Spotless and Checkstyle are bound to `verify`. Run `mvn spotless:apply` before committing.

## Testing

Add or update tests in the module that owns the changed behavior. Most current tests mock `ScrapingAntApi`; for changes to HTTP behavior (headers, query encoding, timeouts, error mapping), prefer a test that exercises the real Feign stack against a local mock server.

## Commands

- Core: `mvn -B -ntp -pl scrapingant4j-core -am test`
- Starter: `mvn -B -ntp -pl scrapingant4j-spring-boot-starter -am test`
- Full build (same as CI): `mvn -B -ntp clean verify`

CI runs the full build, then SonarCloud analysis with JaCoCo coverage. Do not run `sonar:sonar` locally.
