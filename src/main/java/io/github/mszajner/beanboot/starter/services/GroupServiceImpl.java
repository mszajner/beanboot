package io.github.mszajner.beanboot.starter.services;

import io.github.mszajner.beanboot.starter.api.*;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.auditlog.api.AuditLogService;
import io.github.mszajner.beanboot.security.api.AdminAllowed;
import io.github.mszajner.beanboot.starter.api.*;
import io.github.mszajner.beanboot.starter.mappers.GroupMapper;
import io.github.mszajner.beanboot.starter.models.AuditLogAction;
import io.github.mszajner.beanboot.starter.repositories.GroupRepository;
import io.github.mszajner.beanboot.starter.repositories.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    @PermitAll
    public List<Group> getGroups() {
        return groupRepository.findAll().stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PermitAll
    public Group getGroup(UUID id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDto)
                .orElseThrow(() -> new GroupNotFound(id));
    }

    @Override
    @Transactional
    @AdminAllowed
    public Group createGroup(GroupCreate groupCreate) {
        var group = groupMapper.toEntity(groupCreate);
        var savedGroup = groupRepository.save(group);
        auditLogService.log(AuditLogAction.GROUP_CREATED, savedGroup);
        return groupMapper.toDto(savedGroup);
    }

    @Override
    @Transactional
    @AdminAllowed
    public Group updateGroup(UUID id, Group group) {
        var existingGroup = groupRepository.findById(id)
                .orElseThrow(() -> new GroupNotFound(id));
        existingGroup.setName(group.getName());
        existingGroup.setRoles(group.getRoles());
        var updatedGroup = groupRepository.save(existingGroup);
        auditLogService.log(AuditLogAction.GROUP_UPDATED, updatedGroup);
        return groupMapper.toDto(updatedGroup);
    }

    @Override
    @Transactional
    @AdminAllowed
    public void deleteGroup(UUID id) {
        var group = groupRepository.findById(id).orElseThrow(() -> new GroupNotFound(id));
        auditLogService.log(AuditLogAction.GROUP_DELETED, group);
        groupRepository.deleteById(id);
    }

    @Override
    @Transactional
    @AdminAllowed
    public Group setGroupUsers(UUID groupId, Set<UUID> userIds) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFound(groupId));
        var users = userIds.stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFound(id)))
                .collect(Collectors.toSet());
        group.setUsers(users);
        return groupMapper.toDto(groupRepository.save(group));
    }
}
