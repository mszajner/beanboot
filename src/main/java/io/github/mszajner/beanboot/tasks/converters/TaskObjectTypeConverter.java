package io.github.mszajner.beanboot.tasks.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.tasks.api.TaskObjectType;
import io.github.mszajner.beanboot.tasks.api.TaskObjectTypeRegistry;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class TaskObjectTypeConverter implements AttributeConverter<TaskObjectType, String> {

    private final TaskObjectTypeRegistry taskObjectTypeRegistry;

    @Override
    public String convertToDatabaseColumn(TaskObjectType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public TaskObjectType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return taskObjectTypeRegistry.valueOf(dbData);
    }
}
