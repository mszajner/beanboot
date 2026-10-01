package io.github.mszajner.beanboot.security.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

public final class InvalidAuthorizationHeader extends AbstractException {
    public InvalidAuthorizationHeader() {
        super("InvalidAuthorizationHeader", UNAUTHORIZED);
    }
}
