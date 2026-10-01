package io.github.mszajner.beanboot.security.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

public class Unauthenticated extends AbstractException {
    public Unauthenticated() {
        super("Unauthenticated", UNAUTHORIZED);
    }
}
