package io.github.mszajner.beanboot.tasks.executor;

import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;

/**
 * Port — one implementation per action.
 */
public interface TaskActionHandler {
    boolean supports(TaskAction action);
    void execute(TaskEntity task) throws Exception;
}
