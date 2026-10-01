package io.github.mszajner.beanboot.starter.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import io.github.mszajner.beanboot.starter.api.Group;
import io.github.mszajner.beanboot.starter.api.User;
import io.github.mszajner.beanboot.starter.api.UserCreate;
import io.github.mszajner.beanboot.starter.entities.GroupEntity;
import io.github.mszajner.beanboot.starter.entities.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "groups", qualifiedByName = "toShallowGroup")
    @Mapping(target = "password", ignore = true)
    User toDto(UserEntity userEntity);

    @Mapping(target = "groups", qualifiedByName = "toShallowGroup")
    User toDtoWithPassword(UserEntity userEntity);

    @Named("toShallowGroup")
    @Mapping(target = "users", ignore = true)
    Group toShallowGroup(GroupEntity groupEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity toEntity(UserCreate userDto);
}
