package io.github.mszajner.beanboot.migrations.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.migrations.api.MigrationService;
import io.github.mszajner.beanboot.migrations.api.MigrationTask;
import io.github.mszajner.beanboot.migrations.entities.MigrationEntity;
import io.github.mszajner.beanboot.migrations.models.MigrationStatus;
import io.github.mszajner.beanboot.migrations.repositories.MigrationRepository;
import io.github.mszajner.beanboot.migrations.tasks.MigrationTaskRunner;

import java.time.Instant;
import java.util.Collection;

@Service
@RequiredArgsConstructor
@Log4j2
public class MigrationServiceImpl implements MigrationService {

    private final MigrationRepository migrationRepository;
    private final Collection<MigrationTask> migrationTasks;
    private final MigrationTaskRunner migrationTaskRunner;

    @Override
    @Transactional
    public void run() {
        migrationTasks.forEach(task -> {
            MigrationEntity migrationEntity = migrationRepository.findById(task.id()).orElseGet(() -> {
                MigrationEntity entity = new MigrationEntity();
                entity.setId(task.id());
                entity.setStatus(MigrationStatus.UNKNOWN);
                return entity;
            });
            if (task.always() || MigrationStatus.UNKNOWN.equals(migrationEntity.getStatus())) {
                try {
                    migrationEntity.setStartedAt(Instant.now());
                    migrationTaskRunner.runTask(task);
                    migrationEntity.setStatus(MigrationStatus.SUCCESS);
                    migrationEntity.setFinishedAt(Instant.now());
                    log.info("Migration ran: {}", task.id());
                } catch (Throwable e) {
                    migrationEntity.setStatus(MigrationStatus.FAILURE);
                    migrationEntity.setMessage(e.getMessage());
                    log.error("Error running migration {}: {}", task.id(), e.getMessage(), e);
                }
                migrationRepository.save(migrationEntity);
            }
        });
    }
}
