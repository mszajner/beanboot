---
title: Getting started
nav_order: 2
description: Add beanboot to a Spring Boot application and get it running.
---

# Getting started
{: .no_toc }

1. TOC
{:toc}

## 1. Add the dependency

```xml
<dependency>
    <groupId>io.github.mszajner.beanboot</groupId>
    <artifactId>beanboot</artifactId>
    <version>0.1.2</version>
</dependency>
```

Also add the JDBC driver for your database (beanboot does **not** bring one), for example:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

The modules are registered as Spring Boot auto-configurations
(`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`), so adding the dependency is
enough to activate them. There is no `@EnableBeanboot` annotation.

{: .gotcha }
> Everything is activated at once. As soon as the jar is on the classpath your application gets Spring Security,
> Liquibase, JPA entities, REST controllers under `/api/**` and `/auth/login`, an AOP layer and a background task
> thread. See [what the dependency pulls in](#what-the-dependency-pulls-in) below.

## 2. Configure the database

The simplest option is the `db.*` properties, which make beanboot create the `DataSource` for you:

```yaml
db:
  connection: PGSQL        # H2 | PGSQL | MYSQL | MSSQL | ORACLE
  host: localhost
  port: 5432
  database: myapp
  username: myapp
  password: secret
```

Alternatively provide your own `DataSource` (or use `spring.datasource.*`) — but read
[Database configuration]({% link configuration.md %}#database-db) first, there are traps in that combination.

## 3. Implement the required extension points

beanboot stays generic by asking your application to plug in its own values. The **minimum** to boot successfully is:

| What you implement | Why it is required |
|---|---|
| A `Role` enum + a `RoleRegistry` bean | Authorization, JWT claims, user/group role columns |
| A `ParameterName` enum + a `ParameterNameRegistry` bean | Settings store; also stores the JWT keys. Its `defaults()` **must** contain the token expiration |
| A `BeanbootSecurityConfiguration` bean | Tells the security module *which parameter names* hold the JWT settings and which audit action is logged on login |
| A `TaskObjectTypeRegistry` bean | The tasks module injects it to register JSON/JPA converters, even if you never create a task |

The audit log registries are **optional**: the starter provides defaults containing only its own actions and object
types. If you define your own, it **must also return the starter's values** — see
[Audit log]({% link modules/auditlog.md %}).

Full details and code are on the [Extension points]({% link extension-points.md %}) page. A compact, complete example:

```java
public enum AppRole implements Role {
    ADMIN, USER;
    @Override public boolean admin() { return this == ADMIN; }
}

@Component
class AppRoleRegistry implements RoleRegistry {
    public Role[] values() { return AppRole.values(); }
    public Role valueOf(String name) { return AppRole.valueOf(name); }
}

public enum AppParameter implements ParameterName {
    TOKEN_ISSUER, TOKEN_EXPIRATION, TOKEN_PUBLIC_KEY, TOKEN_PRIVATE_KEY, TOKEN_SECRET_KEY
}

@Component
class AppParameterRegistry implements ParameterNameRegistry {
    public ParameterName[] values() { return AppParameter.values(); }
    public ParameterName valueOf(String name) { return AppParameter.valueOf(name); }
    public Map<ParameterName, String> defaults() {
        return Map.of(
            AppParameter.TOKEN_ISSUER, "my-app",
            AppParameter.TOKEN_EXPIRATION, "86400000",   // milliseconds
            AppParameter.TOKEN_PUBLIC_KEY, "",
            AppParameter.TOKEN_PRIVATE_KEY, "",
            AppParameter.TOKEN_SECRET_KEY, "");
    }
}

@Component
class AppSecurityConfiguration implements BeanbootSecurityConfiguration {
    public ParameterName getTokenIssuerParameterName()     { return AppParameter.TOKEN_ISSUER; }
    public ParameterName getTokenExpirationParameterName() { return AppParameter.TOKEN_EXPIRATION; }
    public ParameterName getTokenPublicKeyParameterName()  { return AppParameter.TOKEN_PUBLIC_KEY; }
    public ParameterName getTokenPrivateKeyParameterName() { return AppParameter.TOKEN_PRIVATE_KEY; }
    public ParameterName getTokenSecretKeyParameterName()  { return AppParameter.TOKEN_SECRET_KEY; }
    public AuditLogAction getUserLoggedInAuditLogAction()  {
        return io.github.mszajner.beanboot.starter.models.AuditLogAction.USER_LOGGED_IN;
    }
}
```

The RSA key pair and the AES secret key used for tokens are generated on first start and stored in the `parameters`
table — you do not provide them.

## 4. Create the first administrator

beanboot ships **no default user**. Nothing can log in until a user exists, and creating users over REST needs an
administrator. Bootstrap the first one with a [data migration]({% link modules/migrations.md %}) (this is what the
sample application does):

```java
@Component
@RequiredArgsConstructor
class CreateAdminMigration implements MigrationTask {
    private final UserRepository userRepository;       // io.github.mszajner.beanboot.starter.repositories
    private final PasswordEncoder passwordEncoder;

    public UUID id() { return UUID.fromString("7d2f0c1e-0000-4000-8000-000000000001"); } // never change it

    public void run() {
        var admin = new UserEntity();
        admin.setFirstName("Admin");
        admin.setLastName("Admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("change-me"));
        admin.setRoles(Set.of(AppRole.ADMIN));
        userRepository.save(admin);
    }
}
```

{: .warning }
> The sample application creates `admin@example.com` / `admin`. That is for demonstration only — never ship it.
> Use a one-time random password or force a change on first login (beanboot has no "must change password" feature).

## 5. Run and log in

```bash
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"change-me"}'
```

The response contains a `token`. Send it on every `/api/**` call:

```bash
curl http://localhost:8080/api/users -H "Authorization: Bearer <token>"
```

More in [REST API]({% link rest-api.md %}).

## The reference application

The **complete, working reference** is the sample application in
[`src/test/java/io/github/mszajner/beanboot/webapp`](https://github.com/mszajner/beanboot/tree/main/src/test/java/io/github/mszajner/beanboot/webapp).
It implements every extension point, starts PostgreSQL with Testcontainers and boots the whole library. Run
`WebApplication` from your IDE (Docker required).

## What the dependency pulls in

Because all the starters are normal (compile-scope) dependencies of beanboot, your application transitively gets:

| Dependency | Consequence |
|---|---|
| `spring-boot-starter-web` | Servlet stack (Spring MVC). WebFlux/reactive applications are **not** supported |
| `spring-boot-starter-security` | A `SecurityFilterChain` is registered by beanboot; see [Security]({% link modules/security.md %}) |
| `spring-boot-starter-data-jpa` | Hibernate + JPA. JPA entities and repositories of every module are registered |
| `spring-boot-starter-liquibase` | Schema management; beanboot defines its own `SpringLiquibase` bean — see [Database]({% link database.md %}) |
| `spring-boot-starter-aspectj` | AOP proxies for the authorization/scheduling annotations |
| `springdoc-openapi-starter-webmvc-ui` | **Swagger UI and `/v3/api-docs` are exposed** — and are not behind authentication |
| `beanguard-client` | Present on the classpath even when the licence module is disabled |
| `mapstruct`, `commons-lang3` | Plain libraries |
| `jjwt-impl`, `jjwt-jackson` (runtime) | JWT implementation |

Lombok is `optional` and is **not** transitive.
