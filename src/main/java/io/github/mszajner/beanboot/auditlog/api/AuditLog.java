package io.github.mszajner.beanboot.auditlog.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuditLog {
    Long id;
    AuditLogAction action;
    String actionName;
    String objectId;
    String objectName;
    AuditLogObjectType objectType;
    String objectTypeName;
    UUID userId;
    String userName;
    String message;
    Map<String, String> parameters;
    Instant createdAt;
}
