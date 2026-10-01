package io.github.mszajner.beanboot.scheduler.api;

import io.github.mszajner.beanboot.scheduler.entities.ScheduledEntity;

import java.util.Optional;

public interface ScheduledService {

    Optional<ScheduledEntity> tryAcquire(String name);

    void release(ScheduledEntity scheduledEntity);
}
