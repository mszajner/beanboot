package io.github.mszajner.beanboot.starter.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface GroupService {
    List<Group> getGroups();

    Group getGroup(UUID id);

    Group createGroup(GroupCreate groupCreate);

    Group updateGroup(UUID id, Group group);

    void deleteGroup(UUID id);

    Group setGroupUsers(UUID groupId, Set<UUID> userIds);
}
