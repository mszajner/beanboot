package io.github.mszajner.beanboot.security.api;

public interface RoleRegistry {
    Role[] values();
    Role valueOf(String name);
}
