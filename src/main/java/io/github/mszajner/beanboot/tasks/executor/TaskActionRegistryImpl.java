package io.github.mszajner.beanboot.tasks.executor;

import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskActionRegistry;

public class TaskActionRegistryImpl implements TaskActionRegistry {
    @Override
    public TaskAction[] values() {
        return TaskActionImpl.values();
    }

    @Override
    public TaskAction valueOf(String name) {
        return TaskActionImpl.valueOf(name);
    }
}
