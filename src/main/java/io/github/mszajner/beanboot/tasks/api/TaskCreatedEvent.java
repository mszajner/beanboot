package io.github.mszajner.beanboot.tasks.api;

/**
 * Published after a TaskEntity is saved with status PENDING.
 * Carries no payload — the DB is the source of truth.
 * Used to wake the TaskDispatcher immediately instead of waiting for the polling fallback.
 */
public class TaskCreatedEvent {
}
