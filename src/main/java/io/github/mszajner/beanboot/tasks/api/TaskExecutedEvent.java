package io.github.mszajner.beanboot.tasks.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskExecutedEvent {
    UUID id;
    TaskAction action;
    TaskStatus status;
    String message;
    Instant startedAt;
    Instant finishedAt;
}
