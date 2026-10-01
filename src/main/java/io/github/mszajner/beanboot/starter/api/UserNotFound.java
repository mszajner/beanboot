package io.github.mszajner.beanboot.starter.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

import java.util.UUID;

public final class UserNotFound extends AbstractException {
    public UserNotFound(UUID id) {
        super("UserNotFound", NOT_FOUND, id);
    }
}
