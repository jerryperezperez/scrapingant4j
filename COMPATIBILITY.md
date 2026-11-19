# Compatibility

| Component | Supported baseline | CI coverage |
|---|---|---|
| Java | Java 21 or newer | Java 21 and Java 25 |
| Maven | 3.9 or newer to build | Maven wrapper is not currently included |
| Spring Boot starter | Spring Boot 4.1.x dependency line managed by the parent POM | Included in both Java CI jobs |
| Core SDK | No Spring compile or runtime dependency | Enforced by Maven Enforcer |

The core module depends on Java 21 language/runtime features. The Spring Boot starter uses the Spring Boot version managed in the root POM; consumers should align their application with that supported line. CI validates the declared Java versions against the currently managed Spring Boot line.

## API compatibility

The project is currently at `1.0.0-SNAPSHOT`, so these APIs are still subject to change before the first stable release. After `1.0.0`, incompatible public API changes require a major version. Additive compatible features use a minor version, and compatible fixes use a patch version. See [CHANGELOG.md](CHANGELOG.md) for release notes.
