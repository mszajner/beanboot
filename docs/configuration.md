---
title: Configuration reference
nav_order: 3
description: Every property, default and overridable bean in beanboot.
---

# Configuration reference
{: .no_toc }

1. TOC
{:toc}

beanboot deliberately exposes **few** `application.yml` properties. Most of the behaviour is configured by
implementing Java interfaces (see [Extension points]({% link extension-points.md %})) and by overriding beans.

## Property summary

| Property | Type | Default | Module | Description |
|---|---|---|---|---|
| `db.connection` | `H2`/`PGSQL`/`MYSQL`/`MSSQL`/`ORACLE` | `H2` *(see note)* | utils | Database engine; **setting it activates the beanboot `DataSource`** |
| `db.host` | string | `beanboot` | utils | Host name (for H2: the in-memory database name) |
| `db.port` | integer | – | utils | TCP port (required for everything but H2) |
| `db.database` | string | – | utils | Database / schema / service name |
| `db.username` | string | – | utils | JDBC user |
| `db.password` | string | – | utils | JDBC password |
| `beanboot.scheduler.name` | string | `default` | scheduler | Instance name stored in the `schedulers` table |
| `beanboot.scheduler.check-in-interval` | duration | `30s` | scheduler | Heartbeat interval; an instance whose last heartbeat is older than 2× this value is considered dead |
| `beanboot.tasks.heartbeat-interval` | duration | `30s` | tasks | How often the instance executing a task refreshes its heartbeat; a `RUNNING` task silent for 3× this value is returned to `PENDING` |
| `beanboot.licence.enabled` | boolean | `false` | licence | Turns on the licence module and its HTTP gate |
| `app.config-file` | path | – | setup / starter | External config file the setup wizard writes (see [Setup wizard]({% link modules/setup.md %})) |
| `app.port-file` | path | – | setup | File the setup wizard writes its HTTP port to |

{: .note }
> The BeanGuard properties `beanguard.server.url` and `beanguard.decryptor.*` are **no longer read by the library**
> (since 0.1.2). They are supplied through the `BeanbootLicenceConfiguration` bean — see
> [Licence]({% link modules/licence.md %}).

## Database (`db.*`)

```yaml
db:
  connection: PGSQL
  host: db.internal
  port: 5432
  database: myapp
  username: myapp
  password: ${DB_PASSWORD}
```

The URL and driver class are derived from `db.connection`:

| `db.connection` | JDBC URL template | Driver class |
|---|---|---|
| `H2` | `jdbc:h2:mem:{host};MODE=PostgreSQL;NON_KEYWORDS=VALUE` | `org.h2.Driver` |
| `PGSQL` | `jdbc:postgresql://{host}:{port}/{database}` | `org.postgresql.Driver` |
| `MYSQL` | `jdbc:mysql://{host}:{port}/{database}` | `com.mysql.cj.jdbc.Driver` |
| `MSSQL` | `jdbc:sqlserver://{host}:{port};databaseName={database};encrypt=true;trustServerCertificate=true;` | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| `ORACLE` | `jdbc:oracle:thin:@{host}:{port}/{database}` | `oracle.jdbc.OracleDriver` |

When is the `DataSource` created? Only when **all** of these hold: HikariCP is on the classpath **and** the property
`db.connection` is present. The bean is `@Primary`. If you do not set `db.connection`, beanboot does not create a
`DataSource` and Spring Boot's regular `spring.datasource.*` applies.

{: .gotcha }
> **`db.connection` defaults to `H2` in the properties class, even when you do not set it.** A Hibernate customizer
> reads that default and forces `hibernate.dialect=org.hibernate.dialect.H2Dialect` whenever it is `H2`. If you use
> `spring.datasource.*` with PostgreSQL/MySQL/… and leave `db.connection` unset, Hibernate is told to use the **H2
> dialect**. Always set `db.connection` to your real engine (even if you configure the `DataSource` yourself).

Other things to know:

* **Port is mandatory** for non-H2 engines. It is an `Integer` with no default; a missing port produces the literal
  text `null` in the JDBC URL.
* **No pool tuning.** The beanboot `DataSource` uses a bare `HikariConfig` with URL, driver, user and password only.
  `spring.datasource.hikari.*` is **not** applied. To tune the pool, define your own `DataSource` bean (the beanboot
  one is not `@ConditionalOnMissingBean`, so you must also disable it by not setting `db.connection`).
