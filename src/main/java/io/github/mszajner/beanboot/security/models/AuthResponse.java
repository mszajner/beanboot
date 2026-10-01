package io.github.mszajner.beanboot.security.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {
    String token;
    String id;
    String firstName;
    String lastName;
    String email;
    Set<String> roles;
}
