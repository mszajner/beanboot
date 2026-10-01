package io.github.mszajner.beanboot.tasks.api;

public enum TaskStatus {
    PENDING("Oczekujące"),
    RUNNING("W toku"),
    SUCCESS("Zakończone"),
    FAILURE("Błąd");

    private final String friendlyName;

    TaskStatus(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String friendlyName() {
        return this.friendlyName;
    }
}
