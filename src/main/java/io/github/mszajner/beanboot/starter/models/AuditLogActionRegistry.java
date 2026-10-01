package io.github.mszajner.beanboot.starter.models;

public class AuditLogActionRegistry implements io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry {
    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogAction[] values() {
        return AuditLogAction.values();
    }

    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogAction valueOf(String name) {
        return AuditLogAction.valueOf(name);
    }
}
