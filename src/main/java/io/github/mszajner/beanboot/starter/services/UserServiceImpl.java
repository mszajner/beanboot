package io.github.mszajner.beanboot.starter.services;

import dev.beanguard.client.annotations.DecreasesLicenceLimit;
import dev.beanguard.client.annotations.RequiresLicenceLimit;
import dev.beanguard.client.annotations.RequiresValidLicence;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.starter.api.*;
import io.github.mszajner.beanboot.starter.entities.UserEntity;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.auditlog.api.AuditLogService;
import io.github.mszajner.beanboot.security.api.AdminAllowed;
import io.github.mszajner.beanboot.starter.api.*;
import io.github.mszajner.beanboot.starter.mappers.UserMapper;
import io.github.mszajner.beanboot.starter.models.AuditLogAction;
import io.github.mszajner.beanboot.starter.repositories.GroupRepository;
import io.github.mszajner.beanboot.starter.repositories.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final GroupRepository groupRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional(readOnly = true)
    @PermitAll
    public List<User> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @AdminAllowed
    public User getUser(UUID id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new UserNotFound(id));
    }

    @Override
    @Transactional
    @AdminAllowed
    @RequiresValidLicence
    @RequiresLicenceLimit("users")
    public User createUser(UserCreate userCreateDto) {
        if (userRepository.findByEmail(userCreateDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExists(userCreateDto.getEmail());
        }
        var user = userMapper.toEntity(userCreateDto);
        user.setPassword(passwordEncoder.encode(userCreateDto.getPassword()));
        var savedUser = userRepository.save(user);
        auditLogService.log(AuditLogAction.USER_CREATED, savedUser);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    @AdminAllowed
    @RequiresValidLicence
    public User updateUser(UUID id, UserCreate userDto) {
        var existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFound(id));
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setRoles(userDto.getRoles());
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        var updatedUser = userRepository.save(existingUser);
        auditLogService.log(AuditLogAction.USER_UPDATED, updatedUser);
        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    @AdminAllowed
    @RequiresValidLicence
    @DecreasesLicenceLimit("users")
    public void deleteUser(UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new UserNotFound(id));
        boolean otherAdminExists = userRepository.findAll().stream()
                .filter(u -> !u.getId().equals(id))
                .anyMatch(this::hasAdminRole);
        if (!otherAdminExists) {
            throw new LastAdminException();
        }
        auditLogService.log(AuditLogAction.USER_DELETED, user);
        userRepository.deleteById(id);
    }

    private boolean hasAdminRole(UserEntity u) {
        if (u.getRoles() != null && u.getRoles().stream().anyMatch(Role::admin)) {
            return true;
        }
        return u.getGroups() != null && u.getGroups().stream()
                .anyMatch(g -> g.getRoles() != null
                        && g.getRoles().stream().anyMatch(Role::admin));
    }

    @Override
    @Transactional
    @AdminAllowed
    @RequiresValidLicence
    public User setUserGroups(UUID userId, Set<UUID> groupIds) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFound(userId));
        var groups = groupIds.stream()
                .map(id -> groupRepository.findById(id)
                        .orElseThrow(() -> new GroupNotFound(id)))
                .collect(Collectors.toSet());
        user.setGroups(groups);
        return userMapper.toDto(userRepository.save(user));
    }
}
