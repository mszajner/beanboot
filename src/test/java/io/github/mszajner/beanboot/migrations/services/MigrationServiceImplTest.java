package io.github.mszajner.beanboot.migrations.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.migrations.api.MigrationTask;
import io.github.mszajner.beanboot.migrations.entities.MigrationEntity;
import io.github.mszajner.beanboot.migrations.models.MigrationStatus;
import io.github.mszajner.beanboot.migrations.repositories.MigrationRepository;
import io.github.mszajner.beanboot.migrations.tasks.MigrationTaskRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MigrationServiceImplTest {

    @Mock
    private MigrationRepository repository;

    @Mock
    private MigrationTaskRunner taskRunner;

    /** Tworzy instancję serwisu z podanymi taskami. */
    private MigrationServiceImpl service(MigrationTask... tasks) {
        return new MigrationServiceImpl(repository, List.of(tasks), taskRunner);
    }

    /**
     * Tworzy mock MigrationTask z podanym id i flagą always().
     */
    private MigrationTask taskWith(UUID id, boolean always) {
        MigrationTask t = mock(MigrationTask.class);
        when(t.id()).thenReturn(id);
        when(t.always()).thenReturn(always);
        return t;
    }

    private MigrationEntity entityWithStatus(UUID id, MigrationStatus status) {
        MigrationEntity e = new MigrationEntity();
        e.setId(id);
        e.setStatus(status);
        return e;
    }

    @Test
    void shouldSkipTaskWhenStatusIsSuccess() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, MigrationStatus.SUCCESS)));

        service(task).run();

        verifyNoInteractions(taskRunner);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldSkipTaskWhenStatusIsFailure() {
        // FAILURE is skipped because the run condition is `always() || UNKNOWN`.
        // If retry-on-failure is ever added, update this test accordingly.
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, MigrationStatus.FAILURE)));

        service(task).run();

        verifyNoInteractions(taskRunner);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRunTaskWhenNoRecord() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service(task).run();

        verify(taskRunner).runTask(task);
        verify(repository).save(argThat(e -> MigrationStatus.SUCCESS.equals(e.getStatus())));
    }

    @Test
    void shouldRunTaskWhenStatusIsUnknown() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, MigrationStatus.UNKNOWN)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service(task).run();

        verify(taskRunner).runTask(task);
        verify(repository).save(argThat(e -> MigrationStatus.SUCCESS.equals(e.getStatus())));
    }

    @Test
    void shouldRerunTaskWhenAlwaysTrueAndStatusIsSuccess() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, true);
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, MigrationStatus.SUCCESS)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service(task).run();

        verify(taskRunner).runTask(task);
    }

    @Test
    void shouldRerunTaskWhenAlwaysTrueAndStatusIsFailure() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, true);
        when(repository.findById(id)).thenReturn(Optional.of(entityWithStatus(id, MigrationStatus.FAILURE)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service(task).run();

        verify(taskRunner).runTask(task);
    }

    @Test
    void shouldSetFailureStatusWhenTaskThrows() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("intentional failure")).when(taskRunner).runTask(task);

        service(task).run();

        verify(repository).save(argThat(e ->
            MigrationStatus.FAILURE.equals(e.getStatus()) &&
            "intentional failure".equals(e.getMessage())
        ));
    }

    @Test
    void shouldContinueWithNextTaskAfterOneFailure() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        MigrationTask task1 = taskWith(id1, false);
        MigrationTask task2 = taskWith(id2, false);
        when(repository.findById(id1)).thenReturn(Optional.empty());
        when(repository.findById(id2)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("error")).when(taskRunner).runTask(task1);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service(task1, task2).run();

        verify(taskRunner).runTask(task1);
        verify(taskRunner).runTask(task2);
        verify(repository, times(2)).save(any());
    }

    @Test
    void shouldSetBothTimestampsOnSuccess() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service(task).run();

        verify(repository).save(argThat(e ->
            e.getStartedAt() != null && e.getFinishedAt() != null
        ));
    }

    @Test
    void shouldSetStartedAtButNotFinishedAtOnFailure() {
        UUID id = UUID.randomUUID();
        MigrationTask task = taskWith(id, false);
        when(repository.findById(id)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("err")).when(taskRunner).runTask(task);

        service(task).run();

        verify(repository).save(argThat(e ->
            e.getStartedAt() != null && e.getFinishedAt() == null
        ));
    }
}
