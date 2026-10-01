package io.github.mszajner.beanboot.tasks.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    Page<TaskEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<TaskEntity> findFirstByStatusOrderByCreatedAtAsc(TaskStatus status);

    List<TaskEntity> findAllByStatus(TaskStatus status);
}
