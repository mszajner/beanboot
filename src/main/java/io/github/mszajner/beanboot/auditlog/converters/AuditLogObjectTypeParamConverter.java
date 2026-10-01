package io.github.mszajner.beanboot.auditlog.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectTypeRegistry;

@Component
@RequiredArgsConstructor
public class AuditLogObjectTypeParamConverter implements Converter<String, AuditLogObjectType> {

    private final AuditLogObjectTypeRegistry auditLogObjectTypeRegistry;

    @Override
    public AuditLogObjectType convert(String source) {
        var objectType = auditLogObjectTypeRegistry.valueOf(source);
        if (objectType == null) {
            throw new IllegalArgumentException("Unknown audit log object type: " + source);
        }
        return objectType;
    }
}
