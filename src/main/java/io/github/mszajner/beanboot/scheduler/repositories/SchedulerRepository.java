package io.github.mszajner.beanboot.scheduler.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import io.github.mszajner.beanboot.scheduler.entities.SchedulerEntity;

import java.util.UUID;

public interface SchedulerRepository extends JpaRepository<SchedulerEntity, UUID>, JpaSpecificationExecutor<SchedulerEntity> {

}
