package io.github.mszajner.beanboot.webapp.api;

public enum TaskObjectType implements io.github.mszajner.beanboot.tasks.api.TaskObjectType {
    USER("Użytkownik");

    private final String friendlyName;

    TaskObjectType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String friendlyName() {
        return friendlyName;
    }
}
