# Changelog

Notable changes to this project are recorded here. Versions follow semantic versioning once stable releases begin.

## Unreleased

- Validate request and client URLs; defensively copy request collections and hide secrets from `toString()` output.
- Add configurable HTTP timeouts and typed HTTP/network/response exceptions; expose Retry-After metadata for caller-managed retries.
- Expose parsed `Retry-After` metadata and support typed AI extraction results.
- Add real HTTP-boundary tests, Java 17/21 CI coverage, Spring configuration documentation, and source/Javadoc artifacts.
- Prepare an opt-in Maven Central publishing profile. No release has been published.
- Lower the Java baseline to 17, bind Spotless and Checkstyle to `verify`, enable Javadoc doclint checks (excluding missing-tag warnings), and remove hardcoded versions from active build plugins.
- Increase the default read timeout to 120 seconds, preserve integral extract numbers as `Long`, and drop the unused Feign SLF4J logger.
- Pin Google Java Format to 1.28.0 so Spotless output is identical on JDK 17 and newer.
- Fail the build immediately on Checkstyle violations and errors.
