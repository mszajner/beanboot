package io.github.mszajner.beanboot.auditlog.api;

public interface AuditableObject {
    String getAuditLogObjectId();
    String getAuditLogObjectName();
    AuditLogObjectType getAuditLogObjectType();
}
