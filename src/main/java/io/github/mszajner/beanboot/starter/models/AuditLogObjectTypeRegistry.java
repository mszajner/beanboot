package io.github.mszajner.beanboot.starter.models;

public class AuditLogObjectTypeRegistry implements io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry {
    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType[] values() {
        return AuditLogObjectType.values();
    }

    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType valueOf(String name) {
        return AuditLogObjectType.valueOf(name);
    }
}
