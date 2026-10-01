package io.github.mszajner.beanboot.starter.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.github.mszajner.beanboot.security.api.Role;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Group {
    UUID id;
    String name;
    Set<Role> roles;
    Set<User> users;
    Instant createdAt;
    Instant updatedAt;
}
