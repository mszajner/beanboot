package io.github.mszajner.beanboot.tasks.executor;

import io.github.mszajner.beanboot.tasks.api.TaskAction;

public interface TaskHandlerRegistry {
    TaskActionHandler resolve(TaskAction action);
}
