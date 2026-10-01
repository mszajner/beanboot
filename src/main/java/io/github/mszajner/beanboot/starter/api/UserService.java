package io.github.mszajner.beanboot.starter.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserService {
    List<User> getUsers();

    User getUser(UUID id);

    User createUser(UserCreate user);

    User updateUser(UUID id, UserCreate user);

    void deleteUser(UUID id);

    User setUserGroups(UUID userId, Set<UUID> groupIds);
}
