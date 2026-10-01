package io.github.mszajner.beanboot.auditlog.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.auditlog.api.AuditLogObjectType;
import io.github.mszajner.beanboot.utils.converters.StringMapConverter;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "object_id")
    private String objectId;

    @Column(name = "object_name")
    private String objectName;

    @Column(name = "object_type")
    @JdbcType(VarcharJdbcType.class)
    private AuditLogObjectType objectType;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "action", nullable = false)
    @JdbcType(VarcharJdbcType.class)
    private AuditLogAction action;

    @Column(name = "message")
    private String message;

    @Column(name = "parameters")
    @Convert(converter = StringMapConverter.class)
    private Map<String, String> parameters;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
