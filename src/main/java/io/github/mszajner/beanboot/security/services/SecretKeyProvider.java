package io.github.mszajner.beanboot.security.services;

import io.github.mszajner.beanboot.parameters.api.ParameterName;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {
    SecretKey getSecretKey(ParameterName secretKeyParamName);
}
