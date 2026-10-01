package io.github.mszajner.beanboot.auditlog.controllers;

import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class AuditLogActionDeserializer extends StdDeserializer<AuditLogAction> {
    private final AuditLogActionRegistry auditLogActionRegistry;

    public AuditLogActionDeserializer(AuditLogActionRegistry auditLogActionRegistry) {
        super(AuditLogAction.class);
        this.auditLogActionRegistry = auditLogActionRegistry;
    }

    @Override
    public AuditLogAction deserialize(JsonParser jp, DeserializationContext ctxt) {
        String auditLogActionName = jp.getString();
        AuditLogAction auditLogAction = auditLogActionRegistry.valueOf(auditLogActionName);
        if (auditLogAction == null) {
            throw ctxt.weirdStringException(auditLogActionName, AuditLogAction.class, "AuditLogAction not found in Registry");
        }
        return auditLogAction;
    }
}
