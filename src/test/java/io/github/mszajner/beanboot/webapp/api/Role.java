package io.github.mszajner.beanboot.webapp.api;

public enum Role implements io.github.mszajner.beanboot.security.api.Role {
    ADMIN,
    USER;

    @Override
    public boolean admin() {
        return ADMIN.equals(this);
    }
}
