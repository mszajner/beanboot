package io.github.mszajner.beanboot.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import io.github.mszajner.beanboot.tasks.api.TaskCreatedEvent;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.tasks.executor.TaskExecutionService;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Executes {@code PENDING} tasks one at a time on a daemon thread.
 * <p>
 * Safe to run on several instances: a task is <em>claimed</em> with an atomic conditional update before it is executed,
 * so only one instance runs it. While a task runs, its heartbeat is refreshed; a {@code RUNNING} task whose heartbeat
 * stops (the instance died) is returned to {@code PENDING} by {@link StaleTaskRecoverer}.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class TaskDispatcher implements ApplicationRunner {

    private final TaskRepository taskRepository;
    private final TaskExecutionService taskExecutionService;
    private final StaleTaskRecoverer staleTaskRecoverer;

    private final Semaphore semaphore = new Semaphore(0);
    private volatile UUID currentTaskId;

    @Override
    public void run(ApplicationArguments args) {
        staleTaskRecoverer.recover();
        var thread = new Thread(this::executorLoop, "task-dispatcher");
        thread.setDaemon(true);
        thread.start();
        log.info("Task dispatcher started");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreated(TaskCreatedEvent event) {
        semaphore.release();
    }

    @Scheduled(fixedDelay = 60_000)
    public void scheduledRelease() {
        semaphore.release();
    }

    @Scheduled(fixedDelayString = "${beanboot.tasks.heartbeat-interval:30s}")
    public void heartbeat() {
        var taskId = currentTaskId;
        if (taskId != null) {
            taskRepository.heartbeat(taskId, Instant.now());
        }
    }

    private void executorLoop() {
        while (true) {
            try {
                // tryAcquire is used solely for timing/wakeup; DB is always queried regardless
                // noinspection ResultOfMethodCallIgnored — result intentionally ignored;
                semaphore.tryAcquire(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Task dispatcher thread interrupted, shutting down");
                break;
            }
            try {
                staleTaskRecoverer.recover();
                executePending();
            } catch (Exception e) {
                log.error("Unexpected error in task dispatcher loop", e);
            }
        }
    }

    /**
     * Executes tasks until none is {@code PENDING}. Losing the race for a task to another instance simply moves on to
     * the next one.
     */
    void executePending() {
        while (!Thread.currentThread().isInterrupted()) {
            var next = taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING);
            if (next.isEmpty()) {
                return;
            }
            var taskId = next.get().getId();
            if (taskRepository.claim(taskId, Instant.now()) == 0) {
                continue;
            }
            currentTaskId = taskId;
            try {
                // reload: the claim was a bulk update, the entity read above is stale
                taskRepository.findById(taskId).ifPresent(taskExecutionService::execute);
            } finally {
                currentTaskId = null;
            }
        }
    }
}
