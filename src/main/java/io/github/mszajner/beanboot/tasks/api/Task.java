package io.github.mszajner.beanboot.tasks.api;

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
public class Task {
    UUID id;
    Long orderBy;
    TaskAction action;
    String actionName;
    TaskStatus status;
    String statusName;
    String objectId;
    String objectName;
    TaskObjectType objectType;
    String objectTypeName;
    Map<String, String> parameters;
    String message;
    Instant startedAt;
    Instant finishedAt;
    Instant createdAt;
}
