package io.github.mszajner.beanboot.security.services;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.User;
import io.github.mszajner.beanboot.security.models.AuthResponse;

import java.util.Set;
import java.util.UUID;

public interface SecurityService {

    UUID getUserId();

    boolean isUserAdmin();

    Set<Role> getUserRoles();

    boolean isAuthenticated();

    String generateToken(UserDetails userDetails);

    Claims decryptToken(String token);

    AuthResponse authenticate(User user);
}
