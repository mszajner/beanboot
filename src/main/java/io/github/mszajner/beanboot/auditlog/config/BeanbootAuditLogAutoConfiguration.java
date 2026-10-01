package io.github.mszajner.beanboot.auditlog.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry;
import io.github.mszajner.beanboot.auditlog.controllers.AuditLogActionDeserializer;
import io.github.mszajner.beanboot.auditlog.controllers.AuditLogObjectTypeDeserializer;
import tools.jackson.databind.module.SimpleModule;

@AutoConfiguration
@EnableJpaRepositories("io.github.mszajner.beanboot.auditlog.repositories")
@EntityScan({
        "io.github.mszajner.beanboot.auditlog.entities",
        "io.github.mszajner.beanboot.auditlog.converters"
})
@ComponentScan("io.github.mszajner.beanboot.auditlog")
public class BeanbootAuditLogAutoConfiguration {

    @Bean
    public SimpleModule auditLogActionModule(AuditLogActionRegistry auditLogActionRegistry,
                                             AuditLogObjectTypeRegistry auditLogObjectTypeRegistry) {
        SimpleModule module = new SimpleModule("AuditLogActionModule");
        module.addDeserializer(AuditLogAction.class, new AuditLogActionDeserializer(auditLogActionRegistry));
        module.addDeserializer(AuditLogObjectType.class, new AuditLogObjectTypeDeserializer(auditLogObjectTypeRegistry));
        return module;
    }
}
