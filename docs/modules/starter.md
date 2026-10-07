---
title: Users and groups (starter)
parent: Modules
nav_order: 1
description: The User and Group domain, REST controllers and licence usage counter.
---

# Users and groups (`starter`)
{: .no_toc }

1. TOC
{:toc}

## Domain

* **User** — `id` (UUID), `firstName`, `lastName`, `email` (unique), BCrypt `password`, `roles` (set), `groups`,
  `createdAt`, `updatedAt`.
* **Group** — `id`, `name`, `roles`, `users`, `createdAt`, `updatedAt`.

A user's **effective roles** are the union of the user's own roles and the roles of all groups they belong to. This is
what ends up in the JWT (see [Security]({% link modules/security.md %})).

Tables: `users`, `groups`, `group_users` (see [Database]({% link database.md %})).

## Authorization of the built-in operations

| Operation | Rule |
|---|---|
| list users, list groups, get group | any authenticated user (`@PermitAll`) |
| get user | admin |
| create / update / delete user, set user groups | admin (also require a valid licence if licensing is enabled) |
| create / update / delete group, set group users | admin |

{: .warning }
> `GET /api/users` and `GET /api/groups` are `@PermitAll` — **any authenticated user** (even one with the least
> privileged role) receives every user's name, email, roles and groups. Wrap the services or add your own filter if
> that is not acceptable. The same holds for the [audit log]({% link modules/auditlog.md %}) and
> [tasks]({% link modules/tasks.md %}) listings.

## Behaviour worth knowing

* **Email uniqueness** is checked on create (`409 EmailAlreadyExists`). On **update** there is no such check: changing
  the email to one used by another user is rejected only by the database's unique constraint, so the client gets a
  generic `500`/data-integrity error instead of `EmailAlreadyExists`.
* **The last administrator cannot be deleted** (`409 CannotDeleteLastAdmin`). An administrator is any user with an
  `admin()` role directly **or through a group**.
* An update with a blank/missing `password` keeps the old password. A password is required on create, there is no
  validation of its strength.
* An update **replaces** `roles` with the supplied set (sending `null` clears them).
* Deleting a user or group is a hard delete (audit entries keep the denormalised `userName`/`objectName`).
* Creating, updating and deleting users and groups writes audit log entries
  (`USER_CREATED`, `USER_UPDATED`, `USER_DELETED`, `GROUP_CREATED`, `GROUP_UPDATED`, `GROUP_DELETED`).
* There is **no password reset, "change my password", "forgot password", email verification or account
  lockout** feature, and no self-registration. The `PUT /api/users/{id}` endpoint requires an admin.
* The JSON for creating a group uses the field name **`role`** (singular — `GroupCreate.role`), whereas reading a
  group returns **`roles`**.

## Licence usage counter

`UsageRegistryImpl` reports the number of rows in `users` as the usage of the licence limit named `users` (used by
`@RequiresLicenceLimit("users")` on user creation). Increment/decrement callbacks are no-ops: the count is always read
from the database. Other limit names report `0`. See [Licence]({% link modules/licence.md %}).

## Registries provided by the starter

The starter provides default `AuditLogActionRegistry` and `AuditLogObjectTypeRegistry` beans that return only the
starter's own values (`io.github.mszajner.beanboot.starter.models.AuditLogAction` / `AuditLogObjectType`). If your
application defines its own, **it must include those values** — see [Audit log]({% link modules/auditlog.md %}).

## Other endpoints

* `GET /api/version` — returns the build version if Spring Boot `BuildProperties` exist
  (`spring-boot-maven-plugin` goal `build-info`), otherwise `---`. For `-SNAPSHOT` builds the word `SNAPSHOT` is
  replaced by the build timestamp (`yyyy.MM.dd.HH.mm`, server time zone).
* `GET /api/registries/*` — read-only maps of `name → friendlyName` for audit actions, audit object types, task
  actions, task object types and task statuses. Useful for populating UI dropdowns.

The full list of endpoints is in [REST API]({% link rest-api.md %}).
