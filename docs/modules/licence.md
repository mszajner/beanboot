---
title: Licence (BeanGuard)
parent: Modules
nav_order: 8
description: Optional license enforcement through BeanGuard.
---

# Licence (BeanGuard)
{: .no_toc }

1. TOC
{:toc}

The `licence` module integrates [`dev.beanguard:beanguard-client`](https://github.com/mszajner/beanguard). It is
**disabled by default**; without it beanboot works with no license server.

## Enabling

```yaml
beanboot:
  licence:
    enabled: true
```

and provide a `BeanbootLicenceConfiguration` bean (**required** when enabled):

```java
@Component
class AppLicenceConfiguration implements BeanbootLicenceConfiguration {

    private final String serverUrl;
    private final String publicKey;
    private final String secretKey;

    AppLicenceConfiguration(@Value("${myapp.beanguard.url}") String serverUrl,
                            @Value("${myapp.beanguard.public-key}") String publicKey,
                            @Value("${myapp.beanguard.secret-key}") String secretKey) { ... }

    public ParameterName getLicenceParameterName()       { return AppParameter.LICENCE; }
    public ParameterName getLicenceKeyParameterName()    { return AppParameter.LICENCE_KEY; }
    public ParameterName getLicenceSecretParameterName() { return AppParameter.LICENCE_SECRET; }
    public String getServerUrl()                         { return serverUrl; }
    public String getDecryptorPublicKey()                { return publicKey; }
    public String getDecryptorSecretKey()                { return secretKey; }
}
```

| Method | Meaning |
|---|---|
| `getLicenceParameterName()` | Parameter that stores the encrypted license blob received from the server |
| `getLicenceKeyParameterName()` / `getLicenceSecretParameterName()` | Parameters that store the customer's license key and secret |
| `getServerUrl()` | URL of your BeanGuard server |
| `getDecryptorPublicKey()` / `getDecryptorSecretKey()` | Key pair used to decrypt the license |

{: .note }
> **Changed in 0.1.2 (breaking):** the server URL and decryptor keys used to be read from the properties
> `beanguard.server.url`, `beanguard.decryptor.publicKey` and `beanguard.decryptor.secretKey`. The library no longer
> reads them; supply the values through `BeanbootLicenceConfiguration`, from wherever your application keeps them.
> `getServerConfig()` builds the `ServerConfig` from your bean each time the BeanGuard client asks for it.

All three parameter names must be in your `ParameterNameRegistry` (and should have `""` defaults). The licence key,
secret and license are stored **in the database** (`parameters` table), not in a file.

## What changes when it is enabled

* **HTTP gate.** A `LicenseInterceptor` runs first (`HIGHEST_PRECEDENCE`) for `/api/**` and `/auth/**` **except
  `/api/licence/**`**. If there is no valid license it responds **`402 Payment Required` with an empty body**. This
  includes `/auth/login`: nobody can log in until a license is valid — but `/api/licence/**` stays reachable (and
  public) so a license can be installed.
* **Annotations become active.** `@RequiresValidLicence` and `@RequiresLicenceLimit("users")` (from
  `dev.beanguard.client.annotations`) on `UserServiceImpl` are enforced; when the module is disabled they are inert.
* **Endpoints** (all public — `permitAll` in the security chain):

| Endpoint | Purpose |
|---|---|
| `GET /api/licence` | License details (company, expiry, claims). Throws `MissingOrInvalidLicence` when no license is loaded |
| `GET /api/licence/status` | `{ "valid": true/false, "reason": "…" }` |
| `POST /api/licence/set-key` | Body `{ "key": "…", "secret": "…" }` — stores the key and secret and refreshes the license. On failure the previous values are restored |
| `POST /api/licence/get-demo` | Asks the BeanGuard server for a demo license (`LicenceDemoCreateRequest`) |

* **BeanGuard server errors** (`BeanGuardServerException`) are mapped to **`424 Failed Dependency`** `ProblemDetail`.

{: .warning }
> `/api/licence/**` is **unauthenticated** (`permitAll`) and is excluded from the licence gate. That includes
> `set-key`, which overwrites the stored key and secret, and `get-demo`, which makes your application call the
> BeanGuard server. Restrict these paths at your reverse proxy if the application is reachable by untrusted users.

## Usage limits

The `starter` module reports the number of users as usage of the limit `users`; creating a user is annotated with
`@RequiresLicenceLimit("users")`. Define a limit called `users` in your BeanGuard license to cap the user count.
Other limit names always report a usage of `0`.

## Disabled module

With `beanboot.licence.enabled` absent or `false`, **no** bean of the module exists: no interceptor, no endpoints, and
the annotations do nothing. `beanguard-client` is still on the classpath as a transitive dependency.
