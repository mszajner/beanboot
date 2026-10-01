package io.github.mszajner.beanboot.starter.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

import java.util.UUID;

public final class GroupNotFound extends AbstractException {
    public GroupNotFound(UUID id) {
        super("GroupNotFound", NOT_FOUND, id);
    }
}
