package io.github.mszajner.beanboot.migrations.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableJpaRepositories("io.github.mszajner.beanboot.migrations.repositories")
@EntityScan("io.github.mszajner.beanboot.migrations.entities")
@ComponentScan("io.github.mszajner.beanboot.migrations")
public class BeanbootMigrationsAutoConfiguration {
}
