package io.github.mszajner.beanboot.scheduler.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.scheduler.config.SchedulerProperties;
import io.github.mszajner.beanboot.scheduler.entities.SchedulerEntity;
import io.github.mszajner.beanboot.scheduler.repositories.SchedulerRepository;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanceRegistrarServiceTest {

    @Mock
    private SchedulerRepository schedulerRepository;

    private SchedulerProperties properties;
    private InstanceRegistrarService service;

    @BeforeEach
    void setUp() {
        properties = new SchedulerProperties();
        properties.setName("test-node");
        properties.setCheckInInterval(Duration.ofSeconds(30));
        service = new InstanceRegistrarService(schedulerRepository, properties);
    }

    @Test
    void runInsertsNewEntityWithConfiguredName() {
        when(schedulerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.run(null);

        verify(schedulerRepository).save(argThat(e ->
            "test-node".equals(e.getName()) &&
            Long.valueOf(30_000L).equals(e.getCheckInInterval()) &&
            e.getLastCheckInAt() != null
        ));
    }

    @Test
    void runStoresCurrentInstance() {
        var saved = new SchedulerEntity();
        when(schedulerRepository.save(any())).thenReturn(saved);

        service.run(null);

        assertThat(service.getCurrentInstance()).isSameAs(saved);
    }

    @Test
    void currentInstanceIsNullBeforeRun() {
        assertThat(service.getCurrentInstance()).isNull();
    }
}
