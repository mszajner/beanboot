---
title: Security
parent: Modules
nav_order: 2
description: JWT authentication, role-based authorization and what the HTTP security rules are.
---

# Security
{: .no_toc }

1. TOC
{:toc}

## Authentication flow

1. The client calls `POST /auth/login` with `{"email": "...", "password": "..."}`.
2. The user is looked up with `UserProvider.findByEmail` (an unknown email → `401 Unauthenticated`). The password is
   checked through Spring Security's `AuthenticationManager` with a **BCrypt** `PasswordEncoder`.
3. An audit log entry is written with the action returned by
   `BeanbootSecurityConfiguration.getUserLoggedInAuditLogAction()`.
4. The response contains the token and the user's basic data:

   ```json
   { "token": "eyJ...", "id": "…uuid…", "firstName": "Ada", "lastName": "Admin",
     "email": "ada@example.com", "roles": ["ADMIN"] }
   ```

5. The client sends `Authorization: Bearer <token>` on every request.

Failed login attempts are **not** audited, throttled or locked out. See [Known issues]({% link known-issues.md %}).

## The token

Tokens are *signed and then encrypted*:

* a JWS signed with **RS256** (RSA key pair generated on first start) carrying `sub` (user UUID), `iss`, `iat`, `exp`,
  `jti` and a `roles` claim (comma-separated role names);
* wrapped in a JWE encrypted with **A256GCM** using an AES-256 secret key (also generated on first start).

Therefore the token is opaque to the client — you cannot read the claims in the browser; use the `roles` array from the
login response for UI decisions.

| Aspect | Behaviour |
|---|---|
| Lifetime | Parameter you map as `getTokenExpirationParameterName()`, **in milliseconds** |
| Keys | Created on first start and stored **as Base64 in the `parameters` table** |
| Roles in token | Effective roles = the user's own roles **plus the roles of all their groups**, computed **at login** |
| Refresh / revocation | **None.** There is no refresh token, no logout endpoint and no denylist |
| Algorithm / key size | Fixed (RS256 / A256GCM); not configurable |

Consequences:

* Changing a user's roles or group membership takes effect **only after the user logs in again** (or the old token
  expires). Deleting a user does not invalidate tokens either; requests fail only when the service layer needs the user.
* Rotating keys means deleting the three key parameters from the database and restarting; **all** issued tokens become
  invalid at once.
* In a cluster every node reads the same keys from the database, so tokens are valid on all nodes — but the keys are
  read **once at startup**. Starting the first two nodes simultaneously on an empty database can make each generate its
  own key pair; the last writer wins and the other node keeps its in-memory keys until restart. Start one node first.

## HTTP rules

The `security` module registers one `SecurityFilterChain` (`@Order(1)`):

| Path | Rule |
|---|---|
| `/api/licence`, `/api/licence/**` | `permitAll` |
| `/api/**` | authenticated |
| **everything else** (`/auth/login`, `/swagger-ui/**`, `/v3/api-docs`, **your own non-`/api` endpoints**, actuator, static files) | `permitAll` |

Other settings: stateless sessions, **CSRF disabled**, CORS enabled, `401` for unauthenticated and `403` for access
denied.

{: .warning }
> Anything you expose **outside `/api/**` is public by default.** Put your own REST endpoints under `/api/` or add
> your own `SecurityFilterChain` with a lower `@Order` value (higher precedence) and a `securityMatcher`.

CORS is configured for `/api/**` and `/auth/**`: **every origin** (`allowedOriginPatterns("*")`), methods
`GET, POST, PUT, DELETE, OPTIONS, PATCH`, headers `Authorization` and `Content-Type`, **credentials allowed**. There
is no property to restrict origins. Combined with bearer tokens (not cookies) this is not exploitable by CSRF, but you
may want to restrict origins for defence in depth; the supported way is to add your own `SecurityFilterChain` /
`CorsConfigurationSource`.

## Authorization annotations

Authorization is implemented with **AspectJ advice on annotations**, not with filters:

| Annotation | Package | Meaning |
|---|---|---|
| `@AdminAllowed` | `io.github.mszajner.beanboot.security.api` | The caller must have at least one role whose `admin()` returns `true` |
| `@RolesAllowed({"EDITOR","ADMIN"})` | `jakarta.annotation.security` | The caller must have **at least one** of the listed roles (names are resolved through your `RoleRegistry`) |
| `@PermitAll` | `jakarta.annotation.security` | The caller must merely be **authenticated** |

```java
@Service
public class InvoiceService {

    @AdminAllowed
    public void deleteInvoice(UUID id) { ... }

    @RolesAllowed({"EDITOR", "ADMIN"})
    public Invoice update(UUID id, InvoiceUpdate update) { ... }

    @PermitAll
    public List<Invoice> list() { ... }
}
```

Failures throw (and are rendered as `ProblemDetail`, see [REST API]({% link rest-api.md %})):

| Exception | HTTP | When |
|---|---|---|
| `Unauthenticated` | 401 | No authenticated principal |
| `AdminRequired` | 403 | `@AdminAllowed` and no admin role |
| `MissingRoles` | 403 | `@RolesAllowed` and none of the roles match (the required roles are returned in `object1`) |

{: .gotcha }
> The annotations only work where Spring AOP can intercept the call:
>
> * on **methods** of Spring beans (they have `@Target(METHOD)` — class-level annotations are ignored);
> * only when called **through the Spring proxy** — a call from another method of the same class (`this.foo()`)
>   bypasses the check;
> * `private`/`final`/`static` methods are not intercepted.
>
> An unannotated method is **not protected by these aspects** (the HTTP rule above still demands authentication for
> `/api/**`). A typo in a role name inside `@RolesAllowed` is only detected at call time
> (whatever your `RoleRegistry.valueOf` throws for an unknown name).

## Reading the current user

`SecurityService` is an internal bean; the supported way to obtain the caller is Spring's `SecurityContextHolder`:

```java
var auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
var details = (UserDetails) auth.getPrincipal();
UUID userId = UUID.fromString(details.getUsername());   // the username is the user's UUID
```

The `roles`, `userId` and `isAdmin` values are also set as request attributes by `TokenRequestFilter`.

## Replacing or extending the security configuration

`SecurityConfiguration` is a plain, unconditional `@Configuration`. Consequences:

* You cannot disable it. To add endpoints with different rules, declare an additional `SecurityFilterChain` with a
  **lower** `@Order` than `1` and a `securityMatcher(...)`.
* It declares a `PasswordEncoder` (`BCryptPasswordEncoder`) and an `AuthenticationManager`. Declaring your own bean of
  the same type results in two candidates (ambiguous injection) — implement your needs around BCrypt instead.
* Password policy (length, complexity, history) is **not** enforced anywhere.

## Parameters used

The five parameters named by `BeanbootSecurityConfiguration` (issuer, expiration, public key, private key, secret key).
See [Extension points]({% link extension-points.md %}#parameter-names).
