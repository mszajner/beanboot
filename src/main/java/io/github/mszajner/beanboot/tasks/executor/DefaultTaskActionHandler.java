package io.github.mszajner.beanboot.tasks.executor;

import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;

public class DefaultTaskActionHandler implements TaskActionHandler {

    @Override
    public boolean supports(TaskAction action) {
        return false; // fallback — nigdy nie wybierany przez iterację, zawsze używany jako orElse
    }

    @Override
    public void execute(TaskEntity task) throws Exception {
        throw new UnsupportedOperationException("Not implemented: " + task.getAction());
    }
}
