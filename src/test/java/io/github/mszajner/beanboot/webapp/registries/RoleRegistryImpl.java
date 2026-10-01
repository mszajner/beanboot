package io.github.mszajner.beanboot.webapp.registries;

import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;

@Component
public class RoleRegistryImpl implements RoleRegistry {
    @Override
    public Role[] values() {
        return io.github.mszajner.beanboot.webapp.api.Role.values();
    }

    @Override
    public Role valueOf(String name) {
        return io.github.mszajner.beanboot.webapp.api.Role.valueOf(name);
    }
}
