---
title: Extension points
nav_order: 4
description: The interfaces your application implements to plug its own values into beanboot.
---

# Extension points
{: .no_toc }

1. TOC
{:toc}

## The registry pattern

beanboot stays generic by asking your application to plug in its own values through small interfaces. For an
"enum-like" concept (roles, parameter names, audit actions, task actions…) there are always two pieces:

1. an **enum-like type** — an interface with `name()` (and sometimes `friendlyName()`) that *you* implement, typically
   with a Java `enum`;
2. a **registry** — an interface that lists all values and resolves one by name. You expose it as a Spring bean.

beanboot uses the registry to (de)serialize the type in JSON, store it in the database (a JPA `AttributeConverter`
writes `name()`), and convert it from `@RequestParam` values.

{: .gotcha }
> Names are persisted. **Renaming or removing an enum constant breaks existing rows**: `valueOf(name)` throws
> `IllegalArgumentException`, and the whole entity fails to load (a `GET /api/users` can fail because of one old role
> name). Treat constant names as a stable, versioned contract and migrate data before changing them.

## What you must / may provide

| Interface | Required? | Purpose |
|---|---|---|
| `security.api.Role` + `RoleRegistry` | **Required** | Roles available in your application |
| `security.api.BeanbootSecurityConfiguration` | **Required** | Parameter names holding the JWT settings, and the audit action logged on login |
| `parameters.api.ParameterName` + `ParameterNameRegistry` | **Required** | Parameter names and their default values |
| `tasks.api.TaskObjectType` + `TaskObjectTypeRegistry` | **Required** | Object types that tasks can refer to (can be an empty enum) |
| `tasks.api.TaskAction` + `TaskActionRegistry` + `TaskActionHandler` | Practically required if you use tasks | Task actions and the code executing them |
| `auditlog.api.AuditLogAction` + `AuditLogActionRegistry` | Optional | Extra audit actions. Must include the starter's actions |
| `auditlog.api.AuditLogObjectType` + `AuditLogObjectTypeRegistry` | Optional | Extra audited object types. Must include the starter's types |
| `migrations.api.MigrationTask` | Optional | Data migrations (one bean per migration) |
| `licence.api.BeanbootLicenceConfiguration` | Required **only if** `beanboot.licence.enabled=true` | Parameter names and BeanGuard server/decryptor settings |
| `security.api.UserProvider` | Provided by the starter (`UserProviderImpl`) | Only implement it if you replace the starter's `User` entity |
| `auditlog.api.AuditableObject`, `tasks.api.TaskableObject` | Optional | Marker interfaces for your entities so they can be named in the audit log / tasks |

If a required bean is missing the application fails at startup with a normal `NoSuchBeanDefinitionException`
(for example `Parameter 0 of method roleModule ... required a bean of type RoleRegistry`).

{: .note }
> All interfaces live in `io.github.mszajner.beanboot.<module>.api`. Classes outside `api` packages are internal
> (the project enforces module boundaries with Spring Modulith) and may change without notice. Prefer the `api`
> types, even where an internal class (for example `UserEntity`) is technically public.

## Roles

```java
public enum AppRole implements Role {
    ADMIN, EDITOR, VIEWER;

    @Override
    public boolean admin() {            // true => may use @AdminAllowed methods
        return this == ADMIN;
    }
}

@Component
public class AppRoleRegistry implements RoleRegistry {
    @Override public Role[] values() { return AppRole.values(); }
    @Override public Role valueOf(String name) { return AppRole.valueOf(name); }
}
```

* `admin()` is the only place the library asks "is this an administrator?". You may mark several roles as admin.
* Roles are stored **as a JSON array of names in a text column** (`["ADMIN","EDITOR"]`) by `RoleSetConverter`.
* An application with a single role still has to declare it.

## Parameter names

`ParameterNameRegistry` has one more method than the others:

```java
Map<ParameterName, String> defaults();
```

`ParameterService.getString(name)` returns the stored value or, if there is no row, the entry from `defaults()`.
**A name that is neither stored nor present in `defaults()` yields `null`.** See [Parameters]({% link modules/parameters.md %}).

