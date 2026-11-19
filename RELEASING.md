# Release procedure

The first stable release should be cut only after API and license review. The project currently remains `1.0.0-SNAPSHOT`; there are no published releases.

## Prepare a release

1. Update `CHANGELOG.md`, check `COMPATIBILITY.md`, and change any release-specific documentation.
2. Run `mvn -B -ntp clean verify` on Java 17 and inspect the source/Javadoc JARs.
3. Configure GitHub Actions secrets `CENTRAL_USERNAME`, `CENTRAL_PASSWORD`, `GPG_PRIVATE_KEY`, and `GPG_PASSPHRASE`. Use a Central Portal user token for the Central credentials.
4. Run the **Stage Maven Central release** workflow with a stable `x.y.z` version. The workflow validates the version, signs all artifacts, and stages the deployment in Central Portal. The Maven profile has `autoPublish=false`; inspect and publish the staged deployment in the portal.
5. After Central accepts the artifacts, create a matching `vX.Y.Z` Git tag and GitHub release with the changelog notes.

The workflow is manual so a tag or ordinary build cannot publish artifacts automatically. Do not put credentials or signing keys in the repository, Maven POM, or workflow logs.

The repository license is GPL-3.0. Confirm the project's intended reuse terms before its first public release; this repository change does not make that legal decision.
