package io.github.mszajner.beanboot.auditlog.controllers;

import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class AuditLogObjectTypeDeserializer extends StdDeserializer<AuditLogObjectType> {
    private final AuditLogObjectTypeRegistry auditLogObjectTypeRegistry;

    public AuditLogObjectTypeDeserializer(AuditLogObjectTypeRegistry auditLogObjectTypeRegistry) {
        super(AuditLogAction.class);
        this.auditLogObjectTypeRegistry = auditLogObjectTypeRegistry;
    }

    @Override
    public AuditLogObjectType deserialize(JsonParser jp, DeserializationContext ctxt) {
        String auditLogObjectTypeName = jp.getString();
        AuditLogObjectType auditLogObjectType = auditLogObjectTypeRegistry.valueOf(auditLogObjectTypeName);
        if (auditLogObjectType == null) {
            throw ctxt.weirdStringException(auditLogObjectTypeName, AuditLogObjectType.class, "AuditLogObjectType not found in Registry");
        }
        return auditLogObjectType;
    }
}
