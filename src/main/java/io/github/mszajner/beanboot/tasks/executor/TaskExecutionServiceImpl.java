package io.github.mszajner.beanboot.tasks.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import io.github.mszajner.beanboot.tasks.api.TaskExecutedEvent;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

import java.time.Instant;

@RequiredArgsConstructor
@Log4j2
public class TaskExecutionServiceImpl implements TaskExecutionService {

    private final TaskRepository taskRepository;
    private final TaskHandlerRegistry taskHandlerRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void execute(TaskEntity task) {
        task.setStatus(TaskStatus.RUNNING);
        task.setStartedAt(Instant.now());
        taskRepository.save(task);

        var handler = taskHandlerRegistry.resolve(task.getAction());
        try {
            handler.execute(task);
            task.setStatus(TaskStatus.SUCCESS);
            task.setMessage(null);
        } catch (Exception e) {
            log.warn("Task {} failed with action {}: {}", task.getId(), task.getAction(), e.getMessage());
            task.setStatus(TaskStatus.FAILURE);
            task.setMessage(e.getMessage());
        } finally {
            task.setFinishedAt(Instant.now());
            taskRepository.save(task);
            eventPublisher.publishEvent(new TaskExecutedEvent(task.getId(), task.getAction(), task.getStatus(),
                    task.getMessage(), task.getStartedAt(), task.getFinishedAt()));
        }
    }
}
