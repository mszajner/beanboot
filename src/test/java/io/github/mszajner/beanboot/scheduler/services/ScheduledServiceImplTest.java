package io.github.mszajner.beanboot.scheduler.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import io.github.mszajner.beanboot.scheduler.entities.ScheduledEntity;
import io.github.mszajner.beanboot.scheduler.entities.SchedulerEntity;
import io.github.mszajner.beanboot.scheduler.repositories.ScheduledRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledServiceImplTest {

    @Mock
    private ScheduledRepository scheduledRepository;

    @Mock
    private InstanceRegistrarService instanceRegistrarService;

    private SchedulerEntity currentInstance;
    private ScheduledServiceImpl service;

    @BeforeEach
    void setUp() {
        currentInstance = new SchedulerEntity();
        currentInstance.setId(UUID.randomUUID());
        currentInstance.setCheckInInterval(30_000L);
        currentInstance.setLastCheckInAt(Instant.now());

        service = new ScheduledServiceImpl(scheduledRepository, instanceRegistrarService);
        lenient().when(instanceRegistrarService.getCurrentInstance()).thenReturn(currentInstance);
    }

    @Test
    void tryAcquireCreatesAndAcquiresWhenEntityDoesNotExist() {
        var newEntity = new ScheduledEntity();
        newEntity.setName("test");
        when(scheduledRepository.findByName("test")).thenReturn(Optional.empty());
        when(scheduledRepository.saveAndFlush(any())).thenReturn(newEntity);
        when(scheduledRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.tryAcquire("test");

        assertThat(result).isPresent();
        assertThat(result.get().getScheduler()).isSameAs(currentInstance);
        assertThat(result.get().getRunAt()).isNotNull();
        assertThat(result.get().getFinishedAt()).isNull();
    }

    @Test
    void tryAcquiresWhenSchedulerIsNull() {
        var existing = new ScheduledEntity();
        existing.setName("test");
        existing.setScheduler(null);
        when(scheduledRepository.findByName("test")).thenReturn(Optional.of(existing));
        when(scheduledRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.tryAcquire("test");

        assertThat(result).isPresent();
        assertThat(result.get().getScheduler()).isSameAs(currentInstance);
    }

    @Test
    void tryAcquireReturnsEmptyWhenSchedulerIsAlive() {
        var aliveInstance = new SchedulerEntity();
        aliveInstance.setCheckInInterval(30_000L);
        aliveInstance.setLastCheckInAt(Instant.now());

        var existing = new ScheduledEntity();
        existing.setScheduler(aliveInstance);
        when(scheduledRepository.findByName("test")).thenReturn(Optional.of(existing));

        var result = service.tryAcquire("test");

        assertThat(result).isEmpty();
        verify(scheduledRepository, never()).save(any());
    }

    @Test
    void tryAcquiresWhenSchedulerIsDead() {
        var deadInstance = new SchedulerEntity();
        deadInstance.setCheckInInterval(30_000L);
        deadInstance.setLastCheckInAt(Instant.now().minusSeconds(120));

        var existing = new ScheduledEntity();
        existing.setScheduler(deadInstance);
        when(scheduledRepository.findByName("test")).thenReturn(Optional.of(existing));
        when(scheduledRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.tryAcquire("test");

        assertThat(result).isPresent();
        assertThat(result.get().getScheduler()).isSameAs(currentInstance);
    }

    @Test
    void tryAcquireReturnsEmptyOnOptimisticLockException() {
        var existing = new ScheduledEntity();
        existing.setScheduler(null);
        when(scheduledRepository.findByName("test")).thenReturn(Optional.of(existing));
        when(scheduledRepository.save(any())).thenThrow(new ObjectOptimisticLockingFailureException("", null));

        var result = service.tryAcquire("test");

        assertThat(result).isEmpty();
    }

    @Test
    void tryAcquireReturnsEmptyOnDataIntegrityViolation() {
        when(scheduledRepository.findByName("test")).thenReturn(Optional.empty());
        when(scheduledRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("UNIQUE constraint"));

        var result = service.tryAcquire("test");

        assertThat(result).isEmpty();
    }

    @Test
    void releaseSetsSchedulerToNullAndFinishedAt() {
        var entity = new ScheduledEntity();
        entity.setScheduler(currentInstance);
        when(scheduledRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.release(entity);

        verify(scheduledRepository).save(argThat(e ->
            e.getScheduler() == null && e.getFinishedAt() != null
        ));
    }
}
