---
title: Database
nav_order: 6
description: Tables, Liquibase and how beanboot manages the schema.
---

# Database
{: .no_toc }

1. TOC
{:toc}

## Supported databases

PostgreSQL, MySQL, MS SQL Server, Oracle and H2 (see the
[`db.*` properties]({% link configuration.md %}#database-db)). The sample application runs against PostgreSQL
(Testcontainers). The other engines are supported by URL/driver mapping and Liquibase's cross-database column types,
but the project documents no per-engine verification — **verify the schema on your engine** before relying on it.

## Schema management with Liquibase

The `utils` module declares **one** `SpringLiquibase` bean (`frameworkUtilsLiquibase`) with the change log
`classpath:db/changelog.yml`:

```yaml
databaseChangeLog:
  - includeAll:
      path: db/beanboot/
```

`includeAll` picks up one folder per module, in alphabetical order: `auditlog`, `core` (users/groups), `migrations`,
`parameters`, `scheduler`, `tasks`. The change sets run automatically at startup; `spring.jpa.hibernate.ddl-auto` must
stay `none`.

{: .gotcha }
> **Spring Boot's own Liquibase auto-configuration backs off** as soon as a `SpringLiquibase` bean exists — and
> beanboot defines one. Therefore:
>
> * `spring.liquibase.*` properties (`change-log`, `contexts`, `default-schema`, `enabled`, …) are **ignored** for
>   beanboot's bean, and your own `src/main/resources/db/changelog/db.changelog-master.yaml` is **not run** by Boot's
>   auto-configuration any more.
> * To migrate *your own* tables with Liquibase, declare your own `SpringLiquibase` bean (a second one) pointing to your
>   own change log, **with a different path** than `db/changelog.yml` / `db/beanboot/`.
> * Both change logs share the same `DATABASECHANGELOG` table. Use unique change-set `id`/`author` pairs.
> * Do not name your change log `db/changelog.yml`; it would collide with the one inside the jar.
> * The beanboot bean has no schema/contexts configuration: it uses the connection's default schema.

If you need your tables to be created **before** or **after** beanboot's, control the order with
`@DependsOn` on your `SpringLiquibase` bean.

## Tables

All table names are **unprefixed**. Make sure they do not clash with your own tables (`users`, `groups`,
`parameters`, `tasks`, `migrations` are common names!), or put beanboot in a dedicated database/schema.

| Table | Module | Content |
|---|---|---|
| `users` | starter | Users: id (UUID), names, unique email, BCrypt password, roles (JSON text), timestamps |
| `groups` | starter | Groups: id (UUID), name, roles (JSON text), timestamps |
| `group_users` | starter | Many-to-many join between users and groups |
| `parameters` | parameters | Key/value store; the primary key is the parameter name |
| `audit_logs` | auditlog | Audit entries |
| `tasks` | tasks | Background tasks, their status/result and the heartbeat of the instance running them |
| `migrations` | migrations | Status of each `MigrationTask` |
| `schedulers` | scheduler | One row per application *start* (instance registration + heartbeat) |
| `scheduleds` | scheduler | One row per distributed lock name |
| `databasechangelog`, `databasechangeloglock` | Liquibase | Liquibase bookkeeping |

Type conventions you will notice:

* Timestamps are stored as **`BIGINT` epoch milliseconds**, not as `TIMESTAMP` (because of the auto-applied
  `InstantConverter`; see [Known issues]({% link known-issues.md %}#every-instant-in-your-entities-becomes-a-bigint)).
* Roles and `Map<String,String>` parameters are stored as **JSON text**. They can't be indexed or queried portably.
* UUID primary keys use the native `UUID` type where the database has one.

## Things to watch

* **No migration path is documented between beanboot versions.** New versions add Liquibase change sets (`includeAll`
  executes anything new). Back up before upgrading and read the changelog.
* Existing change sets must never be edited by you. Don't `ALTER` beanboot tables by hand.
* `audit_logs`, `tasks` and `schedulers` only ever grow (no retention). See [Audit log]({% link modules/auditlog.md %}),
  [Tasks]({% link modules/tasks.md %}) and [Scheduler]({% link modules/scheduler.md %}).
* The `parameters` table holds **secrets** (JWT keys, licence secret) in plain text. Encrypt the volume/backups and
  restrict access.
* H2 mode is `MODE=PostgreSQL;NON_KEYWORDS=VALUE` and in-memory only — fine for tests, not production.
