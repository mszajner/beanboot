package io.github.mszajner.beanboot.starter.api;

import io.github.mszajner.beanboot.utils.api.AbstractException;

public class EmailAlreadyExists extends AbstractException {
    public EmailAlreadyExists(String email) {
        super("EmailAlreadyExists", CONFLICT, email);
    }
}
