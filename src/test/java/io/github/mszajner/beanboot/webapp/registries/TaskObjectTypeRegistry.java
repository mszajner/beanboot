package io.github.mszajner.beanboot.webapp.registries;

import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.tasks.api.TaskObjectType;

@Component
public class TaskObjectTypeRegistry implements io.github.mszajner.beanboot.tasks.api.TaskObjectTypeRegistry {
    @Override
    public TaskObjectType[] values() {
        return io.github.mszajner.beanboot.webapp.api.TaskObjectType.values();
    }

    @Override
    public TaskObjectType valueOf(String name) {
        return io.github.mszajner.beanboot.webapp.api.TaskObjectType.valueOf(name);
    }
}
