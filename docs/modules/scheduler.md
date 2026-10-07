---
title: Scheduler
parent: Modules
nav_order: 6
description: Cluster-safe cron-like jobs with @DistributedScheduled.
---

# Scheduler
{: .no_toc }

1. TOC
{:toc}

`@DistributedScheduled` makes sure that **only one application instance at a time** runs a scheduled method. It is
combined with Spring's own `@Scheduled`; beanboot only adds the locking.

```java
@Component
class NightlyCleanup {

    @Scheduled(cron = "0 0 3 * * *")
    @DistributedScheduled                      // lock name = "NightlyCleanup.cleanup"
    public void cleanup() { ... }

    @Scheduled(fixedDelay = 60_000)
    @DistributedScheduled("report-mailer")     // explicit, stable lock name
    public void mailReports() { ... }
}
```

`@EnableScheduling` is already switched on by the module (and by the tasks module).

## How it works

1. On startup each instance inserts a row into `schedulers` (`name` = `beanboot.scheduler.name`, plus a heartbeat
   interval) and then updates `lastCheckInAt` periodically — the **heartbeat**.
2. When a `@DistributedScheduled` method is invoked an AspectJ `@Around` advice tries to **acquire** the lock row in
   `scheduleds` (one row per lock name, created on first use). Acquisition uses **optimistic locking** (`@Version`): the
   instance that wins the race runs the method; the others silently skip it (the advice returns `null`).
3. After the method returns (or throws) the lock is **released**.
4. A lock held by an instance whose heartbeat is older than **2× its check-in interval** is considered abandoned and can
   be taken over by another instance — this is how crashed nodes are handled.

## Configuration

```yaml
beanboot:
  scheduler:
    name: node-1             # default: "default"
    check-in-interval: 30s   # default: 30s
```

`check-in-interval` is both the heartbeat period and the basis of the takeover threshold (2× the interval). A
shorter interval detects crashed nodes sooner but makes a briefly stalled node (long GC pause, slow database) look dead.

## Rules and caveats

* The annotation works on **methods of Spring beans** only (`@Target(METHOD)`), via the Spring proxy. Calling the method
  from within the same class skips the advice and the lock.
* The lock name is the annotation value, or `SimpleClassName.methodName`. Two beans with the same simple class name and
  method share a lock unless you give explicit names. **Renaming a method changes the lock name** if you rely on the
  default.
* The advice returns `null` when the lock is not acquired — use `void` methods (or accept `null` for object returns).
* The lock covers *execution*, not *scheduling*: all nodes still fire their `@Scheduled` trigger; every node but one
  skips. A job that finishes quickly can therefore run once on node A and again, moments later, on node B if B's
  trigger fires after A released the lock. Make jobs idempotent or guard them with your own "last run" check.
* A job running longer than 2× the check-in interval is safe **as long as the node keeps heartbeating**.
* Time comparisons use each node's own clock (`Instant.now()`); keep clocks in sync (NTP).
* **Instance rows are never cleaned up.** Every application start inserts a new row into `schedulers` and nothing
  deletes it, so the table grows by one row per restart. Prune old rows yourself if that matters.
* `beanboot.scheduler.name` is informational; it is not used to route jobs to a node.
