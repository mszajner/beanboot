package io.github.mszajner.beanboot.auditlog.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.github.mszajner.beanboot.auditlog.api.AuditLog;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogService;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public Page<AuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @Size(min = 2) String q,
            @RequestParam(required = false) String objectId,
            @RequestParam(required = false) AuditLogObjectType objectType,
            @RequestParam(required = false) AuditLogAction action,
            @RequestParam(required = false) UUID userId) {
        return auditLogService.getAuditLogs(page, size, q, objectId, objectType, action, userId);
    }
}
