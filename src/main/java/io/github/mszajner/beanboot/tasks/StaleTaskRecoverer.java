package io.github.mszajner.beanboot.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.tasks.config.TaskProperties;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

import java.time.Instant;

/**
 * Puts {@code RUNNING} tasks whose owner stopped sending heartbeats (crashed or stopped instance) back to
 * {@code PENDING}. Tasks being executed by healthy instances keep their heartbeat fresh and are left alone.
 * Lives on a separate bean from {@link TaskDispatcher} so the repository's transaction proxy is always used.
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class StaleTaskRecoverer {

    private final TaskRepository taskRepository;
    private final TaskProperties taskProperties;

    public int recover() {
        var cutoff = Instant.now().minus(taskProperties.staleAfter());
        int recovered = taskRepository.resetStaleRunning(cutoff);
        if (recovered > 0) {
            log.warn("Reset {} stale RUNNING task(s) to PENDING (no heartbeat since {})", recovered, cutoff);
        }
        return recovered;
    }
}
