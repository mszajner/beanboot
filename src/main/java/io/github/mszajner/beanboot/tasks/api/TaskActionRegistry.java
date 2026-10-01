package io.github.mszajner.beanboot.tasks.api;

public interface TaskActionRegistry {
    TaskAction[] values();
    TaskAction valueOf(String name);
}
