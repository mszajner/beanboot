package io.github.mszajner.beanboot.auditlog.services;

import io.github.mszajner.beanboot.auditlog.api.*;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.auditlog.api.*;
import io.github.mszajner.beanboot.auditlog.entities.AuditLogEntity;
import io.github.mszajner.beanboot.auditlog.mappers.AuditLogMapper;
import io.github.mszajner.beanboot.auditlog.repositories.AuditLogRepository;
import io.github.mszajner.beanboot.auditlog.api.AuditActor;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    @PermitAll
    public Page<AuditLog> getAuditLogs(int page, int size, String q, String objectId, AuditLogObjectType objectType, AuditLogAction action, UUID userId) {
        var clampedSize = Math.min(size, 200);
        var pageable = PageRequest.of(page, clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        var spec = buildSpec(q, objectId, objectType, action, userId);
        return auditLogRepository.findAll(spec, pageable)
                .map(auditLogMapper::toDto);
    }

    private Specification<AuditLogEntity> buildSpec(String q, String objectId, AuditLogObjectType objectType, AuditLogAction action, UUID userId) {
        Specification<AuditLogEntity> spec = (root, query, cb) -> cb.conjunction();
        if (q != null) {
            var pattern = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("userName")), pattern),
                    cb.like(cb.lower(root.get("objectName")), pattern),
                    cb.like(cb.lower(root.get("parameters")), pattern),
                    cb.like(cb.lower(root.get("message")), pattern)
            ));
        }
        if (objectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("objectId"), objectId));
        }
        if (objectType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("objectType"), objectType));
        }
        if (action != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        return spec;
    }

    @Override
    @Transactional
    public void log(AuditLogAction action) {
        log(action, null, null, null);
    }

    @Override
    @Transactional
    public void log(AuditLogAction action, AuditableObject object) {
        log(action, object, null, null);
    }

    @Override
    @Transactional
    public void log(AuditLogAction action, AuditableObject object, String message) {
        log(action, object, message, null);
    }

    @Override
    @Transactional
    public void log(AuditLogAction action, AuditableObject object, String message, Map<String, String> parameters) {
        var entry = new AuditLogEntity();
        entry.setAction(action);
        entry.setUserId(currentUserId());
        entry.setUserName(currentUserName());
        if (Objects.nonNull(object)) {
            entry.setObjectId(object.getAuditLogObjectId());
            entry.setObjectName(object.getAuditLogObjectName());
            entry.setObjectType(object.getAuditLogObjectType());
        }
        entry.setMessage(message);
        entry.setParameters(parameters);
        auditLogRepository.save(entry);
    }

    private String currentUserName() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        if (auth.getDetails() instanceof AuditActor userDetails) {
            return userDetails.getName();
        }
        if (auth.getDetails() instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return null;
    }

    private UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return UUID.fromString(auth.getName());
    }
}
