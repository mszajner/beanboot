package io.github.mszajner.beanboot.auditlog.api;

/**
 * Authentication details that can name the actor of an audit log entry.
 */
public interface AuditActor {
    String getName();
}
