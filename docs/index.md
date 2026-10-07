---
title: Home
layout: home
nav_order: 1
description: Developer documentation for the beanboot Spring Boot starter.
permalink: /
---

# beanboot
{: .fs-9 }

A Spring Boot **auto-configuration library** that gives your application a ready-made foundation, so you can start on
the domain logic instead of re-implementing the same plumbing every time.
{: .fs-6 .fw-300 }

[Get started]({% link getting-started.md %}){: .btn .btn-primary .fs-5 .mb-4 .mb-md-0 .mr-2 }
[Known issues]({% link known-issues.md %}){: .btn .fs-5 .mb-4 .mb-md-0 }

---

This documentation is written for **developers who use beanboot in their own application**. It describes every
configuration option, every extension point your application has to (or may) implement, the REST endpoints you get for
free, and — just as importantly — the [gaps, limitations and pitfalls]({% link known-issues.md %}) you should know about
before relying on the library.

{: .note }
> beanboot is in **pre-release (`0.x`)**. The API may change between minor versions and minor releases may contain
> breaking changes. Read the [changelog](https://github.com/mszajner/beanboot/blob/main/CHANGELOG.md) before upgrading.

## What you get

| Module | What it provides | Page |
|--------|------------------|------|
| `starter` | `User` / `Group` entities, services and REST controllers | [Users and groups]({% link modules/starter.md %}) |
| `security` | JWT authentication, role-based authorization through AspectJ annotations | [Security]({% link modules/security.md %}) |
| `parameters` | Database-backed key/value settings store (`ParameterService`) | [Parameters]({% link modules/parameters.md %}) |
| `auditlog` | Audit log of actions performed on `AuditableObject`s | [Audit log]({% link modules/auditlog.md %}) |
| `tasks` | Asynchronous background tasks polled from the database | [Tasks]({% link modules/tasks.md %}) |
| `scheduler` | `@DistributedScheduled` — cluster-safe cron-like jobs | [Scheduler]({% link modules/scheduler.md %}) |
| `migrations` | One-shot / always-run data migrations (`MigrationTask`) | [Data migrations]({% link modules/migrations.md %}) |
| `licence` | Optional [BeanGuard](https://github.com/mszajner/beanguard) license enforcement | [Licence]({% link modules/licence.md %}) |
| `utils` | `ProblemDetail` error handling, multi-database `DataSource`, testable clock | [Utilities]({% link modules/utils.md %}) |
| `setup` | First-run database setup wizard (separate mini application) | [Setup wizard]({% link modules/setup.md %}) |

## Requirements

| | |
|---|---|
| Java | 21 |
| Spring Boot | 4.x — the library is built and tested against **4.1.1** |
| Build tool | Maven (the artifact is on Maven Central) |
| Database | PostgreSQL, MySQL, MS SQL Server, Oracle or H2. **You supply the JDBC driver.** |

Docker is only needed to run the sample application from the library's own test sources.

## Where to go next

1. [Getting started]({% link getting-started.md %}) — dependency, the minimum set of beans, a first run.
2. [Configuration reference]({% link configuration.md %}) — every property and every overridable bean.
3. [Extension points]({% link extension-points.md %}) — the `*Registry` interfaces your application implements.
4. [REST API]({% link rest-api.md %}) — the endpoints, error format and authentication flow.
5. [Database]({% link database.md %}) — tables, Liquibase and how the schema is managed.
6. [Known issues and gotchas]({% link known-issues.md %}) — read this before going to production.
