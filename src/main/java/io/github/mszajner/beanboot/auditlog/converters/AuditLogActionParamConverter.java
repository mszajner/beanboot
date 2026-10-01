package io.github.mszajner.beanboot.auditlog.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogActionRegistry;

@Component
@RequiredArgsConstructor
public class AuditLogActionParamConverter implements Converter<String, AuditLogAction> {

    private final AuditLogActionRegistry auditLogActionRegistry;

    @Override
    public AuditLogAction convert(String source) {
        var action = auditLogActionRegistry.valueOf(source);
        if (action == null) {
            throw new IllegalArgumentException("Unknown audit log action: " + source);
        }
        return action;
    }
}
