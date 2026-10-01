package io.github.mszajner.beanboot.scheduler.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import io.github.mszajner.beanboot.scheduler.entities.ScheduledEntity;

import java.util.Optional;
import java.util.UUID;

public interface ScheduledRepository extends JpaRepository<ScheduledEntity, UUID>, JpaSpecificationExecutor<ScheduledEntity> {

    Optional<ScheduledEntity> findByName(String name);
}
