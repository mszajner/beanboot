package io.github.mszajner.beanboot.starter.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry;
import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskActionRegistry;
import io.github.mszajner.beanboot.tasks.api.TaskObjectType;
import io.github.mszajner.beanboot.tasks.api.TaskObjectTypeRegistry;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/registries")
@RequiredArgsConstructor
@Tag(name = "Registry")
public class RegistryController {

    private final AuditLogActionRegistry auditLogActionRegistry;
    private final AuditLogObjectTypeRegistry auditLogObjectTypeRegistry;
    private final TaskActionRegistry taskActionRegistry;
    private final TaskObjectTypeRegistry taskObjectTypeRegistry;

    @GetMapping("/audit-log-actions")
    public Map<String, String> getAuditLogActions() {
        return Arrays.stream(auditLogActionRegistry.values())
                .collect(Collectors.toMap(AuditLogAction::name, AuditLogAction::friendlyName,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @GetMapping("/audit-log-object-types")
    public Map<String, String> getAuditLogObjectTypes() {
        return Arrays.stream(auditLogObjectTypeRegistry.values())
                .collect(Collectors.toMap(AuditLogObjectType::name, AuditLogObjectType::friendlyName,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @GetMapping("/task-actions")
    public Map<String, String> getTaskActions() {
        return Arrays.stream(taskActionRegistry.values())
                .collect(Collectors.toMap(TaskAction::name, TaskAction::friendlyName,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @GetMapping("/task-object-types")
    public Map<String, String> getTaskObjectTypes() {
        return Arrays.stream(taskObjectTypeRegistry.values())
                .collect(Collectors.toMap(TaskObjectType::name, TaskObjectType::friendlyName,
                        (a, b) -> a, LinkedHashMap::new));
    }

    @GetMapping("/task-statuses")
    public Map<String, String> getTaskStatuses() {
        return Arrays.stream(TaskStatus.values())
                .collect(Collectors.toMap(TaskStatus::name, TaskStatus::friendlyName,
                        (a, b) -> a, LinkedHashMap::new));
    }
}
