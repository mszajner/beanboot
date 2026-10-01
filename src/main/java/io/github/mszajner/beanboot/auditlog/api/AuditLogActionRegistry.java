package io.github.mszajner.beanboot.auditlog.api;

public interface AuditLogActionRegistry {
    AuditLogAction[] values();
    AuditLogAction valueOf(String name);
}
