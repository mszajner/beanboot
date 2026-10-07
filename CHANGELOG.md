# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). While the version is `0.x`, minor releases may contain
breaking changes.

## [Unreleased]

## [0.1.2] - 2026-10-07

### Changed
- The starter's built-in `AuditLogActionRegistry` and `AuditLogObjectTypeRegistry` are now registered as
  `@ConditionalOnMissingBean` defaults, so an application can supply its own registries to extend the audit log
  actions and object types without a bean conflict.
- **Breaking:** `BeanbootLicenceConfiguration` gained `getServerUrl()`, `getDecryptorPublicKey()` and
  `getDecryptorSecretKey()`. The BeanGuard server config is no longer read from the `beanguard.server.url` /
  `beanguard.decryptor.*` properties by the library; the consuming application supplies it through this interface.
- README: added Maven Central, GitHub release, license and Java version badges.

## [0.1.1] - 2026-10-06

### Changed
- Dependency updates (Spring Boot 4.1.1, springdoc-openapi, commons-lang3, maven-source-plugin).
- The release workflow now builds with the Maven Wrapper (Maven 3.9.12) to get a valid Maven Central bundle.

## [0.1.0] - 2026-10-02

### Added
- Initial public release: starter (users/groups), security (JWT, role-based authorization), audit log, background
  tasks, distributed scheduler, dynamic parameters, data migrations and an optional BeanGuard licence module.
- The licence module is opt-in: set `beanboot.licence.enabled=true` to enable it.

[Unreleased]: https://github.com/mszajner/beanboot/compare/v0.1.2...HEAD
[0.1.2]: https://github.com/mszajner/beanboot/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/mszajner/beanboot/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/mszajner/beanboot/releases/tag/v0.1.0
