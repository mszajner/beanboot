package io.github.mszajner.beanboot.migrations.tasks;

import io.github.mszajner.beanboot.migrations.api.MigrationTask;

public interface MigrationTaskRunner {
    void runTask(MigrationTask task);
}
