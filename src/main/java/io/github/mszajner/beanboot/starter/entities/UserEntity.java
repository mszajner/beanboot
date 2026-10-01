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
@Table(name = "users")
@Getter
@Setter
public class UserEntity implements AuditableObject {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "group_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    private Set<GroupEntity> groups;

    @Column(name = "roles")
    @Convert(converter = RoleSetConverter.class)
    private Set<Role> roles;

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
        return firstName + " " + lastName;
    }

    @Override
    public io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType getAuditLogObjectType() {
        return AuditLogObjectType.USER;
    }
}
