# Tasks: Modrinth Auto Publish

## 1. Contract

- [x] 1.1 Define `mod_version` changes on `master` as the automatic publish trigger.
- [x] 1.2 Keep project ID and token outside the repository.
- [x] 1.3 Define idempotent same-hash behavior and refuse different-hash overwrites.

## 2. Implementation

- [x] 2.1 Add the GitHub Actions build-and-publish workflow.
- [x] 2.2 Add a dry-run-capable official Modrinth API publisher.
- [x] 2.3 Validate release notes, JAR metadata, game version and dependency metadata.
- [x] 2.4 Document repository variable, secret and manual retry setup.

## 3. Verification

- [x] 3.1 Shell syntax and workflow structure pass static validation.
- [x] 3.2 Publisher dry run validates the current release JAR.
- [ ] 3.3 Java 25 `./gradlew build --stacktrace` passes.
- [ ] 3.4 Pull request GitHub Actions pass.
- [ ] 3.5 A configured manual dispatch publishes or safely recognizes the current Modrinth version.
