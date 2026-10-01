package io.github.mszajner.beanboot.starter.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.User;
import io.github.mszajner.beanboot.security.api.UserProvider;
import io.github.mszajner.beanboot.starter.entities.UserEntity;
import io.github.mszajner.beanboot.starter.mappers.UserMapper;
import io.github.mszajner.beanboot.starter.repositories.UserRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserProviderImpl implements UserProvider {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::map);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID uuid) {
        return userRepository.findById(uuid).map(this::map);
    }

    private User map(UserEntity user) {
        io.github.mszajner.beanboot.starter.api.User result = userMapper.toDtoWithPassword(user);
        Set<Role> roles = Objects.nonNull(result.getRoles()) ? new HashSet<>(result.getRoles()) : new HashSet<>();
        user.getGroups().forEach(g -> roles.addAll(g.getRoles()));
        result.setRoles(roles);
        return result;
    }
}
