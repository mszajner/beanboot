# beanboot

[![Build](https://github.com/mszajner/beanboot/actions/workflows/build.yml/badge.svg)](https://github.com/mszajner/beanboot/actions/workflows/build.yml)

A Spring Boot **auto-configuration library** that gives your application a ready-made foundation, so you can start
on the domain logic instead of re-implementing the same plumbing every time:

- users and groups with REST endpoints,
- JWT authentication with role-based authorization,
- audit log,
- background tasks and distributed scheduling (one instance runs a job at a time),
- dynamic, database-backed parameters,
- data migrations (separate from Liquibase schema migrations),
- optional license enforcement through [BeanGuard](https://github.com/mszajner/beanguard).

> **Status: pre-release (`0.x`).** The API may change between minor versions. See [CHANGELOG.md](CHANGELOG.md).

## Requirements

- Java 21
- Spring Boot 4.0.x
- A relational database. PostgreSQL, MySQL, MS SQL Server, Oracle and H2 are supported through the `db.*`
  properties; **you provide the JDBC driver** for your database.

## Installation

```xml
<dependency>
    <groupId>io.github.mszajner.beanboot</groupId>
    <artifactId>beanboot</artifactId>
    <version>0.1.0</version>
</dependency>
```

The modules are registered as Spring Boot auto-configurations, so adding the dependency is enough to activate them.

## Modules

| Module | What it provides |
|--------|------------------|
| `starter` | `User` / `Group` entities, services and REST controllers |
| `security` | JWT authentication, `@AdminAllowed` / `@AdminRequired` authorization annotations (AspectJ) |
| `parameters` | `ParameterService`: a key/value settings store in the database (also holds the JWT keys) |
| `auditlog` | Audit log of actions performed on `AuditableObject` entities |
| `tasks` | Asynchronous background tasks polled from the database |
| `scheduler` | `@DistributedScheduled` — cluster-safe cron-like jobs |
| `migrations` | One-shot / always-run data migrations (`MigrationTask`) |
| `licence` | Optional BeanGuard integration (see below) |
| `utils` | Error handling (`ProblemDetail`), multi-database `DataSource`, testable clock |

Database schemas are created by Liquibase (one changelog per module under `db/beanboot/`).

## Configuration

Database connection (used by the `DataSource` that beanboot creates when `db.connection` is set):

```yaml
db:
  connection: PGSQL        # H2 | PGSQL | MYSQL | MSSQL | ORACLE
  host: localhost
  port: 5432
  database: myapp
  username: myapp
  password: secret
```

Scheduler (optional):

```yaml
beanboot:
  scheduler:
    name: default            # instance name
    check-in-interval: 30s   # heartbeat interval
```

## Integrating with your application: the registry pattern

beanboot stays generic by asking your application to plug in its own values through small `*Registry` interfaces.
You implement an enum-like type plus a registry bean; beanboot handles JSON, JPA and request-parameter conversion for it.

Roles are a good example:

```java
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;
import org.springframework.stereotype.Component;

public enum AppRole implements Role {
    ADMIN, USER;

    @Override
    public boolean admin() {
        return this == ADMIN;
    }
}

@Component
public class AppRoleRegistry implements RoleRegistry {
    @Override
    public Role[] values() { return AppRole.values(); }

    @Override
    public Role valueOf(String name) { return AppRole.valueOf(name); }
}
```

Your application needs to provide:

| Interface | Purpose |
|-----------|---------|
| `security.api.RoleRegistry` (+ `Role`) | Roles available in your application |
| `security.api.BeanbootSecurityConfiguration` | Names of the parameters holding the JWT settings, and the audit action logged on login |
| `parameters.api.ParameterNameRegistry` (+ `ParameterName`) | Parameter names and their default values |
| `auditlog.api.AuditLogActionRegistry` (+ `AuditLogAction`) | Audit actions. It must also return the actions defined by the `starter` module (`io.github.mszajner.beanboot.starter.models.AuditLogAction`), because a single registry bean is used |
| `tasks.api.TaskObjectTypeRegistry` (+ `TaskObjectType`) | Object types that tasks can refer to |

The **complete, working reference** is the sample application in
[`src/test/java/io/github/mszajner/beanboot/webapp`](src/test/java/io/github/mszajner/beanboot/webapp): it implements every
extension point above and boots the whole library against a real PostgreSQL started by Testcontainers. Run
`WebApplication` from your IDE (Docker required). It creates a demo administrator `admin@example.com` / `admin` —
for demonstration only.

## License enforcement with BeanGuard (optional)

The `licence` module integrates [`dev.beanguard:beanguard-client`](https://github.com/mszajner/beanguard). It is
**disabled by default**; without it beanboot works with no license server.

Enable it with:

```yaml
beanboot:
  licence:
    enabled: true

beanguard:
  server:
    url: https://your-beanguard-server
  decryptor:
    publicKey: ...
    secretKey: ...
```

You also provide a bean implementing `io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration`
(the names of the parameters that store the license key, secret and license). When enabled:

- requests to `/api/**` and `/auth/**` (except `/api/licence/**`) are rejected with `402 Payment Required` unless a
  valid license is loaded;
- `/api/licence/**` exposes endpoints to read the license status, set the license key and request a demo license;
- the `@RequiresValidLicence` / `@RequiresLicenceLimit` annotations used by the `starter` module are enforced.

When the module is disabled, none of its beans are created and those annotations have no effect.

## Building

```
mvn verify
```

Unit tests do not need Docker. See [CONTRIBUTING.md](CONTRIBUTING.md) for details.

## License

Licensed under the [Apache License, Version 2.0](LICENSE). Copyright 2026 Mirosław Szajner.
