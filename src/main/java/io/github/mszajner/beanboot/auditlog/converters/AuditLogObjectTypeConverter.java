package io.github.mszajner.beanboot.auditlog.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class AuditLogObjectTypeConverter implements AttributeConverter<AuditLogObjectType, String> {

    private final AuditLogObjectTypeRegistry auditLogObjectTypeRegistry;

    @Override
    public String convertToDatabaseColumn(AuditLogObjectType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AuditLogObjectType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return auditLogObjectTypeRegistry.valueOf(dbData);
    }
}
