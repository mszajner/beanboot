---
title: Tasks
parent: Modules
nav_order: 5
description: Asynchronous background tasks stored in and polled from the database.
---

# Tasks
{: .no_toc }

1. TOC
{:toc}

The tasks module executes work asynchronously, outside the request thread, and keeps a durable record of every task
(status, timing, error message) in the `tasks` table.

## Creating tasks

```java
@Service
@RequiredArgsConstructor
class ReportController {
    private final TaskService taskService;

    Task requestReport(Customer customer) {                                    // Customer implements TaskableObject
        return taskService.createTask(AppTaskAction.SEND_REPORT, customer, Map.of("format", "pdf"));
    }
}
```

`createTask(action)`, `createTask(action, object)` and `createTask(action, object, parameters)` all save the task with
status `PENDING` and publish a `TaskCreatedEvent`. The returned `Task` has the generated `id`; poll
`GET /api/tasks` to see the status.

## Executing tasks

Implement one `TaskActionHandler` bean per action:

```java
@Component
class SendReportHandler implements TaskActionHandler {
    public boolean supports(TaskAction action) { return action == AppTaskAction.SEND_REPORT; }

    public void execute(TaskEntity task) throws Exception {
        String format = task.getParameters().get("format");
        // ... throw to mark the task FAILURE; the exception message is saved
    }
}
```

`TaskActionHandler` and `TaskEntity` are not in the module's `api` package; you have to import them from the internal
`tasks.executor` / `tasks.entities` packages.

### Lifecycle

```
PENDING ──(an instance claims it)──▶ RUNNING ──▶ SUCCESS
   ▲                                    │  └───▶ FAILURE   (message = exception message)
   └──── heartbeat lost (instance died) ┘
```

* `TaskDispatcher` is an `ApplicationRunner` that starts a **daemon thread** `task-dispatcher` on every instance.
* The thread wakes up when a `TaskCreatedEvent` is published (**after the creating transaction commits**) or at the
  latest every **60 seconds**, then executes **all** `PENDING` tasks, oldest first, **one at a time** on that thread.
* Before executing, the instance **claims** the task with one atomic, conditional database update
  (`UPDATE … SET status = 'RUNNING' … WHERE id = ? AND status = 'PENDING'`). Only the instance that changes the row
  runs it; an instance that loses the race skips to the next task. Several instances can therefore share one task table
  safely.
* While a task runs, its instance refreshes a **heartbeat** (`tasks.heartbeat_at`) every
  `beanboot.tasks.heartbeat-interval` (default 30 s).
* A `RUNNING` task whose heartbeat is older than **three intervals** (90 s by default), or that has none, is considered
  abandoned and is put back to `PENDING` — at startup and before each dispatcher run — by whichever instance notices
  first. Tasks running on healthy instances are never touched.
* A `TaskExecutedEvent` is published on the executing instance when a task ends (success or failure). Listen to it with
  `@EventListener` for notifications.

```yaml
beanboot:
  tasks:
    heartbeat-interval: 30s   # default
```

## Guarantees and limits

| Topic | Behaviour |
|---|---|
| **Parallelism** | One task at a time **per instance**; a long task blocks the others on that instance (other instances keep working) |
| **Cluster safety** | A task is executed by **one** instance at a time (atomic claim). Instances need a shared database and should have synchronized clocks |
| **Delivery** | **At least once.** If an instance dies mid-task the task runs again on another instance, and a pause longer than the stale threshold (a very long GC pause, a suspended VM) can make a healthy instance lose its task while still running it. Write idempotent handlers |
| **Crash recovery** | Automatic, after the heartbeat has been silent for 3× `heartbeat-interval`. Not immediate: after a crash and restart, orphaned tasks wait up to that long |
| **Retries** | None. A failed task stays `FAILURE`; create a new task to retry |
| **Timeouts / cancellation** | None |
| **Scheduling in the future** | Not supported (no "run at" field); use [`@DistributedScheduled`]({% link modules/scheduler.md %}) to create tasks periodically |
| **Cleanup** | Finished tasks stay in the table forever |
| **Ordering** | By creation time, not priority; with several instances, start order across instances is not guaranteed |

{: .note }
> Do not set `heartbeat-interval` to a very small value: the stale threshold is three times the interval, and a busy
> database or a long pause would make healthy tasks look abandoned. Do not set it to a value larger than you are
> willing to wait for crash recovery.

## Registries

| Bean | Default | Remark |
|---|---|---|
| `TaskActionRegistry` | One value, `UNKNOWN` | **Override it** to define your actions |
| `TaskObjectTypeRegistry` | none | **Required** bean |
| `TaskActionHandler` | `DefaultTaskActionHandler` — throws `UnsupportedOperationException("Not implemented: …")` | One bean per action; the first handler whose `supports()` returns `true` is used |

## Listing tasks

`GET /api/tasks?page=0&size=50&q=…&objectId=…&objectType=…&action=…&status=PENDING|RUNNING|SUCCESS|FAILURE` (any
authenticated user). `friendlyName` of task statuses is hard-coded in **Polish** (`Oczekujące`, `W toku`,
`Zakończone`, `Błąd`) and is returned by `GET /api/registries/task-statuses`.
