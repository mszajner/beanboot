package io.github.mszajner.beanboot.starter.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import io.github.mszajner.beanboot.starter.api.Group;
import io.github.mszajner.beanboot.starter.api.GroupCreate;
import io.github.mszajner.beanboot.starter.api.User;
import io.github.mszajner.beanboot.starter.entities.GroupEntity;
import io.github.mszajner.beanboot.starter.entities.UserEntity;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface GroupMapper {
    @Mapping(target = "users", qualifiedByName = "toShallowUser")
    Group toDto(GroupEntity groupEntity);

    @Mapping(target = "users", ignore = true)
    GroupEntity toEntity(Group group);

    @Named("toShallowUser")
    @Mapping(target = "groups", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toShallowUser(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GroupEntity toEntity(GroupCreate groupCreate);
}
