package io.github.mszajner.beanboot.tasks.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import io.github.mszajner.beanboot.tasks.api.Task;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "actionName", expression = "java(entity.getAction() != null ? entity.getAction().friendlyName() : null)")
    @Mapping(target = "statusName", expression = "java(entity.getStatus() != null ? entity.getStatus().friendlyName() : null)")
    @Mapping(target = "objectTypeName", expression = "java(entity.getObjectType() != null ? entity.getObjectType().friendlyName() : null)")
    Task toDto(TaskEntity entity);
}
