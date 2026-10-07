package io.github.mszajner.beanboot.scheduler.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.scheduler.repositories.SchedulerRepository;

import java.time.Instant;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class InstanceHeartbeatService {

    private final SchedulerRepository schedulerRepository;
    private final InstanceRegistrarService instanceRegistrarService;

    @Scheduled(fixedDelayString = "${beanboot.scheduler.check-in-interval:PT30S}")
    public void heartbeat() {
        var instance = instanceRegistrarService.getCurrentInstance();
        if (Objects.nonNull(instance)) {
            instance.setLastCheckInAt(Instant.now());
            schedulerRepository.save(instance);
        }
    }
}
