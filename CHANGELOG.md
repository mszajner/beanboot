# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). While the version is `0.x`, minor releases may contain
breaking changes.

## [Unreleased]

### Added
- `beanboot.tasks.heartbeat-interval` (default `30s`) controls the task heartbeat.

### Changed
- Crash recovery of tasks is no longer immediate: after an instance dies, its `RUNNING` tasks return to `PENDING` once
  their heartbeat has been silent for three heartbeat intervals (at startup, tasks that were `RUNNING` without any
  heartbeat — for example rows created before this change — are reset immediately).

### Fixed
- Tasks are now safe to run on several instances: a task is claimed with an atomic conditional update before it is
  executed, so it can no longer run on more than one instance at the same time. Previously every instance executed "the
  oldest `PENDING` task" without claiming it.
- Starting an instance no longer resets `RUNNING` tasks that belong to other, healthy instances. A running task now
  refreshes a heartbeat (`tasks.heartbeat_at`, new column added by Liquibase); only tasks whose heartbeat stopped
  (default: 3 × 30 s) are returned to `PENDING`.
- The task dispatcher now executes all pending tasks in one run instead of one task per wake-up.
- The scheduler heartbeat now honours `beanboot.scheduler.check-in-interval`; it used to read the non-existent
  property `framework.scheduler.check-in-interval`, so the heartbeat always ran every 30 s regardless of the setting.

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
