---
title: Data migrations
parent: Modules
nav_order: 7
description: One-shot and always-run data migrations that complement Liquibase.
---

# Data migrations
{: .no_toc }

1. TOC
{:toc}

The migrations module runs **Java code** at startup — typically to load initial data or transform existing data.
It is separate from Liquibase, which handles the **schema** (see [Database]({% link database.md %})).

```java
@Component
@Order(1)
class LoadInitialData implements MigrationTask {

    @Override
    public UUID id() {                                      // identity of the migration — NEVER change it
        return UUID.fromString("0af90907-e6b5-4814-bf87-53942c09728e");
    }

    @Override
    public void run() { /* create default groups, admin user, ... */ }
}
```

`MigrationTask` has three members:

| Member | Meaning |
|---|---|
| `UUID id()` | Stable unique identifier stored in the `migrations` table |
| `boolean always()` | `false` by default. `true` makes the task run on **every** start |
| `void run()` | The work |

## Execution model

* `MigrationsRunner` is an `ApplicationRunner` with `@Order(1)`; it runs after the context has started (Liquibase has
  finished) and **before** the task dispatcher and scheduler instance registration.
* All `MigrationTask` beans are injected as a collection and executed **in bean order** — use `@Order` on the
  implementations if the order matters.
* For each task the status row is looked up by `id()`. The task runs if `always()` is `true` or no row exists yet
  (`UNKNOWN`).
* Every task runs in its **own transaction** (`REQUIRES_NEW`); a failing task is rolled back without affecting the
  others.
* The outcome (`SUCCESS`/`FAILURE`, `startedAt`, `finishedAt`, `message`) is stored in `migrations`.

## Caveats

{: .warning }
> * **A failed migration does not stop application startup** — the error is logged (`Error running migration …`) and
>   the app keeps starting. Monitor the logs / the `migrations` table.
> * **A failed migration is never retried automatically.** The row is saved with status `FAILURE`, and only tasks
>   without a row (or with `always() == true`) run. Fix the cause and **delete the row** (or change the task's `id()` and
>   accept that it will run again) to retry. Make migrations transactional and idempotent.
> * **Not cluster-safe.** If several nodes start at the same time on a database where a migration has not run yet,
>   each may run it. Start a single node first, or make every migration idempotent.
> * **Status rows are committed together at the end** (the outer transaction), while each task's own changes are
>   committed immediately. If the process dies in the middle of a multi-task run, tasks that already finished have
>   committed their data but not their status row, so they run **again** on the next start. Another reason for
>   idempotence.
> * `always()` tasks run on every node at every start; keep them cheap and idempotent.
> * Changing a task's `id()` re-runs it; reusing an old `id()` for different code silently skips it.
> * A migration runs with **no authenticated user**; audit log entries made from it have no actor. Beans protected by
>   `@AdminAllowed` etc. cannot be called from a migration — use repositories directly (as the sample app does).
> * There is no "down"/rollback, no checksum, and no dependency ordering between migrations.

## Bootstrapping the first administrator

Because beanboot ships no default user, a data migration is the intended place to create the first administrator —
see [Getting started]({% link getting-started.md %}#4-create-the-first-administrator).
