package io.github.mszajner.beanboot.tasks.converters;

import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskObjectType;
import io.github.mszajner.beanboot.tasks.api.TaskObjectTypeRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class TaskObjectTypeDeserializer extends StdDeserializer<TaskObjectType> {
    private final TaskObjectTypeRegistry taskObjectTypeRegistry;

    public TaskObjectTypeDeserializer(TaskObjectTypeRegistry taskObjectTypeRegistry) {
        super(TaskAction.class);
        this.taskObjectTypeRegistry = taskObjectTypeRegistry;
    }

    @Override
    public TaskObjectType deserialize(JsonParser jp, DeserializationContext ctxt) {
        String taskObjectTypeName = jp.getString();
        TaskObjectType taskObjectType = taskObjectTypeRegistry.valueOf(taskObjectTypeName);
        if (taskObjectType == null) {
            throw ctxt.weirdStringException(taskObjectTypeName, TaskObjectType.class, "TaskObjectType not found in Registry");
        }
        return taskObjectType;
    }
}
