# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

`beanboot` is a Spring Boot **auto-configuration library** (not a standalone deployable app). It's consumed as a
Maven dependency by Spring Boot applications, which get a ready-made foundation: users/groups, JWT auth with role-based authorization, audit logging, background tasks, distributed
scheduling, dynamic parameters, DB migrations, and license enforcement (via BeanGuard).

Each feature is a self-contained module under `src/main/java/io/github/mszajner/beanboot/<module>/`, wired in as a Spring
Boot `@AutoConfiguration` and registered in
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Modules:

- **starter** — core domain: `User`/`Group` entities, services, controllers. Also the license usage counter
  (`UsageRegistryImpl` reports user count to BeanGuard).
- **security** — JWT-based auth (`jjwt`), `SecurityService`/`UserProvider`, role model, and AOP-driven authorization
  annotations (`@AdminAllowed`, `@AdminRequired`, `Unauthenticated`) enforced via AspectJ aspects, not filters.
- **licence** — *optional, off by default*: enabled with `beanboot.licence.enabled=true`. Integrates
  `dev.beanguard:beanguard-client` for license validation/decryption; `BeanbootBeanGuardConfiguration` feeds it
  keys/license from `ParameterService`. When enabled, `LicenseInterceptor` gates `/api/**` and `/auth/**` (except
  `/api/licence/**`) behind a valid license (402 Payment Required if invalid). When disabled, none of the module's
  beans exist and the `@RequiresValidLicence`/`@RequiresLicenceLimit` annotations in `starter` are inert.
- **parameters** — a DB-backed key/value settings store (`ParameterService`), used e.g. to hold JWT keys and license
  keys at runtime instead of only in `application.yml`.
- **tasks** — async background task execution. `TaskDispatcher` is an `ApplicationRunner` running a daemon thread
  that polls the DB for `PENDING` tasks, woken by a `Semaphore` on `TaskCreatedEvent` (after commit) or every 60s.
- **scheduler** — distributed cron-like locking so only one app instance runs a `@DistributedScheduled` method at a
  time; backed by DB row locking + instance heartbeat (`InstanceHeartbeatService`/`InstanceRegistrarService`).
- **migrations** — a custom one-shot/always-run task runner (`MigrationTask`, `MigrationsRunner`) distinct from
  Liquibase, for data migrations rather than schema migrations. Liquibase itself handles schema, one changelog per
  module under `src/main/resources/db/beanboot/<module>/`, aggregated by `src/main/resources/db/changelog.yml`.
- **auditlog** — records `AuditLogAction`/`AuditLogObjectType` events against entities implementing
  `AuditableObject`.
- **utils** — shared plumbing: `AbstractException` (carries an HTTP status + up to two context objects) +
  `UtilsExceptionHandler` (`@RestControllerAdvice` turning it into a `ProblemDetail`), `DatabaseProperties`
  (multi-DB support: H2/PGSQL/MYSQL/MSSQL/ORACLE), `InstantProvider` (testable clock).
- **setup** — a *separate* minimal `@SpringBootApplication` (`SetupApplication`), intended to run before the main
  app in prod when no `application.properties` exists yet, to drive a first-run DB setup wizard. It explicitly
  excludes all DB/JPA/Liquibase/Security/beanboot auto-configurations so it can start without a working DB
  connection.

### The registry extension pattern

Several modules define an enum-like Java interface plus a `*Registry` interface that the *consuming application*
implements to plug its own values into the library, e.g. `Role`/`RoleRegistry`, `TaskAction`/`TaskActionRegistry`,
`ParameterName`, `AuditLogAction`/`AuditLogActionRegistry`, `AuditLogObjectType`/`AuditLogObjectTypeRegistry`. This
is how the library stays generic while still supporting Jackson (de)serialization, JPA attribute conversion, and
`@RequestParam` conversion for app-specific values — look for the matching `*Converter`/`*Deserializer` pair next to
each registry to see how a value round-trips through JSON and the DB.

### AOP-based cross-cutting concerns

Authorization (`security/aspects`) and distributed locking (`scheduler/aspects`) are both implemented as AspectJ
`@Around`/`@Before` advice on marker annotations, not servlet filters or interceptors. `LicenseInterceptor` is the
one exception — it's a `HandlerInterceptor` gating at the HTTP layer since it must reject *before* any business
logic runs.

## The test app (`src/test/java/io/github/mszajner/beanboot/webapp`)

Because this is a library, there's no runnable app in `main`. `webapp.WebApplication` is a full sample Spring Boot
app under `src/test` that wires every auto-configuration together (`@EnableAutoConfiguration`), starts a real
Postgres via Testcontainers on boot, and implements the registries above with dummy/demo values
(`webapp.registries`, `webapp.config`, `webapp.migrations`). Use it as the reference for how a downstream app is
expected to integrate this library. It is not part of the automated test suite itself (no `*Test`/`*AcceptanceSpec`
name) — it's a manually-runnable harness plus a compile-time check that all modules cooperate.

## Commands

Build/compile:
```
mvn compile
```

Run the full test suite:
```
mvn test
```

Run a single test class:
```
mvn test -Dtest=KeyPairProviderImplTest
```

Run a single test method:
```
mvn test -Dtest=KeyPairProviderImplTest#methodName
```

Package (skip tests):
```
mvn package -DskipTests
```

Release (CI only): push a `v<version>` tag whose version matches `pom.xml` (not a SNAPSHOT); the GitHub Actions
workflow `.github/workflows/release.yml` builds, signs (GPG) and uploads to Maven Central with
`mvn -Prelease deploy`. The upload is not auto-published — approve it in the Central Portal. To check the release
artifacts locally without signing: `mvn -Prelease -Dgpg.skip=true clean package`.

Tests matched by Surefire follow `**/*Test.*` and `**/*AcceptanceSpec.*` (see `pom.xml`); Spock (`spock-core`,
`gmavenplus-plugin`) is on the test classpath for `*Spec` groovy specs, but none currently exist in this repo — all
current tests are plain JUnit 5 + Mockito (`*Test.java`).

### Test patterns to follow

- Unit tests: `@ExtendWith(MockitoExtension.class)`, mock collaborators with `@Mock`, assert with AssertJ.
- Tests needing a real DB (repository/integration tests) use Testcontainers Postgres via
  `DatabaseContainerInitializer` (an `ApplicationContextInitializer`), not an embedded/H2 DB — this matches
  production, which targets real RDBMSs per `DatabaseProperties.Connection`.
- `*MvcTest` classes exercise the web layer (`MockMvc`) separately from `*Test` classes that test the
  service/controller logic directly — see the `licence.controllers` package for the paired pattern
  (`LicenceControllerTest` vs `LicenceControllerMvcTest`).

## Adding a new module

Follow the existing module shape: `api/` (public interfaces + annotations consumers implement or use), `config/`
(one `Beanboot<Name>AutoConfiguration` with `@ComponentScan`/`@EntityScan` scoped to the module package), plus
`entities/`, `repositories/`, `services/`, `controllers/`/`converters`/`mappers` as needed. Register the new
`@AutoConfiguration` class in `AutoConfiguration.imports`. If it needs its own schema, add a Liquibase changelog
under `src/main/resources/db/beanboot/<module>/` and include it from `db/changelog.yml`. If the module introduces
an app-specific extension point, add the `*Registry` interface pattern described above rather than hardcoding enum
values, and update the `webapp` test app to implement it so the module is exercised end-to-end.