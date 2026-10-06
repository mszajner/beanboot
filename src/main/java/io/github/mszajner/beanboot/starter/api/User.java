package io.github.mszajner.beanboot.starter.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.starter.models.AuditLogObjectType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
@Getter
@Setter
public class User implements io.github.mszajner.beanboot.security.api.User {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<Role> roles;
    private Set<Group> groups;
    private Instant createdAt;
    private Instant updatedAt;

    @Override
    @JsonIgnore
    public String getAuditLogObjectId() {
        return id.toString();
    }

    @Override
    @JsonIgnore
    public String getAuditLogObjectName() {
        return firstName + " " + lastName;
    }

    @Override
    @JsonIgnore
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType getAuditLogObjectType() {
        return AuditLogObjectType.USER;
    }
}
