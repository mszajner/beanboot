package io.github.mszajner.beanboot.webapp.api;

import lombok.Getter;

public enum ParameterName implements io.github.mszajner.beanboot.parameters.api.ParameterName {
    TOKEN_EXPIRATION("86400000"),
    TOKEN_PRIVATE_KEY(""),
    TOKEN_PUBLIC_KEY(""),
    TOKEN_SECRET_KEY(""),
    TOKEN_ISSUER("beanboot-webapp"),
    LICENCE_KEY(""),
    LICENCE_SECRET(""),
    LICENCE("");

    @Getter
    private final String defaultValue;

    ParameterName(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
