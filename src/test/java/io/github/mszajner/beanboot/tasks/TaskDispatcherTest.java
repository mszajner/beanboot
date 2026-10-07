package io.github.mszajner.beanboot.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;
import io.github.mszajner.beanboot.tasks.executor.TaskExecutionService;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDispatcherTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskExecutionService taskExecutionService;
    @Mock private StaleTaskRecoverer staleTaskRecoverer;

    private TaskEntity task(UUID id) {
        var task = new TaskEntity();
        task.setId(id);
        return task;
    }

    private TaskDispatcher dispatcher() {
        return new TaskDispatcher(taskRepository, taskExecutionService, staleTaskRecoverer);
    }

    @Test
    void executePending_claimsTaskBeforeExecutingReloadedEntity() {
        var id = UUID.randomUUID();
        var stale = task(id);
        var reloaded = task(id);
        when(taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING))
                .thenReturn(Optional.of(stale), Optional.empty());
        when(taskRepository.claim(eq(id), any())).thenReturn(1);
        when(taskRepository.findById(id)).thenReturn(Optional.of(reloaded));

        dispatcher().executePending();

        InOrder order = inOrder(taskRepository, taskExecutionService);
        order.verify(taskRepository).claim(eq(id), any());
        order.verify(taskRepository).findById(id);
        order.verify(taskExecutionService).execute(reloaded);
        verify(taskExecutionService, never()).execute(stale);
    }

    @Test
    void executePending_whenAnotherInstanceClaimedTheTask_doesNotExecuteItAndMovesOn() {
        var lost = UUID.randomUUID();
        var won = UUID.randomUUID();
        when(taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING))
                .thenReturn(Optional.of(task(lost)), Optional.of(task(won)), Optional.empty());
        when(taskRepository.claim(eq(lost), any())).thenReturn(0);
        when(taskRepository.claim(eq(won), any())).thenReturn(1);
        var wonEntity = task(won);
        when(taskRepository.findById(won)).thenReturn(Optional.of(wonEntity));

        dispatcher().executePending();

        verify(taskRepository, never()).findById(lost);
        verify(taskExecutionService, times(1)).execute(any());
        verify(taskExecutionService).execute(wonEntity);
    }

    @Test
    void executePending_drainsAllPendingTasksInOneRun() {
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        when(taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING))
                .thenReturn(Optional.of(task(first)), Optional.of(task(second)), Optional.empty());
        when(taskRepository.claim(any(), any())).thenReturn(1);
        when(taskRepository.findById(first)).thenReturn(Optional.of(task(first)));
        when(taskRepository.findById(second)).thenReturn(Optional.of(task(second)));

        dispatcher().executePending();

        verify(taskExecutionService, times(2)).execute(any());
    }

    @Test
    void executePending_whenNothingPending_doesNothing() {
        when(taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING)).thenReturn(Optional.empty());

        dispatcher().executePending();

        verify(taskRepository, never()).claim(any(), any());
        verifyNoInteractions(taskExecutionService);
    }

    @Test
    void heartbeat_whileExecuting_refreshesOnlyTheRunningTask() {
        var id = UUID.randomUUID();
        var dispatcher = dispatcher();
        when(taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING))
                .thenReturn(Optional.of(task(id)), Optional.empty());
        when(taskRepository.claim(eq(id), any())).thenReturn(1);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task(id)));
        doAnswer(invocation -> {
            dispatcher.heartbeat();
            return null;
        }).when(taskExecutionService).execute(any());

        dispatcher.executePending();
        dispatcher.heartbeat(); // idle: nothing to refresh

        verify(taskRepository, times(1)).heartbeat(eq(id), any());
    }

    @Test
    void executePending_clearsCurrentTaskEvenWhenExecutionThrows() {
        var id = UUID.randomUUID();
        var dispatcher = dispatcher();
        when(taskRepository.findFirstByStatusOrderByCreatedAtAsc(TaskStatus.PENDING)).thenReturn(Optional.of(task(id)));
        when(taskRepository.claim(eq(id), any())).thenReturn(1);
        when(taskRepository.findById(id)).thenReturn(Optional.of(task(id)));
        doThrow(new IllegalStateException("boom")).when(taskExecutionService).execute(any());

        try {
            dispatcher.executePending();
        } catch (IllegalStateException ignored) {
        }
        dispatcher.heartbeat();

        verify(taskRepository, never()).heartbeat(any(), any());
    }
}
