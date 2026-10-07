package io.github.mszajner.beanboot.starter.config;

import io.github.mszajner.beanboot.starter.models.AuditLogActionRegistry;
import io.github.mszajner.beanboot.starter.models.AuditLogObjectTypeRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories("io.github.mszajner.beanboot.starter.repositories")
@EntityScan("io.github.mszajner.beanboot.starter.entities")
@ComponentScan("io.github.mszajner.beanboot.starter")
public class BeanbootStarterAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry.class)
    public io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry auditLogActionRegistry() {
        return new AuditLogActionRegistry();
    }

    @Bean
    @ConditionalOnMissingBean(io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry.class)
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry auditLogObjectTypeRegistry() {
        return new AuditLogObjectTypeRegistry();
    }
}
