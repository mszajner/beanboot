package io.github.mszajner.beanboot.migrations.api;

import java.util.UUID;

public interface MigrationTask {
    UUID id();

    default boolean always() {
        return false;
    }

    void run();
}
