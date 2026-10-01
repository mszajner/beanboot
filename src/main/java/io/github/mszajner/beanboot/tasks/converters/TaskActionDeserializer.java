package io.github.mszajner.beanboot.tasks.converters;

import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskActionRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class TaskActionDeserializer extends StdDeserializer<TaskAction> {
    private final TaskActionRegistry taskActionRegistry;

    public TaskActionDeserializer(TaskActionRegistry taskActionRegistry) {
        super(TaskAction.class);
        this.taskActionRegistry = taskActionRegistry;
    }

    @Override
    public TaskAction deserialize(JsonParser jp, DeserializationContext ctxt) {
        String taskActionName = jp.getString();
        TaskAction taskAction = taskActionRegistry.valueOf(taskActionName);
        if (taskAction == null) {
            throw ctxt.weirdStringException(taskActionName, TaskAction.class, "TaskAction not found in Registry");
        }
        return taskAction;
    }
}
