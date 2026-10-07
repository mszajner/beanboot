---
title: Utilities
parent: Modules
nav_order: 9
description: Error handling, multi-database DataSource, JSON settings and the testable clock.
---

# Utilities (`utils`)
{: .no_toc }

1. TOC
{:toc}

## Error handling: `AbstractException`

Throw a subclass of `io.github.mszajner.beanboot.utils.api.AbstractException` from anywhere in your code and the
`UtilsExceptionHandler` (`@RestControllerAdvice`) turns it into an RFC 9457 `ProblemDetail`:

```java
public final class InvoiceNotFound extends AbstractException {
    public InvoiceNotFound(UUID id) {
        super("InvoiceNotFound", NOT_FOUND, id);        // message key, HTTP status, up to two context objects
    }
}
```

Response:

```json
{ "type": "about:blank", "title": "Not Found", "status": 404,
  "detail": "InvoiceNotFound", "object1": "9c2f…", "object2": null }
```

* `detail` is the **message key** you pass to `super(...)`, not a translated sentence — let the client translate it.
* `object1` / `object2` carry context (ids, role sets…) and are serialized with Jackson, so keep them simple.
* Status constants (`BAD_REQUEST`, `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `CONFLICT`, `TOO_MANY_REQUESTS`,
  `INTERNAL_SERVER_ERROR`, …, `GATEWAY_TIMEOUT`) are `protected static` constants of the class.
* Every handled exception is **logged at ERROR with a stack trace filtered to `io.github.mszajner.beanboot` frames**
  — a normal 404 or 409 therefore produces an error log line. Frames of *your* packages are not shown.
* The constructor is `protected`; the class is `abstract`.

Not handled by the library (Spring Boot's default handling applies, typically a `500` for unexpected exceptions):
`DataIntegrityViolationException`, bean validation errors and any other `RuntimeException`. Add your own
`@RestControllerAdvice` for those. The security module additionally maps `MalformedJwtException` and
`SecurityException` to `403`, and the licence module maps `BeanGuardServerException` to `424`.

## `DataSource` and database properties

See the [configuration reference]({% link configuration.md %}#database-db) for the `db.*` properties. In addition the
module:

* registers a `HibernatePropertiesCustomizer` that sets `preferred_uuid_jdbc_type=UUID` (UUID columns use the
  database's native type where it has one) and, for `db.connection=H2`, `hibernate.dialect=…H2Dialect`;
* declares the module's **`SpringLiquibase`** bean (see [Database]({% link database.md %})).

## `InstantProvider`

A small abstraction over the clock so time-dependent code is testable:

```java
Instant now();
Instant plusMonth();
Instant plusMonth(Instant currentInstant);
Instant plusMonths(int months, Instant currentInstant);
```

Inject it instead of calling `Instant.now()` and replace it with a fixed implementation in tests.
(The library's own scheduler, task and audit log code call `Instant.now()` directly and do **not** use it, so a fixed `InstantProvider` does not control their timestamps.)

## JPA converters

* `StringMapConverter` (auto-applied) — stores `Map<String,String>` as JSON text; empty/`null` → `NULL`, and `NULL` → an
  empty mutable map.
* `InstantConverter` (auto-applied) — stores **every `Instant` attribute as a `BIGINT` of epoch milliseconds**
  (sub-millisecond precision is lost).
* `RoleSetConverter` (security) — stores `Set<Role>` as a JSON array of names.

## JSON

Beanboot configures Jackson with `FAIL_ON_NULL_FOR_PRIMITIVES` **disabled** for the whole application.
See [Configuration]({% link configuration.md %}#jackson-integration).
