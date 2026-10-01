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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Log4j2
public class TaskDispatcher implements ApplicationRunner {

    private final TaskRepository taskRepository;
    private final TaskExecutionService taskExecutionService;
    private final TaskStartupResetter taskStartupResetter;

    private final Semaphore semaphore = new Semaphore(0);

    @Override
    public void run(ApplicationArguments args) {
        taskStartupResetter.resetRunningTasks();
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
                taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING)
                        .ifPresent(taskExecutionService::execute);
            } catch (Exception e) {
                log.error("Unexpected error in task dispatcher loop", e);
            }
        }
    }
}
