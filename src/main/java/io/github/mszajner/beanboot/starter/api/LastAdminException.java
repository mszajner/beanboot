package io.github.mszajner.beanboot.starter.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

public final class LastAdminException extends AbstractException {
    public LastAdminException() {
        super("CannotDeleteLastAdmin", CONFLICT);
    }
}
