package io.github.mszajner.beanboot.tasks.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import io.github.mszajner.beanboot.tasks.api.TaskAction;
import io.github.mszajner.beanboot.tasks.api.TaskObjectType;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.utils.api.StringMapConverter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Generated
    @Column(name = "order_by", nullable = false, insertable = false, updatable = false)
    private Long orderBy;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "object_type")
    @JdbcType(VarcharJdbcType.class)
    private TaskObjectType objectType;

    @Column(name = "action")
    @JdbcType(VarcharJdbcType.class)
    private TaskAction action;

    @Column(name = "parameters")
    @Convert(converter = StringMapConverter.class)
    private Map<String, String> parameters;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TaskStatus status;

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
