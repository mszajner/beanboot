package io.github.mszajner.beanboot.starter.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import io.github.mszajner.beanboot.auditlog.api.AuditableObject;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.converters.RoleSetConverter;
import io.github.mszajner.beanboot.starter.models.AuditLogObjectType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "groups")
@Getter
@Setter
public class GroupEntity implements AuditableObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    @Column(name = "roles")
    @Convert(converter = RoleSetConverter.class)
    private Set<Role> roles;

    @ManyToMany
    @JoinTable(
            name = "group_users",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> users;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @Override
    public String getAuditLogObjectId() {
        return id.toString();
    }

    @Override
    public String getAuditLogObjectName() {
        return name;
    }

    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType getAuditLogObjectType() {
        return AuditLogObjectType.GROUP;
    }
}
