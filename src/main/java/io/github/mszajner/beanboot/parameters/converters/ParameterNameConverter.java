package io.github.mszajner.beanboot.parameters.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterNameRegistry;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class ParameterNameConverter implements AttributeConverter<ParameterName, String> {

    private final ParameterNameRegistry parameterNameRegistry;

    @Override
    public String convertToDatabaseColumn(ParameterName attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public ParameterName convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return parameterNameRegistry.valueOf(dbData);
    }
}
