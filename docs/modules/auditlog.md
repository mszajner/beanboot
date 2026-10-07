---
title: Audit log
parent: Modules
nav_order: 4
description: Recording who did what, and how to extend the audit actions.
---

# Audit log
{: .no_toc }

1. TOC
{:toc}

## Writing entries

Inject `AuditLogService` and call one of:

```java
auditLogService.log(AppAuditAction.INVOICE_ISSUED);
auditLogService.log(AppAuditAction.INVOICE_ISSUED, invoice);                       // AuditableObject
auditLogService.log(AppAuditAction.INVOICE_ISSUED, invoice, "sent by mail");       // message
auditLogService.log(AppAuditAction.INVOICE_ISSUED, invoice, "sent", Map.of("to", "a@b.c"));
```

Each entry stores: the action, the object (`id`, `name`, `type`, taken from the `AuditableObject`), the acting user
(`userId`, `userName`), a free-text `message`, a `Map<String,String>` of parameters (as JSON text) and `createdAt`.

* `log(...)` is `@Transactional` and joins the caller's transaction — the entry is rolled back with the business
  change, which is usually what you want. It does **not** use `REQUIRES_NEW`, so a failed operation leaves no trace
  (for example a rejected login is not recorded).
* The actor is read from the current `SecurityContext`. Outside a request (migrations, tasks, scheduled jobs) both
  `userId` and `userName` are `null`.

## Reading entries

`GET /api/audit-logs` (any authenticated user) with optional filters, newest first:

| Parameter | Default | Notes |
|---|---|---|
| `page` | `0` | Zero-based |
| `size` | `50` | **Silently capped at 200** |
| `q` | – | Minimum 2 characters; case-insensitive "contains" over `userName`, `objectName`, `parameters` and `message` |
| `objectId` | – | Exact match |
| `objectType` | – | Name of an `AuditLogObjectType` from your registry |
| `action` | – | Name of an `AuditLogAction` from your registry |
| `userId` | – | UUID |

`q` uses `LIKE '%…%'` on non-indexed columns; on large tables this is a full scan.

## Extending the actions

The starter ships `AuditLogAction` (`USER_LOGGED_IN`, `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `GROUP_CREATED`,
`GROUP_UPDATED`, `GROUP_DELETED`) and `AuditLogObjectType` (`USER`, `GROUP`) enums with a default registry for each.
Both defaults are `@ConditionalOnMissingBean`.

To add your own values, define **your own registry bean** — which *replaces* the default:

{: .warning }
> Your registry must return the starter's values **in addition to** yours, both in `values()` and in `valueOf()`.
> Otherwise a login or user operation writes `USER_LOGGED_IN`/`USER_CREATED`/… and reading the log later fails
> to resolve the stored name (and Jackson/`@RequestParam` conversion of those names fails).
> The same applies to `AuditLogObjectTypeRegistry` and the starter's `USER` and `GROUP`.

Full code in [Extension points]({% link extension-points.md %}#audit-log-registries).

## Retention and size

* Entries are never deleted or archived by the library. Plan your own retention (a `@DistributedScheduled` cleanup is
  a good fit).
* The entry stores denormalised names (`objectName`, `userName`) so it survives deletion of the object.
* There is **no tamper protection** (no hash chain, no write-once storage); anyone with database write access can edit
  rows.
