# Contributing to beanboot

Thanks for your interest! Bug reports, ideas and pull requests are welcome.

## Getting started

Requirements: JDK 21 and Maven 3.9+. Docker is needed only to run the sample application
(`src/test/java/io/github/mszajner/beanboot/webapp/WebApplication.java`), which starts PostgreSQL with Testcontainers.

```
mvn verify          # compile, run the unit tests and package
mvn test -Dtest=KeyPairProviderImplTest   # a single test class
```

## Project layout

Each feature is a module under `src/main/java/io/github/mszajner/beanboot/<module>/` wired in as a Spring Boot
`@AutoConfiguration` and registered in
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
`CLAUDE.md` contains a longer description of the architecture and the "registry" extension pattern.

To add a module, follow the existing shape (`api/`, `config/`, `entities/`, `repositories/`, `services/`, ...),
register its auto-configuration, add a Liquibase changelog under `src/main/resources/db/beanboot/<module>/` if it
needs a schema, and extend the sample app so the module is exercised end to end.

## Pull requests

- Open an issue first for larger changes so we can agree on the approach.
- Keep the change focused; unrelated refactoring belongs in a separate PR.
- Add or update tests. Unit tests use JUnit 5, Mockito and AssertJ; tests that need a real database use
  Testcontainers PostgreSQL.
- `mvn verify` must pass.
- Do not commit secrets, real license keys or personal data. Use neutral values such as `example.com`.
- Update `CHANGELOG.md` under `[Unreleased]` for user-visible changes.

By contributing you agree that your contribution is licensed under the Apache License 2.0, and you agree to follow
the [Code of Conduct](CODE_OF_CONDUCT.md).
