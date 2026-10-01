package io.github.mszajner.beanboot.security.aspects;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.security.api.MissingRoles;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;
import io.github.mszajner.beanboot.security.api.Unauthenticated;
import io.github.mszajner.beanboot.security.services.SecurityService;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Component
@RequiredArgsConstructor
public class RolesAllowedAspect {

    private final SecurityService securityService;
    private final RoleRegistry roleRegistry;

    @Before("@annotation(rolesAllowed)")
    public void checkRolesAllowed(RolesAllowed rolesAllowed) {
        if (!securityService.isAuthenticated()) {
            throw new Unauthenticated();
        }
        Set<Role> requiredRoles = Arrays.stream(rolesAllowed.value())
                .map(roleRegistry::valueOf)
                .collect(Collectors.toSet());
        Set<Role> userRoles = securityService.getUserRoles();
        if (requiredRoles.stream().noneMatch(userRoles::contains)) {
            throw new MissingRoles(requiredRoles.stream()
                    .map(r -> roleRegistry.valueOf(r.name()))
                    .collect(Collectors.toSet()));
        }
    }

    @Before("@annotation(permitAll)")
    public void checkPermitAll(PermitAll permitAll) {
        if (!securityService.isAuthenticated()) {
            throw new Unauthenticated();
        }
    }
}
