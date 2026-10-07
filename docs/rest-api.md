---
title: REST API
nav_order: 5
description: The endpoints beanboot exposes, the authentication flow and the error format.
---

# REST API
{: .no_toc }

1. TOC
{:toc}

All endpoints below exist in your application as soon as the dependency is on the classpath (except the licence
endpoints, which need `beanboot.licence.enabled=true`). Request and response bodies are JSON.

## Authentication

`POST /auth/login` is public.

```http
POST /auth/login
Content-Type: application/json

{ "email": "ada@example.com", "password": "secret" }
```

```json
{ "token": "eyJ…", "id": "6f1e…", "firstName": "Ada", "lastName": "Admin",
  "email": "ada@example.com", "roles": ["ADMIN"] }
```

Send the token on all `/api/**` calls: `Authorization: Bearer <token>`. Details: [Security]({% link modules/security.md %}).

## Endpoints

"Auth" column: **public** = no token needed, **user** = any authenticated user, **admin** = a role with `admin() == true`.

### Users and groups

| Method & path | Auth | Body → Response |
|---|---|---|
| `GET /api/users` | user | → `User[]` |
| `GET /api/users/{id}` | admin | → `User` |
| `POST /api/users` | admin | `UserCreate` → `User` |
| `PUT /api/users/{id}` | admin | `UserCreate` → `User` |
| `DELETE /api/users/{id}` | admin | → empty |
| `PUT /api/users/{userId}/groups` | admin | `["<groupId>", …]` → `User` (replaces memberships) |
| `GET /api/groups` | user | → `Group[]` |
| `GET /api/groups/{id}` | user | → `Group` |
| `POST /api/groups` | admin | `GroupCreate` → `Group` |
| `PUT /api/groups/{id}` | admin | `Group` → `Group` |
| `DELETE /api/groups/{id}` | admin | → empty |
| `PUT /api/groups/{groupId}/users` | admin | `["<userId>", …]` → `Group` (replaces members) |

Models:

```json
// UserCreate
{ "firstName": "Ada", "lastName": "Admin", "email": "ada@example.com", "password": "…", "roles": ["ADMIN"] }
// User (response; the password is never returned)
{ "id": "…", "firstName": "…", "lastName": "…", "email": "…", "roles": ["…"], "groups": [ … ],
  "createdAt": "2026-01-01T10:00:00Z", "updatedAt": "…" }
// GroupCreate  — note the singular field name "role"
{ "name": "Editors", "role": ["EDITOR"] }
// Group (response and PUT body) — plural "roles"
{ "id": "…", "name": "Editors", "roles": ["EDITOR"], "users": [ … ], "createdAt": "…", "updatedAt": "…" }
```

Licence-enabled applications additionally require a valid license for the write operations above.

### Audit log and tasks

| Method & path | Auth | Parameters |
|---|---|---|
| `GET /api/audit-logs` | user | `page`, `size` (max 200), `q`, `objectId`, `objectType`, `action`, `userId` |
| `GET /api/tasks` | user | `page`, `size`, `q`, `objectId`, `objectType`, `action`, `status` |

Both return a Spring Data `Page`. There is no endpoint to **create** tasks or audit entries — they are created in
code. See [Audit log]({% link modules/auditlog.md %}) and [Tasks]({% link modules/tasks.md %}).

### Registries (for UI dropdowns)

| Method & path | Auth | Response |
|---|---|---|
| `GET /api/registries/audit-log-actions` | user | `{ "USER_CREATED": "Utworzenie konta użytkownika", … }` |
| `GET /api/registries/audit-log-object-types` | user | name → friendly name |
| `GET /api/registries/task-actions` | user | name → friendly name |
| `GET /api/registries/task-object-types` | user | name → friendly name |
| `GET /api/registries/task-statuses` | user | `PENDING → "Oczekujące"`, … |

The friendly names of the **starter's built-in** values (audit actions/types, task status) are in **Polish**.
Your own enums supply their own `friendlyName()`. There is no locale support; translate on the client using the
`name` keys.

### Other

| Method & path | Auth | Response |
|---|---|---|
| `GET /api/version` | user | Plain text version string (`---` when no build info) |
| `GET /api/licence`, `/api/licence/status`, `POST /api/licence/set-key`, `/api/licence/get-demo` | **public** | see [Licence]({% link modules/licence.md %}) |
| `GET /swagger-ui.html`, `GET /v3/api-docs` | **public** | OpenAPI UI / document (springdoc) |

{: .note }
> Annotate your own application class with springdoc's `@OpenAPIDefinition` / `@SecurityScheme` (as the sample app
> does) to describe the bearer-token scheme in the Swagger UI.

## Pagination format

Paged endpoints return `Page` objects. The library's bundled `application.yml` sets
`spring.data.web.pageable.serialization-mode: via-dto`, which gives the stable DTO shape:

```json
{ "content": [ … ], "page": { "size": 50, "number": 0, "totalElements": 123, "totalPages": 3 } }
```

{: .gotcha }
> If your own `application.yml` shadows the library's (see
> [Configuration]({% link configuration.md %}#defaults-shipped-inside-the-jar)), that property is **not** applied and
> Spring Data falls back to its default `Page` serialization, whose JSON shape differs. Set the property yourself.

## Error format

Errors are RFC 9457 `ProblemDetail` objects with `Content-Type: application/problem+json`:

```json
{ "type": "about:blank", "title": "Conflict", "status": 409,
  "detail": "EmailAlreadyExists", "object1": "ada@example.com", "object2": null }
```

`detail` is a **message key**; `object1`/`object2` carry context.

| Status | `detail` | When |
|---|---|---|
| 401 | `Unauthenticated` | No/invalid authentication (via the service layer) |
| 401 | *(Spring Boot error body, message `Unauthorized`)* | No/expired/invalid token on `/api/**` — answered by the security filter chain, **not** in `ProblemDetail` format |
| 403 | `AdminRequired` | `@AdminAllowed` without an admin role |
| 403 | `MissingRoles` | `@RolesAllowed` without a matching role; `object1` = required roles |
| 403 | *(JWT parser message)* | Malformed/invalid token reaching the controller advice |
| 404 | `UserNotFound`, `GroupNotFound` | Unknown id (`object1` = id) |
| 409 | `EmailAlreadyExists` | Duplicate email on user create |
| 409 | `CannotDeleteLastAdmin` | Deleting the last administrator |
| 402 | *(empty body)* | Licence enabled and no valid license |
| 424 | *(BeanGuard message)* | BeanGuard server unreachable/rejected the call |

{: .gotcha }
> Not every error uses `ProblemDetail`: authentication failures from the security filter chain and the `402` licence
> gate return Spring's default bodies (plain/`sendError`), and the interceptor that checks the token returns `403`
> with a hard-coded **Polish** message (`Brak wystarczających uprawnień`) in the error body. Clients must handle
> both shapes.

## CORS

All origins, methods `GET POST PUT DELETE OPTIONS PATCH`, headers `Authorization` and `Content-Type`, credentials
allowed, for `/api/**` and `/auth/**`. Not configurable by property — see
[Security]({% link modules/security.md %}#http-rules).
