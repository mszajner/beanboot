package io.github.mszajner.beanboot.security.services;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.User;
import io.github.mszajner.beanboot.starter.models.AuditLogObjectType;

import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
public class UserImpl implements User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<Role> roles;

    @Override
    public String getAuditLogObjectId() {
        return id.toString();
    }

    @Override
    public String getAuditLogObjectName() {
        return firstName + " " + lastName;
    }

    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType getAuditLogObjectType() {
        return AuditLogObjectType.USER;
    }
}
