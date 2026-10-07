package io.github.mszajner.beanboot.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.tasks.config.TaskProperties;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaleTaskRecovererTest {

    @Mock private TaskRepository taskRepository;

    @Test
    void recover_usesCutoffOfThreeMissedHeartbeats() {
        var properties = new TaskProperties();
        properties.setHeartbeatInterval(Duration.ofSeconds(10));
        when(taskRepository.resetStaleRunning(org.mockito.ArgumentMatchers.any())).thenReturn(2);
        var before = Instant.now();

        int recovered = new StaleTaskRecoverer(taskRepository, properties).recover();

        var cutoff = ArgumentCaptor.forClass(Instant.class);
        verify(taskRepository).resetStaleRunning(cutoff.capture());
        assertThat(recovered).isEqualTo(2);
        assertThat(cutoff.getValue())
                .isBetween(before.minusSeconds(30), Instant.now().minusSeconds(30));
    }

    @Test
    void defaultStaleAfter_isNinetySeconds() {
        assertThat(new TaskProperties().staleAfter()).isEqualTo(Duration.ofSeconds(90));
    }
}
