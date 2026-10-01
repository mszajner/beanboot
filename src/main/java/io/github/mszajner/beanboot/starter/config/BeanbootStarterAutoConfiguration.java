package io.github.mszajner.beanboot.starter.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("io.github.mszajner.beanboot.starter.repositories")
@EntityScan("io.github.mszajner.beanboot.starter.entities")
@ComponentScan("io.github.mszajner.beanboot.starter")
public class BeanbootStarterAutoConfiguration {
}
