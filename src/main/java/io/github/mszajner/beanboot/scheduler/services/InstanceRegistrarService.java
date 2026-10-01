package io.github.mszajner.beanboot.scheduler.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.scheduler.config.SchedulerProperties;
import io.github.mszajner.beanboot.scheduler.entities.SchedulerEntity;
import io.github.mszajner.beanboot.scheduler.repositories.SchedulerRepository;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class InstanceRegistrarService implements ApplicationRunner {

    private final SchedulerRepository schedulerRepository;
    private final SchedulerProperties schedulerProperties;

    @Getter
    private SchedulerEntity currentInstance;

    @Override
    public void run(ApplicationArguments args) {
        var entity = new SchedulerEntity();
        entity.setName(schedulerProperties.getName());
        entity.setCheckInInterval(schedulerProperties.getCheckInInterval().toMillis());
        entity.setLastCheckInAt(Instant.now());
        currentInstance = schedulerRepository.save(entity);
    }
}
