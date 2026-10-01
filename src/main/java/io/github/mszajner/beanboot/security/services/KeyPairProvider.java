package io.github.mszajner.beanboot.security.services;

import io.github.mszajner.beanboot.parameters.api.ParameterName;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface KeyPairProvider {
    KeyPair getKeyPair(ParameterName publicKeyParamName, ParameterName privateKeyParamName)
            throws NoSuchAlgorithmException, InvalidKeySpecException;
}
