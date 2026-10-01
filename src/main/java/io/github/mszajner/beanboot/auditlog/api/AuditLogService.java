package io.github.mszajner.beanboot.auditlog.api;

import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public interface AuditLogService {
    Page<AuditLog> getAuditLogs(int page, int size, String q, String objectId, AuditLogObjectType objectType, AuditLogAction action, UUID userId);
    void log(AuditLogAction action);
    void log(AuditLogAction action, AuditableObject object);
    void log(AuditLogAction action, AuditableObject object, String message);
    void log(AuditLogAction action, AuditableObject object, String message, Map<String, String> parameters);
}
