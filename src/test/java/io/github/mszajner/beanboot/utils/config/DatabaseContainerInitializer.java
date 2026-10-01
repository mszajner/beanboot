package io.github.mszajner.beanboot.utils.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Set;

public class DatabaseContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        ConfigurableEnvironment env = context.getEnvironment();
        Set<String> activeProfiles = Set.of(env.getActiveProfiles());

        String connection;
        JdbcDatabaseContainer<?> container;

        if (activeProfiles.isEmpty() || activeProfiles.contains("pgsql")) {
            container = new PostgreSQLContainer<>("postgres:18");
            connection = "pgsql";
        } else {
            return;
        }

        container.start();

        TestPropertyValues.of(
                "db.connection=" + connection,
                "db.host=" + container.getHost(),
                "db.port=" + container.getFirstMappedPort(),
                "db.database=" + container.getDatabaseName(),
                "db.username=" + container.getUsername(),
                "db.password=" + container.getPassword()
        ).applyTo(env);
    }
}
