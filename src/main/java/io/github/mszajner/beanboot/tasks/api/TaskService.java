package io.github.mszajner.beanboot.tasks.api;

import org.springframework.data.domain.Page;

import java.util.Map;

public interface TaskService {

    Page<Task> getTasks(int page, int size, String q, String objectId, TaskObjectType objectType, TaskAction action, TaskStatus status);

    Task createTask(TaskAction action);

    Task createTask(TaskAction action, TaskableObject object);

    Task createTask(TaskAction action, TaskableObject object, Map<String, String> parameters);
}
