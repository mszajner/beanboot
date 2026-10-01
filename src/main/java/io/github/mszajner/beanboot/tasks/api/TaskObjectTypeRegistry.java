package io.github.mszajner.beanboot.tasks.api;

public interface TaskObjectTypeRegistry {
    TaskObjectType[] values();
    TaskObjectType valueOf(String name);
}
