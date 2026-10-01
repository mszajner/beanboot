package io.github.mszajner.beanboot.security.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

public final class MissingRoles extends AbstractException {
    public MissingRoles(Iterable<Role> roles) {
        super("MissingRoles", FORBIDDEN, roles);
    }
}
