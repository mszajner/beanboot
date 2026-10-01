package io.github.mszajner.beanboot.security;

import io.github.mszajner.beanboot.security.api.User;
import io.github.mszajner.beanboot.security.api.UserProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TestUserProvider implements UserProvider {

    private final List<User> users = new ArrayList<>();

    public void add(User user) {
        users.add(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return users.stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst();
    }
}
