package io.github.mszajner.beanboot.migrations.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.github.mszajner.beanboot.migrations.entities.MigrationEntity;

import java.util.UUID;

@Repository
public interface MigrationRepository extends JpaRepository<MigrationEntity, UUID> {
}
