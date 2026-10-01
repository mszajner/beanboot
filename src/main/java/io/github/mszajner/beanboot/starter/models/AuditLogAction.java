package io.github.mszajner.beanboot.starter.models;

public enum AuditLogAction implements io.github.mszajner.beanboot.auditlog.api.AuditLogAction {
    USER_LOGGED_IN("Zalogowanie użytkownika"),
    USER_CREATED("Utworzenie konta użytkownika"),
    USER_UPDATED("Aktualizacja konta użytkownika"),
    USER_DELETED("Usunięcie konta użytkownika"),
    GROUP_CREATED("Utworzenie grupy"),
    GROUP_UPDATED("Aktualizacja grupy"),
    GROUP_DELETED("Usunięcie grupy");

    private final String friendlyName;

    AuditLogAction(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String friendlyName() {
        return friendlyName;
    }
}
