package io.github.mszajner.beanboot.tasks.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskActionRegistry;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class TaskActionConverter implements AttributeConverter<TaskAction, String> {

    private final TaskActionRegistry taskActionRegistry;

    @Override
    public String convertToDatabaseColumn(TaskAction attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public TaskAction convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return taskActionRegistry.valueOf(dbData);
    }
}
