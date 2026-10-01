package io.github.mszajner.beanboot.security.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

public final class AdminRequired extends AbstractException {
    public AdminRequired() {
        super("AdminRequired", FORBIDDEN);
    }
}
