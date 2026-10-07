---
title: Parameters
parent: Modules
nav_order: 3
description: The database-backed key/value settings store.
---

# Parameters
{: .no_toc }

1. TOC
{:toc}

`ParameterService` is a small key/value store in the `parameters` table. beanboot itself uses it to hold the **JWT
keys** and the **licence key/secret/licence**, so that they live in the database rather than in `application.yml`.
Your application can store its own runtime settings there too.

```java
@Service
@RequiredArgsConstructor
class Mailer {
    private final ParameterService parameters;

    void send() {
        String host = parameters.getString(AppParameter.SMTP_HOST);
        SmtpSettings s = parameters.getObject(AppParameter.SMTP, SmtpSettings.class);   // JSON
        parameters.setObject(AppParameter.SMTP, s);
    }
}
```

## API

| Method | Behaviour |
|---|---|
| `getString(name)` | Stored value, or the entry from `ParameterNameRegistry.defaults()`, or `null` |
| `setString(name, value)` | Upserts the row |
| `getObject(name, Class)` / `getObject(name, TypeReference)` | Reads the string and parses it as JSON with the application's `ObjectMapper` |
| `setObject(name, object)` | Serializes to JSON and stores it |

## Things to know

* **The service exists only if you provide a `ParameterNameRegistry` bean** (`@ConditionalOnBean`). Without it the
  modules that need `ParameterService` fail at startup.
* **No caching.** Every `getString` is a database query. Do not call it in hot paths; cache in your own code. As a
  result a changed value is visible immediately on all nodes.
* **Defaults are not written back** (except by the security module for the token expiration). A parameter that was never set reads from `defaults()`; changing a default in a new
  release changes the effective value for everyone who never stored one.
* **`getObject` on a missing parameter** (no row, no default) calls the JSON parser with `null` and throws.
* The value column is plain text. **Secrets (JWT keys, licence secret) are stored unencrypted**; protect database
  access and backups accordingly.
* There is **no REST endpoint** for parameters — build your own admin UI/endpoint if you need one (annotate it
  with `@AdminAllowed`).
* Values set through `setString`/`setObject` are not audited.

## Parameter names

`ParameterName` is an enum-like interface (`name()`); `ParameterNameRegistry` lists the names and supplies defaults.
`name()` is the primary key of the table. See [Extension points]({% link extension-points.md %}#parameter-names).
