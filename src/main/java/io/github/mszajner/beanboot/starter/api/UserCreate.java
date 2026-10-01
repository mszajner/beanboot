package io.github.mszajner.beanboot.starter.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.github.mszajner.beanboot.security.api.Role;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreate {
    String firstName;
    String lastName;
    String email;
    String password;
    Set<Role> roles;
}
