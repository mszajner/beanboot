package io.github.mszajner.beanboot.setup;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal Spring Boot application that serves the first-run database setup wizard.
 * Started sequentially before the main WebApplication when no external
 * application.properties exists (prod profile only).
 *
 * All database, JPA, Liquibase, security and beanboot auto-configurations are
 * excluded so the context starts cleanly without any database connection.
 */
@SpringBootApplication(
        scanBasePackages = "io.github.mszajner.beanboot.setup",
        excludeName = {
                // Spring Boot JDBC / JPA / Liquibase
                "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration",
                "org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration",
                "org.springframework.boot.jdbc.autoconfigure.DataSourceInitializationAutoConfiguration",
                "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration",
                "org.springframework.boot.jdbc.autoconfigure.JdbcClientAutoConfiguration",
                "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
                "org.springframework.boot.jpa.autoconfigure.JpaRepositoriesAutoConfiguration",
                "org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
                "org.springframework.boot.persistence.autoconfigure.PersistenceExceptionTranslationAutoConfiguration",
                // Spring Boot Security
                "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration",
                "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration",
                // io.github.mszajner.beanboot – all modules depend on DataSource
                "io.github.mszajner.beanboot.utils.config.BeanbootUtilsAutoConfiguration",
                "io.github.mszajner.beanboot.migrations.config.BeanbootMigrationsAutoConfiguration",
                "io.github.mszajner.beanboot.security.config.BeanbootSecurityAutoConfiguration",
                "io.github.mszajner.beanboot.auditlog.config.BeanbootAuditLogAutoConfiguration",
                "io.github.mszajner.beanboot.parameters.config.BeanbootParametersAutoConfiguration",
                "io.github.mszajner.beanboot.tasks.config.BeanbootTasksAutoConfiguration",
                "io.github.mszajner.beanboot.scheduler.config.BeanbootSchedulerAutoConfiguration",
                "io.github.mszajner.beanboot.licence.config.BeanbootLicenceAutoConfiguration",
                "io.github.mszajner.beanboot.starter.config.BeanbootStarterAutoConfiguration"
        }
)
public class SetupApplication {
}