The security module requires these entries to be usable:

| Parameter | Content | Behaviour if empty |
|---|---|---|
| token issuer | free text, e.g. app name | **Not defaulted.** A `null` issuer is read as-is; always provide a default (and keep it stable — tokens signed under another issuer are rejected) |
| token expiration | **milliseconds**, e.g. `86400000` | Falls back to `defaults()` and is written back to the database. If `defaults()` has no entry either, startup fails |
| token public / private key | Base64 RSA keys | Generated on first start |
| token secret key | Base64 AES-256 key | Generated on first start |

## Security configuration

```java
@Component
public class AppSecurityConfiguration implements BeanbootSecurityConfiguration {
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

All five parameter names must be present in your `ParameterNameRegistry.values()`.

## Audit log registries

```java
public enum AppAuditAction implements AuditLogAction {
    INVOICE_ISSUED("Invoice issued");
    // name() comes from the enum; implement friendlyName()
    private final String friendlyName;
    AppAuditAction(String f) { this.friendlyName = f; }
    public String friendlyName() { return friendlyName; }
}

@Component
public class AppAuditLogActionRegistry implements AuditLogActionRegistry {
    @Override
    public AuditLogAction[] values() {
        return Stream.concat(
                Stream.of(io.github.mszajner.beanboot.starter.models.AuditLogAction.values()),   // REQUIRED
                Stream.of(AppAuditAction.values()))
            .toArray(AuditLogAction[]::new);
    }

    @Override
    public AuditLogAction valueOf(String name) {
        try {
            return io.github.mszajner.beanboot.starter.models.AuditLogAction.valueOf(name);
        } catch (IllegalArgumentException e) {
            return AppAuditAction.valueOf(name);
        }
    }
}
```

{: .warning }
> Because the starter default is `@ConditionalOnMissingBean`, **your registry replaces it completely**. If it does not
> return the starter's values (`USER_LOGGED_IN`, `USER_CREATED`, …) then logging in or creating a user will store a
> row that can no longer be read back. Same for `AuditLogObjectTypeRegistry` and the starter's `USER` / `GROUP`.

## Task registries and handlers

```java
public enum AppTaskAction implements TaskAction {
    SEND_REPORT("Send report");
    private final String friendlyName;
    AppTaskAction(String f) { this.friendlyName = f; }
    public String friendlyName() { return friendlyName; }
}

@Component
public class AppTaskActionRegistry implements TaskActionRegistry {
    public TaskAction[] values() { return AppTaskAction.values(); }
    public TaskAction valueOf(String name) { return AppTaskAction.valueOf(name); }
}

@Component
public class SendReportHandler implements TaskActionHandler {
    public boolean supports(TaskAction action) { return action == AppTaskAction.SEND_REPORT; }
    public void execute(TaskEntity task) throws Exception { /* ... */ }
}
```

`TaskActionHandler` and `TaskEntity` live in the internal `tasks.executor` / `tasks.entities` packages. See
[Tasks]({% link modules/tasks.md %}) for the lifecycle and caveats.

## Migrations

```java
@Component
public class AddDefaultGroups implements MigrationTask {
    public UUID id() { return UUID.fromString("..."); }   // stable, unique, never reused
    public boolean always() { return false; }             // default
    public void run() { /* ... */ }
}
```

See [Data migrations]({% link modules/migrations.md %}).

## Auditable and taskable objects

Implement `AuditableObject` on any model you pass to `AuditLogService.log(action, object)`:

```java
public String getAuditLogObjectId();                 // stored as text
public String getAuditLogObjectName();               // denormalised display name
public AuditLogObjectType getAuditLogObjectType();   // must be in your registry
```

`TaskableObject` is the same idea for `TaskService.createTask(action, object)`.

## Events your application can listen to

| Event | Published when |
|---|---|
| `tasks.api.TaskCreatedEvent` | After a task is saved as `PENDING` (consumed internally to wake the dispatcher) |
| `tasks.api.TaskExecutedEvent` | After a task finishes — carries `id`, `action`, `status`, `message`, `startedAt`, `finishedAt` |
