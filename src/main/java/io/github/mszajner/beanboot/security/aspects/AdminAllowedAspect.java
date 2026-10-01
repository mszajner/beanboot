package io.github.mszajner.beanboot.security.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.security.api.AdminAllowed;
import io.github.mszajner.beanboot.security.api.AdminRequired;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.Unauthenticated;
import io.github.mszajner.beanboot.security.services.SecurityService;

import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class AdminAllowedAspect {

    private final SecurityService securityService;

    @Before("@annotation(adminAllowed)")
    public void checkRolesAllowed(AdminAllowed adminAllowed) {
        if (!securityService.isAuthenticated()) {
            throw new Unauthenticated();
        }
        Set<Role> userRoles = securityService.getUserRoles();
        if (userRoles.stream().noneMatch(Role::admin)) {
            throw new AdminRequired();
        }
    }
}
