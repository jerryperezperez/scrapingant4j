# Release procedure

The first stable release should be cut only after API review. The project currently remains `0.1.0-SNAPSHOT`; there are no published releases.

## Prepare a release

1. Update `CHANGELOG.md`, check `COMPATIBILITY.md`, and change any release-specific documentation. The Flatten Maven Plugin resolves `${revision}` in the published POMs.
2. Run `mvn -B -ntp clean verify` on Java 17 and inspect the source/Javadoc JARs.
3. Create a `central-release` GitHub Environment with required reviewers, and configure the secrets `CENTRAL_USERNAME`, `CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, and `GPG_PASSPHRASE` on it. Use a Central Portal user token for the Central credentials. The release job only runs on `main` and pauses for that environment's approval before using the credentials.
4. Run the **Stage Maven Central release** workflow with a stable `x.y.z` version. The workflow validates the version, signs all artifacts, and stages the deployment in Central Portal. The Maven profile has `autoPublish=false`; inspect and publish the staged deployment in the portal.
5. After Central accepts the artifacts, create a matching `vX.Y.Z` Git tag and GitHub release with the changelog notes.

The workflow is manual so a tag or ordinary build cannot publish artifacts automatically. Do not put credentials or signing keys in the repository, Maven POM, or workflow logs.

The repository is licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE).
