package io.github.mszajner.beanboot.auditlog.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import io.github.mszajner.beanboot.auditlog.api.AuditLog;
import io.github.mszajner.beanboot.auditlog.entities.AuditLogEntity;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {
    @Mapping(target = "actionName", expression = "java(entity.getAction() != null ? entity.getAction().friendlyName() : null)")
    @Mapping(target = "objectTypeName", expression = "java(entity.getObjectType() != null ? entity.getObjectType().friendlyName() : null)")
    AuditLog toDto(AuditLogEntity entity);
}
