---
title: Setup wizard
parent: Modules
nav_order: 10
description: The optional first-run database setup wizard — and why it is not usable out of the box.
---

# Setup wizard (`setup`)
{: .no_toc }

1. TOC
{:toc}

{: .warning }
> **Status: incomplete in 0.1.2.** The wizard controller returns the views `setup/index` and `setup/complete`, but the
> library jar contains **no view templates** and no template engine (no Thymeleaf/FreeMarker/Mustache dependency).
> As published, the wizard cannot render its pages. To use it today you must provide a template engine and the two
> templates in your application, or write the configuration file yourself.

## What it is for

A separate, minimal `@SpringBootApplication` (`SetupApplication`) intended to run **before** your main application when
no configuration file exists yet. It lets an installer enter the database connection details, tests the connection and
writes them to an external properties file, after which the main application starts normally.

`SetupApplication` excludes all DataSource/JPA/Liquibase/Security auto-configurations and all beanboot
auto-configurations, so it starts without any database.

## How it is wired

Your `main` class extends `StarterApplication` and calls its protected helper:

```java
@SpringBootApplication
public class MyApplication extends StarterApplication {
    public static void main(String[] args) throws InterruptedException {
        runApplication("/etc/myapp/application.properties", MyApplication.class, args);
    }
}
```

`runApplication(defaultConfigFile, primarySource, args)`:

1. Resolves the config file: the system property **`app.config-file`** if set, otherwise `defaultConfigFile`; it is
   written back to `app.config-file` for the wizard.
2. If the file does **not exist**, starts `SetupApplication` and blocks until the wizard has finished (the
   context is closed).
3. Sets `spring.config.additional-location=optional:file:<file>` and starts your application.

## The wizard itself

* Listens on the first free TCP port from **23970** upward. If `app.port-file` is set the chosen port is written to that
  file (so an installer or launcher can open the browser).
* `GET /` redirects to `/setup`; the form asks for engine (**MSSQL, PGSQL or MYSQL** only — Oracle and H2 are not
  offered), host, port, database, user and password.
* `POST /setup` opens a JDBC connection with those values. On success it writes the properties
  `db.connection`, `db.host`, `db.port`, `db.database`, `db.username` and `db.password` to the config file, shows a
  completion page and closes the wizard context one second later.

## Caveats

* **No authentication** on the wizard: anyone who can reach the port while it is running can submit connection
  details. Bind it to localhost/the installer's network only.
* The **password is written in plain text** to the properties file (`java.util.Properties`, default file
  permissions). Restrict the file's permissions.
* The JDBC drivers for the chosen engine must be on the classpath of the wizard (they are the same classpath as your
  application).
* The wizard is only about the **database connection**; it does not create the first administrator
  (use a [data migration]({% link modules/migrations.md %})).
* An error message in the wizard is partly **Polish** (`Nieznany typ bazy danych`).
* `SetupApplication` and `StarterApplication` are part of the beanboot jar, under the package
  `io.github.mszajner.beanboot`. If your own `@SpringBootApplication` ever uses a `scanBasePackages` that includes
  `io.github.mszajner.beanboot`, you would pick up the wizard's beans. Keep your scan to your own packages.
