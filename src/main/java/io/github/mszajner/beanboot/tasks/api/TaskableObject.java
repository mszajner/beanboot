package io.github.mszajner.beanboot.tasks.api;

public interface TaskableObject {
    String getTaskObjectId();
    String getTaskObjectName();
    TaskObjectType getTaskObjectType();
}
