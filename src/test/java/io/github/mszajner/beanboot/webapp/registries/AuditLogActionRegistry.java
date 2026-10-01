package io.github.mszajner.beanboot.webapp.registries;

import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;

import java.util.stream.Stream;

@Component
public class AuditLogActionRegistry implements io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry {

    @Override
    public AuditLogAction[] values() {
        return Stream.concat(
                        Stream.of(io.github.mszajner.beanboot.starter.models.AuditLogAction.values()),
                        Stream.of(io.github.mszajner.beanboot.webapp.api.AuditLogAction.values()))
                .toArray(AuditLogAction[]::new);
    }

    @Override
    public AuditLogAction valueOf(String name) {
        try {
            return io.github.mszajner.beanboot.starter.models.AuditLogAction.valueOf(name);
        } catch (IllegalArgumentException e) {
            return io.github.mszajner.beanboot.webapp.api.AuditLogAction.valueOf(name);
        }
    }
}
