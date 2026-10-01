package io.github.mszajner.beanboot.starter.models;

public enum AuditLogObjectType implements io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType {
    USER("Użytkownik"),
    GROUP("Grupa");

    private final String friendlyName;

    AuditLogObjectType(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String friendlyName() {
        return friendlyName;
    }
}
