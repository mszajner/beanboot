package io.github.mszajner.beanboot.auditlog.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class AuditLogActionConverter implements AttributeConverter<AuditLogAction, String> {

    private final AuditLogActionRegistry auditLogActionRegistry;

    @Override
    public String convertToDatabaseColumn(AuditLogAction attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public AuditLogAction convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return auditLogActionRegistry.valueOf(dbData);
    }
}
