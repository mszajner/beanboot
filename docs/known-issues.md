---
title: Known issues and gotchas
nav_order: 7
description: Gaps, limitations and pitfalls to know before using beanboot in production.
---

# Known issues and gotchas
{: .no_toc }

This page lists the gaps, limitations and traps that you will meet when building on beanboot `0.1.x`. They come from
reading the library's source code; each entry says what happens, why it matters and what to do about it. Items are
grouped by how likely they are to hurt you.

{: .note }
> beanboot is pre-release. Several of these are bugs that may be fixed in a later version — check the
> [changelog](https://github.com/mszajner/beanboot/blob/main/CHANGELOG.md) and the
> [issue tracker](https://github.com/mszajner/beanboot/issues).

1. TOC
{:toc}

## Summary

| # | Issue | Severity |
|---|---|---|
| 1 | [Your audit registry replaces the starter's](#your-audit-log-registry-replaces-the-starters) | High |
| 2 | [Spring `DataSource` + unset `db.connection` forces the H2 dialect](#unset-dbconnection-forces-the-h2-hibernate-dialect) | High |
| 3 | [beanboot's Liquibase bean disables Spring Boot's Liquibase](#beanboots-liquibase-bean-disables-spring-boots-liquibase) | High |
| 4 | [Every `Instant` in your entities becomes a `BIGINT`](#every-instant-in-your-entities-becomes-a-bigint) | High |
| 5 | [Everything outside `/api/**` is public](#everything-outside-api-is-public) | High |
| 6 | [Any authenticated user can list users, audit logs and tasks](#any-authenticated-user-can-list-users-audit-logs-and-tasks) | High |
| 7 | [No first user, no password management](#no-first-user-no-password-management) | High |
| 8 | [Failed migrations are skipped silently and never retried](#failed-migrations-are-skipped-silently-and-never-retried) | Medium |
| 9 | [Token model: no revocation, stale roles](#token-model-no-revocation-stale-roles) | Medium |
| 10 | [The jar's `application.yml` is shadowed by yours](#the-jars-applicationyml-is-shadowed-by-yours) | Medium |
| 11 | [The setup wizard has no pages](#the-setup-wizard-has-no-pages) | Medium |
| 12 | [Audit log actor name may be empty](#audit-log-actor-name-may-be-empty) | Medium |
| 13 | [Login hardening is missing](#login-hardening-is-missing) | Medium |
| 14 | [`/api/licence/**` is unauthenticated](#apilicence-is-unauthenticated) | Medium |
| 15 | [Hard-coded Polish texts](#hard-coded-polish-texts) | Low |
| 16 | [Transitive dependencies you did not ask for](#transitive-dependencies-you-did-not-ask-for) | Low |
| 17 | [Smaller API inconsistencies](#smaller-api-inconsistencies) | Low |
| 18 | [Missing features](#missing-features) | – |

## High impact

### Your audit-log registry replaces the starter's

The starter registers default `AuditLogActionRegistry` / `AuditLogObjectTypeRegistry` beans with
`@ConditionalOnMissingBean`. A registry bean of your own **replaces** them. If yours does not also return the starter's
values (`USER_LOGGED_IN`, `USER_CREATED`, …, object types `USER`, `GROUP`), then writes still succeed but reading
entries back — and any JSON/`@RequestParam` conversion of those names — fails.

*Do:* concatenate the starter values in `values()` and fall back to them in `valueOf()`
([example]({% link extension-points.md %}#audit-log-registries)). *Note:* older documentation said the application
**must** provide this registry; since the defaults exist it is only required if you add your own actions.

### Unset `db.connection` forces the H2 Hibernate dialect

`DatabaseProperties.connection` defaults to `H2`. A `HibernatePropertiesCustomizer` sets
`hibernate.dialect=org.hibernate.dialect.H2Dialect` whenever it is `H2` — **even if you did not set `db.connection`
and configured a PostgreSQL/MySQL/… `DataSource` through `spring.datasource.*`.**

*Do:* always set `db.connection` to your real engine (see [Configuration]({% link configuration.md %}#database-db)).

### beanboot's Liquibase bean disables Spring Boot's Liquibase

beanboot declares its own `SpringLiquibase`, so Spring Boot's auto-configuration backs off: `spring.liquibase.*` is
ignored and your own default change log (`db/changelog/db.changelog-master.yaml`) is no longer run.

*Do:* declare your own `SpringLiquibase` bean for your change log, use a different path than `db/changelog.yml`, and use
unique change-set ids. Details in [Database]({% link database.md %}#schema-management-with-liquibase).

### Every `Instant` in your entities becomes a `BIGINT`

`InstantConverter` is registered with `autoApply = true`, so **all** `Instant` attributes of **all** JPA entities in
your application (not only beanboot's) are persisted as epoch milliseconds in a numeric column. The same applies to
`Map<String,String>` attributes (stored as JSON text, via `StringMapConverter`) and `Set<Role>` attributes.

If you declare your own `Instant` columns as `TIMESTAMP`, reads and writes fail or misbehave. You cannot use native
date/time functions or indexes on date ranges in the usual way, and sub-millisecond precision is lost.

*Do:* declare your own `Instant` columns as `BIGINT`, or use another type (`LocalDateTime`, `OffsetDateTime`, `Date`)
in your entities. If you need `Instant` with a real timestamp column, annotate the attribute with
`@Convert(disableConversion = true)`.

### Everything outside `/api/**` is public

The security chain is "`/api/**` requires authentication, **anything else is `permitAll`**". Your own controllers under,
say, `/admin/**` or `/reports/**`, actuator endpoints, `/swagger-ui.html` and `/v3/api-docs` are therefore open.

*Do:* put your REST endpoints under `/api/`, add your own `SecurityFilterChain` (with `@Order` lower than `1`), and
disable springdoc in production ([Configuration]({% link configuration.md %}#springdoc--swagger)). CORS also allows
**every origin** (with credentials) and cannot be restricted by a property.

### Any authenticated user can list users, audit logs and tasks

`GET /api/users`, `GET /api/groups`, `GET /api/audit-logs`, `GET /api/tasks` (and the registries) are `@PermitAll`,
which means *"authenticated"*, not *"admin"*. A least-privileged account sees every user (name, email, roles, groups) and
the full audit trail.

*Do:* if that is not acceptable, front these endpoints with your own controllers/proxy rules, or restrict
`/api/users`, `/api/groups`, `/api/audit-logs` at the gateway.

### No first user, no password management

There is no default administrator, so a fresh installation cannot be logged in to; the first user must be created by a
[data migration]({% link modules/migrations.md %}) (or directly in the database). There is **no** self-registration,
password reset, "change password" for the current user, e-mail verification, password strength rules, account lockout or
MFA. Only an administrator can change a user's password (`PUT /api/users/{id}`).

*Do:* build these features in your application on top of `UserRepository` / `PasswordEncoder` (or your own endpoints).

## Medium impact

### Failed migrations are skipped silently and never retried

A migration that throws is logged and recorded as `FAILURE`, startup **continues**, and since only tasks without a row run,
the migration is **not retried** on the next start (unless `always()`). Rows are committed at the end of the run, so a crash
mid-run re-runs already completed tasks. Not cluster-safe on first start.

*Do:* make migrations idempotent, monitor the log / `migrations` table, delete the `FAILURE` row to retry.
See [Data migrations]({% link modules/migrations.md %}).

### Token model: no revocation, stale roles

Tokens carry the user's effective roles **as of login**. There is no logout, refresh or revocation, deleting a user or
changing roles does not affect issued tokens until they expire, and key rotation invalidates all tokens at once. The
RSA/AES keys are generated lazily at startup and stored unencrypted in the `parameters` table; two nodes starting
simultaneously on an empty database can generate separate keys (last writer wins in the database, each node keeps its own
in memory) until restarted.

*Do:* use a short `TOKEN_EXPIRATION` appropriate for your risk (it is in **milliseconds**), start one node first on a
fresh database, and protect the `parameters` table. See [Security]({% link modules/security.md %}).

### The jar's `application.yml` is shadowed by yours

The library ships an `application.yml` (with `ddl-auto: none`, `open-in-view: false`, `show_sql: false` and the
`via-dto` page serialization). A file with the same name in your application takes precedence and the library's is **not
merged** — those settings silently disappear. The page serialization mode then changes the JSON of every paged endpoint.

*Do:* copy the properties into your own configuration ([Configuration]({% link configuration.md %}#defaults-shipped-inside-the-jar)).

### The setup wizard has no pages

`StarterApplication.runApplication(…)` starts `SetupApplication` when the external config file is missing, but the jar
contains no `setup/index` / `setup/complete` templates and no template engine. As published, the wizard cannot render.
It also has no authentication and writes the DB password in clear text.

*Do:* don't use `StarterApplication` yet unless you provide the templates; create the config file in your installer.
See [Setup wizard]({% link modules/setup.md %}).

### Audit log actor name may be empty

`AuditLogService` takes the actor's *name* from `Authentication.getDetails()` when it is an `AuditActor` or
`UserDetails`. The token filter sets `WebAuthenticationDetails` as details and the principal is a plain Spring Security
`User`, which neither condition matches. From reading the code, `userName` is therefore likely **`null`** for entries
written during a token-authenticated request (the `userId` is set). `UUID.fromString(authentication.getName())` is also
called for any authenticated principal — calling `log(…)` from an endpoint that is reached with Spring's anonymous
authentication would fail to parse `anonymousUser`.

*Do:* verify with your own application after the first entries are written; if you rely on `userName`, resolve the name
from `userId` at read time, and don't log audit events from unauthenticated endpoints.

### Login hardening is missing

No rate limiting, no lockout, and failed logins are not audited. Unknown e-mail addresses and wrong passwords are
likely answered differently (a `ProblemDetail` `Unauthenticated` vs Spring Security's default 401), which allows user
enumeration. When licensing is enabled and invalid, `/auth/login` returns `402`.

*Do:* add rate limiting at your gateway or a servlet filter in front of `/auth/login`.

### `/api/licence/**` is unauthenticated

Even `set-key` and `get-demo`. See [Licence]({% link modules/licence.md %}).

## Low impact

### Hard-coded Polish texts

* `friendlyName()` of the starter's audit actions (`Zalogowanie użytkownika`, …), object types (`Użytkownik`, `Grupa`),
  task statuses (`Oczekujące`, `W toku`, `Zakończone`, `Błąd`) and the default task action (`Nieznane`).
* The `403` body of the token-check interceptor (`Brak wystarczających uprawnień`) and the setup wizard's database
  type error (`Nieznany typ bazy danych`).
* Code comments and some log messages.

There is no i18n. Translate on the client using the `name` keys, and supply your own friendly names for your own enums.

### Transitive dependencies you did not ask for

`spring-boot-starter-web`, `-security`, `-data-jpa`, `-liquibase`, `-aspectj`, springdoc (UI included), `beanguard-client`,
MapStruct and commons-lang3 all come transitively. Servlet only (no WebFlux). If you use another JSON mapper, security
setup, or Liquibase configuration, expect conflicts. Exclude artifacts you don't need *only if* you're sure the library
doesn't use them; beanboot is not designed to run without them.

### Smaller API inconsistencies

* `GroupCreate` uses `role` (singular); `Group` uses `roles`.
* Changing a user's e-mail to an existing one is not checked in the service; the database constraint produces an
  unmapped error (typically `500`) instead of `EmailAlreadyExists`.
* `UserProviderImpl` merges group roles without a null check; a group persisted with no roles can break the login of its
  members.
* A task handler's `execute` signature and the `TaskEntity` type are in internal packages; the public `api` has no
  handler interface.
* Handled exceptions (`AbstractException`) are logged at **ERROR** including normal 404/409 responses.
* A failed login attempt does not write an audit entry (no `USER_LOGIN_FAILED` action exists).
* `InstantProvider` exists but the library's own code does not use it.
* Table names are unprefixed (`users`, `groups`, `tasks`, `parameters`, …).
* `Jackson` is globally configured with `FAIL_ON_NULL_FOR_PRIMITIVES` disabled.
* The DB `DataSource` has no pool tuning and for MSSQL hard-codes `encrypt=true;trustServerCertificate=true`.
* `SetupApplication` and `StarterApplication` are in the same jar and package tree as the auto-configurations.

## Missing features

Things people often expect from a "starter" that beanboot does **not** provide today:

* user self-service (registration, password reset, profile), MFA, social/OIDC login;
* token refresh and logout/revocation;
* fine-grained permissions (only roles; "admin" is a flag on a role);
* tenant/organization model;
* REST endpoints for parameters, creating tasks, or managing the scheduler;
* retention/cleanup for audit logs, tasks, scheduler instances;
* internationalization;
* metrics, health indicators or Actuator integration for tasks/scheduler/licence;
* a published upgrade guide for database changes between versions.

If one of these is blocking you, please open an issue.
