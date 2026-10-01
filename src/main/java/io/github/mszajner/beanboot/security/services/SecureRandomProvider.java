package io.github.mszajner.beanboot.security.services;

public interface SecureRandomProvider {
    String generate(int length);
}
