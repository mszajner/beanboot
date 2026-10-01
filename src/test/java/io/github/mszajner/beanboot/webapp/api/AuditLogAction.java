package io.github.mszajner.beanboot.webapp.api;

public enum AuditLogAction implements io.github.mszajner.beanboot.auditlog.api.AuditLogAction {
    DEMO_ACTION("Akcja demonstracyjna");

    private final String friendlyName;

    AuditLogAction(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String friendlyName() {
        return friendlyName;
    }
}
