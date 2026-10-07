package io.github.mszajner.beanboot.tasks.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.tasks.api.TaskStatus;
import io.github.mszajner.beanboot.tasks.entities.TaskEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<TaskEntity, UUID>, JpaSpecificationExecutor<TaskEntity> {

    Page<TaskEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<TaskEntity> findFirstByStatusOrderByCreatedAtAsc(TaskStatus status);

    List<TaskEntity> findAllByStatus(TaskStatus status);

    /**
     * Atomically moves a {@code PENDING} task to {@code RUNNING}. The conditional update is what makes task execution
     * safe with several instances: only one of them gets {@code 1} back, the others get {@code 0}.
     *
     * @return {@code 1} if this caller claimed the task, {@code 0} if it was no longer {@code PENDING}
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TaskEntity t
               set t.status = io.github.mszajner.beanboot.tasks.api.TaskStatus.RUNNING,
                   t.startedAt = :now,
                   t.heartbeatAt = :now,
                   t.finishedAt = null,
                   t.message = null,
                   t.updatedAt = :now
             where t.id = :id
               and t.status = io.github.mszajner.beanboot.tasks.api.TaskStatus.PENDING
            """)
    int claim(@Param("id") UUID id, @Param("now") Instant now);

    /**
     * Records that the instance executing the task is still alive.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TaskEntity t
               set t.heartbeatAt = :now
             where t.id = :id
               and t.status = io.github.mszajner.beanboot.tasks.api.TaskStatus.RUNNING
            """)
    int heartbeat(@Param("id") UUID id, @Param("now") Instant now);

    /**
     * Returns abandoned {@code RUNNING} tasks (no heartbeat since {@code cutoff}, or none at all) to {@code PENDING}.
     */
    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            update TaskEntity t
               set t.status = io.github.mszajner.beanboot.tasks.api.TaskStatus.PENDING,
                   t.heartbeatAt = null
             where t.status = io.github.mszajner.beanboot.tasks.api.TaskStatus.RUNNING
               and (t.heartbeatAt is null or t.heartbeatAt < :cutoff)
            """)
    int resetStaleRunning(@Param("cutoff") Instant cutoff);
}
