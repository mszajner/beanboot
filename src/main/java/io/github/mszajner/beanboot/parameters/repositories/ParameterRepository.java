package io.github.mszajner.beanboot.parameters.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.github.mszajner.beanboot.parameters.entities.ParameterEntity;

@Repository
public interface ParameterRepository extends JpaRepository<ParameterEntity, String> {
}
