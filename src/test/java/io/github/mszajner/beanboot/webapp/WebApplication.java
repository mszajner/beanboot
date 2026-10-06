package io.github.mszajner.beanboot.webapp;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@OpenAPIDefinition(info = @Info(
        title = "BeanBoot API",
        version = "1.0",
        description = "API for managing users, groups, and roles."),
        security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@SpringBootApplication
@EnableAutoConfiguration
public class WebApplication {
    private static JdbcDatabaseContainer<?> container = startContainer();

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent event) {
        container.stop();
    }

    private static JdbcDatabaseContainer<?> startContainer() {
        JdbcDatabaseContainer<?> container = new PostgreSQLContainer<>("postgres:18");
        container.start();
        System.setProperty("db.connection", "pgsql");
        System.setProperty("db.host", container.getHost());
        System.setProperty("db.port", String.valueOf(container.getFirstMappedPort()));
        System.setProperty("db.database", container.getDatabaseName());
        System.setProperty("db.username", container.getUsername());
        System.setProperty("db.password", container.getPassword());
        return container;
    }
}
