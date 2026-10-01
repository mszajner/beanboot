package io.github.mszajner.beanboot.tasks.executor;

import io.github.mszajner.beanboot.tasks.entities.TaskEntity;

public interface TaskExecutionService {
    void execute(TaskEntity task);
}
