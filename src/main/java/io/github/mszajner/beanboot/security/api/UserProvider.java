package io.github.mszajner.beanboot.security.api;

import java.util.Optional;
import java.util.UUID;

public interface UserProvider {
    Optional<User> findByEmail(String email);

    Optional<User> findById(UUID uuid);
}
