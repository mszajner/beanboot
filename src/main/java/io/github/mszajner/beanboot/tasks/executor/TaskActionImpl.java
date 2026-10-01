package io.github.mszajner.beanboot.tasks.executor;

import io.github.mszajner.beanboot.tasks.api.TaskAction;

public enum TaskActionImpl implements TaskAction {
    UNKNOWN("Nieznane");

    private final String friendlyName;

    TaskActionImpl(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String friendlyName() {
        return friendlyName;
    }
}
