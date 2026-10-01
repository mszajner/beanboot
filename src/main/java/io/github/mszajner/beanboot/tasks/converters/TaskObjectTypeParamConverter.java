package io.github.mszajner.beanboot.tasks.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.tasks.api.TaskObjectType;
import io.github.mszajner.beanboot.tasks.api.TaskObjectTypeRegistry;

@Component
@RequiredArgsConstructor
public class TaskObjectTypeParamConverter implements Converter<String, TaskObjectType> {

    private final TaskObjectTypeRegistry taskObjectTypeRegistry;

    @Override
    public TaskObjectType convert(String source) {
        var objectType = taskObjectTypeRegistry.valueOf(source);
        if (objectType == null) {
            throw new IllegalArgumentException("Unknown task object type: " + source);
        }
        return objectType;
    }
}
