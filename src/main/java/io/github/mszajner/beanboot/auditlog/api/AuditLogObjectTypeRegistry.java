package io.github.mszajner.beanboot.auditlog.api;

public interface AuditLogObjectTypeRegistry {
    AuditLogObjectType[] values();
    AuditLogObjectType valueOf(String name);
}
