package io.github.mszajner.beanboot.tasks.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskActionRegistry;

@Component
@RequiredArgsConstructor
public class TaskActionParamConverter implements Converter<String, TaskAction> {

    private final TaskActionRegistry taskActionRegistry;

    @Override
    public TaskAction convert(String source) {
        var action = taskActionRegistry.valueOf(source);
        if (action == null) {
            throw new IllegalArgumentException("Unknown task action: " + source);
        }
        return action;
    }
}
