# Changelog

Notable changes to this project are recorded here. Versions follow semantic versioning once stable releases begin.

## Unreleased

- Validate request and client URLs; defensively copy request collections and hide secrets from `toString()` output.
- Add configurable HTTP timeouts and typed HTTP/network/response exceptions; expose Retry-After metadata for caller-managed retries.
- Expose parsed `Retry-After` metadata and support typed AI extraction results.
- Add real HTTP-boundary tests, Java 21/25 CI coverage, Spring configuration documentation, and source/Javadoc artifacts.
- Prepare an opt-in Maven Central publishing profile. No release has been published.
