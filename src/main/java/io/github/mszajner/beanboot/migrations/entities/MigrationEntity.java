package io.github.mszajner.beanboot.migrations.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import io.github.mszajner.beanboot.migrations.models.MigrationStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "migrations")
@Getter
@Setter
public class MigrationEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MigrationStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

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
}
