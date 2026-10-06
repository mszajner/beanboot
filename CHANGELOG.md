# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to
[Semantic Versioning](https://semver.org/spec/v2.0.0.html). While the version is `0.x`, minor releases may contain
breaking changes.

## [Unreleased]

### Changed
- Dependency updates (Spring Boot 4.1.1, springdoc-openapi, commons-lang3, maven-source-plugin).

## [0.1.0] - 2026-10-02

### Added
- Initial public release: starter (users/groups), security (JWT, role-based authorization), audit log, background
  tasks, distributed scheduler, dynamic parameters, data migrations and an optional BeanGuard licence module.
- The licence module is opt-in: set `beanboot.licence.enabled=true` to enable it.

[Unreleased]: https://github.com/mszajner/beanboot/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/mszajner/beanboot/releases/tag/v0.1.0
