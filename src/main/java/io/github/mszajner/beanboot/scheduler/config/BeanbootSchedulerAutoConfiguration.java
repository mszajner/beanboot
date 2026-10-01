package io.github.mszajner.beanboot.scheduler.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(SchedulerProperties.class)
@EnableJpaRepositories("io.github.mszajner.beanboot.scheduler.repositories")
@EntityScan("io.github.mszajner.beanboot.scheduler.entities")
@ComponentScan("io.github.mszajner.beanboot.scheduler")
public class BeanbootSchedulerAutoConfiguration {
}
