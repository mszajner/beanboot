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
artifacts locally without signing: `mvn -Prelease -Dgpg.skip=true clean package`. Full procedure (including
`CHANGELOG.md`): see "Releasing a new version" below.

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

## Releasing a new version

Versioning follows [SemVer](https://semver.org/); while the version is `0.x`, minor bumps may be breaking. The
`pom.xml` version on `main` is normally `<next>-SNAPSHOT`; the tag `v<version>` is what triggers the release. Git
remote is called `github` (not `origin`). Do not push, tag, or publish without the user's explicit confirmation.

Steps (let `X.Y.Z` be the version being released):

1. Make sure you are on an up-to-date `main` with a clean working tree and `mvn test` is green.
2. Update `CHANGELOG.md` ([Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format):
   - Review what changed since the previous tag: `git log --oneline vPREV..HEAD`.
   - Make sure `## [Unreleased]` lists user-visible changes grouped under `### Added` / `Changed` / `Deprecated` /
     `Removed` / `Fixed` / `Security`. Call out breaking changes explicitly.
   - Rename `## [Unreleased]` to `## [X.Y.Z] - YYYY-MM-DD` and add a fresh empty `## [Unreleased]` above it.
3. Set the release version in `pom.xml` (drop `-SNAPSHOT`), e.g. `mvn versions:set -DnewVersion=X.Y.Z
   -DgenerateBackupPoms=false`. Pre-releases use a suffix like `X.Y.Z-rc1` (the workflow marks tags containing `-`
   as GitHub pre-releases).
4. Verify the artifacts locally (no signing): `mvn -Prelease -Dgpg.skip=true clean package`.
5. Commit: `git commit -am "Release X.Y.Z"`.
6. Tag and push (this starts the CI release): `git tag vX.Y.Z && git push github main vX.Y.Z`.
7. CI (`.github/workflows/release.yml`) checks tag == `pom.xml` version and not SNAPSHOT, then builds, signs and
   uploads to Maven Central, and creates the GitHub release (`--generate-notes`). Optionally paste the CHANGELOG
   section into the GitHub release notes.
8. Approve the upload manually in the Central Portal (it is not auto-published).
9. Start the next cycle: set `pom.xml` to the next `-SNAPSHOT` (`mvn versions:set -DnewVersion=X.Y.(Z+1)-SNAPSHOT
   -DgenerateBackupPoms=false`), commit `Prepare next development iteration`, and push `main`.

If the tag/version check fails, delete the tag (`git tag -d vX.Y.Z && git push github :refs/tags/vX.Y.Z`), fix
`pom.xml`, and re-tag. A version already published to Maven Central cannot be overwritten — bump the version instead.

## Adding a new module

Follow the existing module shape: `api/` (public interfaces + annotations consumers implement or use), `config/`
(one `Beanboot<Name>AutoConfiguration` with `@ComponentScan`/`@EntityScan` scoped to the module package), plus
`entities/`, `repositories/`, `services/`, `controllers/`/`converters`/`mappers` as needed. Register the new
`@AutoConfiguration` class in `AutoConfiguration.imports`. Module boundaries are enforced by Spring Modulith (`ModuleStructureTest`, `ApplicationModules.verify()`): put the
public types in `api/` with a `package-info.java` annotated `@NamedInterface("api")`, and declare the module's
dependencies in a root `package-info.java` via `@ApplicationModule(allowedDependencies = {"other::api", ...})`. Any new
cross-module dependency must be added there, otherwise the test fails; cycles between modules are not allowed. If it
needs its own schema, add a Liquibase changelog
under `src/main/resources/db/beanboot/<module>/` and include it from `db/changelog.yml`. If the module introduces
an app-specific extension point, add the `*Registry` interface pattern described above rather than hardcoding enum
values, and update the `webapp` test app to implement it so the module is exercised end-to-end.