package io.github.mszajner.beanboot.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

@Component
@RequiredArgsConstructor
@Log4j2
public class TaskStartupResetter {

    private final TaskRepository taskRepository;

    /**
     * Resets all RUNNING tasks to PENDING.
     * Must be on a separate bean from TaskDispatcher so Spring's @Transactional
     * proxy intercepts the call correctly (same-class calls bypass the proxy).
     */
    @Transactional
    public void resetRunningTasks() {
        var runningTasks = taskRepository.findAllByStatus(TaskStatus.RUNNING);
        if (!runningTasks.isEmpty()) {
            log.warn("Resetting {} RUNNING task(s) to PENDING on startup", runningTasks.size());
            runningTasks.forEach(task -> task.setStatus(TaskStatus.PENDING));
            taskRepository.saveAll(runningTasks);
        }
    }
}