* **No SSL/extra JDBC parameters** except what is hard-coded for MSSQL (`encrypt=true;trustServerCertificate=true`,
  which accepts any server certificate).
* **Drivers are yours.** Add the JDBC driver and, for H2, the `com.h2database:h2` artifact.
* The `H2` mode is an in-memory database named by `db.host` — useful for tests, not for persistence.

## Scheduler (`beanboot.scheduler.*`)

```yaml
beanboot:
  scheduler:
    name: node-1             # defaults to "default"
    check-in-interval: 30s
```

`name` is stored for every started instance. Use a unique value per node if you want to tell instances apart in the
`schedulers` table. See [Scheduler]({% link modules/scheduler.md %}).

## Licence (`beanboot.licence.enabled`)

```yaml
beanboot:
  licence:
    enabled: true
```

When `false` or absent, **none** of the module's beans exist. You must then **not** depend on
`io.github.mszajner.beanboot.licence.api.*` beans. When `true`, you must also provide a
`BeanbootLicenceConfiguration` bean. See [Licence]({% link modules/licence.md %}).

## Defaults shipped inside the jar

The jar contains an `application.yml` at the classpath root:

```yaml
spring:
  data:
    web:
      pageable:
        serialization-mode: via-dto
  jpa:
    hibernate:
      ddl-auto: none
    show_sql: false
    open-in-view: false
```

{: .gotcha }
> Two `application.yml` files at the classpath root do not merge: **the first one found wins**. Normally your
> application's own `application.yml` is earlier on the classpath, so the library's defaults are silently ignored.
> Do not rely on these defaults — set them yourself. `spring.jpa.hibernate.ddl-auto: none` matters: beanboot's schema
> is created by Liquibase and Hibernate must not touch it.
> The other defaults are conveniences.

Using `application.properties`, `application-{profile}.yml`, environment variables or `spring.config.import` avoids the
problem because those are different resources.

## Overridable beans

The auto-configurations register several default beans that your application **can replace** simply by declaring its
own bean of the same type (`@ConditionalOnMissingBean`):

| Bean (type) | Default | Typical reason to override |
|---|---|---|
| `AuditLogActionRegistry` | starter's `AuditLogAction` enum only | Add your own audit actions (you must include the starter ones — see [Audit log]({% link modules/auditlog.md %})) |
| `AuditLogObjectTypeRegistry` | starter's `USER`, `GROUP` only | Add your own audited object types |
| `ParameterService` | `ParameterServiceImpl` — **created only if a `ParameterNameRegistry` bean exists** | Custom caching / storage |
| `TaskActionRegistry` | Enum with a single value `UNKNOWN` | **Always** — define your task actions |
| `TaskActionHandler`s | `DefaultTaskActionHandler` throws `UnsupportedOperationException` | **Always** — register one handler per action |
| `TaskHandlerRegistry` | collects all `TaskActionHandler` beans | Custom dispatch |
| `TaskExecutionService` | `TaskExecutionServiceImpl` | Custom execution semantics (retries, timeouts). Task claiming and heartbeats are done by the dispatcher, before this service is called |
| `TaskService` | `TaskServiceImpl` | Rarely |

Not overridable: the `SecurityFilterChain`, `PasswordEncoder`, `AuthenticationManager` (declared in the `security`
module's `@Configuration`, which is component-scanned and has no conditions) — see
[Security]({% link modules/security.md %}) for how to work around that.

## Jackson integration

Each registry gets a `SimpleModule` bean that registers a deserializer for its interface (`Role`, `ParameterName`,
`AuditLogAction`, `AuditLogObjectType`, `TaskAction`, `TaskObjectType`). Spring Boot picks these modules up
automatically. Serialization works because your enums expose `name()`; deserialization resolves names through the
registry's `valueOf`.

Beanboot also applies a `JsonMapperBuilderCustomizer` that **disables
`DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES`** for the whole application's `ObjectMapper`: JSON `null` for a
primitive field becomes `0`/`false` instead of an error.

## Springdoc / Swagger

`springdoc-openapi-starter-webmvc-ui` is a transitive dependency, so `/swagger-ui.html` and `/v3/api-docs` exist in
your application. They are outside `/api/**` and therefore **public**. Disable them in production if that is not what
you want:

```yaml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```
