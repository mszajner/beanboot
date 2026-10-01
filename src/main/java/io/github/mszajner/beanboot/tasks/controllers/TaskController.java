package io.github.mszajner.beanboot.tasks.controllers;

import io.github.mszajner.beanboot.tasks.api.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.github.mszajner.beanboot.tasks.api.*;

@Validated
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public Page<Task> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @Size(min = 2) String q,
            @RequestParam(required = false) String objectId,
            @RequestParam(required = false) TaskObjectType objectType,
            @RequestParam(required = false) TaskAction action,
            @RequestParam(required = false) TaskStatus status) {
        return taskService.getTasks(page, size, q, objectId, objectType, action, status);
    }
}
