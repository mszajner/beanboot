---
title: Modules
nav_order: 5
has_children: true
permalink: /modules/
description: One page per beanboot module.
---

# Modules

Every feature of beanboot is a self-contained module under
`io.github.mszajner.beanboot.<module>`, wired in as a Spring Boot `@AutoConfiguration`. Public types live in the
module's `api` package; everything else is internal.

| Module | Auto-configuration | Can be disabled? |
|--------|--------------------|------------------|
| [starter]({% link modules/starter.md %}) | `BeanbootStarterAutoConfiguration` | No |
| [security]({% link modules/security.md %}) | `BeanbootSecurityAutoConfiguration` | No |
| [parameters]({% link modules/parameters.md %}) | `BeanbootParametersAutoConfiguration` | No |
| [auditlog]({% link modules/auditlog.md %}) | `BeanbootAuditLogAutoConfiguration` | No |
| [tasks]({% link modules/tasks.md %}) | `BeanbootTasksAutoConfiguration` | No |
| [scheduler]({% link modules/scheduler.md %}) | `BeanbootSchedulerAutoConfiguration` | No |
| [migrations]({% link modules/migrations.md %}) | `BeanbootMigrationsAutoConfiguration` | No |
| [licence]({% link modules/licence.md %}) | `BeanbootLicenceAutoConfiguration` | **Yes — off by default** (`beanboot.licence.enabled`) |
| [utils]({% link modules/utils.md %}) | `BeanbootUtilsAutoConfiguration` | No |
| [setup]({% link modules/setup.md %}) | *(separate application, not an auto-configuration)* | Opt-in |

{: .gotcha }
> The modules are **not independently switchable**. Except for `licence`, there is no `enabled` flag. You can use
> Spring Boot's `spring.autoconfigure.exclude` to exclude a module's auto-configuration class, but the modules depend
> on each other (the starter needs security, parameters, audit log, …), so excluding one is not a supported configuration.
> Treat beanboot as an all-in-one foundation.

## Module dependencies

Allowed dependencies are declared per module and verified at build time (Spring Modulith, `ApplicationModules.verify()`):

| Module | Declared dependencies |
|---|---|
| `starter` | `security`, `auditlog`, `tasks`, `utils` (API packages) and `setup` |
| `security` | `parameters`, `utils`, `auditlog` (API packages) |
| `licence` | `parameters` (API) |
| `auditlog`, `tasks` | `utils` (API) |

`parameters`, `scheduler`, `migrations` and `utils` have no `@ApplicationModule` declaration (no restriction is
configured for them). Cycles are not allowed. This only restricts the library's own code; your application may use any `api` package freely.
