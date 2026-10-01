package io.github.mszajner.beanboot.scheduler.services;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import io.github.mszajner.beanboot.scheduler.api.ScheduledService;
import io.github.mszajner.beanboot.scheduler.entities.ScheduledEntity;
import io.github.mszajner.beanboot.scheduler.repositories.ScheduledRepository;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScheduledServiceImpl implements ScheduledService {

    private final ScheduledRepository scheduledRepository;
    private final InstanceRegistrarService instanceRegistrarService;

    @Override
    @Transactional
    public Optional<ScheduledEntity> tryAcquire(String name) {
        try {
            var scheduled = scheduledRepository.findByName(name)
                    .orElseGet(() -> createNew(name));
            var scheduler = instanceRegistrarService.getCurrentInstance();

            if (Objects.isNull(scheduler) || !isAvailable(scheduled)) {
                return Optional.empty();
            }

            scheduled.setScheduler(scheduler);
            scheduled.setRunAt(Instant.now());
            scheduled.setFinishedAt(null);
            return Optional.of(scheduledRepository.save(scheduled));
        } catch (ObjectOptimisticLockingFailureException | DataIntegrityViolationException e) {
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void release(ScheduledEntity scheduledEntity) {
        try {
            scheduledEntity.setScheduler(null);
            scheduledEntity.setFinishedAt(Instant.now());
            scheduledRepository.save(scheduledEntity);
        } catch (ObjectOptimisticLockingFailureException e) {
            // Lock was already taken over by another instance (e.g., this instance
            // appeared dead and was preempted). The lock is already gone — ignore.
        }
    }

    private ScheduledEntity createNew(String name) {
        var entity = new ScheduledEntity();
        entity.setName(name);
        return scheduledRepository.saveAndFlush(entity);
    }

    private boolean isAvailable(ScheduledEntity scheduled) {
        var scheduler = scheduled.getScheduler();
        if (scheduler == null) {
            return true;
        }
        var lastCheckIn = scheduler.getLastCheckInAt();
        var interval = scheduler.getCheckInInterval();
        if (lastCheckIn == null || interval == null) {
            return true;
        }
        return lastCheckIn.plusMillis(2 * interval).isBefore(Instant.now());
    }
}
