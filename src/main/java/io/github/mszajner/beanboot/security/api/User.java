package io.github.mszajner.beanboot.security.api;

import io.github.mszajner.beanboot.auditlog.api.AuditableObject;

import java.util.Set;
import java.util.UUID;

public interface User extends AuditableObject {
    UUID getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPassword();
    Set<Role> getRoles();
}
