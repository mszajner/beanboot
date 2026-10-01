package io.github.mszajner.beanboot.security.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class RoleConverter implements AttributeConverter<Role, String> {

    private final RoleRegistry roleRegistry;

    @Override
    public String convertToDatabaseColumn(Role attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public Role convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return roleRegistry.valueOf(dbData);
    }
}
